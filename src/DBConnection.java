import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=QuizAppDB;encrypt=true;trustServerCertificate=true";

    private static final String USER = "sa";
    private static final String PASSWORD = "1";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Tải driver SQL Server (bạn cần tải file mssql-jdbc.jar)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("Lỗi: Không tìm thấy thư viện JDBC. Hãy thêm mssql-jdbc.jar vào dự án!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối CSDL. Vui lòng kiểm tra lại URL, Username hoặc Password.");
            e.printStackTrace();
        }
        return conn;
    }

    public static int getOrCreateSubjectId(Connection conn, String subjectName) throws SQLException {
        // Kiểm tra xem môn học đã tồn tại chưa
        String checkSql = "SELECT SubjectID FROM Subjects WHERE SubjectName = ?";
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, subjectName);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("SubjectID");
                }
            }
        }
        
        // Nếu chưa tồn tại, chèn môn học mới
        String insertSql = "INSERT INTO Subjects (SubjectName) VALUES (?)";
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, subjectName);
            pstmt.executeUpdate();
            try (java.sql.ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Không thể lấy hoặc tạo SubjectID cho môn học: " + subjectName);
    }
}
