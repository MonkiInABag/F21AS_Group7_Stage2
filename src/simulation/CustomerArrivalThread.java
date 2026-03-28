package simulation;

import Main.Order;
import java.util.List;

// simulates customers arriving at the coffee shop.
public class CustomerArrivalThread extends Thread {

    private final List<Order> orders;
    private final CustomerQueue queue;
    private final SimulationModel model;
    private volatile int arrivalDelayMs; // volatile so can be changed at runtime via the speed slider
    private volatile boolean running = true; 

    public CustomerArrivalThread(List<Order> orders,
            CustomerQueue queue,
            SimulationModel model,
            int arrivalDelayMs) {
        super("CustomerArrivalThread");
        setDaemon(false);
        this.orders = orders;
        this.queue = queue;
        this.model = model;
        this.arrivalDelayMs = Math.max(100, arrivalDelayMs);
    }

    // Adjusts arrival speed at runtime (called by speed slider).
    public void setArrivalDelayMs(int ms) {
        this.arrivalDelayMs = Math.max(100, ms);
    }

    public void requestStop() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        SimulationLogger logger = SimulationLogger.getInstance();
        logger.log("Coffee shop opened. " + orders.size() + " order(s) to process.");

        for (Order order : orders) {
            // pause check
            model.waitIfPaused();
            if(!running)
            {
                logger.log("Arrival thread stop requested");
                break;
            }
            try {
                int baseDelay = arrivalDelayMs;
                int varient = (int)(baseDelay * 0.2);
                int actualDelay = baseDelay + (int) (Math.random() * (2 * varient + 1)) - varient;
                actualDelay = Math.max(100, actualDelay);
                Thread.sleep(actualDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log("Arrival thread interrupted – stopping early.");
                break;
            }
            // pause check
            model.waitIfPaused();
            try {
                queue.enqueue(order);
            }catch(IllegalStateException e){
                logger.log("Queue already closed");
                break;
            }
            logger.log("Customer '" + order.getCustomerId() + "' joined the queue with "
                    + order.getMenuItemIdList().size() + " item(s).");
            model.notifyObservers(); // refresh GUI to show new queue entry
        }

        queue.setDone();
        if (running) {
            logger.log("All customers have arrived. Queue is now closed.");
        } else {
            logger.log("Arrival thread stopped early. Queue is now closed.");
        }
        model.notifyObservers();
    }
}


