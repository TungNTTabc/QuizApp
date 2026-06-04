import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class A_ManageStudentsPanel extends JPanel {
    private JPanel listPanel;
    private JTextField txtSearch;

    public A_ManageStudentsPanel() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Thanh tìm kiếm
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 18));
        txtSearch.setBorder(BorderFactory.createTitledBorder("Tìm kiếm học sinh (Tên, Tài khoản, Mã số):"));
        JButton btnSearch = new JButton("Tìm");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 14));
        btnSearch.addActionListener(e -> loadStudents(txtSearch.getText().trim()));
        
        topPanel.add(txtSearch, BorderLayout.CENTER);
        topPanel.add(btnSearch, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Danh sách Học sinh
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Tải dữ liệu ban đầu
        loadStudents("");
    }

    public void loadStudents(String keyword) {
        listPanel.removeAll();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            String sql = "SELECT * FROM Users WHERE Role = 'HS' AND (FullName LIKE ? OR Username LIKE ? OR StudentID LIKE ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            pstmt.setString(3, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("UserID");
                String username = rs.getString("Username");
                String fullName = rs.getString("FullName");
                String mssv = rs.getString("StudentID");
                String className = rs.getString("ClassName");
                
                JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
                itemPanel.setBackground(Color.WHITE);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
                itemPanel.setMaximumSize(new Dimension(2000, 90));

                JPanel textInfoPanel = new JPanel();
                textInfoPanel.setLayout(new BoxLayout(textInfoPanel, BoxLayout.Y_AXIS));
                textInfoPanel.setOpaque(false);

                JLabel lblName = new JLabel("Học sinh: " + fullName + " (" + username + ")");
                lblName.setFont(new Font("Arial", Font.BOLD, 16));
                
                JLabel lblDesc = new JLabel("Mã số: " + mssv + "  -  Lớp: " + className);
                lblDesc.setFont(new Font("Arial", Font.ITALIC, 14));
                lblDesc.setForeground(Color.GRAY);

                textInfoPanel.add(lblName);
                textInfoPanel.add(Box.createVerticalStrut(5));
                textInfoPanel.add(lblDesc);

                itemPanel.add(textInfoPanel, BorderLayout.CENTER);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                btnPanel.setOpaque(false);
                
                JButton btnView = new JButton("Xem");
                btnView.setBackground(new Color(95, 225, 235));
                btnView.addActionListener(e -> viewStudent(id));
                
                JButton btnDelete = new JButton("Xóa");
                btnDelete.setBackground(new Color(255, 100, 100));
                btnDelete.addActionListener(e -> deleteStudent(id, fullName));

                btnPanel.add(btnView);
                btnPanel.add(btnDelete);
                itemPanel.add(btnPanel, BorderLayout.EAST);

                listPanel.add(itemPanel);
                listPanel.add(Box.createVerticalStrut(10));
            }
            listPanel.revalidate();
            listPanel.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void viewStudent(int id) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thông Tin Chi Tiết Học Sinh", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(11, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] labels = {
            "Tài khoản:", "Họ và Tên:", "Ngày sinh:", "Giới tính:", 
            "Mã số:", "Lớp:", "Môn chính:", "SĐT:", "Email:", "Địa chỉ:"
        };
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            fields[i] = new JTextField();
            fields[i].setEditable(false);
            panel.add(fields[i]);
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE UserID = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                fields[0].setText(rs.getString("Username"));
                fields[1].setText(rs.getString("FullName"));
                
                java.sql.Date dob = rs.getDate("DOB");
                if (dob != null) {
                    fields[2].setText(new SimpleDateFormat("dd/MM/yyyy").format(dob));
                }
                
                fields[3].setText(rs.getString("Gender"));
                fields[4].setText(rs.getString("StudentID"));
                fields[5].setText(rs.getString("ClassName"));
                fields[6].setText(rs.getString("MainSubject"));
                fields[7].setText(rs.getString("Phone"));
                fields[8].setText(rs.getString("Email"));
                fields[9].setText(rs.getString("Address"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        panel.add(new JLabel());
        panel.add(btnClose);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteStudent(int id, String name) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "CẢNH BÁO: Bạn có chắc chắn muốn xóa học sinh \"" + name + "\"?\n" +
            "Hành động này sẽ xóa VĨNH VIỄN tài khoản và toàn bộ Lịch sử làm bài/Ghi chú của học sinh này!", 
            "Xác nhận xóa tài khoản", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 1. Xóa ghi chú học sinh
                    String delNotes = "DELETE FROM StudentNotes WHERE StudentID = ?";
                    try (PreparedStatement ps = conn.prepareStatement(delNotes)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    // 2. Xóa lịch sử tự luyện tập
                    String delHist = "DELETE FROM PracticeHistory WHERE StudentID = ?";
                    try (PreparedStatement ps = conn.prepareStatement(delHist)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    // 3. Xóa kết quả thi
                    String delResults = "DELETE FROM ExamResults WHERE StudentID = ?";
                    try (PreparedStatement ps = conn.prepareStatement(delResults)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    // 4. Xóa tài khoản học sinh
                    String delUser = "DELETE FROM Users WHERE UserID = ?";
                    try (PreparedStatement ps = conn.prepareStatement(delUser)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Đã xóa học sinh thành công!");
                    loadStudents(txtSearch.getText().trim());
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa học sinh: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
