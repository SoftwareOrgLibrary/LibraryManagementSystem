package libmng.fine;

public class BookFineStrategy implements FineStrategy {
    @Override
    public int calculateFine(int overdueDays) {
        if (overdueDays <= 0) return 0;
        return 10 * overdueDays;
    }
}

