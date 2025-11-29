package libmng.notify;

import libmng.domain.User;

public class EmailNotifier implements Notifier {
    private final EmailServer server;

    public EmailNotifier(EmailServer server) {
        this.server = server;
    }

    @Override
    public void send(User user, String message) {
        server.send(user.getEmail(), "Library Reminder", message);
    }
}

