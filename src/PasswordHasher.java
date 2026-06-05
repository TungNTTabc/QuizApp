import java.security.MessageDigest;

public class PasswordHasher {
    // Mã hóa mật khẩu sử dụng SHA-256 cơ bản
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi mã hóa mật khẩu", ex);
        }
    }

    // So sánh mật khẩu
    public static boolean checkPassword(String password, String storedHash) {
        // Mã hóa mật khẩu nhập vào và so sánh với mã trong Database
        String newHash = hashPassword(password);
        return newHash.equals(storedHash);
    }
}
