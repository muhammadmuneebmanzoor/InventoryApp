import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;

public class InventoryApp extends JFrame {

    InventoryApp() {
        setTitle("Inventory Management System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(new Color(245, 245, 245));
        add(mainPanel, BorderLayout.CENTER);

        // Title
        JLabel title = new JLabel("Inventory Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(34, 49, 63));
        mainPanel.add(title, BorderLayout.NORTH);

        // Button panel (vertical)
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));

        JButton productButton = new JButton("Add Product");
        JButton supplierButton = new JButton("Manage Suppliers");
        JButton reportButton = new JButton("Generate Report");
        JButton exitButton = new JButton("Exit");

        JButton[] buttons = {productButton, supplierButton, reportButton, exitButton};
        for (JButton btn : buttons) {
            btn.setBackground(new Color(52, 152, 219)); // consistent blue theme
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Hover effect
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(41, 128, 185));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(52, 152, 219));
                }
            });
        }

        buttonPanel.add(productButton);
        buttonPanel.add(supplierButton);
        buttonPanel.add(reportButton);
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Action listeners
        productButton.addActionListener(e -> new ProductUI());
        supplierButton.addActionListener(e -> new SupplierUI());
        reportButton.addActionListener(e -> generateReport());
        exitButton.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void generateReport() {
        try (Connection con = DB.getConnection();
             FileWriter fw = new FileWriter("inventory_report.txt")) {

            fw.write("=== PRODUCT REPORT ===\n\n");
            Statement st1 = con.createStatement();
            ResultSet rs1 = st1.executeQuery("SELECT * FROM product");

            while (rs1.next()) {
                fw.write("Product ID: " + rs1.getInt("product_id") + "\n");
                fw.write("Name: " + rs1.getString("name") + "\n");
                fw.write("Category: " + rs1.getString("category") + "\n");
                fw.write("Price: " + rs1.getDouble("price") + "\n");
                fw.write("Quantity: " + rs1.getInt("quantity") + "\n");
                fw.write("-----------------------------\n");
            }

            fw.write("\n\n=== SUPPLIER REPORT ===\n\n");
            Statement st2 = con.createStatement();
            ResultSet rs2 = st2.executeQuery("SELECT * FROM supplier");

            while (rs2.next()) {
                fw.write("Supplier ID: " + rs2.getInt("supplier_id") + "\n");
                fw.write("Name: " + rs2.getString("name") + "\n");
                fw.write("Contact: " + rs2.getString("contact") + "\n");
                fw.write("Email: " + rs2.getString("email") + "\n");
                fw.write("-----------------------------\n");
            }

            JOptionPane.showMessageDialog(this, "Report Generated Successfully!\nFile: inventory_report.txt");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating report!");
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch(Exception e) {}
        SwingUtilities.invokeLater(LoginUI::new);
    }
}
