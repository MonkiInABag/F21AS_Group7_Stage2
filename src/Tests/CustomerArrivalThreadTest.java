package Tests;

import Main.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simulation.CustomerArrivalThread;
import simulation.CustomerQueue;
import simulation.SimulationModel;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerArrivalThreadTest {

    @Test
    @DisplayName("CustomerArrivalThread: enqueues all orders in order")
    void customerArrivalThreadEnqueuesAllOrders() throws Exception {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        List<Order> orders = List.of(
                new Order("CUST-001", List.of("BEV-001"), LocalDateTime.now()),
                new Order("CUST-002", List.of("FOOD-001"), LocalDateTime.now()),
                new Order("CUST-003", List.of("OTH-001"), LocalDateTime.now())
        );

        CustomerArrivalThread thread = new CustomerArrivalThread(orders, queue, model, 10);
        thread.start();
        thread.join();

        assertEquals(3, queue.size());

        Order first = queue.dequeue();
        Order second = queue.dequeue();
        Order third = queue.dequeue();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertEquals("CUST-001", first.getCustomerId());
        assertEquals("CUST-002", second.getCustomerId());
        assertEquals("CUST-003", third.getCustomerId());
    }

    @Test
    @DisplayName("CustomerArrivalThread: marks queue done after all arrivals")
    void customerArrivalThreadMarksQueueDone() throws Exception {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        List<Order> orders = List.of(
                new Order("CUST-001", List.of("BEV-001"), LocalDateTime.now())
        );

        CustomerArrivalThread thread = new CustomerArrivalThread(orders, queue, model, 10);
        thread.start();
        thread.join();

        assertEquals(1, queue.size());

        Order first = queue.dequeue();
        assertNotNull(first);
        assertEquals("CUST-001", first.getCustomerId());

        Order afterDone = queue.dequeue();
        assertNull(afterDone);
        assertTrue(queue.isFullyDone());
    }

    @Test
    @DisplayName("CustomerArrivalThread: handles empty order list correctly")
    void customerArrivalThreadHandlesEmptyOrderList() throws Exception {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        List<Order> orders = List.of();

        CustomerArrivalThread thread = new CustomerArrivalThread(orders, queue, model, 10);
        thread.start();
        thread.join();

        assertTrue(queue.isEmpty());
        assertTrue(queue.isFullyDone());
    }
}