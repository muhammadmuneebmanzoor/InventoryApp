import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;

public class ProductUI extends JFrame {

    private JTextField nameField, categoryField, priceField, quantityField, searchField;
    private JComboBox<String> supplierBox;
    private JTable table;
    private DefaultTableModel model;
    private HashMap<String, Integer> supplierMap = new HashMap<>();

    public ProductUI() {
        setTitle("Product Management");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 550));
        setLayout(new BorderLayout(10, 10));

        // --- Top panel for product input fields ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(245, 245, 245));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 0, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Labels and fields
        String[] labels = {"Name:", "Category:", "Price:", "Quantity:", "Supplier:"};
        JTextField[] textFields = new JTextField[4];
        for(int i=0;i<4;i++){
            gbc.gridx=0; gbc.gridy=i;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            inputPanel.add(lbl, gbc);

            gbc.gridx=1;
            textFields[i] = new JTextField();
            textFields[i].setFont(new Font("Segoe UI", Font.PLAIN, 16));
            inputPanel.add(textFields[i], gbc);
        }
        nameField=textFields[0]; categoryField=textFields[1]; priceField=textFields[2]; quantityField=textFields[3];

        // Supplier ComboBox
        gbc.gridx=0; gbc.gridy=4;
        JLabel supplierLabel = new JLabel("Supplier:");
        supplierLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputPanel.add(supplierLabel, gbc);

        gbc.gridx=1;
        supplierBox = new JComboBox<>();
        supplierBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputPanel.add(supplierBox, gbc);
        loadSuppliersIntoComboBox();

        add(inputPanel, BorderLayout.NORTH);

        // --- Center panel for buttons + search + table ---
        JPanel centerPanel = new JPanel(new BorderLayout(10,10));
        centerPanel.setBackground(new Color(245, 245, 245));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton addButton = createStyledButton("Add Product");
        JButton updateButton = createStyledButton("Update Product");
        JButton deleteButton = createStyledButton("Delete Product");
        JButton sellButton = createStyledButton("Sell Product");

        JButton[] buttons = {addButton, updateButton, deleteButton, sellButton};
        for(JButton btn : buttons) buttonPanel.add(btn);

        centerPanel.add(buttonPanel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(245, 245, 245));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Table
        table = new JTable();
        model = new DefaultTableModel(new String[]{
                "ID","Name","Category","Price","Quantity","Supplier","Sold"},0);
        table.setModel(model);
        loadProducts();
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(245, 245, 245));
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- Action listeners ---
        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        sellButton.addActionListener(e -> sellProduct());

        // Search functionality
        searchField.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e){ searchProducts(searchField.getText()); }
        });

        // Populate fields when selecting a row
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                int row = table.getSelectedRow();
                if(row>=0){
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

    // --- Utility Methods ---

    private JButton createStyledButton(String text){
        JButton btn = new JButton(text);
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(150,40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt){ btn.setBackground(new Color(41,128,185)); }
            public void mouseExited(java.awt.event.MouseEvent evt){ btn.setBackground(new Color(52,152,219)); }
        });
        return btn;
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
                        rs.getInt("sold")
                });
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void searchProducts(String keyword){
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
        SwingUtilities.invokeLater(ProductUI::new);
    }
}
