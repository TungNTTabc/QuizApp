import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class S_DoQuestionPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel settingsPanel, quizPanel;
    private JComboBox<String> cbSubject;
    private JTextField txtCount;
    private JTextField txtTime;
    private User currentUser;
    
    private JPanel questionsListPanel;
    private Timer timer;
    private int secondsRemaining;
    private int totalSeconds;
    private JLabel lblTimer;
    
    // Lưu đáp án đúng của từng câu
    private List<String> listCorrectAnswers = new ArrayList<>();
    // Lưu các Group button để chấm điểm
    private List<ButtonGroup> listBtnGroups = new ArrayList<>();

    public S_DoQuestionPanel(User user) {
        this.currentUser = user;
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setOpaque(false);

        initSettingsPanel();
        initQuizPanel();

        add(settingsPanel, "Settings");
        add(quizPanel, "Quiz");
    }

    public void refreshSubjects() {
        cbSubject.removeAllItems();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT Subject FROM Questions WHERE QuestionID NOT IN (SELECT QuestionID FROM ExamQuestions)");
            while (rs.next()) {
                cbSubject.addItem(rs.getString("Subject"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initSettingsPanel() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setOpaque(false);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel lblTitle = new JLabel("Luyện Tập Câu Hỏi");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 40));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsPanel.add(lblTitle);
        settingsPanel.add(Box.createVerticalStrut(40));

        cbSubject = new JComboBox<>();
        cbSubject.setBorder(BorderFactory.createTitledBorder("Môn học:"));
        cbSubject.setMaximumSize(new Dimension(500, 60));
        cbSubject.setFont(new Font("Arial", Font.PLAIN, 18));
        settingsPanel.add(cbSubject);
        settingsPanel.add(Box.createVerticalStrut(20));

        txtCount = new JTextField();
        txtCount.setBorder(BorderFactory.createTitledBorder("Số lượng câu hỏi muốn làm:"));
        txtCount.setMaximumSize(new Dimension(500, 60));
        txtCount.setFont(new Font("Arial", Font.PLAIN, 18));
        settingsPanel.add(txtCount);
        settingsPanel.add(Box.createVerticalStrut(20));

        txtTime = new JTextField();
        txtTime.setBorder(BorderFactory.createTitledBorder("Thời gian làm bài (phút):"));
        txtTime.setMaximumSize(new Dimension(500, 60));
        txtTime.setFont(new Font("Arial", Font.PLAIN, 18));
        settingsPanel.add(txtTime);
        settingsPanel.add(Box.createVerticalStrut(40));

        JButton btnStart = new JButton("Bắt Đầu");
        btnStart.setFont(new Font("Arial", Font.BOLD, 20));
        btnStart.setBackground(new Color(95, 225, 235));
        btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStart.addActionListener(e -> startQuiz());
        settingsPanel.add(btnStart);
    }

    private void initQuizPanel() {
        quizPanel = new JPanel(new BorderLayout());
        quizPanel.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        lblTimer = new JLabel("Thời gian: 00:00");
        lblTimer.setFont(new Font("Arial", Font.BOLD, 18));
        lblTimer.setForeground(Color.RED);
        topBar.add(lblTimer, BorderLayout.EAST);
        quizPanel.add(topBar, BorderLayout.NORTH);

        questionsListPanel = new JPanel();
        questionsListPanel.setLayout(new BoxLayout(questionsListPanel, BoxLayout.Y_AXIS));
        questionsListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(questionsListPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        quizPanel.add(scroll, BorderLayout.CENTER);

        JButton btnSubmit = new JButton("Nộp Bài");
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 18));
        btnSubmit.setBackground(new Color(95, 225, 235));
        btnSubmit.addActionListener(e -> submitQuiz());
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnSubmit);
        quizPanel.add(bottom, BorderLayout.SOUTH);

        timer = new Timer(1000, e -> {
            secondsRemaining--;
            if (secondsRemaining <= 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Hết thời gian! Tự động nộp bài.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                submitQuiz();
                return;
            }
            int m = secondsRemaining / 60;
            int s = secondsRemaining % 60;
            lblTimer.setText(String.format("Thời gian còn lại: %02d:%02d", m, s));
        });
    }

    private void startQuiz() {
        if (cbSubject.getSelectedItem() == null) return;
        String subject = (String) cbSubject.getSelectedItem();
        int count = 0;
        int time = 0;
        try {
            count = Integer.parseInt(txtCount.getText().trim());
            if (count <= 0) throw new Exception();
            time = Integer.parseInt(txtTime.getText().trim());
            if (time <= 0) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số lượng câu hỏi hoặc thời gian không hợp lệ!");
            return;
        }

        totalSeconds = time * 60;

        questionsListPanel.removeAll();
        listCorrectAnswers.clear();
        listBtnGroups.clear();

        try (Connection conn = DBConnection.getConnection()) {
            // Lấy ngẫu nhiên N câu hỏi Độc lập (Không nằm trong Exam)
            String sql = "SELECT TOP (?) * FROM Questions WHERE Subject = ? AND QuestionID NOT IN (SELECT QuestionID FROM ExamQuestions) ORDER BY NEWID()";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, count);
            ps.setString(2, subject);
            ResultSet rs = ps.executeQuery();

            int qIndex = 1;
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                JPanel qPanel = new JPanel(new BorderLayout(5, 5));
                qPanel.setBackground(Color.WHITE);
                qPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                    BorderFactory.createTitledBorder("Câu hỏi " + qIndex)
                ));

                JTextArea txtQ = new JTextArea(rs.getString("Content"));
                txtQ.setEditable(false);
                txtQ.setLineWrap(true);
                txtQ.setWrapStyleWord(true);
                txtQ.setFont(new Font("Arial", Font.BOLD, 16));
                qPanel.add(txtQ, BorderLayout.NORTH);

                // Lấy 4 đáp án và đảo vị trí
                List<AnswerItem> answers = new ArrayList<>();
                answers.add(new AnswerItem("A", rs.getString("AnswerA")));
                answers.add(new AnswerItem("B", rs.getString("AnswerB")));
                answers.add(new AnswerItem("C", rs.getString("AnswerC")));
                answers.add(new AnswerItem("D", rs.getString("AnswerD")));
                
                String correctOrig = rs.getString("CorrectAnswer");
                String correctText = "";
                for (AnswerItem a : answers) {
                    if (a.originalKey.equals(correctOrig)) {
                        correctText = a.text;
                        break;
                    }
                }
                listCorrectAnswers.add(correctText); // Lưu text đáp án đúng để so sánh sau khi đảo

                Collections.shuffle(answers);

                JPanel ansPanel = new JPanel(new GridLayout(4, 1, 2, 2));
                ansPanel.setOpaque(false);
                ButtonGroup bg = new ButtonGroup();
                for (int j = 0; j < 4; j++) {
                    JRadioButton rb = new JRadioButton(answers.get(j).text);
                    rb.setFont(new Font("Arial", Font.PLAIN, 15));
                    rb.setOpaque(false);
                    bg.add(rb);
                    ansPanel.add(rb);
                }
                listBtnGroups.add(bg);

                qPanel.add(ansPanel, BorderLayout.CENTER);
                questionsListPanel.add(qPanel);
                questionsListPanel.add(Box.createVerticalStrut(15));
                qIndex++;
            }

            if (!hasData) {
                JOptionPane.showMessageDialog(this, "Không đủ câu hỏi cho môn học này!");
                return;
            }

            if (qIndex - 1 < count) {
                JOptionPane.showMessageDialog(this, "Chỉ tìm thấy " + (qIndex - 1) + " câu hỏi cho môn này. Đang tạo bài với số lượng tối đa...");
            }

            questionsListPanel.revalidate();
            questionsListPanel.repaint();
            
            secondsRemaining = time * 60;
            int m = secondsRemaining / 60;
            int s = secondsRemaining % 60;
            lblTimer.setText(String.format("Thời gian còn lại: %02d:%02d", m, s));
            timer.start();
            cardLayout.show(this, "Quiz");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void submitQuiz() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn nộp bài?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        timer.stop();
        int correctCount = 0;
        int totalCount = listCorrectAnswers.size();

        for (int i = 0; i < totalCount; i++) {
            ButtonGroup bg = listBtnGroups.get(i);
            String correctAns = listCorrectAnswers.get(i);
            String selectedAns = null;

            java.util.Enumeration<AbstractButton> elements = bg.getElements();
            while (elements.hasMoreElements()) {
                AbstractButton button = elements.nextElement();
                if (button.isSelected()) {
                    selectedAns = button.getText();
                    break;
                }
            }

            if (selectedAns != null && selectedAns.equals(correctAns)) {
                correctCount++;
            }
        }

        // Lưu vào CSDL PracticeHistory
        try (Connection conn = DBConnection.getConnection()) {
            int timeTaken = totalSeconds - secondsRemaining;
            String sql = "INSERT INTO PracticeHistory (StudentID, Subject, CorrectCount, TotalCount, DurationInSeconds) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentUser.getUserId());
            ps.setString(2, (String) cbSubject.getSelectedItem());
            ps.setInt(3, correctCount);
            ps.setInt(4, totalCount);
            ps.setInt(5, timeTaken);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Nộp bài thành công!\nKết quả: " + correctCount + " / " + totalCount + "\nThời gian: " + timeTaken + " giây.", "Kết Quả", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(this, "Settings");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu lịch sử: " + ex.getMessage());
        }
    }

    private class AnswerItem {
        String originalKey;
        String text;
        public AnswerItem(String k, String t) { originalKey = k; text = t; }
    }
}
