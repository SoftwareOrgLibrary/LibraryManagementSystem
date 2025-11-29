package libmng.domain;

import java.time.LocalDate;
import libmng.util.IdGenerator;

public class Loan {
    private final int id;
    private final int userId;
    private final int itemId;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private boolean returned;

    public Loan(int userId, int itemId, LocalDate borrowDate, LocalDate dueDate) {
        this.id = IdGenerator.nextId();
        this.userId = userId;
        this.itemId = itemId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returned = false;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getItemId() {
        return itemId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void markReturned() {
        this.returned = true;
    }
}
