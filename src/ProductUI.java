import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;

public class ProductUI extends JFrame {

    JTextField nameField, categoryField, priceField, quantityField, searchField;
    JComboBox<String> supplierBox;
    JTable table;
    DefaultTableModel model;
    HashMap<String, Integer> supplierMap = new HashMap<>();

    public ProductUI() {
        setTitle("Product Management");
        setSize(950, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.LIGHT_GRAY);
        setLayout(new BorderLayout(10, 10));

        // --- Top panel for product input fields ---
        JPanel inputPanel = new JPanel(new GridLayout(5,2,10,10));
        inputPanel.setBackground(Color.LIGHT_GRAY);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20,50,0,50));

        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        inputPanel.add(categoryField);

        inputPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        inputPanel.add(priceField);

        inputPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        inputPanel.add(quantityField);

        inputPanel.add(new JLabel("Supplier:"));
        supplierBox = new JComboBox<>();
        loadSuppliersIntoComboBox();
        inputPanel.add(supplierBox);

        add(inputPanel, BorderLayout.NORTH);

        // --- Center panel for buttons + search + table ---
        JPanel centerPanel = new JPanel(new BorderLayout(10,10));
        centerPanel.setBackground(Color.LIGHT_GRAY);

        // Button panel (top of center panel)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.LIGHT_GRAY);

        JButton addButton = new JButton("Add Product");
        JButton updateButton = new JButton("Update Product");
        JButton deleteButton = new JButton("Delete Product");
        JButton sellButton = new JButton("Sell Product");  // New button

        JButton[] buttons = {addButton, updateButton, deleteButton, sellButton};
        for(JButton btn : buttons){
            btn.setBackground(new Color(34,139,34));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Arial", Font.BOLD, 14));
            btn.setPreferredSize(new Dimension(150,40));
            buttonPanel.add(btn);
        }
        centerPanel.add(buttonPanel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.LIGHT_GRAY);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Table
        table = new JTable();
        model = new DefaultTableModel(new String[]{
                "ID","Name","Category","Price","Quantity","Supplier","Sold"},0);  // Added Sold column
        table.setModel(model);
        loadProducts();
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.LIGHT_GRAY);
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- Action listeners ---
        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());

        // Sell product functionality
        sellButton.addActionListener(e -> sellProduct());

        // Search functionality
        searchField.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e){
                String keyword = searchField.getText().toLowerCase();
                model.setRowCount(0);
                try(Connection con = DB.getConnection()){
                    String sql = "SELECT p.*, s.name AS supplier_name FROM product p " +
                            "LEFT JOIN supplier s ON p.supplier_id = s.supplier_id " +
                            "WHERE p.name LIKE ? OR p.category LIKE ? OR s.name LIKE ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, "%"+keyword+"%");
                    ps.setString(2, "%"+keyword+"%");
                    ps.setString(3, "%"+keyword+"%");
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()){
                        model.addRow(new Object[]{
                                rs.getInt("product_id"),
                                rs.getString("name"),
                                rs.getString("category"),
                                rs.getDouble("price"),
                                rs.getInt("quantity"),
                                rs.getString("supplier_name"),
                                rs.getInt("sold")
                        });
                    }
                } catch(Exception ex){ ex.printStackTrace(); }
            }
        });

        // Populate fields when selecting a row
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                int row = table.getSelectedRow();
                if(row >= 0){
                    nameField.setText(model.getValueAt(row,1).toString());
                    categoryField.setText(model.getValueAt(row,2).toString());
                    priceField.setText(model.getValueAt(row,3).toString());
                    quantityField.setText(model.getValueAt(row,4).toString());
                    supplierBox.setSelectedItem(model.getValueAt(row,5).toString());
                }
            }
        });

        setVisible(true);
    }

    private void loadSuppliersIntoComboBox(){
        try(Connection con = DB.getConnection()){
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT supplier_id,name FROM supplier");
            supplierBox.removeAllItems();
            supplierMap.clear();
            while(rs.next()){
                supplierMap.put(rs.getString("name"), rs.getInt("supplier_id"));
                supplierBox.addItem(rs.getString("name"));
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void loadProducts(){
        model.setRowCount(0);
        try(Connection con = DB.getConnection()){
            String sql = "SELECT p.*, s.name AS supplier_name FROM product p " +
                    "LEFT JOIN supplier s ON p.supplier_id = s.supplier_id";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("supplier_name"),
                        rs.getInt("sold")  // Sold column
                });
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void addProduct(){
        try(Connection con = DB.getConnection()){
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO product(name,category,price,quantity,supplier_id,sold) VALUES(?,?,?,?,?,0)"
            );
            ps.setString(1,nameField.getText());
            ps.setString(2,categoryField.getText());
            ps.setDouble(3,Double.parseDouble(priceField.getText()));
            ps.setInt(4,Integer.parseInt(quantityField.getText()));
            ps.setInt(5,supplierMap.get(supplierBox.getSelectedItem().toString()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,"Product Added!");
            loadProducts();
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void updateProduct(){
        int row = table.getSelectedRow();
        if(row>=0){
            int id = (int) model.getValueAt(row,0);
            try(Connection con = DB.getConnection()){
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE product SET name=?, category=?, price=?, quantity=?, supplier_id=? WHERE product_id=?"
                );
                ps.setString(1,nameField.getText());
                ps.setString(2,categoryField.getText());
                ps.setDouble(3,Double.parseDouble(priceField.getText()));
                ps.setInt(4,Integer.parseInt(quantityField.getText()));
                ps.setInt(5,supplierMap.get(supplierBox.getSelectedItem().toString()));
                ps.setInt(6,id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this,"Product Updated!");
                loadProducts();
            } catch(Exception e){ e.printStackTrace(); }
        }
    }

    private void deleteProduct(){
        int row = table.getSelectedRow();
        if(row>=0){
            int id = (int) model.getValueAt(row,0);
            try(Connection con = DB.getConnection()){
                PreparedStatement ps = con.prepareStatement("DELETE FROM product WHERE product_id=?");
                ps.setInt(1,id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this,"Product Deleted!");
                loadProducts();
            } catch(Exception e){ e.printStackTrace(); }
        }
    }

    // --- Sell Product functionality ---
    private void sellProduct(){
        int row = table.getSelectedRow();
        if(row < 0){
            JOptionPane.showMessageDialog(this,"Select a product to sell!");
            return;
        }
        int id = (int) model.getValueAt(row,0);
        int availableQty = (int) model.getValueAt(row,4);
        String soldStr = JOptionPane.showInputDialog("Enter quantity sold:");
        if(soldStr == null || soldStr.isEmpty()) return;
        int soldQty = Integer.parseInt(soldStr);
        if(soldQty > availableQty){
            JOptionPane.showMessageDialog(this,"Not enough stock!");
            return;
        }
        try(Connection con = DB.getConnection()){
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE product SET quantity=quantity-?, sold=sold+? WHERE product_id=?"
            );
            ps.setInt(1,soldQty);
            ps.setInt(2,soldQty);
            ps.setInt(3,id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,"Sale recorded!");
            loadProducts();
        } catch(Exception e){ e.printStackTrace(); }
    }

    public static void main(String[] args){
        new ProductUI();
    }
}
