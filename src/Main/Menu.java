package Main;

import java.io.*;
import java.util.*;

import Exception.FileReadException;
import Exception.MenuItemMissingDataException;

import Structures.Category;

public class Menu {

    private FileHandler rawMenuData; // hold the raw data read from the file as a 2D array
    private Map<String, MenuItem> menuItems;

    public Menu() {
        this.menuItems = new HashMap<>();
    }

    private boolean isIdValid(String id) {
        // Validate ID format: <CATEGORY>-XXX
        if (id.matches("^[A-Za-z]+-\\d{3}$")) {
            return true;    
        }else{
            return false;
        }
    }

    private double getPriceInt(String priceString) {
        try {
        //parse string into double
           double price = Double.parseDouble(priceString);
           if (price < 0) {
               return -1; // Invalid price value
           }else{
            return price;
           }
        } catch (NumberFormatException e) {
            return -1; // Invalid price format
        }
    }

    private Category getCategory(String categoryString) {
        try {
            //coudl do some more parseing here to allow for more flexible category names. Eg DRI, FOO, DES and whatnot
            return Category.valueOf(categoryString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Return null for invalid category
        }
    }
    private boolean isNameValid(String name) {
        return name != null && !name.isEmpty();
    }
    private boolean isDescriptionValid(String description) {
        return description != null && !description.isEmpty();
    }


    public void loadMenuFromFile(String filename) throws FileReadException, MenuItemMissingDataException {        
        this.rawMenuData = new FileHandler(filename);
        this.rawMenuData.readFile();
        //iterate through raw data as rows
        for (List<String> row : this.rawMenuData.getFileArray()) {
            String id = row.get(0);
            String name = row.get(1);
            String description = row.get(2);
            String categoryString = row.get(3);
            String priceString = row.get(4);

            if (!isIdValid(id)) {
                continue; // skip row if invalid ID
            }
            if (!isNameValid(name)) {
                continue; // skip row if invalid name
            }
            if (!isDescriptionValid(description)) {
                continue; // skip row if invalid description
            }

            Category category = getCategory(categoryString);
            if (category == null) {
                continue; // skip row if invalid category
            }

            double price = getPriceInt(priceString);
            if (price == -1) {
                continue; // skip row if invalid price
            }

            //if all items are valid, create new menu item
            MenuItem item = new MenuItem(id, name, description, category, price);
            menuItems.put(id, item);
        }
    }

    public MenuItem getMenuItemById(String id) {
        return menuItems.get(id);
    }

    public boolean isValidItemId(String id) {
        return menuItems.containsKey(id);
    }

    public Map<String, MenuItem> getAllMenuItems() {
        return new HashMap<>(menuItems);
    }

}
