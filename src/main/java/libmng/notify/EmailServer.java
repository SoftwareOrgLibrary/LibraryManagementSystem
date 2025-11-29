package libmng.notify;

public interface EmailServer {
    void send(String to, String subject, String body);
}

