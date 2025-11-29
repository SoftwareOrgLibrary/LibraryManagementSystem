package libmng.service;

public class SessionManager {
    private boolean adminLoggedIn;
    private Integer currentUserId;

    public void adminLogin() {
        this.adminLoggedIn = true;
    }

    public void adminLogout() {
        this.adminLoggedIn = false;
    }

    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    public void userLogin(int userId) {
        this.currentUserId = userId;
    }

    public void userLogout() {
        this.currentUserId = null;
    }

    public boolean isUserLoggedIn() {
        return currentUserId != null;
    }

    public Integer currentUserId() {
        return currentUserId;
    }
}
