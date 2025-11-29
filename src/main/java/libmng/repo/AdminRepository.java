package libmng.repo;

import libmng.domain.Admin;

public interface AdminRepository {
    void save(Admin admin);
    Admin findByUsername(String username);
}
