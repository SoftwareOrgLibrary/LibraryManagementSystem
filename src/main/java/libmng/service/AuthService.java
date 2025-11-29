package libmng.service;

import libmng.domain.Admin;
import libmng.repo.AdminRepository;
import libmng.service.ex.AuthenticationException;

public class AuthService {
    private final AdminRepository adminRepository;
    private final SessionManager sessionManager;

    public AuthService(AdminRepository adminRepository, SessionManager sessionManager) {
        this.adminRepository = adminRepository;
        this.sessionManager = sessionManager;
    }

    public void login(String username, String password) {
        Admin admin = adminRepository.findByUsername(username);
        if (admin == null) throw new AuthenticationException("invalid credentials");
        if (!admin.getPassword().equals(password)) throw new AuthenticationException("invalid credentials");
        sessionManager.adminLogin();
    }

    public void logout() {
        sessionManager.adminLogout();
    }
}
