package libmng.repo;

import java.util.ArrayList;
import java.util.List;

import libmng.domain.Admin;

public class InMemoryAdminRepository implements AdminRepository {
    private final List<Admin> admins = new ArrayList<>();

    public InMemoryAdminRepository() {
        admins.add(new Admin("admin", "secret", "admin@example.com"));
    }

    @Override
    public void save(Admin admin) {
        int idx = -1;
        for (int i = 0; i < admins.size(); i++) if (admins.get(i).getUsername().equals(admin.getUsername())) { idx = i; break; }
        if (idx >= 0) admins.set(idx, admin); else admins.add(admin);
    }

    @Override
    public Admin findByUsername(String username) {
        for (Admin a : admins) if (a.getUsername().equals(username)) return a;
        return null;
    }
}
