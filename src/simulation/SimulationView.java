package simulation;

import Main.Menu;
import Main.MenuItem;
import Main.Order;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

// Displays the live simulation:
// Observes the simulation model and updates UI in real time
public class SimulationView extends JFrame {

    private final SimulationController controller;
    private final SimulationModel model;
    private final Menu menu;

    //  Queue panel widgets (shows waiting customers)
    private final JTextArea queueArea;
    private final JLabel queueCountLabel;
    private final JLabel completedCountLabel;

    // Staff panels (each staff member has their own display area)
    private final Map<Integer, JTextArea> staffAreas = new LinkedHashMap<>();

    // Speed slider (extension 1: dynamic simulation speed control) 
    private final JSlider speedSlider;

    // Prevent the completion dialog from showing more than once
    private boolean completionShown = false;

    /*
     * Constructor for the SimulationView.
     * Initialises UI components, registers observer, and sets up window behaviour.
     */
    public SimulationView() {
        
    }
  
    /*
     * Builds the main layout of the window.
     * Combines header (top) and main content (centre) panels.
     */
    private JPanel buildLayout() {
        
    }

    /*
     * Builds the header section of the GUI.
     * Contains:
     * - Title
     * - Queue statistics
     * - Speed control slider
     */
    private JPanel buildHeader() {
        
    }

    /*
     * Builds the central section of the GUI.
     * Splits the screen into:
     * - Left: Customer queue
     * - Right: Staff activity panels
     */
    private JSplitPane buildCenter() {
        
    }


    /*
     * Called automatically when the model updates (Observer pattern).
     * Refreshes queue and staff displays and checks if simulation is complete.
     */
    @Override
    public void update() {
        
    }

    /*
     * Updates the queue display panel.
     * Shows:
     * - Number of customers waiting
     * - Number of completed orders
     * - List of orders and their items
     */
    private void refreshQueue() {
        
    }

    /*
     * Updates all staff panels.
     * Displays current status of each staff member
     * (for e.g: idle, processing an order).
     */
    private void refreshStaff() {
        
    }

    /*
     * Displays a final report dialog when the simulation completes.
     * Shows summary of all processed orders and allows user to exit.
     */
    private void showCompletionDialog() {
        
    }
}