package Main;

import Exception.MenuItemMissingDataException;
import Structures.Category;

public class MenuItem {

    
    private String id;
    private String name;
    private String description;
    private Category category;
    private Double price;

    public MenuItem(String id,
                    String name,
                    String description,
                    Category category,
                    Double price)
            throws MenuItemMissingDataException {

        if (id == null) {
            throw new MenuItemMissingDataException("Menu item ID cannot be null.");
        }

        if (name == null || name.isBlank()) {
            throw new MenuItemMissingDataException("Menu item name is required.");
        }

        if (category == null) {
            throw new MenuItemMissingDataException("Menu item category is required.");
        }

        //TODO: was <=, changed it to <. Free items may be allowed
        if (price == null || price < 0) {
            throw new MenuItemMissingDataException("Menu item price must be positive.");
        }

        this.id = id;
        this.name = name;
        this.description = description; // optional, so no strict validation
        this.category = category;
        this.price = price;
    }

    public String getId() {
    return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
    return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public Double getPrice() {
        return this.price;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", price=" + price +
                '}';
    }
}
