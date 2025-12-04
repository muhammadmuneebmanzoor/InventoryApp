import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ForgotPasswordUI extends JFrame {

    JTextField usernameField;
    JTextField secretKeyField;
    JPasswordField newPasswordField;
    JPasswordField confirmPasswordField;

    public ForgotPasswordUI() {

        setTitle("Reset Password");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.LIGHT_GRAY);

        JLabel title = new JLabel("Reset Password", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Fields
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Secret Key:"));
        secretKeyField = new JTextField();
        panel.add(secretKeyField);

        panel.add(new JLabel("New Password:"));
        newPasswordField = new JPasswordField();
        panel.add(newPasswordField);

        panel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        add(panel, BorderLayout.CENTER);

        JButton resetBtn = new JButton("Reset Password");
        resetBtn.setBackground(new Color(34, 139, 34));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFont(new Font("Arial", Font.BOLD, 14));
        resetBtn.addActionListener(e -> resetPassword());

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.LIGHT_GRAY);
        bottom.add(resetBtn);

        add(bottom, BorderLayout.SOUTH);

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

            // Check if username + secret key match
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

            // Update password
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
