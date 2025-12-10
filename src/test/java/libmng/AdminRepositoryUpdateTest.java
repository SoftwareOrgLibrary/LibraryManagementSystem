package libmng;

import libmng.domain.Admin;
import libmng.repo.InMemoryAdminRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

 class AdminRepositoryUpdateTest {
    @Test
    void updateExistingAdmin() {
        InMemoryAdminRepository repo = new InMemoryAdminRepository();
        Admin a = new Admin("admin","secret");
        repo.save(a);
        Admin updated = new Admin("admin","newpass");
        repo.save(updated);
        Assertions.assertEquals("newpass", repo.findByUsername("admin").getPassword());
    }
}

