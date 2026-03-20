package Main;

import java.io.*;
import java.util.*;

import Exception.OrderEmptyException;
import Main.FileHandler;
import Main.Order;
import Exception.FileReadException;
import Exception.FileWriteException;
import Exception.MenuItemMissingDataException;

import java.time.LocalDateTime;

public class OrderQueue {

    //time in minutes after which an order will be invalid. 1440 Minutes is 24hrs
    static int timeStampExpiry = 1440;
    //time in minutes which the timestamp can be from the future
    static int timeStampBuffer = 1;

    // private object to store the queue of orders
    private Queue<Order> orderQueue;
    private FileHandler rawQueueData; // hold the raw data read from the file as a 2D array
    private String filename; // hold the filename of the order queue file
    private Menu menu; //used for validating itemId list
    // Initialize the order queue

    private String menuItemIdListToString(List<String> menuItemIdList) {
        return String.join(";", menuItemIdList);
    }

    private List<String> stringToMenuItemIdList(String menuItemIdString) {
        return Arrays.asList(menuItemIdString.split(";"));
    }

    private boolean validateCustomerId(String customerId) {
        //TODO: more validation required. such as length or formatting <name>-001
        return (!customerId.isEmpty());
        // if (id.matches("^[A-Za-z]+-\\d{3}$")) {
        //     return true;    
        // }else{
        //     return false;
        // }
    }

    private boolean validateMenuItemIds(List<String> menuItemIdList) {
        
        for (String itemID : menuItemIdList){
            //if any of the itemID's are invalid, return false
            if (!this.menu.isValidItemId(itemID)){
                return false;
            }
        }
        //if loop completes, return true
        return true; 
    }

    private boolean validateTimestamp(LocalDateTime timestamp) {
        //to be valid, the order's timestamp cannot be in the future or too far in the past. 
        LocalDateTime present = java.time.LocalDateTime.now();
        LocalDateTime future = present.plusMinutes(timeStampBuffer); 
        LocalDateTime past = present.minusMinutes(timeStampExpiry);

        return (!timestamp.isAfter(future) && !timestamp.isBefore(past)); // Placeholder return value
    }

    private boolean validateOrderNote(String ordernote) {
        //order note is optional so no validation reqruied. 
        // Empty values get replaced with "None", so validation could check if empty. I dont think it is necessary
        return true;
    }
    
    public OrderQueue(String filename) throws FileReadException{
        this(filename, null);
    };

    public OrderQueue(String filename, Menu menu) throws FileReadException {
        this.filename = filename;
        // Load orders from a file and populate the queue
        this.orderQueue = new LinkedList<>();

        this.rawQueueData = new FileHandler(this.filename);
        this.rawQueueData.readFile();

        this.menu = menu;

        for (List<String> row : this.rawQueueData.getFileArray()) {
            // validate orders
            String customerId = row.get(0);
            List<String> menuItemIdList = stringToMenuItemIdList(row.get(1));
            LocalDateTime timestamp = LocalDateTime.parse(row.get(2));
            String orderNote = row.get(3);

            if (!validateCustomerId(customerId)) {
                continue; // Skip for invalid customer ID
            }
            if (!validateMenuItemIds(menuItemIdList)) {
                continue; // Skip for invalid menu item ID list
            }
            if (!validateTimestamp(timestamp)) {
                continue; // Skip for invalid timestamp
            }
            if (!validateOrderNote(orderNote)) {
                continue;
            }

            try {
                Order order = new Order(customerId, menuItemIdList, timestamp,orderNote);
                this.orderEnqueue(order, false); // Enqueue without writing to file
            } catch (Exception e) {
                throw new FileReadException("Error reading order from file '" + filename + "': " + e.getMessage());
            }
        }
    }

    // overloading method so other classes must remove last item in file
    public Order orderDequeue() throws FileReadException, FileWriteException{
        return orderDequeue(true);
    }

    // Remove and return the next order in the queue
    private Order orderDequeue(boolean writeToFile) throws FileReadException, FileWriteException {
        // Remove the first order in the file
        Order head = orderQueue.poll(); // Remove and return the head of the queue
        if (writeToFile && rawQueueData != null) {
            try {
                this.rawQueueData.removeRow(0); // Remove the first row from the file
            } catch (FileReadException e) {
                throw new FileReadException("Error removing order from file '" + filename + "': " + e.getMessage());
            } catch (FileWriteException e) {
                throw new FileWriteException("Error writing updated queue to file '" + filename + "': " + e.getMessage());
            } catch (Exception e) {
                throw new FileWriteException("Unexpected error while updating order file '" + filename + "': " + e.getMessage());
            }
        }
        return head;
    }

    // overloading method so other classes must write file
    public void orderEnqueue(Order order) throws FileReadException, FileWriteException {
        orderEnqueue(order, true);
    }

    // Add a new order to the end of the queue
    public void orderEnqueue(Order order, boolean writeToFile) throws FileReadException, FileWriteException {
        // Add order to end to end of file
        if (writeToFile && this.rawQueueData != null) {
            try {
                this.rawQueueData.appendRow(List.of(order.getCustomerId(),

                        menuItemIdListToString(order.getMenuItemIdList()), order.getTimestamp().toString(), order.getOrderNote()));
            } catch (FileReadException e) {
                throw new FileReadException("Error accessing order file '" + filename + "' while adding new order: " + e.getMessage());
            } catch (FileWriteException e) {
                throw new FileWriteException("Error writing new order to file '" + filename + "': " + e.getMessage());
            } catch (Exception e) {
                throw new FileWriteException("Unexpected error while saving order to file '" + filename + "': " + e.getMessage());
            }
        }
        orderQueue.add(order);
    }

    // Return a list of orders for a specific customer ID
    public List<Order> getOrdersByCustomerId(String customerId) throws OrderEmptyException {

        // Create a copy of the order queue to iterate through
        Queue<Order> orderQueueCopy = new LinkedList<>();
        orderQueueCopy.addAll(orderQueue);
        int size = orderQueueCopy.size();
        List<Order> orders = new ArrayList<Order>();

        // iterate through copy of queue and add orders with matching id's to orders
        // list
        for (int i = 0; i <= size - 1; i++) {
            Order thisOrder = orderQueueCopy.remove(); // Remove the head of the queue to move to the next order
            if (thisOrder.getCustomerId().equals(customerId)) {
                orders.add(thisOrder);
            }

        }
        if (orders.isEmpty()) {
            throw new OrderEmptyException("No orders found for customer ID: " + customerId);
        } else {
            return orders;
        }
    }

    public List<Order> getOrdersAsList() {
        return new LinkedList<>(orderQueue); // return copy (safe)
    }

    public OrderQueue() {
        this.orderQueue = new LinkedList<>();
    }
}