import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {
    private JComboBox<String> roleCombo;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("Đăng nhập");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Nền cyan sáng
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(95, 225, 235));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalStrut(30));

        // Tiêu đề
        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 40));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);

        mainPanel.add(Box.createVerticalStrut(40));

        // Vai trò
        roleCombo = new JComboBox<>(new String[] { "Học sinh", "Giáo viên" });
        roleCombo.setBorder(BorderFactory.createTitledBorder("Vai trò:"));
        styleComponent(roleCombo);
        mainPanel.add(roleCombo);
        mainPanel.add(Box.createVerticalStrut(20));

        // Tài khoản
        txtUsername = new JTextField();
        txtUsername.setBorder(BorderFactory.createTitledBorder("Tài khoản:"));
        styleComponent(txtUsername);
        mainPanel.add(txtUsername);
        mainPanel.add(Box.createVerticalStrut(20));

        // Mật khẩu
        txtPassword = new JPasswordField();
        txtPassword.setBorder(BorderFactory.createTitledBorder("Mật khẩu:"));
        styleComponent(txtPassword);
        mainPanel.add(txtPassword);
        mainPanel.add(Box.createVerticalStrut(30));

        // Nút Đăng nhập
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Arial", Font.PLAIN, 20));
        btnLogin.setBackground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(300, 50));

        btnLogin.addActionListener(e -> handleLogin());
        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(15));

        // Nút quay lại (Giống nút Đăng nhập nhưng màu khác)
        JButton btnBack = new JButton("Quay lại");
        btnBack.setFont(new Font("Arial", Font.PLAIN, 15));
        btnBack.setBackground(new Color(240, 240, 240));
        btnBack.setFocusPainted(false);
        btnBack.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> {
            new WelcomeFrame().setVisible(true);
            dispose();
        });
        mainPanel.add(btnBack);

        add(mainPanel);
    }

    private void styleComponent(JComponent comp) {
        comp.setMaximumSize(new Dimension(300, 50)); // Tăng chiều cao lên chút để chứa title
        comp.setFont(new Font("Arial", Font.PLAIN, 15));
        comp.setBackground(Color.WHITE);
        comp.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void handleLogin() {
        String roleStr = (String) roleCombo.getSelectedItem();
        String roleCode = roleStr.equals("Giáo viên") ? "GV" : "HS";
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this,
                        "Không thể kết nối Database! (Kiểm tra lại mật khẩu sa hoặc SQL Server)", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "SELECT UserID, FullName FROM Users WHERE Role = ? AND Username = ? AND Password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, roleCode);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(rs.getInt("UserID"), roleCode, username, rs.getString("FullName"));
                if (roleCode.equals("GV")) {
                    new TeacherDashboard(user).setVisible(true);
                } else {
                    new StudentDashboard(user).setVisible(true);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản, mật khẩu hoặc vai trò!", "Thông báo",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + ex.getMessage());
        }
    }
}
