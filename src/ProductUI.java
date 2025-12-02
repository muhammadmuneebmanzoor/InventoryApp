import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;

public class ProductUI extends JFrame {

    JTextField nameField, categoryField, priceField, quantityField;
    JComboBox<String> supplierBox;
    JTable table;
    DefaultTableModel model;

    HashMap<String, Integer> supplierMap = new HashMap<>();

    public ProductUI() {
        setTitle("Product Management");
        setSize(800, 500);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Product Details"));

        panel.add(new JLabel("Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        panel.add(categoryField);

        panel.add(new JLabel("Price:"));
        priceField = new JTextField();
        panel.add(priceField);

        panel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        panel.add(quantityField);

        panel.add(new JLabel("Supplier:"));
        supplierBox = new JComboBox<>();
        loadSuppliersIntoComboBox();
        panel.add(supplierBox);

        JButton addButton = new JButton("Add Product");
        JButton updateButton = new JButton("Update Product");
        JButton deleteButton = new JButton("Delete Product");

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);

        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());

        add(panel, BorderLayout.NORTH);

        table = new JTable();
        model = new DefaultTableModel(new String[]{
                "ID", "Name", "Category", "Price", "Quantity", "Supplier"
        }, 0);
        table.setModel(model);
        loadProducts();
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if(row >= 0) {
                    nameField.setText(model.getValueAt(row, 1).toString());
                    categoryField.setText(model.getValueAt(row, 2).toString());
                    priceField.setText(model.getValueAt(row, 3).toString());
                    quantityField.setText(model.getValueAt(row, 4).toString());
                    supplierBox.setSelectedItem(model.getValueAt(row, 5).toString());
                }
            }
        });

        setVisible(true);
    }

    private void loadSuppliersIntoComboBox() {
        try (Connection con = DB.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT supplier_id, name FROM supplier");
            supplierBox.removeAllItems();
            supplierMap.clear();
            while(rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("supplier_id");
                supplierMap.put(name, id);
                supplierBox.addItem(name);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        model.setRowCount(0);
        try (Connection con = DB.getConnection()) {
            String sql = "SELECT p.*, s.name AS supplier_name FROM product p LEFT JOIN supplier s ON p.supplier_id = s.supplier_id";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("supplier_name")
                });
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void addProduct() {
        try(Connection con = DB.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO product(name, category, price, quantity, supplier_id) VALUES(?,?,?,?,?)"
            );

            ps.setString(1, nameField.getText());
            ps.setString(2, categoryField.getText());
            ps.setDouble(3, Double.parseDouble(priceField.getText()));
            ps.setInt(4, Integer.parseInt(quantityField.getText()));
            ps.setInt(5, supplierMap.get(supplierBox.getSelectedItem().toString()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product Added!");
            loadProducts();
            clearFields();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProduct() {
        int row = table.getSelectedRow();
        if(row >= 0) {
            int id = (int) model.getValueAt(row, 0);

            try(Connection con = DB.getConnection()) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE product SET name=?, category=?, price=?, quantity=?, supplier_id=? WHERE product_id=?"
                );

                ps.setString(1, nameField.getText());
                ps.setString(2, categoryField.getText());
                ps.setDouble(3, Double.parseDouble(priceField.getText()));
                ps.setInt(4, Integer.parseInt(quantityField.getText()));
                ps.setInt(5, supplierMap.get(supplierBox.getSelectedItem().toString()));
                ps.setInt(6, id);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product Updated!");
                loadProducts();
                clearFields();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if(row >= 0) {
            int id = (int) model.getValueAt(row, 0);
            try(Connection con = DB.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM product WHERE product_id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product Deleted!");
                loadProducts();
                clearFields();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearFields() {
        nameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        quantityField.setText("");
    }

    public static void main(String[] args) {
        new ProductUI();
    }
}
