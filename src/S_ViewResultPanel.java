import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class S_ViewResultPanel extends JPanel {
    private JComboBox<String> cbExams;
    private JTable table;
    private DefaultTableModel tableModel;
    private User currentUser;

    public S_ViewResultPanel(User user) {
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
        cbExams.removeAllItems();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            String sql = "SELECT ExamID, Title FROM Exams";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int examId = rs.getInt("ExamID");
                String title = rs.getString("Title");
                cbExams.addItem(examId + " - " + title);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadResults() {
        tableModel.setRowCount(0);
        String selected = (String) cbExams.getSelectedItem();
        if (selected == null) return;
        int examId = Integer.parseInt(selected.split(" - ")[0]);
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            String sql = "SELECT u.FullName, u.StudentID as MSSV, r.CorrectCount, r.TotalCount, r.DurationInSeconds " +
                         "FROM ExamResults r " +
                         "JOIN Users u ON r.StudentID = u.UserID " +
                         "WHERE r.ExamID = ? " +
                         "ORDER BY r.CorrectCount DESC, r.DurationInSeconds ASC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, examId);
            ResultSet rs = pstmt.executeQuery();
            
            int stt = 1;
            while (rs.next()) {
                String fullName = rs.getString("FullName");
                String mssv = rs.getString("MSSV");
                int correct = rs.getInt("CorrectCount");
                int total = rs.getInt("TotalCount");
                int dur = rs.getInt("DurationInSeconds");
                
                tableModel.addRow(new Object[]{
                    stt++, fullName, mssv, correct + " / " + total, dur
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadData() {
        loadExamsIntoCombo();
    }
}
