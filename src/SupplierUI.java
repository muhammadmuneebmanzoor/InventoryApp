import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SupplierUI extends JFrame {

    JTextField nameField, contactField, emailField, searchField;
    JTable table;
    DefaultTableModel model;

    public SupplierUI() {
        setTitle("Supplier Management");
        setSize(700, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.LIGHT_GRAY);
        setLayout(new BorderLayout(10, 10));

        // --- Top panel for supplier input fields ---
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBackground(Color.LIGHT_GRAY);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 0, 50));
        inputPanel.add(new JLabel("Name:")); nameField = new JTextField(); inputPanel.add(nameField);
        inputPanel.add(new JLabel("Contact:")); contactField = new JTextField(); inputPanel.add(contactField);
        inputPanel.add(new JLabel("Email:")); emailField = new JTextField(); inputPanel.add(emailField);

        add(inputPanel, BorderLayout.NORTH);

        // --- Center panel for buttons + search + table ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.LIGHT_GRAY);

        // Button panel (top)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        JButton addButton = new JButton("Add Supplier");
        JButton updateButton = new JButton("Update Supplier");
        JButton deleteButton = new JButton("Delete Supplier");
        JButton[] buttons = {addButton, updateButton, deleteButton};
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
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        centerPanel.add(searchPanel, BorderLayout.CENTER);

        // Table
        table = new JTable();
        model = new DefaultTableModel(new String[]{"ID","Name","Contact","Email"},0);
        table.setModel(model);
        loadSuppliers();
        centerPanel.add(new JScrollPane(table), BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // --- Action listeners ---
        addButton.addActionListener(e -> addSupplier());
        updateButton.addActionListener(e -> updateSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());

        // Search functionality
        searchField.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e){
                String keyword = searchField.getText().toLowerCase();
                model.setRowCount(0);
                try(Connection con = DB.getConnection()){
                    String sql = "SELECT * FROM supplier WHERE name LIKE ? OR contact LIKE ? OR email LIKE ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, "%"+keyword+"%");
                    ps.setString(2, "%"+keyword+"%");
                    ps.setString(3, "%"+keyword+"%");
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()){
                        model.addRow(new Object[]{
                                rs.getInt("supplier_id"),
                                rs.getString("name"),
                                rs.getString("contact"),
                                rs.getString("email")
                        });
                    }
                }catch(Exception ex){ ex.printStackTrace(); }
            }
        });

        // Populate fields when selecting row
        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                int row = table.getSelectedRow();
                if(row >= 0){
                    nameField.setText(model.getValueAt(row,1).toString());
                    contactField.setText(model.getValueAt(row,2).toString());
                    emailField.setText(model.getValueAt(row,3).toString());
                }
            }
        });

        setVisible(true);
    }

    private void loadSuppliers(){
        model.setRowCount(0);
        try(Connection con = DB.getConnection()){
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM supplier");
            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("email")
                });
            }
        }catch(Exception e){ e.printStackTrace(); }
    }

    private void addSupplier(){
        try(Connection con = DB.getConnection()){
            PreparedStatement ps = con.prepareStatement("INSERT INTO supplier(name,contact,email) VALUES(?,?,?)");
            ps.setString(1,nameField.getText());
            ps.setString(2,contactField.getText());
            ps.setString(3,emailField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,"Supplier Added!");
            loadSuppliers(); clearFields();
        }catch(Exception e){ e.printStackTrace(); JOptionPane.showMessageDialog(this,"Error adding supplier!"); }
    }

    private void updateSupplier(){
        int row = table.getSelectedRow();
        if(row >= 0){
            int id = (int) model.getValueAt(row,0);
            try(Connection con = DB.getConnection()){
                PreparedStatement ps = con.prepareStatement("UPDATE supplier SET name=?, contact=?, email=? WHERE supplier_id=?");
                ps.setString(1,nameField.getText());
                ps.setString(2,contactField.getText());
                ps.setString(3,emailField.getText());
                ps.setInt(4,id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this,"Supplier Updated!");
                loadSuppliers(); clearFields();
            }catch(Exception e){ e.printStackTrace(); JOptionPane.showMessageDialog(this,"Error updating supplier!"); }
        } else JOptionPane.showMessageDialog(this,"Select a supplier to update.");
    }

    private void deleteSupplier(){
        int row = table.getSelectedRow();
        if(row >= 0){
            int confirm = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete this supplier?","Delete Confirmation",JOptionPane.YES_NO_OPTION);
            if(confirm==JOptionPane.YES_OPTION){
                int id = (int) model.getValueAt(row,0);
                try(Connection con = DB.getConnection()){
                    PreparedStatement ps = con.prepareStatement("DELETE FROM supplier WHERE supplier_id=?");
                    ps.setInt(1,id);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this,"Supplier Deleted!");
                    loadSuppliers(); clearFields();
                }catch(Exception e){ e.printStackTrace(); JOptionPane.showMessageDialog(this,"Error deleting supplier!"); }
            }
        } else JOptionPane.showMessageDialog(this,"Select a supplier to delete.");
    }

    private void clearFields(){ nameField.setText(""); contactField.setText(""); emailField.setText(""); }

    public static void main(String[] args){ new SupplierUI(); }
}
