package Main;

import java.time.LocalDateTime;
import java.util.List;

import Exception.OrderEmptyException;

public class Order {
    
    private String customerId;
    private List<String> menuItemIdList;
    private LocalDateTime timestamp;
    private String orderNote;

    public Order(String customerId, List<String> menuItemIdList, LocalDateTime timestamp) throws OrderEmptyException {
        this(customerId,menuItemIdList,timestamp,"");
    }

    public Order(String customerId, List<String> menuItemIdList, LocalDateTime timestamp, String orderNote) 
            throws OrderEmptyException {

        if (menuItemIdList == null || menuItemIdList.isEmpty()) {
            throw new OrderEmptyException("Order cannot be empty");
        }
        
        this.customerId = customerId;
        this.menuItemIdList = menuItemIdList;
        this.timestamp = timestamp;
        this.orderNote=orderNote;

    }

    public String getCustomerId() {
        return this.customerId;
    }

    public List<String> getMenuItemIdList() {
        return this.menuItemIdList;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getOrderNote(){
        String newOrderNote = this.orderNote
        .replace(',',' ')//replace commas with spaces as to not confuse the csv formatting
        .replace('\n', '-'); // replace new lines with -

        return newOrderNote;
    }
}