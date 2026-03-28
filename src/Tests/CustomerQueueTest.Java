package Tests;

import simulation.CustomerQueue;
import Main.Order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerQueueTest {

    // Helper method to create valid Order objects
    private Order createSampleOrder(String customerId) throws Exception {
        return new Order(
                customerId,
                List.of("ITEM-1", "ITEM-2"),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Enqueue: Order is added to queue")
    void enqueueAddsOrder() throws Exception {
        CustomerQueue queue = new CustomerQueue();
        Order order = createSampleOrder("CUST-001");

        queue.enqueue(order);

        assertEquals(1, queue.size());
        assertEquals("CUST-001", queue.getSnapshot().get(0).getCustomerId());
    }

    @Test
    @DisplayName("Enqueue: Throws when order is null")
    void enqueueThrowsWhenNull() {
        CustomerQueue queue = new CustomerQueue();

        assertThrows(IllegalArgumentException.class, () -> {
            queue.enqueue(null);
        });
    }

    @Test
    @DisplayName("Enqueue: Throws when queue is marked done")
    void enqueueThrowsWhenDone() throws Exception {
        CustomerQueue queue = new CustomerQueue();
        queue.setDone();

        Order order = createSampleOrder("CUST-001");

        assertThrows(IllegalStateException.class, () -> {
            queue.enqueue(order);
        });
    }

    @Test
    @DisplayName("Dequeue: Returns orders in FIFO order")
    void dequeueReturnsFIFO() throws Exception {
        CustomerQueue queue = new CustomerQueue();

        Order order1 = createSampleOrder("CUST-001");
        Order order2 = createSampleOrder("CUST-002");

        queue.enqueue(order1);
        queue.enqueue(order2);

        Order out1 = queue.dequeue();
        Order out2 = queue.dequeue();

        assertEquals("CUST-001", out1.getCustomerId());
        assertEquals("CUST-002", out2.getCustomerId());
    }

    @Test
    @DisplayName("Dequeue: Returns null when done and empty")
    void dequeueReturnsNullWhenDoneAndEmpty() throws Exception {
        CustomerQueue queue = new CustomerQueue();

        queue.setDone();

        Order result = queue.dequeue();

        assertNull(result);
    }

    @Test
    @DisplayName("isFullyDone: True only when done and empty")
    void isFullyDoneWorksCorrectly() throws Exception {
        CustomerQueue queue = new CustomerQueue();

        assertFalse(queue.isFullyDone());

        Order order = createSampleOrder("CUST-001");
        queue.enqueue(order);

        queue.setDone();
        assertFalse(queue.isFullyDone());

        queue.dequeue();
        assertTrue(queue.isFullyDone());
    }

    @Test
    @DisplayName("Snapshot: Returns copy, not original queue")
    void snapshotIsSafeCopy() throws Exception {
        CustomerQueue queue = new CustomerQueue();

        Order order = createSampleOrder("CUST-001");
        queue.enqueue(order);

        List<Order> snapshot = queue.getSnapshot();
        snapshot.clear(); // modify snapshot

        assertEquals(1, queue.size()); // original queue unchanged
    }

    @Test
    @DisplayName("Size and isEmpty reflect queue state correctly")
    void sizeAndIsEmptyWorkCorrectly() throws Exception {
        CustomerQueue queue = new CustomerQueue();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());

        Order order = createSampleOrder("CUST-001");
        queue.enqueue(order);

        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
    }

    @Test
    @DisplayName("Dequeue blocks until item is added")
    void dequeueBlocksUntilItemAdded() throws Exception {
        CustomerQueue queue = new CustomerQueue();

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(100);
                queue.enqueue(new Order("CUST-001", List.of("BEV-001"), LocalDateTime.now()));
            } catch (Exception ignored) {}
        });

        t.start();

        Order result = queue.dequeue();

        assertNotNull(result);
    }
}