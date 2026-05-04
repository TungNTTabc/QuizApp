import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class T_UpdateInfoPanel extends JPanel {
    private JTextField txtUsername, txtFullName, txtDob, txtMSSV, txtClass, txtSubject, txtPhone, txtEmail, txtAddress;
    private JPasswordField txtPassword;
    private JComboBox<String> genderCombo;
    private User currentUser;

    public T_UpdateInfoPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Tiêu đề
        JLabel lblTitle = new JLabel("Thay đổi thông tin");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 45));
        add(lblTitle, BorderLayout.NORTH);

        // Chứa 2 cột (Cân bằng 5 item mỗi cột)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // === CỘT TRÁI ===
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        txtUsername = createField("Tài khoản:");
        leftPanel.add(txtUsername); leftPanel.add(Box.createVerticalStrut(15));

        txtPassword = new JPasswordField();
        txtPassword.setBorder(BorderFactory.createTitledBorder("Mật Khẩu Mới (Hoặc giữ nguyên):"));
        styleField(txtPassword);
        leftPanel.add(txtPassword); leftPanel.add(Box.createVerticalStrut(15));

        txtFullName = createField("Họ và Tên:");
        leftPanel.add(txtFullName); leftPanel.add(Box.createVerticalStrut(15));

        txtClass = createField("Lớp:");
        leftPanel.add(txtClass); leftPanel.add(Box.createVerticalStrut(15));

        txtSubject = createField("Môn chính:");
        leftPanel.add(txtSubject);

        // === CỘT PHẢI ===
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // Hàng 1 của cột phải (Vai trò + Giới tính)
        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(800, 50));
        
        JTextField txtRole = createField("Vai trò:");
        txtRole.setText(user.getRole().equals("GV") ? "Giáo viên" : "Học sinh");
        txtRole.setEditable(false);
        row1.add(txtRole);

        genderCombo = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        genderCombo.setBorder(BorderFactory.createTitledBorder("Giới tính:"));
        styleField(genderCombo);
        row1.add(genderCombo);
        
        rightPanel.add(row1); rightPanel.add(Box.createVerticalStrut(15));

        // Hàng 2 (MSSV + Ngày sinh)
        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(800, 50));
        
        txtMSSV = createField("MSSV:");
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
        add(centerPanel, BorderLayout.CENTER);

        // Nút Hoàn thành và Xóa tài khoản
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        bottomPanel.setOpaque(false);
        
        JButton btnDeleteAcc = new JButton("Xóa Tài Khoản");
        btnDeleteAcc.setFont(new Font("Arial", Font.PLAIN, 18));
        btnDeleteAcc.setBackground(new Color(255, 100, 100));
        btnDeleteAcc.setForeground(Color.BLACK);
        btnDeleteAcc.setFocusPainted(false);
        btnDeleteAcc.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnDeleteAcc.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeleteAcc.addActionListener(e -> deleteAccount());
        
        JButton btnUpdate = new JButton("Hoàn Thành");
        btnUpdate.setFont(new Font("Arial", Font.PLAIN, 20));
        btnUpdate.setBackground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdate.addActionListener(e -> updateInfo());
        
        bottomPanel.add(btnDeleteAcc);
        bottomPanel.add(btnUpdate);
        
        add(bottomPanel, BorderLayout.SOUTH);

        // Load dữ liệu
        loadCurrentData();
    }

    private void loadCurrentData() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE UserID = ?");
            ps.setInt(1, currentUser.getUserId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtUsername.setText(rs.getString("Username"));
                txtPassword.setText(rs.getString("Password"));
                txtFullName.setText(rs.getString("FullName"));
                txtMSSV.setText(rs.getString("StudentID") != null ? rs.getString("StudentID") : "");
                
                // Format lại ngày từ CSDL ra dạng dd/MM/yyyy để hiện thị
                java.sql.Date sqlDate = rs.getDate("DOB");
                if (sqlDate != null) {
                    txtDob.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(sqlDate));
                }
                
                genderCombo.setSelectedItem(rs.getString("Gender"));
                txtClass.setText(rs.getString("ClassName"));
                txtSubject.setText(rs.getString("MainSubject"));
                txtPhone.setText(rs.getString("Phone"));
                txtEmail.setText(rs.getString("Email"));
                txtAddress.setText(rs.getString("Address"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(this, "CẢNH BÁO: Hành động này sẽ xóa VĨNH VIỄN tài khoản của bạn và TOÀN BỘ bài thi/câu hỏi bạn đã tạo! Bạn có chắc chắn không?", "Xác nhận xóa tài khoản", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Xóa PracticeHistory nếu là học sinh (ở đây là GV nhưng cứ để cho an toàn nếu code dùng chung)
                    PreparedStatement psHist = conn.prepareStatement("DELETE FROM PracticeHistory WHERE StudentID = ?");
                    psHist.setInt(1, currentUser.getUserId());
                    psHist.executeUpdate();
                    
                    // Xóa kết quả bài thi của học sinh
                    PreparedStatement psRes = conn.prepareStatement("DELETE FROM ExamResults WHERE StudentID = ?");
                    psRes.setInt(1, currentUser.getUserId());
                    psRes.executeUpdate();

                    // Tìm các Exam do GV này tạo
                    PreparedStatement psGetExams = conn.prepareStatement("SELECT ExamID FROM Exams WHERE TeacherID = ?");
                    psGetExams.setInt(1, currentUser.getUserId());
                    ResultSet rsEx = psGetExams.executeQuery();
                    
                    while (rsEx.next()) {
                        int examId = rsEx.getInt("ExamID");
                        // Xóa PracticeHistory & ExamResults liên kết với bài thi này (do học sinh làm)
                        PreparedStatement p1 = conn.prepareStatement("DELETE FROM PracticeHistory WHERE ExamID = ?");
                        p1.setInt(1, examId); p1.executeUpdate();
                        
                        PreparedStatement p2 = conn.prepareStatement("DELETE FROM ExamResults WHERE ExamID = ?");
                        p2.setInt(1, examId); p2.executeUpdate();
                        
                        // Xóa các câu hỏi trong bài thi
                        PreparedStatement pQ = conn.prepareStatement("DELETE FROM Questions WHERE QuestionID IN (SELECT QuestionID FROM ExamQuestions WHERE ExamID = ?)");
                        pQ.setInt(1, examId);
                        
                        PreparedStatement pLink = conn.prepareStatement("DELETE FROM ExamQuestions WHERE ExamID = ?");
                        pLink.setInt(1, examId);
                        
                        pLink.executeUpdate();
                        pQ.executeUpdate();
                    }
                    
                    // Xóa các bài thi do GV tạo
                    PreparedStatement psDelExams = conn.prepareStatement("DELETE FROM Exams WHERE TeacherID = ?");
                    psDelExams.setInt(1, currentUser.getUserId());
                    psDelExams.executeUpdate();

                    // Cuối cùng xóa User
                    PreparedStatement psDelUser = conn.prepareStatement("DELETE FROM Users WHERE UserID = ?");
                    psDelUser.setInt(1, currentUser.getUserId());
                    psDelUser.executeUpdate();
                    
                    conn.commit();
                    
                    JOptionPane.showMessageDialog(this, "Đã xóa tài khoản thành công!");
                    // Đăng xuất bằng cách tắt Jframe hiện tại và mở Login
                    Window win = SwingUtilities.getWindowAncestor(this);
                    if (win != null) {
                        win.dispose();
                    }
                    new LoginFrame().setVisible(true);
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa tài khoản: " + ex.getMessage());
            }
        }
    }

    private void updateInfo() {
        String dobStr = txtDob.getText().trim();
        java.sql.Date sqlDob = null;
        try {
            java.util.Date parsedDate = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(dobStr);
            sqlDob = new java.sql.Date(parsedDate.getTime());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng dd/mm/yyyy (Ví dụ: 1/1/2001)!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newUsername = txtUsername.getText().trim();
        String newFullName = txtFullName.getText().trim();
        String newMSSV = txtMSSV.getText().trim();
        String oldUsername = currentUser.getUsername();
        String isTeacher = currentUser.getRole().equals("GV") ? "GV" : "HS";
        
        // Cảnh báo giáo viên không được thay đổi MSSV từ 0 sang cái khác
        if (isTeacher.equals("GV") && !newMSSV.equals("0")) {
            JOptionPane.showMessageDialog(this, "Lỗi: Mã số sinh viên của giáo viên PHẢI là 0 và không thể thay đổi!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Kiểm tra trùng lặp Tài khoản, Họ Tên, MSSV
        try (Connection conn = DBConnection.getConnection()) {
            // Kiểm tra Username
            if (!newUsername.equals(oldUsername)) {
                PreparedStatement checkPs = conn.prepareStatement("SELECT COUNT(*) FROM Users WHERE Username = ? AND UserID != ?");
                checkPs.setString(1, newUsername);
                checkPs.setInt(2, currentUser.getUserId());
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Tài khoản này đã tồn tại! Vui lòng chọn tài khoản khác.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Kiểm tra FullName
            PreparedStatement checkName = conn.prepareStatement("SELECT COUNT(*) FROM Users WHERE FullName = ? AND UserID != ?");
            checkName.setString(1, newFullName);
            checkName.setInt(2, currentUser.getUserId());
            ResultSet rsName = checkName.executeQuery();
            if (rsName.next() && rsName.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Họ và Tên này đã có người sử dụng! Vui lòng thêm chữ đệm để phân biệt.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Kiểm tra StudentID (chỉ kiểm tra khi không phải 0)
            if (!newMSSV.equals("0")) {
                PreparedStatement checkMSSV = conn.prepareStatement("SELECT COUNT(*) FROM Users WHERE StudentID = ? AND UserID != ? AND StudentID != '0'");
                checkMSSV.setString(1, newMSSV);
                checkMSSV.setInt(2, currentUser.getUserId());
                ResultSet rsMSSV = checkMSSV.executeQuery();
                if (rsMSSV.next() && rsMSSV.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "MSSV này đã tồn tại! Mỗi học sinh phải có một MSSV riêng biệt.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kiểm tra: " + ex.getMessage());
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE Users SET Username=?, Password=?, FullName=?, StudentID=?, DOB=?, Gender=?, ClassName=?, MainSubject=?, Phone=?, Email=?, Address=? WHERE UserID=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newUsername);
            ps.setString(2, new String(txtPassword.getPassword()));
            ps.setString(3, newFullName);
            ps.setString(4, isTeacher.equals("GV") ? "0" : newMSSV);
            ps.setDate(5, sqlDob);
            ps.setString(6, (String) genderCombo.getSelectedItem());
            ps.setString(7, txtClass.getText());
            ps.setString(8, txtSubject.getText());
            ps.setString(9, txtPhone.getText());
            ps.setString(10, txtEmail.getText());
            ps.setString(11, txtAddress.getText());
            ps.setInt(12, currentUser.getUserId());
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage());
        }
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
}
