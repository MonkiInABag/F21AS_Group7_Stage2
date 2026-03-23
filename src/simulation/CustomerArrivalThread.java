package simulation;

import Main.Order;
import java.util.List;

// simulates customers arriving at the coffee shop.
public class CustomerArrivalThread extends Thread {

    private final List<Order> orders;
    private final CustomerQueue queue;
    private final SimulationModel model;
    private volatile int arrivalDelayMs; // volatile so can be changed at runtime via the speed slider

    public CustomerArrivalThread(List<Order> orders,
            CustomerQueue queue,
            SimulationModel model,
            int arrivalDelayMs) {
        super("CustomerArrivalThread");
        setDaemon(false);
        this.orders = orders;
        this.queue = queue;
        this.model = model;
        this.arrivalDelayMs = arrivalDelayMs;
    }

    // Adjusts arrival speed at runtime (called by speed slider).
    public void setArrivalDelayMs(int ms) {
        this.arrivalDelayMs = Math.max(100, ms);
    }

    @Override
    public void run() {
        SimulationLogger logger = SimulationLogger.getInstance();
        logger.log("Coffee shop opened. " + orders.size() + " order(s) to process.");

        for (Order order : orders) {
            try {
                Thread.sleep(arrivalDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log("Arrival thread interrupted – stopping early.");
                break;
            }

            queue.enqueue(order);
            logger.log("Customer '" + order.getCustomerId() + "' joined the queue with "
                    + order.getMenuItemIdList().size() + " item(s).");
            model.notifyObservers(); // refresh GUI to show new queue entry
        }

        queue.setDone();
        logger.log("All customers have arrived. Queue is now closed.");
        model.notifyObservers();
    }
}


