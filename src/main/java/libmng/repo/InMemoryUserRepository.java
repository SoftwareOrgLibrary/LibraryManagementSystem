package libmng.repo;

import java.util.ArrayList;
import java.util.List;

import libmng.domain.User;

public class InMemoryUserRepository implements UserRepository {
    private final List<User> users = new ArrayList<>();

    @Override
    public void save(User user) {
        User existing = findById(user.getId());
        if (existing == null) {
            users.add(user);
        } else {
            int idx = -1;
            for (int i = 0; i < users.size(); i++) if (users.get(i).getId() == user.getId()) { idx = i; break; }
            if (idx >= 0) users.set(idx, user);
        }
    }

    @Override
    public User findById(int id) {
        for (User u : users) if (u.getId() == id) return u;
        return null;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    @Override
    public void deleteById(int id) {
        int idx = -1;
        for (int i = 0; i < users.size(); i++) if (users.get(i).getId() == id) { idx = i; break; }
        if (idx >= 0) users.remove(idx);
    }
}
