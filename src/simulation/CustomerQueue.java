package simulation;

import Main.Order;
import java.util.LinkedList;
import java.util.List;

//   Thread-safe FIFO queue for simulation orders.

public class CustomerQueue {

    private final LinkedList<Order> queue;
    private boolean done; // true once the arrival thread has finished adding orders

    public CustomerQueue() {
        queue = new LinkedList<>();
        done = false;
    }

    // Adds an order to the back of the queue and wakes any waiting staff.
    public synchronized void enqueue(Order order) {
        queue.addLast(order);
        notifyAll(); // wake staff threads blocked in dequeue()
    }

    // Removes and returns the front order.
    public synchronized Order dequeue() throws InterruptedException {
        while (queue.isEmpty() && !done) {
            wait(); // release lock and sleep until enqueue() or setDone() calls notifyAll()
        }
        if (queue.isEmpty()) {
            return null; // simulation finished
        }
        return queue.removeFirst();
    }

    // Signals that no more customers will arrive.
    public synchronized void setDone() {
        done = true;
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized int size() {
        return queue.size();
    }

    // True when done flag is set AND the queue has been fully drained.
    public synchronized boolean isFullyDone() {
        return done && queue.isEmpty();
    }

    // Returns a snapshot of the current queue contents
    public synchronized List<Order> getSnapshot() {
        return new LinkedList<>(queue);
    }
}
