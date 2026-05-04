import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class TeacherDashboard extends JFrame {
    private User currentUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public TeacherDashboard(User user) {
        this.currentUser = user;
        setTitle("Giáo viên: " + user.getFullName());
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Tạo thanh menu top
        JPanel topMenu = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topMenu.setBackground(new Color(95, 225, 235)); // Màu xanh cyan

        JButton btnAddQuestion = createMenuButton("Thêm câu hỏi");
        JButton btnViewQuestion = createMenuButton("Xem câu hỏi");
        JButton btnAddExam = createMenuButton("Thêm bài thi");
        JButton btnViewExam = createMenuButton("Xem bài thi");
        JButton btnViewResult = createMenuButton("Kết quả bài thi");
        JButton btnUpdateInfo = createMenuButton("Thay đổi thông tin");
        JButton btnLogout = createMenuButton("Đăng xuất");

        topMenu.add(btnAddQuestion);
        topMenu.add(btnViewQuestion);
        topMenu.add(btnAddExam);
        topMenu.add(btnViewExam);
        topMenu.add(btnViewResult);
        topMenu.add(btnUpdateInfo);
        topMenu.add(btnLogout);

        add(topMenu, BorderLayout.NORTH);

        // Phần nội dung chính (Dùng CardLayout để chuyển trang)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(230, 230, 230)); // Màu xám nhạt như thiết kế

        // Tạo màn hình giới thiệu ban đầu
        JPanel welcomePanel = createTeacherIntroPanel();

        // Khởi tạo các màn hình
        T_ViewQuestionPanel viewQuestionPanel = new T_ViewQuestionPanel(cardLayout, contentPanel);
        T_AddExamPanel addExamPanel = new T_AddExamPanel(user);
        T_ViewExamPanel viewExamPanel = new T_ViewExamPanel(user);
        T_ViewResultPanel viewResultPanel = new T_ViewResultPanel(user);
        T_UpdateInfoPanel updateInfoPanel = new T_UpdateInfoPanel(user);

        // Thêm các màn hình vào CardLayout
        contentPanel.add(welcomePanel, "Welcome");
        contentPanel.add(createAddQuestionPanel(), "AddQuestion");
        contentPanel.add(viewQuestionPanel, "ViewQuestion");
        contentPanel.add(addExamPanel, "AddExam");
        contentPanel.add(viewExamPanel, "ViewExam");
        contentPanel.add(viewResultPanel, "ViewResult");
        contentPanel.add(updateInfoPanel, "UpdateInfo");

        add(contentPanel, BorderLayout.CENTER);

        // --- XỬ LÝ SỰ KIỆN CÁC NÚT MENU ---
        btnAddQuestion.addActionListener(e -> cardLayout.show(contentPanel, "AddQuestion"));
        
        btnViewQuestion.addActionListener(e -> {
            viewQuestionPanel.loadQuestions(""); // Reload mới nhất
            cardLayout.show(contentPanel, "ViewQuestion");
        });

        btnAddExam.addActionListener(e -> cardLayout.show(contentPanel, "AddExam"));

        btnViewExam.addActionListener(e -> {
            viewExamPanel.loadExams("");
            cardLayout.show(contentPanel, "ViewExam");
        });

        btnViewResult.addActionListener(e -> cardLayout.show(contentPanel, "ViewResult"));

        btnUpdateInfo.addActionListener(e -> cardLayout.show(contentPanel, "UpdateInfo"));

        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createTeacherIntroPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel box = new JPanel();
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setPreferredSize(new Dimension(900, 500));

        JLabel lblTitle = new JLabel("Hướng dẫn chức năng dành cho giáo viên");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lblTitle);
        box.add(Box.createVerticalStrut(20));

        JTextArea txtDesc = new JTextArea(
            "1. Thêm câu hỏi: Nhập tên môn học, nội dung câu hỏi, 4 đáp án, chọn đáp án đúng rồi ấn nút \"Thêm\" để lưu vào ngân hàng câu hỏi.\n\n" +
            "2. Xem câu hỏi: Truy cập danh sách câu hỏi đã tạo, sử dụng thanh tìm kiếm để lọc nội dung hoặc nhấn \"Sửa\" để thay đổi và \"Xóa\" để gỡ bỏ từng câu.\n\n" +
            "3. Thêm bài thi: Thiết lập chủ đề, môn học, số lượng câu hỏi và thời gian, sau đó nhập nội dung chi tiết cho từng câu trong bảng hiện ra và ấn \"Thêm bài thi\".\n\n" +
            "4. Xem bài thi: Quản lý danh sách bài thi thông qua thanh tìm kiếm tiêu đề, cho phép chọn \"Sửa\" để cập nhật nội dung cũ hoặc \"Xóa\" để loại bỏ bài thi hoàn toàn.\n\n" +
            "5. Kết quả bài thi: Chọn tiêu đề bài thi để xem danh sách học sinh kèm số câu đúng và thời gian làm bài, được sắp xếp tự động theo điểm từ cao đến thấp.\n\n" +
            "6. Thay đổi thông tin: Cập nhật các thông tin cá nhân, tài khoản hoặc mật khẩu trong bảng đăng ký (ngoại trừ vai trò) để sử dụng cho các lần đăng nhập sau.\n\n" +
            "7. Đăng xuất: Nhấn nút đăng xuất để kết thúc phiên làm việc và quay trở lại màn hình đăng nhập hoặc đăng ký ban đầu."
        );
        txtDesc.setFont(new Font("Arial", Font.PLAIN, 16));
        txtDesc.setEditable(false);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setOpaque(false);
        txtDesc.setBorder(null);
        box.add(txtDesc);

        wrapper.add(box);
        return wrapper;
    }

    // Giao diện Thêm Câu Hỏi
    private JPanel createAddQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Phần giữa (Câu hỏi và Trả lời)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Nhập câu hỏi
        JTextArea txtQuestion = new JTextArea(4, 50);
        txtQuestion.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane scrollQuestion = new JScrollPane(txtQuestion);
        scrollQuestion.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Câu hỏi:"));
        scrollQuestion.setMaximumSize(new Dimension(800, 150));
        centerPanel.add(scrollQuestion);
        centerPanel.add(Box.createVerticalStrut(20));

        // Nhóm RadioButton cho 4 đáp án
        ButtonGroup bgAnswers = new ButtonGroup();
        JPanel[] pnlAnswers = new JPanel[4];
        JTextField[] txtAnswers = new JTextField[4];
        JRadioButton[] rdbCorrect = new JRadioButton[4];

        for (int i = 0; i < 4; i++) {
            pnlAnswers[i] = new JPanel(new BorderLayout(10, 0));
            pnlAnswers[i].setOpaque(false);
            pnlAnswers[i].setMaximumSize(new Dimension(800, 50));

            rdbCorrect[i] = new JRadioButton();
            rdbCorrect[i].setOpaque(false);
            rdbCorrect[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            bgAnswers.add(rdbCorrect[i]);

            txtAnswers[i] = new JTextField();
            txtAnswers[i].setFont(new Font("Arial", Font.PLAIN, 16));
            txtAnswers[i].setBorder(BorderFactory.createTitledBorder("Câu trả lời số " + (i + 1) + ":"));

            pnlAnswers[i].add(rdbCorrect[i], BorderLayout.WEST);
            pnlAnswers[i].add(txtAnswers[i], BorderLayout.CENTER);

            centerPanel.add(pnlAnswers[i]);
            centerPanel.add(Box.createVerticalStrut(15));
        }

        // Mặc định chọn đáp án 1 là đúng
        rdbCorrect[0].setSelected(true);

        // Phần bên phải (Môn học và Nút Thêm)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(200, 0));

        JTextField txtSubject = new JTextField();
        txtSubject.setFont(new Font("Arial", Font.PLAIN, 16));
        txtSubject.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Môn học:"));
        txtSubject.setPreferredSize(new Dimension(200, 60));

        JPanel topOfRight = new JPanel(new BorderLayout());
        topOfRight.setOpaque(false);
        topOfRight.add(txtSubject, BorderLayout.NORTH);

        JButton btnSubmit = new JButton("Thêm ->");
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 18));
        btnSubmit.setBackground(new Color(95, 225, 235));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setPreferredSize(new Dimension(150, 50));
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel bottomOfRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomOfRight.setOpaque(false);
        bottomOfRight.add(btnSubmit);

        rightPanel.add(topOfRight, BorderLayout.NORTH);
        rightPanel.add(bottomOfRight, BorderLayout.SOUTH);

        // Ghép vào panel chính
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        // Xử lý sự kiện thêm câu hỏi
        btnSubmit.addActionListener(e -> {
            String subject = txtSubject.getText().trim();
            String content = txtQuestion.getText().trim();
            String ansA = txtAnswers[0].getText().trim();
            String ansB = txtAnswers[1].getText().trim();
            String ansC = txtAnswers[2].getText().trim();
            String ansD = txtAnswers[3].getText().trim();

            if (subject.isEmpty() || content.isEmpty() || ansA.isEmpty() || ansB.isEmpty() || ansC.isEmpty() || ansD.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ môn học, nội dung câu hỏi và 4 đáp án!");
                return;
            }

            char correctAns = 'A';
            if (rdbCorrect[1].isSelected()) correctAns = 'B';
            else if (rdbCorrect[2].isSelected()) correctAns = 'C';
            else if (rdbCorrect[3].isSelected()) correctAns = 'D';

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO Questions (Subject, Content, AnswerA, AnswerB, AnswerC, AnswerD, CorrectAnswer) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, subject);
                pstmt.setString(2, content);
                pstmt.setString(3, ansA);
                pstmt.setString(4, ansB);
                pstmt.setString(5, ansC);
                pstmt.setString(6, ansD);
                pstmt.setString(7, String.valueOf(correctAns));

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Đã thêm câu hỏi thành công!");
                
                // Xóa trắng form sau khi thêm
                txtQuestion.setText("");
                txtAnswers[0].setText("");
                txtAnswers[1].setText("");
                txtAnswers[2].setText("");
                txtAnswers[3].setText("");
                rdbCorrect[0].setSelected(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu câu hỏi: " + ex.getMessage());
            }
        });

        return panel;
    }
}
