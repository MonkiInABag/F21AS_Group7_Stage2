package Tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import Exception.MenuItemMissingDataException;
import Main.MenuItem;
import Structures.Category;

public class MenuItemTest {

    // Helper method to create a valid MenuItem for use in multiple tests
    private MenuItem createValidMenuItem() throws MenuItemMissingDataException
    {
        return new MenuItem("BEV-001", "Latte", "Hot milk coffee", Category.DRINK, 3.50);
    }

    // Tests that the constructor throws MenuItemMissingDataException when required fields are null
    @Test
    @DisplayName("Constructor: Throws when required fields are null")
    void constructorThrowsWhenNull() 
    {
        assertThrows(MenuItemMissingDataException.class,
                () -> new MenuItem(null, "Latte", "Hot milk coffee", Category.DRINK, 3.50));
        assertThrows(MenuItemMissingDataException.class,
                () -> new MenuItem("BEV-001", null, "Hot milk coffee", Category.DRINK, 3.50));
        assertThrows(MenuItemMissingDataException.class,
                () -> new MenuItem("BEV-001", "Latte", "Hot milk coffee", null, 3.50));
        assertThrows(MenuItemMissingDataException.class,
                () -> new MenuItem("BEV-001", "Latte", "Hot milk coffee", Category.DRINK, null));
    }

    // Tests that the constructor throws MenuItemMissingDataException when price is negative
    @Test
    @DisplayName("Constructor: Test creating a MenuItem with negative price")
    void constructorThrowsWhenNegativePrice()
    {
        assertThrows(MenuItemMissingDataException.class,
                () -> new MenuItem("BEV-001", "Latte", "Hot milk coffee", Category.DRINK, -1.00));
    }

    // Tests that the constructor throws MenuItemMissingDataException when required string fields are empty
    @Test
    @DisplayName("Constructor: Throws when creating MenuItem with empty strings")
    void constructorThrowsWhenEmptyStrings()
    {
        assertThrows(MenuItemMissingDataException.class,
                () -> new MenuItem("BEV-001", "", "Hot milk coffee", Category.DRINK, 3.50));
    }

    // Tests that the constructor successfully creates a MenuItem when all required fields are valid
    @Test
    @DisplayName("Constructor: creates a valid MenuItem")
    void constructorCreatesValidMenuItem() throws MenuItemMissingDataException
    {
        assertDoesNotThrow(() -> new MenuItem("BEV-001", "Latte", "Hot milk coffee", Category.DRINK, 3.50));
    }

    // Tests that the constructor allows null description but does not throw an exception
    @Test
    @DisplayName("Constructor: Test creating a MenuItem with null description")
    void constructorThrowsWhenNullDescription() throws MenuItemMissingDataException
    {
        assertDoesNotThrow(() -> new MenuItem("BEV-001", "Latte", null, Category.DRINK, 3.50));
    }

    // Tests that the constructor allows empty description but does not throw an exception
    @Test
    @DisplayName("Getters: Test getters return correct values")
    void gettersReturnCorrectValues()throws MenuItemMissingDataException
    {
        MenuItem item = createValidMenuItem();
        assertEquals("BEV-001", item.getId());
        assertEquals("Latte", item.getName());
        assertEquals("Hot milk coffee", item.getDescription());
        assertEquals(Category.DRINK, item.getCategory());
        assertEquals(3.50, item.getPrice(), 0.01);
    }

    // Tests that the toString method returns a string in the expected format
    @Test
    @DisplayName("toString: Test toString returns expected format")
    void toStringReturnsExpectedFormat() throws MenuItemMissingDataException
    {
        MenuItem item = createValidMenuItem();
        String expected = "MenuItem{id=BEV-001, name='Latte', description='Hot milk coffee', category=DRINK, price=3.5}";
        assertEquals(expected, item.toString());
    }
}