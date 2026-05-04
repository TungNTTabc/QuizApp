import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class T_ViewResultPanel extends JPanel {
    private JComboBox<String> cbExams;
    private JTable table;
    private DefaultTableModel tableModel;
    private User currentUser;

    public T_ViewResultPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Thanh chọn bài thi
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Chọn bài thi để xem: "));
        
        cbExams = new JComboBox<>();
        cbExams.setPreferredSize(new Dimension(400, 30));
        loadExamsIntoCombo();
        
        JButton btnView = new JButton("Xem Kết Quả");
        btnView.setBackground(new Color(95, 225, 235));
        btnView.addActionListener(e -> loadResults());

        topPanel.add(cbExams);
        topPanel.add(btnView);
        add(topPanel, BorderLayout.NORTH);

        // Bảng kết quả
        tableModel = new DefaultTableModel(new String[]{"STT", "Họ và Tên", "MSSV", "Số câu đúng", "Thời gian (giây)"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadExamsIntoCombo() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            String sql = "SELECT ExamID, Title FROM Exams WHERE TeacherID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentUser.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbExams.addItem(rs.getInt("ExamID") + " - " + rs.getString("Title"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadResults() {
        if (cbExams.getSelectedItem() == null) return;
        String selected = (String) cbExams.getSelectedItem();
        int examId = Integer.parseInt(selected.split(" - ")[0]);

        tableModel.setRowCount(0); // Xóa dữ liệu cũ

        try (Connection conn = DBConnection.getConnection()) {
            // Sắp xếp: Điểm cao -> thấp; thời gian ngắn -> dài
            String sql = "SELECT u.FullName, u.StudentID, r.CorrectCount, r.TotalCount, r.DurationInSeconds " +
                         "FROM ExamResults r " +
                         "JOIN Users u ON r.StudentID = u.UserID " +
                         "WHERE r.ExamID = ? " +
                         "ORDER BY r.CorrectCount DESC, r.DurationInSeconds ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();

            int stt = 1;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    stt++,
                    rs.getString("FullName"),
                    rs.getString("StudentID"),
                    rs.getInt("CorrectCount") + "/" + rs.getInt("TotalCount"),
                    rs.getInt("DurationInSeconds")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }
}
