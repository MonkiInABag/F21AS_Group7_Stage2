package simulation;

import Main.Order;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


// MVC Model and Observable Subject.
public class SimulationModel {

    private final CustomerQueue          queue;
    private final int                    numStaff;
    private final Map<Integer, String>   staffStatus;       // staffId -> display text
    private final List<Order>            completedOrders;
    private final List<SimulationObserver> observers;
    private int                          finishedStaffCount;
    private volatile int                 speedMultiplier;   // 1 = normal speed
    private boolean                      simulationComplete;

    //pause flag
    private boolean paused = false;
    private final Object pauseLock;

    public SimulationModel(CustomerQueue queue, int numStaff) {
        this.queue              = queue;
        this.numStaff           = numStaff;
        this.staffStatus        = new LinkedHashMap<>();
        this.completedOrders    = new ArrayList<>();
        this.observers          = new ArrayList<>();
        this.finishedStaffCount = 0;
        this.speedMultiplier    = 1;
        this.simulationComplete = false;

        this.paused = false;
        this.pauseLock = new Object();

        for (int i = 1; i <= numStaff; i++) {
            staffStatus.put(i, "Starting...");
        }
    }

    public synchronized void addObserver(SimulationObserver o) {
        observers.add(o);
    }

    
    // Notifies all registered observers.
    public synchronized void notifyObservers() {
        for (SimulationObserver o : observers) {
            o.update(this);
        }
    }


    public synchronized void setStaffStatus(int staffId, String status) {
        staffStatus.put(staffId, status);
    }

    public synchronized String getStaffStatus(int staffId) {
        return staffStatus.getOrDefault(staffId, "Unknown");
    }

    /** Returns a copy of the entire staff-status map. */
    public synchronized Map<Integer, String> getAllStaffStatus() {
        return new LinkedHashMap<>(staffStatus);
    }

    public List<Order> getQueueSnapshot() {
        return queue.getSnapshot();     // CustomerQueue.getSnapshot() is already synchronized
    }

    public synchronized void addCompletedOrder(Order o) {
        completedOrders.add(o);
    }

    public synchronized List<Order> getCompletedOrders() {
        return new ArrayList<>(completedOrders);
    }

    // called by StaffMember upon exiting
    public synchronized void staffFinished() {
        finishedStaffCount++;
        if (finishedStaffCount >= numStaff) {
            simulationComplete = true;
            notifyObservers();
        }
    }

    public synchronized boolean isSimulationComplete() {
        return simulationComplete;
    }


    public int getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(int multiplier) {
        this.speedMultiplier = Math.max(1, multiplier);
        SimulationLogger.getInstance().log("Simulation speed changed to " + multiplier + "×.");
    }

    public int getNumStaff() {
        return numStaff;
    }

    // PAUSE / RESUME FUNCTIONALITY

    public void setPaused(boolean paused) {
        synchronized (pauseLock) {
            this.paused = paused;
            if (!paused) {
                pauseLock.notifyAll();
            }
        }

        SimulationLogger.getInstance().log(
                paused ? "Simulation paused." : "Simulation resumed.");
        notifyObservers();
    }

    public boolean isPaused() {
        synchronized (pauseLock) {
            return paused;
        }
    }

    public void waitIfPaused() throws InterruptedException {
        synchronized (pauseLock) {
            while (paused) {
                pauseLock.wait();
            }
        }
    }
}