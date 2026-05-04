import javax.swing.*;
import java.awt.*;

public class WelcomeFrame extends JFrame {
    public WelcomeFrame() {
        setTitle("Welcome");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Nền màu xanh cyan sáng
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(95, 225, 235));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalStrut(50));

        // Tiêu đề
        JLabel lblTitle = new JLabel("Welcome");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 45));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);

        mainPanel.add(Box.createVerticalStrut(50));

        // Nút Đăng nhập
        JButton btnLogin = createCustomButton("Đăng nhập");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        mainPanel.add(btnLogin);

        mainPanel.add(Box.createVerticalStrut(20));

        // Nút Đăng ký
        JButton btnRegister = createCustomButton("Đăng kí");
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        mainPanel.add(btnRegister);

        add(mainPanel);
    }

    // Hàm tạo nút trắng bo góc giả lập
    private JButton createCustomButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 20));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(10, 50, 10, 50)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(250, 50));
        return btn;
    }
}
