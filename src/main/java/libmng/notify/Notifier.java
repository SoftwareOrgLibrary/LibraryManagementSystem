package libmng.notify;

import libmng.domain.User;

public interface Notifier {
    void send(User user, String message);
}

