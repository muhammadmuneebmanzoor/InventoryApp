import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginUI extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;

    public LoginUI() {
        setTitle("Inventory System Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(Color.LIGHT_GRAY);

        JLabel title = new JLabel("Sign In", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        add(panel, BorderLayout.CENTER);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(34, 139, 34));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> authenticate());

        // Forgot Password Button
        JButton forgotButton = new JButton("Forgot Password?");
        forgotButton.setBorder(null);
        forgotButton.setForeground(Color.BLUE);
        forgotButton.setBackground(Color.LIGHT_GRAY);
        forgotButton.addActionListener(e -> new ForgotPasswordUI());

        JPanel bottomPanel = new JPanel(new GridLayout(2,1));
        bottomPanel.setBackground(Color.LIGHT_GRAY);
        bottomPanel.add(loginButton);
        bottomPanel.add(forgotButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void authenticate() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if(username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password!");
            return;
        }

        try(Connection con = DB.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM users WHERE username=? AND password=?"
            );
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                dispose();
                new InventoryApp();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!");
            }

        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database!");
        }
    }

    public static void main(String[] args) {
        new LoginUI();
    }
}
