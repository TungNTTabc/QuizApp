import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class A_ManageExamsPanel extends JPanel {
    private JPanel listPanel;
    private JTextField txtSearch;

    public A_ManageExamsPanel() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Thanh tìm kiếm
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 18));
        txtSearch.setBorder(BorderFactory.createTitledBorder("Tìm kiếm bài thi (Tiêu đề, Môn học):"));
        JButton btnSearch = new JButton("Tìm");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 14));
        btnSearch.addActionListener(e -> loadExams(txtSearch.getText().trim()));
        
        topPanel.add(txtSearch, BorderLayout.CENTER);
        topPanel.add(btnSearch, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Danh sách Bài thi
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Tải dữ liệu ban đầu
        loadExams("");
    }

    public void loadExams(String keyword) {
        listPanel.removeAll();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            String sql = "SELECT e.*, u.FullName as Creator FROM Exams e JOIN Users u ON e.TeacherID = u.UserID WHERE e.Title LIKE ? OR e.Subject LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("ExamID");
                String title = rs.getString("Title");
                String subject = rs.getString("Subject");
                int count = rs.getInt("QuestionCount");
                int dur = rs.getInt("Duration");
                String creator = rs.getString("Creator");
                
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

                JLabel lblTitle = new JLabel("Bài thi: " + title);
                lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
                
                JLabel lblDesc = new JLabel("Môn học: " + subject + "  -  " + count + " câu hỏi  -  " + dur + " phút  -  Tạo bởi: " + creator);
                lblDesc.setFont(new Font("Arial", Font.ITALIC, 14));
                lblDesc.setForeground(Color.GRAY);

                textInfoPanel.add(lblTitle);
                textInfoPanel.add(Box.createVerticalStrut(5));
                textInfoPanel.add(lblDesc);

                itemPanel.add(textInfoPanel, BorderLayout.CENTER);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                btnPanel.setOpaque(false);
                
                JButton btnView = new JButton("Xem");
                btnView.setBackground(new Color(95, 225, 235));
                btnView.addActionListener(e -> viewExam(id));
                
                JButton btnDelete = new JButton("Xóa");
                btnDelete.setBackground(new Color(255, 100, 100));
                btnDelete.addActionListener(e -> deleteExam(id));

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

    private void viewExam(int id) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi Tiết Bài Thi", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topInfo = new JPanel(new GridLayout(1, 3, 10, 10));
        JTextField txtTitle = new JTextField();
        txtTitle.setBorder(BorderFactory.createTitledBorder("Tiêu đề:"));
        txtTitle.setEditable(false);
        JTextField txtSubject = new JTextField();
        txtSubject.setBorder(BorderFactory.createTitledBorder("Môn học:"));
        txtSubject.setEditable(false);
        JTextField txtDur = new JTextField();
        txtDur.setBorder(BorderFactory.createTitledBorder("Thời gian (phút):"));
        txtDur.setEditable(false);
        topInfo.add(txtTitle);
        topInfo.add(txtSubject);
        topInfo.add(txtDur);
        panel.add(topInfo, BorderLayout.NORTH);

        JPanel centerList = new JPanel();
        centerList.setLayout(new BoxLayout(centerList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(centerList);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        try (Connection conn = DBConnection.getConnection()) {
            // Tải thông tin đề thi
            PreparedStatement psEx = conn.prepareStatement("SELECT * FROM Exams WHERE ExamID = ?");
            psEx.setInt(1, id);
            ResultSet rsEx = psEx.executeQuery();
            if (rsEx.next()) {
                txtTitle.setText(rsEx.getString("Title"));
                txtSubject.setText(rsEx.getString("Subject"));
                txtDur.setText(String.valueOf(rsEx.getInt("Duration")));
            }

            // Tải danh sách câu hỏi
            String sqlQ = "SELECT q.* FROM Questions q JOIN ExamQuestions eq ON q.QuestionID = eq.QuestionID WHERE eq.ExamID = ?";
            PreparedStatement psQ = conn.prepareStatement(sqlQ);
            psQ.setInt(1, id);
            ResultSet rsQ = psQ.executeQuery();
            
            int qIndex = 1;
            while (rsQ.next()) {
                JPanel qPanel = new JPanel(new BorderLayout(5, 5));
                qPanel.setBorder(BorderFactory.createTitledBorder("Câu hỏi " + qIndex++));
                
                JTextArea txtQ = new JTextArea(rsQ.getString("Content"), 3, 40);
                txtQ.setEditable(false);
                qPanel.add(new JScrollPane(txtQ), BorderLayout.NORTH);

                JPanel ansPanel = new JPanel(new GridLayout(4, 1, 2, 2));
                JTextField[] arrAns = new JTextField[4];
                JRadioButton[] arrRdb = new JRadioButton[4];
                ButtonGroup bg = new ButtonGroup();
                String correct = rsQ.getString("CorrectAnswer");

                for (int j = 0; j < 4; j++) {
                    JPanel row = new JPanel(new BorderLayout());
                    arrRdb[j] = new JRadioButton();
                    arrRdb[j].setEnabled(false);
                    bg.add(arrRdb[j]);
                    arrAns[j] = new JTextField();
                    arrAns[j].setEditable(false);
                    arrAns[j].setBorder(BorderFactory.createTitledBorder("Đáp án " + (char)('A' + j)));
                    row.add(arrRdb[j], BorderLayout.WEST);
                    row.add(arrAns[j], BorderLayout.CENTER);
                    ansPanel.add(row);
                }
                
                arrAns[0].setText(rsQ.getString("AnswerA"));
                arrAns[1].setText(rsQ.getString("AnswerB"));
                arrAns[2].setText(rsQ.getString("AnswerC"));
                arrAns[3].setText(rsQ.getString("AnswerD"));
                
                if (correct.equals("A")) arrRdb[0].setSelected(true);
                else if (correct.equals("B")) arrRdb[1].setSelected(true);
                else if (correct.equals("C")) arrRdb[2].setSelected(true);
                else if (correct.equals("D")) arrRdb[3].setSelected(true);

                qPanel.add(ansPanel, BorderLayout.CENTER);
                centerList.add(qPanel);
                centerList.add(Box.createVerticalStrut(10));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnClose);
        panel.add(bottom, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteExam(int id) {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bài thi này? (Các câu hỏi trong bài cũng sẽ bị xóa)", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 1. Xóa kết quả làm bài thi liên quan
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ExamResults WHERE ExamID = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                    
                    // 2. Xóa các câu hỏi nằm trong bài thi
                    String sqlDelQ = "DELETE FROM Questions WHERE QuestionID IN (SELECT QuestionID FROM ExamQuestions WHERE ExamID = ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlDelQ)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                    
                    // 3. Xóa liên kết trong ExamQuestions
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ExamQuestions WHERE ExamID = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                    
                    // 4. Xóa bài thi
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Exams WHERE ExamID = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Xóa bài thi thành công!");
                    loadExams(txtSearch.getText().trim());
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa bài thi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
