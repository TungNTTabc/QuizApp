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

        String[] cols = {"ID", "Môn học", "Ngày làm", "Điểm số", "Thời gian (giây)"};
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
        
        // Ẩn cột ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnViewDetails = new JButton("Xem chi tiết");
        btnViewDetails.setFont(new Font("Arial", Font.BOLD, 16));
        btnViewDetails.setBackground(new Color(95, 225, 235));
        btnViewDetails.setFocusPainted(false);
        btnViewDetails.addActionListener(e -> viewDetails());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnViewDetails);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một kết quả luyện tập để xem chi tiết!");
            return;
        }
        int resultId = (int) tableModel.getValueAt(row, 0);
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame) {
            new AttemptDetailDialog((JFrame) win, resultId).setVisible(true);
        }
    }

    public void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            String sql = "SELECT r.*, s.SubjectName FROM QuizResults r JOIN Subjects s ON r.SubjectID = s.SubjectID WHERE r.StudentID = ? AND r.ResultType = 'PRACTICE' ORDER BY r.DateTaken DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            while (rs.next()) {
                int id = rs.getInt("ResultID");
                String subject = rs.getString("SubjectName");
                String date = sdf.format(rs.getTimestamp("DateTaken"));
                int correct = rs.getInt("CorrectCount");
                int total = rs.getInt("TotalCount");
                int dur = rs.getInt("DurationInSeconds");
                
                tableModel.addRow(new Object[]{
                    id, subject, date, correct + " / " + total, dur
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
