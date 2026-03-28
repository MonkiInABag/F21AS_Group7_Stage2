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
public class SimulationView extends JFrame implements SimulationObserver {

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
    public SimulationView(SimulationController controller, Menu menu) {
        super("Coffee Shop Simulation");
        this.controller = controller;
        this.model = controller.getModel();
        this.menu = menu;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 700));

        // Queue area
        queueArea = new JTextArea();
        queueArea.setEditable(false);
        queueArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        queueArea.setBackground(new Color(250, 250, 255));

        queueCountLabel = new JLabel("In queue: 0");
        completedCountLabel = new JLabel("Completed: 0");
        styleStatLabel(queueCountLabel);
        styleStatLabel(completedCountLabel);

        // Staff areas
        int n = model.getNumStaff();
        for (int i = 1; i <= n; i++) {
            JTextArea ta = new JTextArea("Starting...");
            ta.setEditable(false);
            ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setBackground(new Color(240, 255, 245));
            staffAreas.put(i, ta);
        }

        // Speed slider (extension)
        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 5, 1);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setSnapToTicks(true);
        speedSlider.setOpaque(false);
        speedSlider.addChangeListener(e -> {
            if (!speedSlider.getValueIsAdjusting()) {
                controller.setSpeed(speedSlider.getValue());
            }
        });

        setContentPane(buildLayout());

        // Register this view as an observer BEFORE threads start
        model.addObserver(this);

        // Close handler: write log & exit
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                SimulationLogger.getInstance().writeToFile("simulation_log.txt");
                dispose();
                System.exit(0);
            }
        });
    }
  
    /*
     * Builds the main layout of the window.
     * Combines header (top) and main content (centre) panels.
     */
    private JPanel buildLayout() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        return root;
    }

    /*
     * Builds the header section of the GUI.
     * Contains:
     * - Title
     * - Queue statistics
     * - Speed control slider
     */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 90, 160));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("Coffee Shop Simulation");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Stats + speed slider on the right
        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightBar.setOpaque(false);

        rightBar.add(queueCountLabel);
        rightBar.add(completedCountLabel);

        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setForeground(Color.WHITE);
        speedLabel.setFont(new Font("Arial", Font.BOLD, 13));
        rightBar.add(speedLabel);
        rightBar.add(speedSlider);

        header.add(rightBar, BorderLayout.EAST);
        return header;
    }

    /*
     * Builds the central section of the GUI.
     * Splits the screen into:
     * - Left: Customer queue
     * - Right: Staff activity panels
     */
    private JSplitPane buildCenter() {
        // LEFT: customer queue
        JPanel queuePanel = new JPanel(new BorderLayout(6, 6));
        queuePanel.setBorder(new TitledBorder("Customer Queue"));
        queuePanel.add(new JScrollPane(queueArea), BorderLayout.CENTER);

        // RIGHT: staff columns
        int n = model.getNumStaff();
        JPanel staffWrapper = new JPanel(new GridLayout(1, n, 10, 0));
        staffWrapper.setBorder(new TitledBorder("Serving Staff"));

        for (int i = 1; i <= n; i++) {
            JPanel sp = new JPanel(new BorderLayout());
            sp.setBorder(new TitledBorder("Staff " + i));
            JScrollPane scroll = new JScrollPane(staffAreas.get(i));
            sp.add(scroll, BorderLayout.CENTER);
            staffWrapper.add(sp);
        }

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                queuePanel, staffWrapper);
        split.setDividerLocation(340);
        split.setResizeWeight(0.30);
        split.setBorder(null);
        return split;
    }


    /*
     * Called automatically when the model updates (Observer pattern).
     * Refreshes queue and staff displays and checks if simulation is complete.
     */
    @Override
    public void update(SimulationModel model) {
        SwingUtilities.invokeLater(() -> {
            refreshQueue(model);
            refreshStaff(model);
            if (model.isSimulationComplete() && !completionShown) {
                completionShown = true;
                showCompletionDialog();
            }
        });
    }

    /*
     * Updates the queue display panel.
     * Shows:
     * - Number of customers waiting
     * - Number of completed orders
     * - List of orders and their items
     */
    private void refreshQueue(SimulationModel model) {
        List<Order> snapshot = model.getQueueSnapshot();
        int completed = model.getCompletedOrders().size();

        queueCountLabel.setText("In queue: " + snapshot.size());
        completedCountLabel.setText("Completed: " + completed);

        StringBuilder sb = new StringBuilder();
        if (snapshot.isEmpty()) {
            sb.append("  (no customers waiting)");
        } else {
            for (int i = 0; i < snapshot.size(); i++) {
                Order o = snapshot.get(i);
                sb.append(i + 1).append(". ").append(o.getCustomerId())
                        .append("  (").append(o.getMenuItemIdList().size()).append(" item(s))\n");
                for (String itemId : o.getMenuItemIdList()) {
                    MenuItem item = menu.getMenuItemById(itemId);
                    sb.append("    • ").append(item != null ? item.getName() : itemId).append("\n");
                }
                sb.append("\n");
            }
        }
        queueArea.setText(sb.toString());
        queueArea.setCaretPosition(0);
    }

    /*
     * Updates all staff panels.
     * Displays current status of each staff member
     * (for e.g: idle, processing an order).
     */
    private void refreshStaff(SimulationModel model) {
        Map<Integer, String> allStatus = model.getAllStaffStatus();
        for (Map.Entry<Integer, String> entry : allStatus.entrySet()) {
            JTextArea ta = staffAreas.get(entry.getKey());
            if (ta != null) {
                ta.setText(entry.getValue());
            }
        }
    }

    /*
     * Displays a final report dialog when the simulation completes.
     * Shows summary of all processed orders and allows user to exit.
     */
    private void showCompletionDialog() {
        String report = controller.onSimulationComplete();

        JFrame reportFrame = new JFrame("Simulation Complete – Final Report");
        reportFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(240, 248, 255));

        // Header
        JLabel hdr = new JLabel("  Coffee Shop Closed – Daily Summary");
        hdr.setFont(new Font("Arial", Font.BOLD, 18));
        hdr.setForeground(Color.WHITE);
        hdr.setOpaque(true);
        hdr.setBackground(new Color(30, 90, 160));
        hdr.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(hdr, BorderLayout.NORTH);

        // Report text
        JTextArea ta = new JTextArea(report);
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setMargin(new Insets(8, 8, 8, 8));
        panel.add(new JScrollPane(ta), BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(new Color(240, 248, 255));
        JButton closeBtn = new JButton("Close & Exit");
        closeBtn.setBackground(new Color(200, 30, 30));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> System.exit(0));
        footer.add(closeBtn);
        panel.add(footer, BorderLayout.SOUTH);

        reportFrame.setContentPane(panel);
        reportFrame.setSize(650, 520);
        reportFrame.setLocationRelativeTo(this);
        reportFrame.setVisible(true);
    }

    private void styleStatLabel(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 13));
    }
}