import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ForgotPasswordUI extends JFrame {

    private JTextField usernameField;
    private JTextField secretKeyField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ForgotPasswordUI() {
        setTitle("Reset Password");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 350));
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(new Color(245, 245, 245));
        add(mainPanel, BorderLayout.CENTER);

        // Title
        JLabel title = new JLabel("Reset Password", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(34, 49, 63));
        mainPanel.add(title, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(usernameField, gbc);

        // Secret Key
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel keyLabel = new JLabel("Secret Key:");
        keyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(keyLabel, gbc);

        gbc.gridx = 1;
        secretKeyField = new JTextField();
        secretKeyField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(secretKeyField, gbc);

        // New Password
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel newPassLabel = new JLabel("New Password:");
        newPassLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(newPassLabel, gbc);

        gbc.gridx = 1;
        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(newPasswordField, gbc);

        // Confirm Password
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        confirmPassLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(confirmPassLabel, gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        formPanel.add(confirmPasswordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Reset Button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton resetBtn = new JButton("Reset Password");
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resetBtn.setBackground(new Color(52, 152, 219));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> resetPassword());

        // Hover effect
        resetBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                resetBtn.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                resetBtn.setBackground(new Color(52, 152, 219));
            }
        });

        buttonPanel.add(resetBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void resetPassword() {
        String username = usernameField.getText().trim();
        String secretKey = secretKeyField.getText().trim();
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if(username.isEmpty() || secretKey.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if(!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }

        try(Connection con = DB.getConnection()) {

            PreparedStatement check = con.prepareStatement(
                    "SELECT * FROM users WHERE username=? AND secret_key=?"
            );
            check.setString(1, username);
            check.setString(2, secretKey);

            ResultSet rs = check.executeQuery();

            if(!rs.next()) {
                JOptionPane.showMessageDialog(this, "Invalid username or secret key!");
                return;
            }

            PreparedStatement update = con.prepareStatement(
                    "UPDATE users SET password=? WHERE username=?"
            );
            update.setString(1, newPass);
            update.setString(2, username);
            update.executeUpdate();

            JOptionPane.showMessageDialog(this, "Password Updated Successfully!");
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error resetting password!");
        }
    }
}
