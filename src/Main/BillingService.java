package Main;

import java.util.List;
import java.util.Queue;

import Main.Menu;
import Main.MenuItem;
import Main.Order;
import Main.OrderQueue;

public class BillingService {

    private List<Order> orders;     // List of orders for calculations

    public double calculateTotal(OrderQueue queue, Menu menu) {

        double total = 0.0; // Reset total before calculation

        this.orders = queue.getOrdersAsList();  // Get the current list of orders from the queue
        
        // Loop through each order in the list
        for (Order order : orders) {
            
            // Loop through each item ID in the order's menu item list
            for (String itemId : order.getMenuItemIdList()) {

                MenuItem item = menu.getMenuItemById(itemId);   // Retrieve the MenuItem object using the item ID

                // Check if the item exists in the menu
                if (item != null) {
                    total += item.getPrice();   // Add the price of the item to the total
                }
            }
        }

        return total;
    }

    // Method to calculate discount based on the total number of items ordered
    public double getDiscount(OrderQueue queue, Menu menu) {

        double total = calculateTotal(queue, menu); // Calculate the total amount first

        // Count the total number of items ordered across all orders
        int itemCount = 0;

        // Get the current list of orders from the queue for discount calculation
        this.orders = queue.getOrdersAsList();

        // Loop through each order and count the total number of items ordered
        for (Order order : orders) {
            // Add the number of items in the current order to the total item count
            itemCount += order.getMenuItemIdList().size();
        }

        // 20% discount if 5 or more items ordered
        if (itemCount >= 5) {
            return total * 0.20;
        }

        return 0.0; // No discount if less than 5 items ordered
    }

    // Method to calculate the final amount after applying discounts
    public double getFinalAmount(OrderQueue queue, Menu menu) {

        double total = calculateTotal(queue, menu); // Calculate the total amount before discount
        double discount = getDiscount(queue, menu); // Calculate the discount based on the total amount and item count

        return total - discount;    // Final amount after applying the discount
    }
}