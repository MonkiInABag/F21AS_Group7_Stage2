package Tests;

import Main.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simulation.CustomerQueue;
import simulation.SimulationModel;
import simulation.SimulationObserver;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationModelTest {

    @Test
    @DisplayName("addCompletedOrder: stores completed orders correctly")
    void addCompletedOrderStoresOrders() throws Exception {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        Order order = new Order("CUST-001", java.util.List.of("BEV-001"), LocalDateTime.now());
        model.addCompletedOrder(order);

        assertEquals(1, model.getCompletedOrders().size());
        assertEquals("CUST-001", model.getCompletedOrders().get(0).getCustomerId());
    }

    @Test
    @DisplayName("setStaffStatus: updates staff status text")
    void setStaffStatusUpdatesText() {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        model.setStaffStatus(2, "Processing order");

        assertEquals("Processing order", model.getStaffStatus(2));
    }

    @Test
    @DisplayName("getAllStaffStatus: returns defensive copy")
    void getAllStaffStatusReturnsDefensiveCopy() {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        Map<Integer, String> copy = model.getAllStaffStatus();
        copy.put(1, "Tampered");

        assertNotEquals("Tampered", model.getStaffStatus(1));
    }

    @Test
    @DisplayName("setSpeedMultiplier: clamps minimum value to 1")
    void setSpeedMultiplierClampsMinimum() {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        model.setSpeedMultiplier(0);

        assertEquals(1, model.getSpeedMultiplier());
    }

    @Test
    @DisplayName("staffFinished: simulation completes after all staff finish")
    void staffFinishedMarksSimulationComplete() {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        model.staffFinished();
        model.staffFinished();
        assertFalse(model.isSimulationComplete());

        model.staffFinished();
        assertTrue(model.isSimulationComplete());
    }

    @Test
    @DisplayName("notifyObservers: notifies registered observers")
    void notifyObserversCallsObservers() {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        AtomicInteger counter = new AtomicInteger(0);

        SimulationObserver observer = updatedModel -> counter.incrementAndGet();
        model.addObserver(observer);

        model.notifyObservers();

        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("getQueueSnapshot: returns current queue contents")
    void getQueueSnapshotReturnsQueueContents() throws Exception {
        CustomerQueue queue = new CustomerQueue();
        SimulationModel model = new SimulationModel(queue, 3);

        Order order = new Order("CUST-001", java.util.List.of("BEV-001"), LocalDateTime.now());
        queue.enqueue(order);

        assertEquals(1, model.getQueueSnapshot().size());
        assertEquals("CUST-001", model.getQueueSnapshot().get(0).getCustomerId());
    }
}