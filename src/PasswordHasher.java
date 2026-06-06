import java.security.MessageDigest;

public class PasswordHasher {
    // Bỏ mã hóa, trả về nguyên dạng Plain Text theo yêu cầu
    public static String hashPassword(String password) {
        return password;
    }

    // So sánh trực tiếp chữ thường
    public static boolean checkPassword(String password, String storedHash) {
        return password.equals(storedHash);
    }
}
