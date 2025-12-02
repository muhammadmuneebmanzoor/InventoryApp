import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;

public class InventoryApp extends JFrame {

    public InventoryApp() {
        setTitle("Inventory Management System");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        JButton productButton = new JButton("Manage Products");
        JButton supplierButton = new JButton("Manage Suppliers");
        JButton reportButton = new JButton("Generate Report");
        JButton exitButton = new JButton("Exit");

        add(productButton);
        add(supplierButton);
        add(reportButton);
        add(exitButton);

        productButton.addActionListener(e -> new ProductUI());
        supplierButton.addActionListener(e -> new SupplierUI());
        reportButton.addActionListener(e -> generateReport());
        exitButton.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    // REPORT GENERATION METHOD
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

            fw.close();
            JOptionPane.showMessageDialog(this, "Report Generated Successfully!\nFile: inventory_report.txt");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating report!");
        }
    }

    public static void main(String[] args) {
        new InventoryApp();
    }
}
