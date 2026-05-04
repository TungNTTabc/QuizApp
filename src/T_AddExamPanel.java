import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class T_AddExamPanel extends JPanel {
    private JPanel step1Panel;
    private JPanel step2Panel;
    private CardLayout cardLayout;
    
    private JTextField txtTitle, txtSubject, txtCount, txtDuration;
    private JPanel questionsListPanel;
    
    private User currentUser;

    public T_AddExamPanel(User user) {
        this.currentUser = user;
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setOpaque(false);

        initStep1();
        initStep2();

        add(step1Panel, "Step1");
        add(step2Panel, "Step2");
    }

    private void initStep1() {
        step1Panel = new JPanel();
        step1Panel.setLayout(new BoxLayout(step1Panel, BoxLayout.Y_AXIS));
        step1Panel.setOpaque(false);
        step1Panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel lblHeader = new JLabel("Tạo bài thi");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 40));
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        step1Panel.add(lblHeader);
        step1Panel.add(Box.createVerticalStrut(40));

        txtTitle = createField("Chủ đề bài thi:");
        step1Panel.add(txtTitle);
        step1Panel.add(Box.createVerticalStrut(20));

        JPanel row = new JPanel(new GridLayout(1, 3, 20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(800, 60));
        
        txtSubject = createField("Môn học:");
        txtCount = createField("Số lượng câu hỏi:");
        txtDuration = createField("Thời gian (phút):");

        row.add(txtSubject);
        row.add(txtCount);
        row.add(txtDuration);
        
        step1Panel.add(row);
        step1Panel.add(Box.createVerticalStrut(40));

        JButton btnNext = new JButton("Tiếp ->");
        btnNext.setFont(new Font("Arial", Font.BOLD, 20));
        btnNext.setBackground(new Color(95, 225, 235));
        btnNext.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnNext.addActionListener(e -> goToStep2());
        step1Panel.add(btnNext);
    }

    private void initStep2() {
        step2Panel = new JPanel(new BorderLayout());
        step2Panel.setOpaque(false);
        
        questionsListPanel = new JPanel();
        questionsListPanel.setLayout(new BoxLayout(questionsListPanel, BoxLayout.Y_AXIS));
        questionsListPanel.setOpaque(false);
        
        JScrollPane scroll = new JScrollPane(questionsListPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        
        step2Panel.add(scroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        
        JButton btnBack = new JButton("<- Quay lại");
        btnBack.addActionListener(e -> cardLayout.show(this, "Step1"));
        
        JButton btnSubmit = new JButton("Hoàn Thành Bài Thi");
        btnSubmit.setBackground(new Color(95, 225, 235));
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 16));
        btnSubmit.addActionListener(e -> submitExam());
        
        bottomPanel.add(btnBack);
        bottomPanel.add(btnSubmit);
        step2Panel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void goToStep2() {
        try {
            int count = Integer.parseInt(txtCount.getText().trim());
            if (count <= 0 || count > 100) {
                JOptionPane.showMessageDialog(this, "Số lượng câu hỏi phải từ 1 đến 100!");
                return;
            }
            if (txtTitle.getText().trim().isEmpty() || txtSubject.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ chủ đề và môn học!");
                return;
            }
            Integer.parseInt(txtDuration.getText().trim()); // Test number
            
            buildQuestionForms(count);
            cardLayout.show(this, "Step2");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng câu hỏi và Thời gian phải là số nguyên!");
        }
    }

    // Các danh sách để lưu component lấy dữ liệu sau này
    private java.util.List<JTextArea> listTxtQ = new java.util.ArrayList<>();
    private java.util.List<JTextField[]> listTxtAns = new java.util.ArrayList<>();
    private java.util.List<JRadioButton[]> listRdb = new java.util.ArrayList<>();

    private void buildQuestionForms(int count) {
        questionsListPanel.removeAll();
        listTxtQ.clear();
        listTxtAns.clear();
        listRdb.clear();

        for (int i = 1; i <= count; i++) {
            JPanel qPanel = new JPanel(new BorderLayout(10, 10));
            qPanel.setBackground(Color.WHITE);
            qPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 20, 10, 20),
                BorderFactory.createTitledBorder("Câu hỏi số " + i)
            ));

            JTextArea txtQ = new JTextArea(3, 40);
            listTxtQ.add(txtQ);
            qPanel.add(new JScrollPane(txtQ), BorderLayout.NORTH);

            JPanel ansPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            ansPanel.setOpaque(false);
            
            JTextField[] arrAns = new JTextField[4];
            JRadioButton[] arrRdb = new JRadioButton[4];
            ButtonGroup bg = new ButtonGroup();
            
            for (int j = 0; j < 4; j++) {
                JPanel row = new JPanel(new BorderLayout(5, 0));
                row.setOpaque(false);
                arrRdb[j] = new JRadioButton();
                bg.add(arrRdb[j]);
                arrAns[j] = new JTextField();
                arrAns[j].setBorder(BorderFactory.createTitledBorder("Đáp án " + (j+1)));
                
                row.add(arrRdb[j], BorderLayout.WEST);
                row.add(arrAns[j], BorderLayout.CENTER);
                ansPanel.add(row);
            }
            arrRdb[0].setSelected(true);
            
            listTxtAns.add(arrAns);
            listRdb.add(arrRdb);
            
            qPanel.add(ansPanel, BorderLayout.CENTER);
            questionsListPanel.add(qPanel);
            questionsListPanel.add(Box.createVerticalStrut(10));
        }
        questionsListPanel.revalidate();
        questionsListPanel.repaint();
    }

    private void submitExam() {
        // Validate dữ liệu
        for (int i = 0; i < listTxtQ.size(); i++) {
            if (listTxtQ.get(i).getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Câu hỏi số " + (i+1) + " đang trống!");
                return;
            }
            for (int j = 0; j < 4; j++) {
                if (listTxtAns.get(i)[j].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Đáp án của câu hỏi số " + (i+1) + " đang trống!");
                    return;
                }
            }
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction
            try {
                // 1. Thêm Bài thi
                String sqlExam = "INSERT INTO Exams (TeacherID, Title, Subject, QuestionCount, Duration) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psExam = conn.prepareStatement(sqlExam, Statement.RETURN_GENERATED_KEYS);
                psExam.setInt(1, currentUser.getUserId());
                psExam.setString(2, txtTitle.getText().trim());
                psExam.setString(3, txtSubject.getText().trim());
                psExam.setInt(4, listTxtQ.size());
                psExam.setInt(5, Integer.parseInt(txtDuration.getText().trim()));
                psExam.executeUpdate();
                
                ResultSet rsKeys = psExam.getGeneratedKeys();
                int examId = -1;
                if (rsKeys.next()) {
                    examId = rsKeys.getInt(1);
                }

                // 2. Thêm từng Câu hỏi và liên kết vào ExamQuestions
                String sqlQ = "INSERT INTO Questions (Subject, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement psQ = conn.prepareStatement(sqlQ, Statement.RETURN_GENERATED_KEYS);
                
                String sqlLink = "INSERT INTO ExamQuestions (ExamID, QuestionID) VALUES (?, ?)";
                PreparedStatement psLink = conn.prepareStatement(sqlLink);

                for (int i = 0; i < listTxtQ.size(); i++) {
                    psQ.setString(1, txtSubject.getText().trim());
                    psQ.setString(2, listTxtQ.get(i).getText().trim());
                    psQ.setString(3, listTxtAns.get(i)[0].getText().trim());
                    psQ.setString(4, listTxtAns.get(i)[1].getText().trim());
                    psQ.setString(5, listTxtAns.get(i)[2].getText().trim());
                    psQ.setString(6, listTxtAns.get(i)[3].getText().trim());
                    
                    char correct = 'A';
                    if (listRdb.get(i)[1].isSelected()) correct = 'B';
                    else if (listRdb.get(i)[2].isSelected()) correct = 'C';
                    else if (listRdb.get(i)[3].isSelected()) correct = 'D';
                    
                    psQ.setString(7, String.valueOf(correct));
                    psQ.executeUpdate();
                    
                    ResultSet rsQ = psQ.getGeneratedKeys();
                    if (rsQ.next()) {
                        int qId = rsQ.getInt(1);
                        psLink.setInt(1, examId);
                        psLink.setInt(2, qId);
                        psLink.addBatch();
                    }
                }
                psLink.executeBatch();
                conn.commit(); // Hoàn tất
                JOptionPane.showMessageDialog(this, "Thêm bài thi và các câu hỏi thành công!");
                
                txtTitle.setText("");
                txtCount.setText("");
                cardLayout.show(this, "Step1");
                
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tạo bài thi: " + ex.getMessage());
        }
    }

    private JTextField createField(String title) {
        JTextField f = new JTextField();
        f.setBorder(BorderFactory.createTitledBorder(title));
        f.setFont(new Font("Arial", Font.PLAIN, 16));
        f.setMaximumSize(new Dimension(800, 60));
        return f;
    }
}
