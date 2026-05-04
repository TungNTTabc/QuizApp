import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class S_DoExamPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel listExamPanel, testPanel;
    private User currentUser;
    
    // UI for list
    private JPanel examsListPanel;
    
    // UI for taking test
    private JPanel questionsListPanel;
    private Timer timer;
    private int secondsRemaining;
    private JLabel lblTimer;
    private int currentExamId;
    private String currentSubject;
    
    // Data for grading
    private List<String> listCorrectAnswers = new ArrayList<>();
    private List<ButtonGroup> listBtnGroups = new ArrayList<>();

    public S_DoExamPanel(User user) {
        this.currentUser = user;
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setOpaque(false);

        initListExamPanel();
        initTestPanel();

        add(listExamPanel, "List");
        add(testPanel, "Test");
    }

    private void initListExamPanel() {
        listExamPanel = new JPanel(new BorderLayout(10, 10));
        listExamPanel.setOpaque(false);
        listExamPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Chọn Bài Thi");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 30));
        listExamPanel.add(lblTitle, BorderLayout.NORTH);

        examsListPanel = new JPanel();
        examsListPanel.setLayout(new BoxLayout(examsListPanel, BoxLayout.Y_AXIS));
        examsListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(examsListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        listExamPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void loadExams() {
        examsListPanel.removeAll();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            // Hiển thị tất cả bài thi
            String sql = "SELECT * FROM Exams";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            boolean hasExam = false;
            while (rs.next()) {
                hasExam = true;
                int examId = rs.getInt("ExamID");
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

                JLabel lblExTitle = new JLabel("Bài thi: " + title);
                lblExTitle.setFont(new Font("Arial", Font.BOLD, 18));
                
                JLabel lblDesc = new JLabel("Môn học: " + subject + "  -  " + count + " câu hỏi  -  " + dur + " phút");
                lblDesc.setFont(new Font("Arial", Font.ITALIC, 14));
                lblDesc.setForeground(Color.GRAY);

                textInfoPanel.add(lblExTitle);
                textInfoPanel.add(Box.createVerticalStrut(5));
                textInfoPanel.add(lblDesc);

                itemPanel.add(textInfoPanel, BorderLayout.CENTER);

                JButton btnStart = new JButton("Làm Bài");
                btnStart.setFont(new Font("Arial", Font.BOLD, 16));
                btnStart.setBackground(new Color(95, 225, 235));
                btnStart.addActionListener(e -> startExam(examId, subject, dur));

                itemPanel.add(btnStart, BorderLayout.EAST);

                examsListPanel.add(itemPanel);
                examsListPanel.add(Box.createVerticalStrut(10));
            }
            if (!hasExam) {
                JLabel lblEmpty = new JLabel("Hiện không có bài thi nào mới dành cho bạn.");
                lblEmpty.setFont(new Font("Arial", Font.PLAIN, 18));
                examsListPanel.add(lblEmpty);
            }
            examsListPanel.revalidate();
            examsListPanel.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initTestPanel() {
        testPanel = new JPanel(new BorderLayout());
        testPanel.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        lblTimer = new JLabel("Thời gian còn lại: 00:00");
        lblTimer.setFont(new Font("Arial", Font.BOLD, 22));
        lblTimer.setForeground(Color.RED);
        topBar.add(lblTimer, BorderLayout.EAST);
        testPanel.add(topBar, BorderLayout.NORTH);

        questionsListPanel = new JPanel();
        questionsListPanel.setLayout(new BoxLayout(questionsListPanel, BoxLayout.Y_AXIS));
        questionsListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(questionsListPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        testPanel.add(scroll, BorderLayout.CENTER);

        JButton btnSubmit = new JButton("Nộp Bài");
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 20));
        btnSubmit.setBackground(new Color(95, 225, 235));
        btnSubmit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn nộp bài sớm?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                submitExam();
            }
        });
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.add(btnSubmit);
        testPanel.add(bottom, BorderLayout.SOUTH);

        timer = new Timer(1000, e -> {
            secondsRemaining--;
            if (secondsRemaining <= 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Hết thời gian! Tự động nộp bài.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                submitExam();
                return;
            }
            int m = secondsRemaining / 60;
            int s = secondsRemaining % 60;
            lblTimer.setText(String.format("Thời gian còn lại: %02d:%02d", m, s));
        });
    }

    private void startExam(int examId, String subject, int durationMinutes) {
        currentExamId = examId;
        currentSubject = subject;
        questionsListPanel.removeAll();
        listCorrectAnswers.clear();
        listBtnGroups.clear();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT q.* FROM Questions q JOIN ExamQuestions eq ON q.QuestionID = eq.QuestionID WHERE eq.ExamID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();

            int qIndex = 1;
            while (rs.next()) {
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
                listCorrectAnswers.add(correctText); 

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

            questionsListPanel.revalidate();
            questionsListPanel.repaint();
            
            secondsRemaining = durationMinutes * 60;
            int m = secondsRemaining / 60;
            int s = secondsRemaining % 60;
            lblTimer.setText(String.format("Thời gian còn lại: %02d:%02d", m, s));
            timer.start();
            cardLayout.show(this, "Test");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void submitExam() {
        timer.stop();
        int correctCount = 0;
        int totalCount = listCorrectAnswers.size();
        
        // Thời gian đã làm = Tổng thời gian - Thời gian còn lại
        // Tính toán lại tổng thời gian bằng cách query DB hoặc lấy từ secondsRemaining. Ta chỉ có secondsRemaining.
        // Để chuẩn, ta nên lưu durationMinutes lúc bắt đầu. Nhưng ta có thể lấy qua ExamID.
        // Tạm thời query lại.
        int totalSecs = 0;
        try(Connection conn = DBConnection.getConnection()){
            PreparedStatement ps = conn.prepareStatement("SELECT Duration FROM Exams WHERE ExamID = ?");
            ps.setInt(1, currentExamId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) totalSecs = rs.getInt("Duration") * 60;
        } catch(Exception e) { e.printStackTrace(); }
        
        int timeTaken = totalSecs - secondsRemaining;

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

        // Lưu vào CSDL ExamResults
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO ExamResults (ExamID, StudentID, CorrectCount, TotalCount, DurationInSeconds) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentExamId);
            ps.setInt(2, currentUser.getUserId());
            ps.setInt(3, correctCount);
            ps.setInt(4, totalCount);
            ps.setInt(5, timeTaken);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Hoàn thành bài thi!\nKết quả: " + correctCount + " / " + totalCount + "\nThời gian: " + timeTaken + " giây.", "Kết Quả", JOptionPane.INFORMATION_MESSAGE);
            loadExams();
            cardLayout.show(this, "List");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi nộp bài: " + ex.getMessage());
        }
    }

    private class AnswerItem {
        String originalKey;
        String text;
        public AnswerItem(String k, String t) { originalKey = k; text = t; }
    }
}
