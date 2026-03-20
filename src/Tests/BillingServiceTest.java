package Tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import Main.BillingService;
import Main.Menu;
import Main.MenuItem;
import Main.Order;
import Main.OrderQueue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class BillingServiceTest {

    // Pah to the menu file for testing
    String MENU_FILE = "src/Database/Menu.csv";

    // Helper method to load the menu for testing
    Menu loadMenu() throws Exception {
        Menu menu = new Menu();
        menu.loadMenuFromFile(MENU_FILE);
        return menu;
    }

    // Helper method to get the first n item IDs from a list of MenuItems
    List<String> firstIDs(List<MenuItem> items, int n) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ids.add(items.get(i).getId());
        }
        return ids;
    }

    // Helper method to convert menu items from a map to a list for easier access in tests
    List<MenuItem> menuItemsAsList(Menu menu) {
        List<MenuItem> items = new ArrayList<>();
        for (MenuItem item : menu.getAllMenuItems().values()) {
            items.add(item);
        }
        return items;
    }

    // Tests that calculateTotal returns the correct total for a simple order with known item prices
    @Test
    @DisplayName("Test calculateTotal equals manually calculated total")
    void testCalculateTotal() throws Exception {
        Menu menu = loadMenu();
        BillingService billingService = new BillingService();

        List<MenuItem> items = menuItemsAsList(menu);
        assertTrue(items.size() >= 2, "Menu should have at least 2 items for this test");

        List<String> itemIDs = firstIDs(items, 2);
        OrderQueue queue = new OrderQueue();
        Order order = new Order("C1", itemIDs, LocalDateTime.now());
        queue.orderEnqueue(order, false);
        double expectedTotal = 0.0;
        for (int i = 0; i < itemIDs.size(); i++) {
            String id = itemIDs.get(i);
            expectedTotal += menu.getMenuItemById(id).getPrice();

        }
        double actualTotal = billingService.calculateTotal(queue, menu);
        assertEquals(expectedTotal, actualTotal, 0.001, "Calculated total should match expected total");
    }

    // Tests that getDiscount returns 0 for orders with less than 5 items 
    // and that getFinalAmount returns the correct final amount without any discount applied.
    @Test
    @DisplayName("getDiscount returns correct discount for less than 5 items")
    void testGetDiscountNoItems() throws Exception {
        Menu menu = loadMenu();
        BillingService billingService = new BillingService();

        List<MenuItem> items = menuItemsAsList(menu);
        assertTrue(items.size() >= 4, "Menu should have at least 4 items for this test");
        List<String> itemIDs = firstIDs(items, 4);
        OrderQueue queue = new OrderQueue();
        Order order = new Order("C1", itemIDs, LocalDateTime.now());
        queue.orderEnqueue(order, false);
        double discount = billingService.getDiscount(queue, menu);
        assertEquals(0.0, discount, 0.001, "Discount should be 0 for less than 5 items");
    }

    // Tests that getDiscount applies 20% discount for 5 or more items 
    // and that getFinalAmount returns the correct final amount after applying the discount.
    @Test
    @DisplayName("getDiscount applies 20% discount for 5 or more items")
    void testGetDiscountWithItems() throws Exception {
        Menu menu = loadMenu();
        BillingService billingService = new BillingService();

        List<MenuItem> items = menuItemsAsList(menu);
        assertTrue(items.size() >= 5, "Menu should have at least 5 items for this test");
        List<String> itemIDs = firstIDs(items, 5);
        OrderQueue queue = new OrderQueue();
        Order order = new Order("C1", itemIDs, LocalDateTime.now());
        queue.orderEnqueue(order, false);

        double total = billingService.calculateTotal(queue, menu);
        double discount = billingService.getDiscount(queue, menu);
        double finalAmount = billingService.getFinalAmount(queue, menu);

        assertEquals(total * 0.2, discount, 0.0001, "Discount should be 20% for 5 or more items");
        assertEquals(total - discount, finalAmount, 0.001, "Final amount should be total minus discount");
    }

    // Tests that getFinalAmount returns the correct final amount after applying the discount for an order with 5 or more items
    @Test
    @DisplayName("getFinalAmount returns correct final amount with discount")
    void testGetFinalAmount() throws Exception {
        Menu menu = loadMenu();
        BillingService billingService = new BillingService();

        List<MenuItem> items = menuItemsAsList(menu);
        assertTrue(items.size() >= 3, "Menu should have at least 3 items for this test");
        List<String> itemIDs = firstIDs(items, 3);
        OrderQueue queue = new OrderQueue();
        Order order = new Order("C1", itemIDs, LocalDateTime.now());
        queue.orderEnqueue(order, false);

        double total = billingService.calculateTotal(queue, menu);
        double discount = billingService.getDiscount(queue, menu);
        double finalAmount = billingService.getFinalAmount(queue, menu);

        assertEquals(total - discount, finalAmount, 0.001, "Final amount should be total minus discount");
    }

    // Tests that calculateTotal ignores invalid menu item IDs and only sums the prices of valid items in the order
    @Test
    @DisplayName("CalculateTotal ingnore invalid menu item IDs")
    void testCalculateTotalIgnoresInvalidItemIDs() throws Exception {
        Menu menu = loadMenu();
        BillingService billingService = new BillingService();

        List<MenuItem> items = menuItemsAsList(menu);
        assertTrue(items.size() >= 1, "Menu should have at least 1 item for this test");
        String validID = items.get(0).getId();
        String invalidID = "INVALID_ID";
        List<String> itemIDs = new ArrayList<>();
        itemIDs.add(validID);
        itemIDs.add(invalidID);
        OrderQueue queue = new OrderQueue();
        Order order = new Order("C1", itemIDs, LocalDateTime.now());
        queue.orderEnqueue(order, false);

        double expectedTotal = menu.getMenuItemById(validID).getPrice();
        double actualTotal = billingService.calculateTotal(queue, menu);

        assertEquals(expectedTotal, actualTotal, 0.001, "Calculated total should ignore invalid item IDs");
    }
}