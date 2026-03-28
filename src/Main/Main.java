package Main;

import Exception.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.swing.*;
import simulation.*;


public class Main {
    
    private static final String MENU_FILE = "stage2\\src\\Database\\menu.csv";
    private static final String QUEUE_FILE = "stage2\\src\\Database\\orderQueue.csv";
    
    
    public static void main(String[] args) {

        SimulationLogger.getInstance().log("Stage 2 application starting.");

        //Load menu
        Menu menu = new Menu();
        try {
            menu.loadMenuFromFile(MENU_FILE);
            SimulationLogger.getInstance().log("Menu loaded successfully.");
        } catch (FileReadException | MenuItemMissingDataException e) {
            System.err.println("Error loading menu: " + e.getMessage());
            e.printStackTrace();
        }

        //Load orders from CSV
        List<Order> orders = new ArrayList<>();
        try {
            OrderQueue oq = new OrderQueue(QUEUE_FILE, menu);
            orders = oq.getOrdersAsList();
            SimulationLogger.getInstance().log("Loaded " + orders.size() + " order(s) from file.");
        } catch (FileReadException e) {
            System.err.println("Warning: Could not load orders from file – " + e.getMessage());
        }

        // If no file orders loaded, load demo data
        if (orders.isEmpty()) {
            SimulationLogger.getInstance().log("No file orders found – using built-in demo orders.");
            orders = buildDemoOrders();
        }

        final List<Order> finalOrders = orders;
        final Menu finalMenu = menu;

        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ignored) {
            }

            SimulationController controller = new SimulationController(finalOrders, finalMenu);
            SimulationView view = new SimulationView(controller, finalMenu);
            view.setVisible(true);
            controller.start(); // starts arrival + staff threads
        });
    }

    
    private static List<Order> buildDemoOrders() {
        List<Order> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Each inner array: { customerId, itemId1, itemId2, ... }
        String[][] data = {
                { "Alice", "BEV-001" },
                { "Bob", "FOOD-001", "FOOD-002", "BEV-003" },
                { "Carol", "BEV-005", "FOOD-004" },
                { "Dave", "OTH-003" },
                { "Eve", "FOOD-007", "BEV-006", "FOOD-012", "BEV-009", "FOOD-003" },
                { "Frank", "BEV-012" },
                { "Grace", "FOOD-009", "BEV-001" },
                { "Hiro", "OTH-004", "BEV-008" },
                { "Isla", "FOOD-002", "FOOD-005", "BEV-002" },
                { "Jack", "BEV-015" },
                { "Karen", "FOOD-014", "BEV-013", "OTH-001" },
                { "Liam", "BEV-011" },
                { "Maya", "FOOD-006", "FOOD-004", "BEV-009", "BEV-001", "FOOD-001" },
                { "Noah", "BEV-003", "FOOD-012" },
                { "Olivia", "OTH-005", "BEV-004" },
        };

        for (String[] row : data) {
            String customerId = row[0];
            List<String> items = Arrays.asList(Arrays.copyOfRange(row, 1, row.length));
            try {
                list.add(new Order(customerId, items, now));
            } catch (OrderEmptyException e) {
                System.err.println("Demo order skipped: " + e.getMessage());
            }
        }
        return list;
    }
}