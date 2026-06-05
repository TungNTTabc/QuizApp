import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        String URL = "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=QuizAppDB;encrypt=true;trustServerCertificate=true";
        String USER = "sa";
        String PASSWORD = "1";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
