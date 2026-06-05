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
    private JComboBox<SubjectItem> cbSubject;
    private JTextField txtCount;
    private JTextField txtTime;
    private User currentUser;
    
    private JPanel questionsListPanel;
    private Timer timer;
    private int secondsRemaining;
    private int totalSeconds;
    private JLabel lblTimer;
    
    // Lưu các Group button để chấm điểm
    private List<ButtonGroup> listBtnGroups = new ArrayList<>();
    // Lưu ID câu hỏi và đáp án đúng để chấm
    private List<QuestionAttemptData> questionDataList = new ArrayList<>();

    private class SubjectItem {
        int id;
        String name;
        public SubjectItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private class QuestionAttemptData {
        int questionId;
        String correctAnsOrig;
        public QuestionAttemptData(int qId, String orig) {
            questionId = qId; correctAnsOrig = orig;
        }
    }

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
            ResultSet rs = conn.createStatement().executeQuery("SELECT SubjectID, SubjectName FROM Subjects");
            while (rs.next()) {
                cbSubject.addItem(new SubjectItem(rs.getInt("SubjectID"), rs.getString("SubjectName")));
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
        SubjectItem subject = (SubjectItem) cbSubject.getSelectedItem();
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
        questionDataList.clear();
        listBtnGroups.clear();

        try (Connection conn = DBConnection.getConnection()) {
            // Lấy ngẫu nhiên N câu hỏi Độc lập (Không nằm trong Exam)
            String sql = "SELECT TOP (?) * FROM Questions WHERE SubjectID = ? AND QuestionID NOT IN (SELECT QuestionID FROM ExamQuestions) ORDER BY NEWID()";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, count);
            ps.setInt(2, subject.id);
            ResultSet rs = ps.executeQuery();

            int qIndex = 1;
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int qId = rs.getInt("QuestionID");
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
                questionDataList.add(new QuestionAttemptData(qId, correctOrig));

                Collections.shuffle(answers);

                JPanel ansPanel = new JPanel(new GridLayout(4, 1, 2, 2));
                ansPanel.setOpaque(false);
                ButtonGroup bg = new ButtonGroup();
                for (int j = 0; j < 4; j++) {
                    JRadioButton rb = new JRadioButton(answers.get(j).text);
                    rb.setActionCommand(answers.get(j).originalKey); // Lưu lại đáp án gốc A/B/C/D
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
        int totalCount = questionDataList.size();

        // Chấm điểm trước
        for (int i = 0; i < totalCount; i++) {
            ButtonGroup bg = listBtnGroups.get(i);
            String correctAns = questionDataList.get(i).correctAnsOrig;
            String selectedAns = bg.getSelection() != null ? bg.getSelection().getActionCommand() : null;

            if (selectedAns != null && selectedAns.equals(correctAns)) {
                correctCount++;
            }
        }

        // Lưu vào CSDL
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int timeTaken = totalSeconds - secondsRemaining;
                SubjectItem subject = (SubjectItem) cbSubject.getSelectedItem();

                // Lưu bảng QuizResults
                String sqlRes = "INSERT INTO QuizResults (StudentID, ExamID, SubjectID, ResultType, CorrectCount, TotalCount, DurationInSeconds) VALUES (?, NULL, ?, 'PRACTICE', ?, ?, ?)";
                PreparedStatement psRes = conn.prepareStatement(sqlRes, java.sql.Statement.RETURN_GENERATED_KEYS);
                psRes.setInt(1, currentUser.getUserId());
                psRes.setInt(2, subject.id);
                psRes.setInt(3, correctCount);
                psRes.setInt(4, totalCount);
                psRes.setInt(5, timeTaken);
                psRes.executeUpdate();

                int resultId = 0;
                try (ResultSet rsKeys = psRes.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        resultId = rsKeys.getInt(1);
                    }
                }

                // Lưu bảng QuizAttemptDetails
                String sqlDet = "INSERT INTO QuizAttemptDetails (ResultID, QuestionID, SelectedAnswer, IsCorrect) VALUES (?, ?, ?, ?)";
                PreparedStatement psDet = conn.prepareStatement(sqlDet);
                
                for (int i = 0; i < totalCount; i++) {
                    QuestionAttemptData qd = questionDataList.get(i);
                    ButtonGroup bg = listBtnGroups.get(i);
                    String selectedAns = bg.getSelection() != null ? bg.getSelection().getActionCommand() : null;
                    boolean isCorrect = (selectedAns != null && selectedAns.equals(qd.correctAnsOrig));

                    psDet.setInt(1, resultId);
                    psDet.setInt(2, qd.questionId);
                    if (selectedAns == null) {
                        psDet.setNull(3, java.sql.Types.CHAR);
                    } else {
                        psDet.setString(3, selectedAns);
                    }
                    psDet.setBoolean(4, isCorrect);
                    psDet.executeUpdate(); // Thực thi lưu trực tiếp thay vì addBatch
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Nộp bài thành công!\nKết quả: " + correctCount + " / " + totalCount + "\nThời gian: " + timeTaken + " giây.", "Kết Quả", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(this, "Settings");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu lịch sử: " + ex.getMessage());
        }
    }

    private class AnswerItem {
        String originalKey;
        String text;
        public AnswerItem(String k, String t) { originalKey = k; text = t; }
    }
}
