import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class S_ViewHistoryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private User currentUser;

    public S_ViewHistoryPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Lịch Sử Luyện Tập (Làm Câu Hỏi)");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Môn học", "Ngày làm", "Điểm số", "Thời gian (giây)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            String sql = "SELECT * FROM PracticeHistory WHERE StudentID = ? ORDER BY DateTaken DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            while (rs.next()) {
                String subject = rs.getString("Subject");
                String date = sdf.format(rs.getTimestamp("DateTaken"));
                int correct = rs.getInt("CorrectCount");
                int total = rs.getInt("TotalCount");
                int dur = rs.getInt("DurationInSeconds");
                
                tableModel.addRow(new Object[]{
                    subject, date, correct + " / " + total, dur
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
