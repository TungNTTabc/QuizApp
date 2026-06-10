import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {
    
    // Sử dụng thuật toán băm SHA-256 (Thuật toán cơ bản, phổ biến và an toàn)
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi mã hóa mật khẩu", e);
        }
    }

    // Băm mật khẩu người dùng vừa nhập và so sánh với mã Hash trong Database
    public static boolean checkPassword(String password, String storedHash) {
        return hashPassword(password).equals(storedHash);
    }
}
