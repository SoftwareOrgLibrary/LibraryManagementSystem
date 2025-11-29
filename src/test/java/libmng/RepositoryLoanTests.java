package libmng;

import libmng.domain.Loan;
import libmng.repo.InMemoryLoanRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class RepositoryLoanTests {
    @Test
    void saveFindReturnSingle() {
        InMemoryLoanRepository repo = new InMemoryLoanRepository();
        Loan l = new Loan(1, 2, LocalDate.of(2024,1,1), LocalDate.of(2024,1,10));
        repo.save(l);
        Assertions.assertEquals(l, repo.findActiveByItemId(2));
        Assertions.assertEquals(1, repo.findActiveByUserId(1).size());
        repo.markReturned(l.getId());
        Assertions.assertNull(repo.findActiveByItemId(2));
        Assertions.assertTrue(repo.findActiveByUserId(1).isEmpty());
    }

    @Test
    void multipleLoansFilterActiveAndMarkUnknown() {
        InMemoryLoanRepository repo = new InMemoryLoanRepository();
        Loan l1 = new Loan(1, 10, LocalDate.of(2024,1,1), LocalDate.of(2024,1,5));
        Loan l2 = new Loan(1, 11, LocalDate.of(2024,1,2), LocalDate.of(2024,1,6));
        Loan l3 = new Loan(2, 10, LocalDate.of(2024,1,3), LocalDate.of(2024,1,7));
        repo.save(l1); repo.save(l2); repo.save(l3);
        repo.markReturned(9999);
        repo.markReturned(l2.getId());
        Assertions.assertEquals(1, repo.findActiveByUserId(1).size());
        Assertions.assertNull(repo.findActiveByItemId(11));
        Assertions.assertNotNull(repo.findActiveByItemId(10));
    }
}

