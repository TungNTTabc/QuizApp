import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class T_ViewQuestionPanel extends JPanel {
    private JPanel listPanel;
    private JTextField txtSearch;
    private CardLayout cardLayout;
    private JPanel mainCardPanel;

    public T_ViewQuestionPanel(CardLayout cardLayout, JPanel mainCardPanel) {
        this.cardLayout = cardLayout;
        this.mainCardPanel = mainCardPanel;
        
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Thanh tìm kiếm
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 18));
        txtSearch.setBorder(BorderFactory.createTitledBorder("Tìm kiếm câu hỏi:"));
        JButton btnSearch = new JButton("Tìm");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 14));
        btnSearch.addActionListener(e -> loadQuestions(txtSearch.getText().trim()));
        
        topPanel.add(txtSearch, BorderLayout.CENTER);
        topPanel.add(btnSearch, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Danh sách câu hỏi
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Tải dữ liệu ban đầu
        loadQuestions("");
    }

    public void loadQuestions(String keyword) {
        listPanel.removeAll();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            // Chỉ lấy các câu hỏi ĐỘC LẬP (Không nằm trong bài thi nào) 
            // VÀ tìm kiếm theo cả Nội dung hoặc Môn học
            String sql = "SELECT * FROM Questions WHERE (Content LIKE ? OR Subject LIKE ?) AND QuestionID NOT IN (SELECT QuestionID FROM ExamQuestions)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("QuestionID");
                String content = rs.getString("Content");
                String subject = rs.getString("Subject");
                
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

                JLabel lblContent = new JLabel("Câu hỏi: " + content);
                lblContent.setFont(new Font("Arial", Font.BOLD, 16));
                
                JLabel lblSubject = new JLabel("Môn học: " + subject);
                lblSubject.setFont(new Font("Arial", Font.ITALIC, 14));
                lblSubject.setForeground(Color.GRAY);

                textInfoPanel.add(lblContent);
                textInfoPanel.add(Box.createVerticalStrut(5));
                textInfoPanel.add(lblSubject);

                itemPanel.add(textInfoPanel, BorderLayout.CENTER);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                btnPanel.setOpaque(false);
                
                JButton btnEdit = new JButton("Sửa");
                btnEdit.setBackground(new Color(95, 225, 235));
                btnEdit.addActionListener(e -> editQuestion(id));
                
                JButton btnDelete = new JButton("Xóa");
                btnDelete.setBackground(new Color(255, 100, 100));
                btnDelete.addActionListener(e -> deleteQuestion(id));

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

    private void deleteQuestion(int id) {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa câu hỏi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Questions WHERE QuestionID = ?");
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadQuestions(txtSearch.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());
            }
        }
    }

    private void editQuestion(int id) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa Câu Hỏi", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JTextField txtSubject = new JTextField();
        txtSubject.setBorder(BorderFactory.createTitledBorder("Môn học:"));
        
        JTextArea txtQuestion = new JTextArea(3, 40);
        JScrollPane scrollQuestion = new JScrollPane(txtQuestion);
        scrollQuestion.setBorder(BorderFactory.createTitledBorder("Câu hỏi:"));
        
        center.add(txtSubject);
        center.add(Box.createVerticalStrut(10));
        center.add(scrollQuestion);
        center.add(Box.createVerticalStrut(10));

        JTextField[] txtAnswers = new JTextField[4];
        JRadioButton[] rdbCorrect = new JRadioButton[4];
        ButtonGroup bg = new ButtonGroup();
        
        for (int i = 0; i < 4; i++) {
            JPanel row = new JPanel(new BorderLayout(5, 0));
            rdbCorrect[i] = new JRadioButton();
            bg.add(rdbCorrect[i]);
            txtAnswers[i] = new JTextField();
            txtAnswers[i].setBorder(BorderFactory.createTitledBorder("Đáp án " + (char)('A' + i)));
            
            row.add(rdbCorrect[i], BorderLayout.WEST);
            row.add(txtAnswers[i], BorderLayout.CENTER);
            center.add(row);
        }
        panel.add(center, BorderLayout.CENTER);

        // Load current data
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Questions WHERE QuestionID = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtSubject.setText(rs.getString("Subject"));
                txtQuestion.setText(rs.getString("Content"));
                txtAnswers[0].setText(rs.getString("AnswerA"));
                txtAnswers[1].setText(rs.getString("AnswerB"));
                txtAnswers[2].setText(rs.getString("AnswerC"));
                txtAnswers[3].setText(rs.getString("AnswerD"));
                String correct = rs.getString("CorrectAnswer");
                if (correct.equals("A")) rdbCorrect[0].setSelected(true);
                else if (correct.equals("B")) rdbCorrect[1].setSelected(true);
                else if (correct.equals("C")) rdbCorrect[2].setSelected(true);
                else if (correct.equals("D")) rdbCorrect[3].setSelected(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JButton btnUpdate = new JButton("Cập nhật");
        btnUpdate.setBackground(new Color(95, 225, 235));
        btnUpdate.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE Questions SET Subject=?, Content=?, AnswerA=?, AnswerB=?, AnswerC=?, AnswerD=?, CorrectAnswer=? WHERE QuestionID=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtSubject.getText().trim());
                ps.setString(2, txtQuestion.getText().trim());
                ps.setString(3, txtAnswers[0].getText().trim());
                ps.setString(4, txtAnswers[1].getText().trim());
                ps.setString(5, txtAnswers[2].getText().trim());
                ps.setString(6, txtAnswers[3].getText().trim());
                
                char correctAns = 'A';
                if (rdbCorrect[1].isSelected()) correctAns = 'B';
                else if (rdbCorrect[2].isSelected()) correctAns = 'C';
                else if (rdbCorrect[3].isSelected()) correctAns = 'D';
                ps.setString(7, String.valueOf(correctAns));
                ps.setInt(8, id);
                
                ps.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Sửa thành công!");
                dialog.dispose();
                loadQuestions(txtSearch.getText().trim());
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
