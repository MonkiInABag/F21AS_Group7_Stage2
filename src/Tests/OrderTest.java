package Tests;

import Exception.OrderEmptyException;
import Main.Order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    // Tests that the constructor successfully creates an Order when all required fields are valid
    @Test
    @DisplayName("Constructor: Creates valid Order successfully")
    void constructorCreatesValidOrder() throws OrderEmptyException {
        List<String> items = Arrays.asList("BEV-001", "FOOD-002");
        LocalDateTime time = LocalDateTime.now();

        Order order = new Order("CUST-001", items, time);

        assertEquals("CUST-001", order.getCustomerId());
        assertEquals(items, order.getMenuItemIdList());
        assertEquals(time, order.getTimestamp());
        assertEquals("", order.getOrderNote());
    }

    // Tests that the constructor successfully creates an Order with a note when all required fields are valid
    @Test
    @DisplayName("Constructor: Throws exception when menuItemIdList is null")
    void constructorThrowsWhenListIsNull() {
        OrderEmptyException exception = assertThrows(OrderEmptyException.class,
                () -> new Order("CUST-001", null, LocalDateTime.now()));
        assertEquals("Order cannot be empty", exception.getMessage());
    }

    // Tests that the constructor throws OrderEmptyException when menuItemIdList is empty
    @Test
    @DisplayName("Constructor: Throws exception when menuItemIdList is empty")
    void constructorThrowsWhenListIsEmpty() {
        OrderEmptyException exception = assertThrows(OrderEmptyException.class,
                () -> new Order("CUST-001", Collections.emptyList(), LocalDateTime.now()));
        assertEquals("Order cannot be empty", exception.getMessage());
    }

    // Tests commas in order notes are replaced with spaces to prevent CSV formatting issues
    @Test
    @DisplayName("Constructor: Replaces commas with spaces")
    void constructorReplacesCommasInNote() throws OrderEmptyException {
        Order order = new Order("CUST-001", Arrays.asList("BEV-001"), LocalDateTime.now(), "No sugar, please");
        assertEquals("No sugar  please", order.getOrderNote());
    }

    // Tests that newlines in order notes are replaced with '-' to prevent CSV formatting issues
    @Test
    @DisplayName("Constructor: Replaces newlines with '-'")
    void constructorReplacesNewlinesInNote() throws OrderEmptyException {
        Order order = new Order("CUST-001", Arrays.asList("BEV-001"), LocalDateTime.now(), "Please\nmake it hot");
        assertEquals("Please-make it hot", order.getOrderNote());
    }

    // Tests that both commas and newlines in order notes are replaced correctly together
    @Test
    @DisplayName("Constructor: Replaces commas and newlines together")
    void constructorReplacesCommasAndNewlinesInNote() throws OrderEmptyException {
        Order order = new Order("CUST-001", Arrays.asList("BEV-001"), LocalDateTime.now(), "No sugar,\nplease");
        assertEquals("No sugar -please", order.getOrderNote());
    }
}