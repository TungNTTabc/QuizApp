import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class S_NotePanel extends JPanel {
    private JTextArea txtNote;
    private User currentUser;

    public S_NotePanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Ghi Chú Học Tập");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        txtNote = new JTextArea();
        txtNote.setFont(new Font("Arial", Font.PLAIN, 18));
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(txtNote);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnSave = new JButton("Lưu Ghi Chú");
        btnSave.setFont(new Font("Arial", Font.BOLD, 18));
        btnSave.setBackground(new Color(95, 225, 235));
        btnSave.addActionListener(e -> saveNote());
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);
    }

    public void loadNote() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT NoteContent FROM StudentNotes WHERE StudentID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, currentUser.getUserId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNote.setText(rs.getString("NoteContent"));
            } else {
                txtNote.setText("");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveNote() {
        try (Connection conn = DBConnection.getConnection()) {
            // Check if exists
            String check = "SELECT NoteID FROM StudentNotes WHERE StudentID = ?";
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setInt(1, currentUser.getUserId());
            ResultSet rs = psCheck.executeQuery();
            
            if (rs.next()) {
                String sql = "UPDATE StudentNotes SET NoteContent = ? WHERE StudentID = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtNote.getText());
                ps.setInt(2, currentUser.getUserId());
                ps.executeUpdate();
            } else {
                String sql = "INSERT INTO StudentNotes (StudentID, NoteContent) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, currentUser.getUserId());
                ps.setString(2, txtNote.getText());
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Đã lưu ghi chú thành công!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu ghi chú: " + ex.getMessage());
        }
    }
}
