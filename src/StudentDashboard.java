import javax.swing.*;
import java.awt.*;

public class StudentDashboard extends JFrame {
    private User currentUser;
    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Panels
    private S_DoQuestionPanel doQuestionPanel;
    private S_ViewHistoryPanel viewHistoryPanel;
    private S_DoExamPanel doExamPanel;
    private S_ViewResultPanel viewResultPanel;
    private S_NotePanel notePanel;
    private T_UpdateInfoPanel updateInfoPanel;

    public StudentDashboard(User user) {
        this.currentUser = user;
        setTitle("Học sinh: " + user.getFullName());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 245));
        
        // Tạo thanh menu top
        JPanel topMenu = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topMenu.setBackground(new Color(95, 225, 235));

        JButton btnDoQuestion = createMenuButton("Câu hỏi ôn tập");
        JButton btnViewHistory = createMenuButton("Xem lịch sử");
        JButton btnDoExam = createMenuButton("Làm bài thi");
        JButton btnViewResult = createMenuButton("Kết quả bài thi");
        JButton btnNote = createMenuButton("Ghi chú");
        JButton btnUpdateInfo = createMenuButton("Thay đổi thông tin");
        JButton btnLogout = createMenuButton("Đăng xuất");

        topMenu.add(btnDoQuestion);
        topMenu.add(btnViewHistory);
        topMenu.add(btnDoExam);
        topMenu.add(btnViewResult);
        topMenu.add(btnNote);
        topMenu.add(btnUpdateInfo);
        topMenu.add(btnLogout);

        add(topMenu, BorderLayout.NORTH);

        // Khởi tạo CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Welcome panel
        JPanel welcomePanel = createStudentIntroPanel();

        // Init feature panels
        doQuestionPanel = new S_DoQuestionPanel(user);
        viewHistoryPanel = new S_ViewHistoryPanel(user);
        doExamPanel = new S_DoExamPanel(user);
        viewResultPanel = new S_ViewResultPanel(user);
        notePanel = new S_NotePanel(user);
        updateInfoPanel = new T_UpdateInfoPanel(user); // Tái sử dụng form của giáo viên vì giống nhau

        contentPanel.add(welcomePanel, "Welcome");
        contentPanel.add(doQuestionPanel, "DoQuestion");
        contentPanel.add(viewHistoryPanel, "ViewHistory");
        contentPanel.add(doExamPanel, "DoExam");
        contentPanel.add(viewResultPanel, "ViewResult");
        contentPanel.add(notePanel, "Note");
        contentPanel.add(updateInfoPanel, "UpdateInfo");

        add(contentPanel, BorderLayout.CENTER);

        // --- SỰ KIỆN MENU ---
        btnDoQuestion.addActionListener(e -> {
            doQuestionPanel.refreshSubjects();
            cardLayout.show(contentPanel, "DoQuestion");
        });

        btnViewHistory.addActionListener(e -> {
            viewHistoryPanel.loadData();
            cardLayout.show(contentPanel, "ViewHistory");
        });

        btnDoExam.addActionListener(e -> {
            doExamPanel.loadExams();
            cardLayout.show(contentPanel, "DoExam");
        });

        btnViewResult.addActionListener(e -> {
            viewResultPanel.loadData();
            cardLayout.show(contentPanel, "ViewResult");
        });

        btnNote.addActionListener(e -> {
            notePanel.loadNote();
            cardLayout.show(contentPanel, "Note");
        });

        btnUpdateInfo.addActionListener(e -> {
            cardLayout.show(contentPanel, "UpdateInfo");
        });

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

    private JPanel createStudentIntroPanel() {
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

        JLabel lblTitle = new JLabel("Hướng dẫn chức năng dành cho học sinh");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lblTitle);
        box.add(Box.createVerticalStrut(20));

        JTextArea txtDesc = new JTextArea(
            "1. Câu hỏi ôn tập: Chọn môn học, số lượng câu hỏi và thời gian, sau đó làm bài luyện tập. Kết quả sẽ được lưu lại.\n\n" +
            "2. Xem lịch sử: Xem lại các lần đã làm bài, biết điểm số và thời gian từng lần.\n\n" +
            "3. Làm bài thi: Chọn bài thi hiện có, làm bài và nộp; hệ thống cho phép thi lại nhiều lần.\n\n" +
            "4. Kết quả bài thi: Chọn bài thi để xem bảng kết quả của mình và các bạn khác, bao gồm họ tên, MSSV, số câu đúng và thời gian.\n\n" +
            "5. Ghi chú: Viết hoặc chỉnh sửa ghi chú cá nhân để lưu lại ý kiến, nhắc nhở hoặc bài tập.\n\n" +
            "6. Thay đổi thông tin: Cập nhật tài khoản, họ và tên, MSSV và các thông tin cá nhân khác (ngoại trừ vai trò) để sử dụng cho đăng nhập sau.\n\n" +
            "7. Đăng xuất: Nhấn nút đăng xuất để kết thúc phiên và quay lại màn hình đăng nhập."
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
}
