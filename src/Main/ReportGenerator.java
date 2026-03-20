package Main;

import java.util.*;

import Main.BillingService;
import Main.Menu;
import Main.MenuItem;
import Main.Order;
import Main.OrderQueue;

public class ReportGenerator {

    public String generateSummaryReport(Menu menu, List<Order> orders, BillingService billingService) {

        StringBuilder report = new StringBuilder(); // Using StringBuilder for efficient string concatenation

        report.append("\n           DAILY SALES SUMMARY         \n");            // Header
        report.append("Total Orders: ").append(orders.size()).append("\n"); // Total number of orders

        Map<String, Integer> frequencyMap = countItemFrequency(orders); // Get frequency of each menu item sold

        report.append("\nItems Sold:\n");   // Section for items sold

        // Loop through frequency map to list each item and its count
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {

            String itemId = entry.getKey();     // Get the menu item ID
            Integer count = entry.getValue();   // Get the count of how many times this item was sold

            MenuItem item = menu.getMenuItemById(itemId);   // Retrieve the MenuItem object using the item ID

            // If the item exists in the menu
            if (item != null) {
                // Append the item name and count to the report
                report.append(item.getName()).append(" x").append(count).append("\n");
            }
        }

        // Create temporary OrderQueue for BillingService
        OrderQueue tempQueue = new OrderQueue();
        // For all instances of Order
        for (Order order : orders) { 
            try {
                tempQueue.orderEnqueue(order, false);   // Enqueue order without writing to file
            } catch (Exception e) {
                // Ignore, since writeToFil e= false shouldn't fail
            }
        }

        double total = billingService.calculateTotal(tempQueue, menu);       // Calculate total revenue using BillingService
        double discount = billingService.getDiscount(tempQueue, menu);       // Calculate total discount using BillingService
        double finalTotal = billingService.getFinalAmount(tempQueue, menu);  // Calculate final total after discount using BillingService

        report.append(String.format("\nTotal Revenue: £%.2f\n", total));    // Append total revenue to the report
        report.append(String.format("Discount: £%.2f\n", discount));        // Append total discount to the report
        report.append(String.format("Final Total: £%.2f\n", finalTotal));   // Append final total to the report

        // Section for order notes
        report.append("\nOrder Notes:\n");
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);            // Get each order from the list of orders
            // Append the order number and its note to the report
            report.append("Order ").append(i + 1).append(": ").append(order.getOrderNote()).append("\n");
        }

        report.append("           END OF REPORT         \n");

        return report.toString();   // Return the complete report as a string
    }

    // Method to count the frequency of each menu item sold across all orders
    public Map<String, Integer> countItemFrequency(List<Order> orders) {

        Map<String, Integer> frequency = new HashMap<>();   // Use HashMap to store the frequency of each menu item

        // For all instances of Order, loop through each order and count the frequency of each menu item sold
        for (Order order : orders) {
            for (String itemId : order.getMenuItemIdList()) {

                // getOrDefault is used to return the current count of the item ID,
                // or 0 if it doesn't exist in the map, and then add 1 to it
                frequency.put(
                        itemId,
                        frequency.getOrDefault(itemId, 0) + 1
                );
            }
        }

        return frequency;   // Return the frequency map containing the count of each menu item sold
    }

    // Method to calculate the total revenue from a list of orders and the menu
    public double calculateTotalRevenue(List<Order> orders, Menu menu) {

        double total = 0.0;

        // For all instances of Order, loop through each order and calculate the total revenue by summing the price of each menu item sold
        for (Order order : orders) {
            for (String itemId : order.getMenuItemIdList()) {

                // Retrieve the MenuItem object using the item ID from the menu to get its price
                MenuItem item = menu.getMenuItemById(itemId);

                if (item != null) {
                    total += item.getPrice();
                }
            }
        }

        return total;   // Return the total revenue calculated from all orders
    }
}