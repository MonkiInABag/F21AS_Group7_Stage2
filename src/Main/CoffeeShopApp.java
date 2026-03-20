package Main;
 

import Exception.FileReadException;
import Exception.MenuItemMissingDataException;
import Main.*;


import java.time.LocalDateTime;
import java.util.*;

import javax.swing.SwingUtilities;

public class CoffeeShopApp {

    // File paths for menu and order queue data
    static final String menuFilename = "src/Database/menu.csv";
    static final String queueFilename = "src/Database/orderQueue.csv";


    private Menu menu;
    private BillingService billingService;
    private OrderQueue orderQueue;

    // Constructor to initialize the CoffeeShopApp
    public CoffeeShopApp() {
        //create new objects
        this.menu = new Menu();                     // Initialise the menu object
        this.billingService = new BillingService(); // Initialise the billing service object
        
        // Initialise the menu map
        try {
            this.menu.loadMenuFromFile(menuFilename);   // Load the menu data from the specified file and populate the menu map
        } catch (FileReadException e) {
            e.printStackTrace();
        } catch (MenuItemMissingDataException e) {      // Handle the case where menu items have missing data during loading
            e.printStackTrace();
        }

        // initialise the order queue
        try {

            // Load the order queue data from the specified file 
            this.orderQueue = new OrderQueue(queueFilename, menu);  
        } catch (FileReadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    public void launchGUI() {

        // Use SwingUtilities to ensure that the GUI is created and updated on the Event Dispatch Thread (EDT) for thread safety
        SwingUtilities.invokeLater(() -> {
            CoffeeShopFrame frame = new CoffeeShopFrame(this.menu, this.orderQueue, this.billingService);
            frame.setVisible(true); // Make the GUI frame visible to the user
        });
    }

}
