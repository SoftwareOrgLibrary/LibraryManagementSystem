package libmng.repo;

import java.util.List;

import libmng.domain.User;

public interface UserRepository {
    void save(User user);
    User findById(int id);
    List<User> findAll();
    void deleteById(int id);
}
