package libmng.service.ex;

public class BorrowingNotAllowedException extends RuntimeException {
    public BorrowingNotAllowedException(String message) {
        super(message);
    }
}

