package libmng.repo;

import java.util.ArrayList;
import java.util.List;

import libmng.domain.Loan;

public class InMemoryLoanRepository implements LoanRepository {
    private final List<Loan> loans = new ArrayList<>();

    @Override
    public void save(Loan loan) {
        loans.add(loan);
    }

    @Override
    public Loan findActiveByItemId(int itemId) {
        for (Loan l : loans) if (!l.isReturned() && l.getItemId() == itemId) return l;
        return null;
    }

    @Override
    public List<Loan> findActiveByUserId(int userId) {
        List<Loan> active = new ArrayList<>();
        for (Loan l : loans) if (!l.isReturned() && l.getUserId() == userId) active.add(l);
        return active;
    }

    @Override
    public void markReturned(int loanId) {
        for (Loan l : loans) if (l.getId() == loanId) { l.markReturned(); break; }
    }
}
