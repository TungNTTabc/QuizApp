import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class T_ViewExamPanel extends JPanel {
    private JPanel listPanel;
    private JTextField txtSearch;
    private User currentUser;

    public T_ViewExamPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Thanh tìm kiếm
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 18));
        txtSearch.setBorder(BorderFactory.createTitledBorder("Tìm kiếm tiêu đề bài thi:"));
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
            // Cho phép tìm kiếm theo Tiêu đề hoặc Môn học
            String sql = "SELECT * FROM Exams WHERE TeacherID = ? AND (Title LIKE ? OR Subject LIKE ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUser.getUserId());
            pstmt.setString(2, "%" + keyword + "%");
            pstmt.setString(3, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("ExamID");
                String title = rs.getString("Title");
                String subject = rs.getString("Subject");
                int count = rs.getInt("QuestionCount");
                int dur = rs.getInt("Duration");
                
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
                
                JLabel lblDesc = new JLabel("Môn học: " + subject + "  -  " + count + " câu hỏi  -  " + dur + " phút");
                lblDesc.setFont(new Font("Arial", Font.ITALIC, 14));
                lblDesc.setForeground(Color.GRAY);

                textInfoPanel.add(lblTitle);
                textInfoPanel.add(Box.createVerticalStrut(5));
                textInfoPanel.add(lblDesc);

                itemPanel.add(textInfoPanel, BorderLayout.CENTER);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                btnPanel.setOpaque(false);
                
                JButton btnEdit = new JButton("Sửa");
                btnEdit.setBackground(new Color(95, 225, 235));
                btnEdit.addActionListener(e -> editExam(id));
                
                JButton btnDelete = new JButton("Xóa");
                btnDelete.setBackground(new Color(255, 100, 100));
                btnDelete.addActionListener(e -> deleteExam(id));

                btnPanel.add(btnEdit);
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

    private void deleteExam(int id) {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bài thi này? (Các câu hỏi trong bài cũng sẽ bị xóa)", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                // Xóa câu hỏi trong bảng Questions thông qua liên kết (Cascading không được bật nên xóa thủ công)
                String sqlDelQ = "DELETE FROM Questions WHERE QuestionID IN (SELECT QuestionID FROM ExamQuestions WHERE ExamID = ?)";
                PreparedStatement psQ = conn.prepareStatement(sqlDelQ);
                psQ.setInt(1, id);
                
                // Xóa liên kết trong ExamQuestions
                String sqlDelLink = "DELETE FROM ExamQuestions WHERE ExamID = ?";
                PreparedStatement psL = conn.prepareStatement(sqlDelLink);
                psL.setInt(1, id);
                
                // Xóa Bài thi
                String sqlDelEx = "DELETE FROM Exams WHERE ExamID = ?";
                PreparedStatement psE = conn.prepareStatement(sqlDelEx);
                psE.setInt(1, id);
                
                // Transaction (tùy chọn nhưng tốt)
                conn.setAutoCommit(false);
                psL.executeUpdate(); // Xóa liên kết trước
                psQ.executeUpdate(); // Xóa câu hỏi
                psE.executeUpdate(); // Xóa bài thi
                conn.commit();
                
                JOptionPane.showMessageDialog(this, "Xóa bài thi thành công!");
                loadExams(txtSearch.getText().trim());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());
            }
        }
    }

    private void editExam(int id) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa Bài Thi", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topInfo = new JPanel(new GridLayout(1, 3, 10, 10));
        JTextField txtTitle = new JTextField();
        txtTitle.setBorder(BorderFactory.createTitledBorder("Tiêu đề:"));
        JTextField txtSubject = new JTextField();
        txtSubject.setBorder(BorderFactory.createTitledBorder("Môn học:"));
        JTextField txtDur = new JTextField();
        txtDur.setBorder(BorderFactory.createTitledBorder("Thời gian (phút):"));
        topInfo.add(txtTitle);
        topInfo.add(txtSubject);
        topInfo.add(txtDur);
        panel.add(topInfo, BorderLayout.NORTH);

        JPanel centerList = new JPanel();
        centerList.setLayout(new BoxLayout(centerList, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(centerList);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        java.util.List<Integer> qIds = new java.util.ArrayList<>();
        java.util.List<JTextArea> listTxtQ = new java.util.ArrayList<>();
        java.util.List<JTextField[]> listTxtAns = new java.util.ArrayList<>();
        java.util.List<JRadioButton[]> listRdb = new java.util.ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            // Load exam info
            PreparedStatement psEx = conn.prepareStatement("SELECT * FROM Exams WHERE ExamID = ?");
            psEx.setInt(1, id);
            ResultSet rsEx = psEx.executeQuery();
            if (rsEx.next()) {
                txtTitle.setText(rsEx.getString("Title"));
                txtSubject.setText(rsEx.getString("Subject"));
                txtDur.setText(String.valueOf(rsEx.getInt("Duration")));
            }

            // Load questions
            String sqlQ = "SELECT q.* FROM Questions q JOIN ExamQuestions eq ON q.QuestionID = eq.QuestionID WHERE eq.ExamID = ?";
            PreparedStatement psQ = conn.prepareStatement(sqlQ);
            psQ.setInt(1, id);
            ResultSet rsQ = psQ.executeQuery();
            
            int qIndex = 1;
            while (rsQ.next()) {
                qIds.add(rsQ.getInt("QuestionID"));
                
                JPanel qPanel = new JPanel(new BorderLayout(5, 5));
                qPanel.setBorder(BorderFactory.createTitledBorder("Câu hỏi " + qIndex++));
                
                JTextArea txtQ = new JTextArea(rsQ.getString("Content"), 3, 40);
                listTxtQ.add(txtQ);
                qPanel.add(new JScrollPane(txtQ), BorderLayout.NORTH);

                JPanel ansPanel = new JPanel(new GridLayout(4, 1, 2, 2));
                JTextField[] arrAns = new JTextField[4];
                JRadioButton[] arrRdb = new JRadioButton[4];
                ButtonGroup bg = new ButtonGroup();
                String correct = rsQ.getString("CorrectAnswer");

                for (int j = 0; j < 4; j++) {
                    JPanel row = new JPanel(new BorderLayout());
                    arrRdb[j] = new JRadioButton();
                    bg.add(arrRdb[j]);
                    arrAns[j] = new JTextField();
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

                listTxtAns.add(arrAns);
                listRdb.add(arrRdb);
                qPanel.add(ansPanel, BorderLayout.CENTER);
                
                centerList.add(qPanel);
                centerList.add(Box.createVerticalStrut(10));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JButton btnUpdate = new JButton("Cập nhật Bài thi");
        btnUpdate.setBackground(new Color(95, 225, 235));
        btnUpdate.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    PreparedStatement psEx = conn.prepareStatement("UPDATE Exams SET Title=?, Subject=?, Duration=? WHERE ExamID=?");
                    psEx.setString(1, txtTitle.getText().trim());
                    psEx.setString(2, txtSubject.getText().trim());
                    psEx.setInt(3, Integer.parseInt(txtDur.getText().trim()));
                    psEx.setInt(4, id);
                    psEx.executeUpdate();

                    PreparedStatement psQ = conn.prepareStatement("UPDATE Questions SET Subject=?, Content=?, AnswerA=?, AnswerB=?, AnswerC=?, AnswerD=?, CorrectAnswer=? WHERE QuestionID=?");
                    for (int i = 0; i < qIds.size(); i++) {
                        psQ.setString(1, txtSubject.getText().trim());
                        psQ.setString(2, listTxtQ.get(i).getText().trim());
                        psQ.setString(3, listTxtAns.get(i)[0].getText().trim());
                        psQ.setString(4, listTxtAns.get(i)[1].getText().trim());
                        psQ.setString(5, listTxtAns.get(i)[2].getText().trim());
                        psQ.setString(6, listTxtAns.get(i)[3].getText().trim());
                        
                        char correctAns = 'A';
                        if (listRdb.get(i)[1].isSelected()) correctAns = 'B';
                        else if (listRdb.get(i)[2].isSelected()) correctAns = 'C';
                        else if (listRdb.get(i)[3].isSelected()) correctAns = 'D';
                        psQ.setString(7, String.valueOf(correctAns));
                        psQ.setInt(8, qIds.get(i));
                        psQ.executeUpdate();
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(dialog, "Cập nhật bài thi thành công!");
                    dialog.dispose();
                    loadExams(txtSearch.getText().trim());
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi cập nhật: " + ex.getMessage());
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnUpdate);
        panel.add(bottom, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
}
