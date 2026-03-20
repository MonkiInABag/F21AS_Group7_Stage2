package Main;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoffeeShopFrame extends JFrame {

    //holds all MenuItem objects by id
    private final Menu menu;

    //used to calculate totals
    private final BillingService billingService;

    //private object to hold the orderQueue 
    private OrderQueue orderQueue;

    //left gui components
    //search box 
    private final JTextField searchField = new JTextField();

    //list model backing the JList shown as (<ID> <Name>)
    private final DefaultListModel<String> menuListModel = new DefaultListModel<>();

    //menu items list
    private final JList<String> menuList = new JList<>(menuListModel);

    //description area
    private final JTextArea descriptionArea = new JTextArea();


    //middle gui components
    //note box text field
    private final JTextArea noteArea = new JTextArea(6, 20);

    //bottom gui components
    //table showing the selected items and respective prices
    private final DefaultTableModel selectedModel =
            new DefaultTableModel(new Object[]{"Selected Items", "Price"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final JTable selectedTable = new JTable(selectedModel);
    
    //totals table showing basket total, discount and final total
    private final DefaultTableModel totalsModel =
            new DefaultTableModel(new Object[]{"", "£"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final JTable totalsTable = new JTable(totalsModel);

    //basket items as ids
    private final List<String> basketItemIds = new ArrayList<>();
    
    //main gui frame 
    public CoffeeShopFrame(Menu menu, OrderQueue orderQueue, BillingService billingService) {
        this.menu = menu;
        this.billingService = billingService;
        this.orderQueue = orderQueue;

        //changes the deafault look of the gui
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        }catch (Exception ignored) {
        }
        
        setTitle("Coffee Shop Ordering System");

        //disable defauld frame closing action
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(960, 720));

        //frame location on startup (not needed)
        //setLocationRelativeTo(null);

        
        setContentPane(buildRootLayout());
        loadMenuList("");     
        initTotalsTable();   
        hookEvents();

        //runs the report after closing the window
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                showReportAndExit();
            }
        });
    }
    
    //layount consists of three columns in the center (top area)
    //and a south area with billing table and order button
    private JPanel buildRootLayout() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 10);
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        //left column (search bar, menu list and item description)
        gc.gridx = 0; gc.weightx = 0.55;
        top.add(buildLeftPanel(), gc);

        //middle colunm (item note text box and respective button)
        gc.gridx = 1; gc.weightx = 0.40;
        top.add(buildMiddlePanel(), gc);

        //right column (buttons-add/remove/clear)
        gc.gridx = 2;
        gc.weightx = 0.02;
        gc.insets = new Insets(0, 0, 0, 0);
        gc.anchor = GridBagConstraints.NORTHEAST;
        top.add(buildRightPanel(), gc);

        root.add(top, BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);

        return root;
    }

    //left panel including search bar menu list and item description
    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 10, 0);
        gc.fill = GridBagConstraints.BOTH;

        // Search + list
        JPanel searchPanel = new JPanel(new BorderLayout(6, 6));
        searchPanel.setBorder(new TitledBorder("Enter Item Name or ID"));
        searchPanel.add(searchField, BorderLayout.NORTH);

        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setPreferredSize(new Dimension(300, 200));
        searchPanel.add(menuScroll, BorderLayout.CENTER);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; gc.weighty = 0.80;
        left.add(searchPanel, gc);


        // gc.gridy = 1; gc.weighty = 0.20; gc.insets = new Insets(0, 0, 0, 0);
        // left.add(descPanel, gc);

        // Description panel
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBorder(new TitledBorder("Item Description"));
        descPanel.setPreferredSize(new Dimension(1, 140));
        descPanel.setMinimumSize(new Dimension(1, 140));
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setOpaque(true); // optional: blends with Nimbus panel
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        descScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        descPanel.add(descScroll, BorderLayout.CENTER);

        gc.gridy = 1; gc.weighty = 0.35; gc.insets = new Insets(0, 0, 0, 0);
        left.add(descPanel, gc);

        return left;
    }

    //middle panel
    private JPanel buildMiddlePanel() {
        JPanel mid = new JPanel(new BorderLayout(6, 6));
        mid.setBorder(new TitledBorder("Item Note"));

        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        mid.add(new JScrollPane(noteArea), BorderLayout.CENTER);

        //clear note Jbutton
        //to be changed with customButton class style
        JButton clearNote = new JButton("Clear Note");
        clearNote.addActionListener(e -> noteArea.setText(""));
        mid.add(clearNote, BorderLayout.SOUTH);

        return mid;
    }

    //right button interation panel
    private JPanel buildRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JButton addBtn = new CustomButton("ADD ITEM", new Color(46, 139, 87), 28);
        JButton removeBtn = new CustomButton("REMOVE PREV ITEM", new Color(160, 34, 34), 28);
        JButton clearBtn = new CustomButton("CLEAR ORDER", new Color(138, 43, 226), 28);

        // Make buttons large like the prototype
        Dimension big = new Dimension(300, 100);
        addBtn.setMaximumSize(big);
        removeBtn.setMaximumSize(big);
        clearBtn.setMaximumSize(big);
        addBtn.setPreferredSize(big);
        removeBtn.setPreferredSize(big);
        clearBtn.setPreferredSize(big);
        addBtn.setMinimumSize(big);
        removeBtn.setMinimumSize(big);
        clearBtn.setMinimumSize(big);
        
        //spacing and alignment
        addBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        removeBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        clearBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(Box.createVerticalStrut(10));
        right.add(addBtn);
        right.add(Box.createVerticalStrut(12));
        right.add(removeBtn);
        right.add(Box.createVerticalStrut(12));
        right.add(clearBtn);
        right.add(Box.createVerticalGlue());

        //link actions to methods
        addBtn.addActionListener(e -> onAddSelected());
        removeBtn.addActionListener(e -> onRemovePrevious());
        clearBtn.addActionListener(e -> onClearOrder());

        return right;
    }

    //bottom panel with billing and oreder button
    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(12, 12));
        bottom.setBorder(new TitledBorder("Billing"));

        JScrollPane selectedScroll = new JScrollPane(selectedTable);
        selectedScroll.setPreferredSize(new Dimension(650, 160));

        JScrollPane totalsScroll = new JScrollPane(totalsTable);
        totalsScroll.setPreferredSize(new Dimension(300, 160));

        JPanel tables = new JPanel(new BorderLayout(12, 12));
        tables.add(selectedScroll, BorderLayout.CENTER);
        tables.add(totalsScroll, BorderLayout.EAST);

        bottom.add(tables, BorderLayout.CENTER);

        //place order button
        JButton placeOrder = new CustomButton("PLACE ORDER", new Color(102, 187, 106), 28);
        placeOrder.setPreferredSize(new Dimension(1, 90));
        placeOrder.addActionListener(e -> onPlaceOrder());
        bottom.add(placeOrder, BorderLayout.SOUTH);

        return bottom;
    }

    //searchbar events
    private void hookEvents() {
        //filter list as the user types
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }

            private void refresh() {
                loadMenuList(searchField.getText().trim());
            }
        });

        //when an item is clicked in the list the description uodates
        menuList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return; // avoids duplicate events
            updateDescriptionFromSelection();
        });
    }

    //pulls selected row from menuList, lookup MenuItem by ID, and then shows formatted details
    private void updateDescriptionFromSelection() {
        String selected = menuList.getSelectedValue();
    
    //dont show anything
    if (selected == null) {
        descriptionArea.setText("");
        return;
    }

    String itemId = parseIdFromListRow(selected);
    MenuItem item = menu.getMenuItemById(itemId);
    if (item == null) {
        descriptionArea.setText("");
        return;
    }
    //get items details
    descriptionArea.setText(
            item.getName() + "\n\n" +
            item.getDescription() + "\n\n" +
            "ID: " + itemId
    );
    //show at the top
    descriptionArea.setCaretPosition(0);
    }


   //display of the billing

    //set initial values in the table
    private void initTotalsTable() {
        totalsModel.setRowCount(0);
        totalsModel.addRow(new Object[]{"Total", "0.00"});
        totalsModel.addRow(new Object[]{"Discount", "0.00"});
        totalsModel.addRow(new Object[]{"Final Total", "0.00"});
    }

    //calculate totals from the basket items 
    //if 5 items are in the basket then: 20% of the total
    private void updateTotals() {
        double total = 0.0;

        // Sum prices from menu using id from the basket
        for (String id : basketItemIds) {
            MenuItem item = menu.getMenuItemById(id);
            if (item != null) {
                total = (total + item.getPrice());
            } 
        }

        double discount = (basketItemIds.size() >= 5) ? total * 0.20 : 0.0;
        double finalTotal = total - discount;

        //update table values
        totalsModel.setValueAt(String.format("%.2f", total), 0, 1);
        totalsModel.setValueAt(String.format("%.2f", discount), 1, 1);
        totalsModel.setValueAt(String.format("%.2f", finalTotal), 2, 1);
    }


    //button handlers

    //add Item button
    //requires a selected menu item, it adds its ID to basketItemIds, it adds a row to selectedModel and updates totals
    private void onAddSelected() {
        String selected = menuList.getSelectedValue();
        if (selected == null) {
            showError("Please select at least one item");
            return;
        }

        String itemId = parseIdFromListRow(selected);
        MenuItem item = menu.getMenuItemById(itemId);
        if (item == null) {
            showError("Invalid item selection");
            return;
        }

        basketItemIds.add(itemId);
        selectedModel.addRow(new Object[]{
                item.getName(),
                String.format("£%.2f", item.getPrice())
        });
        updateTotals();
    }

    //remove Previous Item button,removes last item added (in a stack behaviour) and updates totals
    private void onRemovePrevious() {
        if (basketItemIds.isEmpty() || selectedModel.getRowCount() == 0) {
            showError("No items to remove");
            return;
        }

        basketItemIds.remove(basketItemIds.size() - 1);
        selectedModel.removeRow(selectedModel.getRowCount() - 1);
        updateTotals();
    }

    // Clear Order button, empties basket and clears table, resets totals back to 0.00
    private void onClearOrder() {
        basketItemIds.clear();
        selectedModel.setRowCount(0);
        initTotalsTable();
    }

    //Place Order button, validates basket isn't empty, creates an Order from current basket, stores it in completedOrders and shows confirmation then clears current order
    private void onPlaceOrder() {
        if (basketItemIds.isEmpty()) {
            showError("Please select at least one item");
            return;
        }

        try {
            //TODO: input box for customer name
            String customerId = "WalkIn-001";
            String orderNote = noteArea.getText();
            if (orderNote.isEmpty()){ 
                orderNote="None";
            }

            Order order = new Order(
                    customerId,
                    new ArrayList<>(basketItemIds),
                    java.time.LocalDateTime.now(),
                    orderNote           
            );
            orderQueue.orderEnqueue(order);
            // completedOrders.add(order);

            JOptionPane.showMessageDialog(
                    this,
                    "Order placed!\nItems: " + basketItemIds.size(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            onClearOrder();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    //menu filtering for id or item name
    private void loadMenuList(String filter) {
        menuListModel.clear();
        String f = (filter == null) ? "" : filter.toLowerCase();

        for (Map.Entry<String, MenuItem> entry : menu.getAllMenuItems().entrySet()) {
            String id = entry.getKey();
            MenuItem item = entry.getValue();

            String row = id + " · " + item.getName();
            if (f.isEmpty()
                    || id.toLowerCase().contains(f)
                    || item.getName().toLowerCase().contains(f)) {
                menuListModel.addElement(row);
            }
        }
    }

    //generates the exit report when user closes the main frame
    private void showReportAndExit() {
        ReportGenerator rg = new ReportGenerator();
        String report = rg.generateSummaryReport(menu, orderQueue.getOrdersAsList(), billingService);

            // Create a styled report frame
            JFrame reportFrame = new JFrame("Order Summary Report");
            reportFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            
            // Main panel with gradient-like background
            JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
            mainPanel.setBackground(new Color(240, 248, 255));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            // Header panel
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(new Color(25, 85, 145));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            JLabel titleLabel = new JLabel("Order Summary Report");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
            titleLabel.setForeground(Color.WHITE);
            headerPanel.add(titleLabel);
            
            // Report text area with styling
            JTextArea reportArea = new JTextArea(report);
            reportArea.setEditable(false);
            reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            reportArea.setBackground(new Color(255, 255, 255));
            reportArea.setForeground(new Color(50, 50, 50));
            reportArea.setLineWrap(true);
            reportArea.setWrapStyleWord(true);
            reportArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            reportArea.setMargin(new Insets(10, 10, 10, 10));
            
            JScrollPane scrollPane = new JScrollPane(reportArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            buttonPanel.setBackground(new Color(240, 248, 255));
            JButton closeBtn = new JButton("Close & Exit");
            closeBtn.setBackground(new Color(220, 20, 60));
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setFont(new Font("Arial", Font.BOLD, 12));
            closeBtn.setFocusPainted(false);
            closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeBtn.addActionListener(ev -> System.exit(0));
            buttonPanel.add(closeBtn);
            
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            reportFrame.add(mainPanel);
            reportFrame.setSize(650, 500);
            reportFrame.setLocationRelativeTo(null);
            reportFrame.setVisible(true);

            dispose();
        }

    //shows warning dialog
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.WARNING_MESSAGE);
    }

    //extracts ID from list row (row format<ID> <Name>)
    private String parseIdFromListRow(String row) {
        int idx = row.indexOf("·");
        if (idx < 0) return row.trim();
        return row.substring(0, idx).trim();
    }

    //escapes HTML so JLabel doesn't break if names contain < or &
    private String escape(String s) {
        return s == null ? "" :
                s.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;");
    }
}