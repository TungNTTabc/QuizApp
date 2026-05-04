import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Thông tin kết nối SQL Server
    // Thay đổi thông tin này cho phù hợp với máy của bạn
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuizAppDB;encrypt=true;trustServerCertificate=true;";
    //private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuizAppDB;encrypt=true;trustServerCertificate=true;integratedSecurity=true;";
    private static final String USER = "quizadmin"; // Tên đăng nhập SQL Server (thường là sa)
    private static final String PASSWORD = "123"; // Mật khẩu SQL Server của bạn

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
}
