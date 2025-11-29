package libmng;

import java.time.LocalDate;

import libmng.domain.Book;
import libmng.domain.Loan;
import libmng.fine.FineCalculator;
import libmng.repo.InMemoryMediaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FineCalculatorTests {
    @Test
    void missingMediaReturnsZero() {
        InMemoryMediaRepository media = new InMemoryMediaRepository();
        FineCalculator calc = new FineCalculator(media);
        Loan l = new Loan(1, 9999, LocalDate.of(2024,1,1), LocalDate.of(2024,1,10));
        int fine = calc.fineForLoan(l, LocalDate.of(2024,2,1));
        Assertions.assertEquals(0, fine);
    }

    @Test
    void atDueDateNoFine() {
        InMemoryMediaRepository media = new InMemoryMediaRepository();
        Book b = new Book("t","a","i");
        media.saveBook(b);
        Loan l = new Loan(1, b.getId(), LocalDate.of(2024,1,1), LocalDate.of(2024,1,10));
        FineCalculator calc = new FineCalculator(media);
        Assertions.assertEquals(0, calc.fineForLoan(l, LocalDate.of(2024,1,10)));
    }
}

