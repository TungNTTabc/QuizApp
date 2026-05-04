public class User {
    private int userId;
    private String role; // "GV" hoặc "HS"
    private String username;
    private String fullName;

    public User(int userId, String role, String username, String fullName) {
        this.userId = userId;
        this.role = role;
        this.username = username;
        this.fullName = fullName;
    }

    public int getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }
}
