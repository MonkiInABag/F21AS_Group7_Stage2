package simulation;

import Main.Menu;
import Main.MenuItem;
import Main.Order;
import Structures.Category;

import java.util.Random;

//thread representing one member of serving staff.
public class StaffMember extends Thread {

    private final int staffId;
    private final CustomerQueue queue;
    private final SimulationModel model;
    private final Menu menu;
    private volatile boolean running;
    private static final Random RANDOM = new Random();

    public StaffMember(int staffId,
            CustomerQueue queue,
            SimulationModel model,
            Menu menu) {
        super("StaffMember-" + staffId);
        setDaemon(false);
        this.staffId = staffId;
        this.queue = queue;
        this.model = model;
        this.menu = menu;
        this.running = true;
    }

    @Override
    public void run() {
        SimulationLogger logger = SimulationLogger.getInstance();
        updateStatus("Idle – waiting for first customer");

        while (running) {
            Order order;
            try {
                updateStatus("Waiting for next order...");
                order = queue.dequeue(); // blocks until an order arrives or queue is done
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log("Staff " + staffId + " interrupted.");
                break;
            }

            if (order == null) {
                // Queue exhausted and done flag set – shift is over
                updateStatus("Shift ended – all orders complete ✔");
                logger.log("Staff " + staffId + " finished their shift.");
                model.staffFinished();
                break;
            }

            // --- Start processing this order ---
            String customerName = order.getCustomerId();
            String itemList = buildItemList(order);
            double total = calculateOrderTotal(order);
            double discount = calculateDiscount(order, total);
            String priceStr = discount > 0
                    ? String.format("Total £%.2f  (£%.2f discount applied)", total - discount, discount)
                    : String.format("Total £%.2f  (no discount)", total);

            String statusText = "Processing: " + customerName + "\n\n"
                    + itemList + "\n" + priceStr;
            updateStatus(statusText);

            logger.log("Staff " + staffId + " started order for '" + customerName
                    + "' (" + order.getMenuItemIdList().size() + " item(s)).");

            // Simulate the time it takes to prepare the order
            int processingMs = calculateProcessingTimeMs(order);
            try {
                Thread.sleep(processingMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // --- Order complete ---
            logger.log("Staff " + staffId + " completed order for '" + customerName + "'.");
            model.addCompletedOrder(order);
            model.notifyObservers();
        }
    }

    private void updateStatus(String status) {
        model.setStaffStatus(staffId, status);
        model.notifyObservers();
    }

    /** Builds a bullet-list string of item names for display. */
    private String buildItemList(Order order) {
        StringBuilder sb = new StringBuilder();
        for (String itemId : order.getMenuItemIdList()) {
            MenuItem item = menu.getMenuItemById(itemId);
            String name = (item != null) ? item.getName() : itemId;
            sb.append("  • ").append(name).append("\n");
        }
        return sb.toString().trim();
    }

    private double calculateOrderTotal(Order order) {
        double total = 0;
        for (String itemId : order.getMenuItemIdList()) {
            MenuItem item = menu.getMenuItemById(itemId);
            if (item != null)
                total += item.getPrice();
        }
        return total;
    }

    /** 20 % discount if 5 or more items – mirrors Stage 1 rule. */
    private double calculateDiscount(Order order, double total) {
        return (order.getMenuItemIdList().size() >= 5) ? total * 0.20 : 0.0;
    }

    /**
     * Processing time based on category:
     * Drink → 2–4 seconds each
     * Food → 6–10 seconds each
     * Other → 1–3 seconds each
     *
     * The speed multiplier divides the total so the GUI stays watchable
     * at high speed settings.
     */
    private int calculateProcessingTimeMs(Order order) {
        int totalMs = 0;
        for (String itemId : order.getMenuItemIdList()) {
            MenuItem item = menu.getMenuItemById(itemId);
            if (item != null) {
                Category cat = item.getCategory();
                int seconds;
                switch (cat) {
                    case DRINK:
                        seconds = 2 + RANDOM.nextInt(3); // 2, 3 or 4
                        break;
                    case FOOD:
                        seconds = 6 + RANDOM.nextInt(5); // 6–10
                        break;
                    default: // OTHER
                        seconds = 1 + RANDOM.nextInt(3); // 1–3
                        break;
                }
                totalMs += seconds * 1000;
            }
        }
        int speed = Math.max(1, model.getSpeedMultiplier());
        return Math.max(400, totalMs / speed); // never go below 400 ms
    }

    public int getStaffId() {
        return staffId;
    }

    public void stopStaff() {
        running = false;
        interrupt();
    }
}
