package simulation;

import Main.Menu;
import Main.Order;
import Main.ReportGenerator;
import java.util.ArrayList;
import java.util.List;

//   Owns the CustomerQueue, SimulationModel, CustomerArrivalThread,
//   and the pool of StaffMember threads.  The SimulationView delegates
// user interactions (speed changes, window close) back to this class.
public class SimulationController {

    public static final int NUM_STAFF = 3;
    private static final int BASE_ARRIVAL_MS = 2000;

    private final SimulationModel model;
    private final CustomerQueue queue;
    private final CustomerArrivalThread arrivalThread;
    private final List<StaffMember> staffMembers;
    private final Menu menu;

    public SimulationController(List<Order> orders, Menu menu) {
        this.menu = menu;
        this.queue = new CustomerQueue();
        this.model = new SimulationModel(queue, NUM_STAFF);
        this.arrivalThread = new CustomerArrivalThread(orders, queue, model, BASE_ARRIVAL_MS);

        this.staffMembers = new ArrayList<>();
        for (int i = 1; i <= NUM_STAFF; i++) {
            staffMembers.add(new StaffMember(i, queue, model, menu));
        }
    }

    /** Returns the model so the View can register as an observer. */
    public SimulationModel getModel() {
        return model;
    }

    /** Starts all threads to begin the simulation. */
    public void start() {
        SimulationLogger logger = SimulationLogger.getInstance();
        logger.log("=== Simulation starting with " + NUM_STAFF + " staff members ===");
        for (StaffMember s : staffMembers) {
            s.start();
        }
        arrivalThread.start();
    }

    // Adjusts simulation speed.
    public void setSpeed(int multiplier) {
    SimulationLogger logger = SimulationLogger.getInstance();

    model.setSpeedMultiplier(multiplier);
    int newArrivalMs = Math.max(200, BASE_ARRIVAL_MS / multiplier);
    arrivalThread.setArrivalDelayMs(newArrivalMs);

    logger.log("Simulation speed set to x" + multiplier);
}

    // Called by SimulationView when the simulation finishes.
    public String onSimulationComplete() {
    SimulationLogger logger = SimulationLogger.getInstance();
    logger.log("Simulation complete. Generating report...");

    SimulationLogger.getInstance().writeToFile("simulation_log.txt");

        // Build report using Stage 1 ReportGenerator
        ReportGenerator rg = new ReportGenerator();
        // Create a temporary in-memory OrderQueue for the report generator
        Main.OrderQueue tempQueue = new Main.OrderQueue();
        for (Order o : model.getCompletedOrders()) {
            try {
                tempQueue.orderEnqueue(o, false);
            } catch (Exception ignored) {
            }
        }
        Main.BillingService billing = new Main.BillingService();
        return rg.generateSummaryReport(menu, model.getCompletedOrders(), billing);
    }
}
