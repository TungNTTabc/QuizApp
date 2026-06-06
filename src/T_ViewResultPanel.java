import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class T_ViewResultPanel extends JPanel {
    private JComboBox<String> cbExams;
    private JTable table;
    private DefaultTableModel tableModel;
    private User currentUser;
    private JTextField txtSearch;

    public T_ViewResultPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Thanh chọn bài thi
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(new JLabel("Chọn bài thi: "));
        
        cbExams = new JComboBox<>();
        cbExams.setPreferredSize(new Dimension(300, 30));
        loadExamsIntoCombo();
        
        topPanel.add(cbExams);
        
        topPanel.add(new JLabel(" | Lọc Học sinh/MSSV:"));
        txtSearch = new JTextField(12);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        topPanel.add(txtSearch);
        
        JButton btnView = new JButton("Xem Kết Quả");
        btnView.setBackground(new Color(95, 225, 235));
        btnView.addActionListener(e -> loadResults(txtSearch.getText().trim()));

        topPanel.add(btnView);
        add(topPanel, BorderLayout.NORTH);

        // Bảng kết quả
        tableModel = new DefaultTableModel(new String[]{"ID", "STT", "Họ và Tên", "Mã số", "Số câu đúng", "Thời gian (giây)", "Ngày làm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Ẩn cột ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một kết quả để xem chi tiết!");
            return;
        }
        int resultId = (int) tableModel.getValueAt(row, 0);
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame) {
            new AttemptDetailDialog((JFrame) win, resultId).setVisible(true);
        }
    }

    private void loadExamsIntoCombo() {
        cbExams.removeAllItems();
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

    private void loadResults(String keyword) {
        if (cbExams.getSelectedItem() == null) return;
        String selected = (String) cbExams.getSelectedItem();
        int examId = Integer.parseInt(selected.split(" - ")[0]);

        tableModel.setRowCount(0); // Xóa dữ liệu cũ

        try (Connection conn = DBConnection.getConnection()) {
            // Sắp xếp: Điểm cao -> thấp; thời gian ngắn -> dài
            String sql = "SELECT r.ResultID, u.FullName, u.StudentID, r.CorrectCount, r.TotalCount, r.DurationInSeconds, r.DateTaken " +
                         "FROM QuizResults r " +
                         "JOIN Users u ON r.StudentID = u.UserID " +
                         "WHERE r.ExamID = ? AND r.ResultType = 'EXAM' " +
                         "AND (u.FullName LIKE ? OR u.StudentID LIKE ?) " +
                         "ORDER BY r.CorrectCount DESC, r.DurationInSeconds ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, examId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            int stt = 1;
            while (rs.next()) {
                int dur = rs.getInt("DurationInSeconds");
                
                // Tính thời gian bắt đầu
                long endTime = rs.getTimestamp("DateTaken").getTime();
                long durationMillis = dur * 1000L;
                long startTime = endTime - durationMillis;
                String ngayLam = sdf.format(new java.util.Date(startTime));

                tableModel.addRow(new Object[]{
                    rs.getInt("ResultID"),
                    stt++,
                    rs.getString("FullName"),
                    rs.getString("StudentID"),
                    rs.getInt("CorrectCount") + "/" + rs.getInt("TotalCount"),
                    dur,
                    ngayLam
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    public void loadData() {
        loadExamsIntoCombo();
        tableModel.setRowCount(0);
    }
}
