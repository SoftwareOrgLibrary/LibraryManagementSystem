package libmng.repo;

import java.util.List;

import libmng.domain.Loan;

public interface LoanRepository {
    void save(Loan loan);
    Loan findActiveByItemId(int itemId);
    List<Loan> findActiveByUserId(int userId);
    void markReturned(int loanId);
}
