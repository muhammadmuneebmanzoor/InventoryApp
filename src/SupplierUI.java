import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.Pattern;

public class SupplierUI extends JFrame {

    private JTextField nameField, contactField, emailField, searchField;
    private JTable table;
    private DefaultTableModel model;

    public SupplierUI() {
        setTitle("Supplier Management");
        setSize(750, 550);
        setMinimumSize(new Dimension(700,500));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Top panel for supplier input fields ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(new Color(245, 245, 245));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 0, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        String[] labels = {"Name:", "Contact:", "Email:"};
        JTextField[] fields = new JTextField[3];

        for(int i=0;i<3;i++){
            gbc.gridx=0; gbc.gridy=i;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            inputPanel.add(lbl, gbc);

            gbc.gridx=1;
            fields[i] = new JTextField();
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 16));
            inputPanel.add(fields[i], gbc);
        }

        nameField = fields[0];
        contactField = fields[1];
        emailField = fields[2];

        add(inputPanel, BorderLayout.NORTH);

        // ⭐ NAME VALIDATION (Only alphabets + spaces)
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                if (!Character.isLetter(c) && c != ' ') {
                    e.consume();
                }
            }
        });

        // ⭐ CONTACT VALIDATION (Only digits + max 11)
        contactField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                if (!Character.isDigit(c)) {
                    e.consume();
                }

                if (contactField.getText().length() >= 11) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        // --- Center panel for buttons + search + table ---
        JPanel centerPanel = new JPanel(new BorderLayout(10,10));
        centerPanel.setBackground(new Color(245, 245, 245));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton addButton = createStyledButton("Add Supplier");
        JButton updateButton = createStyledButton("Update Supplier");
        JButton deleteButton = createStyledButton("Delete Supplier");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        centerPanel.add(buttonPanel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(245, 245, 245));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        table = new JTable();
        model = new DefaultTableModel(new String[]{"ID","Name","Contact","Email"},0);
        table.setModel(model);
        loadSuppliers();

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(245, 245, 245));
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- Action listeners ---
        addButton.addActionListener(e -> addSupplier());
        updateButton.addActionListener(e -> updateSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                searchSuppliers(searchField.getText());
            }
        });

        table.addMouseListener(new MouseAdapter() {
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

    private JButton createStyledButton(String text){
        JButton btn = new JButton(text);
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(150,40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt){ btn.setBackground(new Color(41,128,185)); }
            public void mouseExited(MouseEvent evt){ btn.setBackground(new Color(52,152,219)); }
        });
        return btn;
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
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void searchSuppliers(String keyword){
        model.setRowCount(0);
        try(Connection con = DB.getConnection()){
            String sql = "SELECT * FROM supplier WHERE name LIKE ? OR contact LIKE ? OR email LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1,"%"+keyword+"%");
            ps.setString(2,"%"+keyword+"%");
            ps.setString(3,"%"+keyword+"%");
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt("supplier_id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("email")
                });
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    private void addSupplier(){
        if(!validateFields()) return;

        try(Connection con = DB.getConnection()){
            PreparedStatement ps = con.prepareStatement("INSERT INTO supplier(name,contact,email) VALUES(?,?,?)");
            ps.setString(1,nameField.getText());
            ps.setString(2,contactField.getText());
            ps.setString(3,emailField.getText());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"Supplier Added!");
            loadSuppliers(); clearFields();

        } catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error adding supplier!");
        }
    }

    private void updateSupplier(){
        int row = table.getSelectedRow();
        if(row < 0){
            JOptionPane.showMessageDialog(this,"Select a supplier to update.");
            return;
        }

        if(!validateFields()) return;

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

        } catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error updating supplier!");
        }
    }

    private void deleteSupplier(){
        int row = table.getSelectedRow();
        if(row < 0){
            JOptionPane.showMessageDialog(this,"Select a supplier to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,"Are you sure you want to delete this supplier?","Delete Confirmation",JOptionPane.YES_NO_OPTION);

        if(confirm == JOptionPane.YES_OPTION){
            int id = (int) model.getValueAt(row,0);

            try(Connection con = DB.getConnection()){
                PreparedStatement ps = con.prepareStatement("DELETE FROM supplier WHERE supplier_id=?");
                ps.setInt(1,id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this,"Supplier Deleted!");
                loadSuppliers(); clearFields();

            } catch(Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,"Error deleting supplier!");
            }
        }
    }

    // ⭐ FIELD VALIDATION (Name + Contact + Email)
    private boolean validateFields() {

        if(nameField.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(this,"Name cannot be empty!");
            return false;
        }
        if(contactField.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(this,"Contact cannot be empty!");
            return false;
        }
        if(contactField.getText().length() < 10){
            JOptionPane.showMessageDialog(this,"Contact number must be at least 10 digits!");
            return false;
        }

        // EMAIL FORMAT CHECK
        String email = emailField.getText().trim();
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z.-]+\\.[a-zA-Z]{2,6}$";

        if(!Pattern.matches(emailPattern, email)){
            JOptionPane.showMessageDialog(this,"Invalid Email Format! Example: abc@gmail.com");
            return false;
        }

        return true;
    }

    private void clearFields(){
        nameField.setText("");
        contactField.setText("");
        emailField.setText("");
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(SupplierUI::new);
    }
}
