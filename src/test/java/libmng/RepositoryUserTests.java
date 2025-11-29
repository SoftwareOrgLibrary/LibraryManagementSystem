package libmng;

import libmng.domain.User;
import libmng.repo.InMemoryUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RepositoryUserTests {
    @Test
    void saveFindDelete() {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        User u = new User("a","e");
        repo.save(u);
        Assertions.assertEquals(u, repo.findById(u.getId()));
        repo.save(u);
        repo.deleteById(u.getId());
        Assertions.assertNull(repo.findById(u.getId()));
        repo.deleteById(9999);
    }
}

