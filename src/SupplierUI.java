import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SupplierUI extends JFrame {

    JTextField nameField, contactField, emailField;
    JTable table;
    DefaultTableModel model;

    public SupplierUI() {
        setTitle("Supplier Management");
        setSize(700, 450);
        setLocationRelativeTo(null);

        // Input Panel
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Supplier Details"));

        panel.add(new JLabel("Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Contact:"));
        contactField = new JTextField();
        panel.add(contactField);

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        JButton addButton = new JButton("Add Supplier");
        JButton updateButton = new JButton("Update Supplier");
        JButton deleteButton = new JButton("Delete Supplier");

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);

        addButton.addActionListener(e -> addSupplier());
        updateButton.addActionListener(e -> updateSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());

        add(panel, BorderLayout.NORTH);

        // Table for displaying suppliers
        table = new JTable();
        model = new DefaultTableModel(new String[]{"ID", "Name", "Contact", "Email"}, 0);
        table.setModel(model);
        loadSuppliers();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Populate fields when selecting a row
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if(row >= 0) {
                    nameField.setText(model.getValueAt(row, 1).toString());
                    contactField.setText(model.getValueAt(row, 2).toString());
                    emailField.setText(model.getValueAt(row, 3).toString());
                }
            }
        });

        setVisible(true);
    }

    // Load suppliers from database
    private void loadSuppliers() {
        model.setRowCount(0); // Clear table
        try (Connection con = DB.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM supplier");
            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("email")
                });
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Add new supplier
    private void addSupplier() {
        try(Connection con = DB.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO supplier(name, contact, email) VALUES(?,?,?)"
            );
            ps.setString(1, nameField.getText());
            ps.setString(2, contactField.getText());
            ps.setString(3, emailField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier Added!");
            loadSuppliers();
            clearFields();
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding supplier!");
        }
    }

    // Update selected supplier
    private void updateSupplier() {
        int row = table.getSelectedRow();
        if(row >= 0) {
            int id = (int) model.getValueAt(row, 0);
            try(Connection con = DB.getConnection()) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE supplier SET name=?, contact=?, email=? WHERE supplier_id=?"
                );
                ps.setString(1, nameField.getText());
                ps.setString(2, contactField.getText());
                ps.setString(3, emailField.getText());
                ps.setInt(4, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Supplier Updated!");
                loadSuppliers();
                clearFields();
            } catch(Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating supplier!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a supplier to update.");
        }
    }

    // Delete selected supplier
    private void deleteSupplier() {
        int row = table.getSelectedRow();
        if(row >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this supplier?",
                    "Delete Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if(confirm == JOptionPane.YES_OPTION) {
                int id = (int) model.getValueAt(row, 0);
                try(Connection con = DB.getConnection()) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM supplier WHERE supplier_id=?");
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Supplier Deleted!");
                    loadSuppliers();
                    clearFields();
                } catch(Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting supplier!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a supplier to delete.");
        }
    }

    // Clear input fields
    private void clearFields() {
        nameField.setText("");
        contactField.setText("");
        emailField.setText("");
    }

    // Run as standalone
    public static void main(String[] args) {
        new SupplierUI();
    }
}
