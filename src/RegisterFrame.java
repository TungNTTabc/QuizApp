import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterFrame extends JFrame {
    private JComboBox<String> roleCombo, genderCombo;
    private JTextField txtUsername, txtFullName, txtDob, txtMSSV, txtClass, txtSubject, txtPhone, txtEmail, txtAddress;
    private JPasswordField txtPassword;

    public RegisterFrame() {
        setTitle("Đăng Ký");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(95, 225, 235));

        // Tiêu đề
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 50, 20));
        topPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Đăng Ký");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 45));
        topPanel.add(lblTitle);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Chứa 2 cột (Cân bằng 5 item mỗi cột)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));

        // === CỘT TRÁI ===
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        leftPanel.add(Box.createVerticalStrut(20));
        txtUsername = createField("Tài khoản:");
        leftPanel.add(txtUsername); leftPanel.add(Box.createVerticalStrut(15));

        txtPassword = new JPasswordField();
        txtPassword.setBorder(BorderFactory.createTitledBorder("Mật Khẩu:"));
        styleField(txtPassword);
        leftPanel.add(txtPassword); leftPanel.add(Box.createVerticalStrut(15));

        txtFullName = createField("Họ và Tên:");
        leftPanel.add(txtFullName); leftPanel.add(Box.createVerticalStrut(15));

        txtClass = createField("Lớp:");
        leftPanel.add(txtClass); leftPanel.add(Box.createVerticalStrut(15));

        txtSubject = createField("Môn chính:"); // Chuyển môn chính sang cột trái cho cân bằng
        leftPanel.add(txtSubject);

        // === CỘT PHẢI ===
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(Box.createVerticalStrut(20));

        // Hàng 1 của cột phải (Vai trò + Giới tính)
        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(800, 50)); // Cố định chiều cao, không bị kéo dãn
        
        roleCombo = new JComboBox<>(new String[]{"Học sinh", "Giáo viên"});
        roleCombo.setBorder(BorderFactory.createTitledBorder("Vai trò:"));
        styleField(roleCombo);
        row1.add(roleCombo);

        genderCombo = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        genderCombo.setBorder(BorderFactory.createTitledBorder("Giới tính:"));
        styleField(genderCombo);
        row1.add(genderCombo);
        
        rightPanel.add(row1); rightPanel.add(Box.createVerticalStrut(15));

        // Hàng 2 (MSSV + Ngày sinh)
        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(800, 50)); // Cố định chiều cao
        
        txtMSSV = createField("MSSV (Giáo viên ghi 0):");
        row2.add(txtMSSV);
        txtDob = createField("dd/mm/yyyy (ngày sinh) (VD:1/1/2001):");
        row2.add(txtDob);
        
        rightPanel.add(row2); rightPanel.add(Box.createVerticalStrut(15));

        // Các hàng dưới
        txtPhone = createField("SĐT:");
        rightPanel.add(txtPhone); rightPanel.add(Box.createVerticalStrut(15));

        txtEmail = createField("Email:");
        rightPanel.add(txtEmail); rightPanel.add(Box.createVerticalStrut(15));

        txtAddress = createField("Địa chỉ:");
        rightPanel.add(txtAddress);

        // Ghép 2 cột
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Nút Hoàn thành và Quay lại
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JButton btnBack = new JButton("Quay lại");
        btnBack.setFont(new Font("Arial", Font.PLAIN, 18));
        btnBack.setBackground(new Color(240, 240, 240));
        btnBack.setFocusPainted(false);
        btnBack.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new WelcomeFrame().setVisible(true);
            dispose();
        });

        JButton btnRegister = new JButton("Hoàn Thành");
        btnRegister.setFont(new Font("Arial", Font.PLAIN, 20));
        btnRegister.setBackground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> handleRegister());

        bottomPanel.add(btnBack);
        bottomPanel.add(btnRegister);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JTextField createField(String title) {
        JTextField field = new JTextField();
        field.setBorder(BorderFactory.createTitledBorder(title));
        styleField(field);
        return field;
    }

    private void styleField(JComponent comp) {
        comp.setBackground(Color.WHITE);
        comp.setFont(new Font("Arial", Font.PLAIN, 15));
        comp.setMaximumSize(new Dimension(800, 50));
    }

    private void handleRegister() {
        String roleStr = (String) roleCombo.getSelectedItem();
        String roleCode = roleStr.equals("Giáo viên") ? "GV" : "HS";
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String fullName = txtFullName.getText().trim();
        String dobStr = txtDob.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String mssv = txtMSSV.getText().trim();
        String className = txtClass.getText().trim();
        String subject = txtSubject.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || dobStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tài khoản, mật khẩu, tên và ngày sinh không được để trống!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.sql.Date sqlDob = null;
        try {
            java.util.Date parsedDate = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(dobStr);
            sqlDob = new java.sql.Date(parsedDate.getTime());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng dd/mm/yyyy (Ví dụ: 1/1/2001)!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Cảnh báo giáo viên phải nhập MSSV là 0
        if (roleCode.equals("GV") && !mssv.equals("0")) {
            JOptionPane.showMessageDialog(this, "Lỗi: Mã số sinh viên của giáo viên PHẢI là 0! Vui lòng nhập 0 cho MSSV.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Không thể kết nối đến Database! Vui lòng kiểm tra lại cấu hình DBConnection.", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Kiểm tra trùng lặp Tài khoản, Họ Tên, MSSV
            String checkSql = "SELECT Username, FullName, StudentID FROM Users WHERE Username = ? OR FullName = ? OR (StudentID = ? AND StudentID != '0')";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setString(1, username);
            psCheck.setString(2, fullName);
            psCheck.setString(3, mssv);
            java.sql.ResultSet rsCheck = psCheck.executeQuery();
            
            while (rsCheck.next()) {
                String existUser = rsCheck.getString("Username");
                String existName = rsCheck.getString("FullName");
                String existMssv = rsCheck.getString("StudentID");
                
                if (username.equalsIgnoreCase(existUser)) {
                    JOptionPane.showMessageDialog(this, "Tài khoản (Username) này đã tồn tại! Vui lòng chọn tên đăng nhập khác.", "Lỗi trùng lặp", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (fullName.equalsIgnoreCase(existName)) {
                    JOptionPane.showMessageDialog(this, "Họ và Tên này đã có người sử dụng! (Không được trùng tên giữa Học sinh và Giáo viên). Vui lòng thêm chữ đệm để phân biệt.", "Lỗi trùng lặp", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (mssv.equalsIgnoreCase(existMssv) && !mssv.equals("0")) {
                    JOptionPane.showMessageDialog(this, "MSSV này đã tồn tại! Mỗi học sinh phải có một MSSV riêng biệt.", "Lỗi trùng lặp", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String sql = "INSERT INTO Users (Role, Username, Password, FullName, DOB, Gender, StudentID, ClassName, MainSubject, Phone, Email, Address) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, roleCode);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, fullName);
            pstmt.setDate(5, sqlDob);
            pstmt.setString(6, gender);
            pstmt.setString(7, roleCode.equals("GV") ? "0" : mssv);
            pstmt.setString(8, className);
            pstmt.setString(9, subject);
            pstmt.setString(10, phone);
            pstmt.setString(11, email);
            pstmt.setString(12, address);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.");
            new LoginFrame().setVisible(true);
            dispose();
            
        } catch (SQLException ex) {
            // Bắt lỗi trùng tài khoản (Unique Key Violation)
            if (ex.getErrorCode() == 2627 || ex.getMessage().contains("UNIQUE KEY")) {
                JOptionPane.showMessageDialog(this, "Tài khoản (Username) này đã tồn tại! Vui lòng chọn tên đăng nhập khác.", "Lỗi trùng lặp", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu (Hãy kiểm tra ngày sinh có đúng định dạng YYYY-MM-DD không). Chi tiết: " + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi không xác định: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
