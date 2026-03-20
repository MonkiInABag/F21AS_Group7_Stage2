package Tests;

import Exception.OrderEmptyException;
import Main.Order;
import Main.OrderQueue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderQueueTest {
    /* 
    * Helper method to make valid Oder Object.
    * Avoids duplication across tests and all orders in test are valid. 
    */
    
    private Order createSampleOrder(String customerId) throws Exception {
        return new Order(
                customerId,
                List.of("ITEM-1", "ITEM-2"),
                LocalDateTime.now());
    }
    
    // Tests that oderEnqueue correclty ass an Order to the queue
    @Test
    @DisplayName("Enqueue: Order is added to queue")
    void enqueueAddsOrder() throws Exception {
        OrderQueue queue = new OrderQueue();
        Order order = createSampleOrder("CUST-001");

        queue.orderEnqueue(order, false);

        assertEquals(1, queue.getOrdersAsList().size());
        assertEquals("CUST-001", queue.getOrdersAsList().get(0).getCustomerId());
    }

    // Tests that getOrdersByCustomerId returns the correct orders for a given customer ID
    @Test
    @DisplayName("GetOrdersByCustomerId: Returns matching orders")
    void getOrdersByCustomerIdReturnsCorrectOrders() throws Exception {
        OrderQueue queue = new OrderQueue();

        Order order1 = createSampleOrder("CUST-001");
        Order order2 = createSampleOrder("CUST-002");
        Order order3 = createSampleOrder("CUST-001");

        queue.orderEnqueue(order1, false);
        queue.orderEnqueue(order2, false);
        queue.orderEnqueue(order3, false);

        List<Order> result = queue.getOrdersByCustomerId("CUST-001");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getCustomerId().equals("CUST-001")));
    }

    // Tests that getOrdersByCustomerId throws an OrderEmptyException when no orders are found for the given customer ID
    @Test
    @DisplayName("GetOrdersByCustomerId: Throws when no orders found")
    void getOrdersByCustomerIdThrowsWhenEmpty() throws Exception {
        OrderQueue queue = new OrderQueue();

        assertThrows(OrderEmptyException.class,
                () -> queue.getOrdersByCustomerId("UNKNOWN"));
    }

    // Tests that orderDequeue returns orders in FIFO order and returns null when the queue is empty
    // Also tests that the order is removed from the queue after being dequeued
    @Test
    @DisplayName("Dequeue: FIFO order is kept")
    void dequeueReturnsInFIFOorder() throws Exception {
        OrderQueue queue = new OrderQueue();

        Order order1 = createSampleOrder("CUST-001");
        Order order2 = createSampleOrder("CUST-002");

        queue.orderEnqueue(order1, false);
        queue.orderEnqueue(order2, false);

        Order out1 = queue.orderDequeue();
        Order out2 = queue.orderDequeue();

        assertEquals("CUST-001", out1.getCustomerId());
        assertEquals("CUST-002", out2.getCustomerId());
        assertEquals(0, queue.getOrdersAsList().size());
        assertNull(queue.orderDequeue());
    }

    // Tests that orderDequeue returns null when the queue is empty
    @Test
    @DisplayName("Dequeue: returns null when queue is empty")
    void dequeueReturnsNullWhenEmpty() throws Exception {
        OrderQueue queue = new OrderQueue();
        assertNull(queue.orderDequeue());
    }

    // Tests that an order with an empty menu item list throws an OrderEmptyException
    @Test
    @DisplayName("Order: throws when menu item list is empty")
    void orderThrowsWhenMenuItemListEmpty() {
        assertThrows(OrderEmptyException.class, () ->
            new Order("CUST-001", List.of(), LocalDateTime.now())
        );
    }
}
