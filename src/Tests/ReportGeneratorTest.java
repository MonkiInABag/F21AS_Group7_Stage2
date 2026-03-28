package Tests;

import Main.BillingService;
import Main.Menu;
import Main.ReportGenerator;
import Main.Order;

import Exception.OrderEmptyException;
import Exception.FileReadException;
import Exception.MenuItemMissingDataException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private Menu loadTestMenu() throws FileReadException, MenuItemMissingDataException {
        Menu menu = new Menu();
        menu.loadMenuFromFile("src\\Database\\menu.csv");
        return menu;
    }

    @Test
    @DisplayName("countItemFrequency: Counts repeated item IDs correctly")
    void countItemFrequencyCountsItemsCorrectly() throws Exception {
        ReportGenerator rg = new ReportGenerator();

        Order order1 = new Order(
                "CUST-001",
                Arrays.asList("BEV-001", "FOOD-001"),
                LocalDateTime.now()
        );

        Order order2 = new Order(
                "CUST-002",
                Arrays.asList("BEV-001", "FOOD-002", "FOOD-002"),
                LocalDateTime.now()
        );

        Map<String, Integer> frequency = rg.countItemFrequency(Arrays.asList(order1, order2));

        assertEquals(2, frequency.get("BEV-001"));
        assertEquals(1, frequency.get("FOOD-001"));
        assertEquals(2, frequency.get("FOOD-002"));
    }

    @Test
    @DisplayName("calculateTotalRevenue: Sums valid item prices correctly")
    void calculateTotalRevenueSumsCorrectly() throws Exception {
        Menu menu = loadTestMenu();
        ReportGenerator rg = new ReportGenerator();

        Order order1 = new Order(
                "CUST-001",
                Arrays.asList("BEV-001"),
                LocalDateTime.now()
        );

        Order order2 = new Order(
                "CUST-002",
                Arrays.asList("FOOD-001"),
                LocalDateTime.now()
        );

        double expected = 0.0;
        expected += menu.getMenuItemById("BEV-001").getPrice();
        expected += menu.getMenuItemById("FOOD-001").getPrice();

        double actual = rg.calculateTotalRevenue(Arrays.asList(order1, order2), menu);

        assertEquals(expected, actual, 0.001);
    }

    @Test
    @DisplayName("calculateTotalRevenue: Ignores invalid item IDs")
    void calculateTotalRevenueIgnoresInvalidIds() throws Exception {
        Menu menu = loadTestMenu();
        ReportGenerator rg = new ReportGenerator();

        Order order = new Order(
                "CUST-001",
                Arrays.asList("BEV-001", "INVALID-999"),
                LocalDateTime.now()
        );

        double expected = menu.getMenuItemById("BEV-001").getPrice();
        double actual = rg.calculateTotalRevenue(List.of(order), menu);

        assertEquals(expected, actual, 0.001);
    }

    @Test
    @DisplayName("generateSummaryReport: Contains expected headings and totals")
    void generateSummaryReportContainsExpectedText() throws Exception {
        Menu menu = loadTestMenu();
        ReportGenerator rg = new ReportGenerator();
        BillingService billing = new BillingService();

        Order order1 = new Order(
                "CUST-001",
                Arrays.asList("BEV-001", "FOOD-001"),
                LocalDateTime.now(),
                "No sugar"
        );

        Order order2 = new Order(
                "CUST-002",
                Arrays.asList("BEV-001"),
                LocalDateTime.now(),
                "Extra hot"
        );

        String report = rg.generateSummaryReport(menu, Arrays.asList(order1, order2), billing);

        assertTrue(report.contains("DAILY SALES SUMMARY"));
        assertTrue(report.contains("Total Orders: 2"));
        assertTrue(report.contains("Items Sold:"));
        assertTrue(report.contains("Total Revenue:"));
        assertTrue(report.contains("Discount:"));
        assertTrue(report.contains("Final Total:"));
        assertTrue(report.contains("Order Notes:"));
        assertTrue(report.contains("Order 1: No sugar"));
        assertTrue(report.contains("Order 2: Extra hot"));
        assertTrue(report.contains("END OF REPORT"));
    }
}