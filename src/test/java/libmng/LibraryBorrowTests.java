package libmng;

import java.time.LocalDate;

import libmng.domain.Book;
import libmng.domain.CD;
import libmng.domain.Loan;
import libmng.repo.InMemoryAdminRepository;
import libmng.repo.InMemoryLoanRepository;
import libmng.repo.InMemoryMediaRepository;
import libmng.repo.InMemoryUserRepository;
import libmng.service.AuthService;
import libmng.service.LibraryService;
import libmng.service.SessionManager;
import libmng.time.TimeProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LibraryBorrowTests {
    InMemoryAdminRepository admins;
    InMemoryMediaRepository media;
    InMemoryUserRepository users;
    InMemoryLoanRepository loans;
    SessionManager sessions;
    AuthService auth;
    LibraryService library;
    TimeProvider time;

    @BeforeEach
    void setup() {
        admins = new InMemoryAdminRepository();
        media = new InMemoryMediaRepository();
        users = new InMemoryUserRepository();
        loans = new InMemoryLoanRepository();
        sessions = new SessionManager();
        auth = new AuthService(admins, sessions);
        time = Mockito.mock(TimeProvider.class);
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,1,1));
        library = new LibraryService(media, users, loans, time, sessions);
        auth.login("admin", "secret");
    }

    @Test
    void borrowBookSetsDue28() {
        Book b = library.addBook("A", "B", "I");
        int userId = library.registerUser("u", "u@example.com").getId();
        Loan l = library.borrowItem(userId, b.getId());
        Assertions.assertEquals(LocalDate.of(2024,1,29), l.getDueDate());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,1,30));
        Assertions.assertTrue(library.isOverdue(l));
    }

    @Test
    void cdDue7Days() {
        CD c = library.addCD("t", "a");
        int userId = library.registerUser("u", "e@example.com").getId();
        Loan l = library.borrowItem(userId, c.getId());
        Assertions.assertEquals(LocalDate.of(2024,1,8), l.getDueDate());
    }

    @Test
    void sameItemUnavailableForSecondUser() {
        Book b = library.addBook("A", "B", "I");
        int u1 = library.registerUser("u1", "u1@e").getId();
        int u2 = library.registerUser("u2", "u2@e").getId();
        library.borrowItem(u1, b.getId());
        Assertions.assertThrows(libmng.service.ex.BorrowingNotAllowedException.class, () -> library.borrowItem(u2, b.getId()));
    }

    @Test
    void returnMakesAvailable() {
        Book b = library.addBook("A2", "B2", "I2");
        int u1 = library.registerUser("u1", "u1@e").getId();
        int u2 = library.registerUser("u2", "u2@e").getId();
        library.borrowItem(u1, b.getId());
        library.returnItem(b.getId());
        Assertions.assertDoesNotThrow(() -> library.borrowItem(u2, b.getId()));
    }

    @Test
    void returnNoActiveLoanDoesNothing() {
        library.returnItem(9999);
    }

    @Test
    void borrowForDaysClampedForBook() {
        Book b = library.addBook("T", "A", "ISBN");
        int u = library.registerUser("u", "e").getId();
        Loan l = library.borrowItemForDays(u, b.getId(), 100);
        Assertions.assertEquals(LocalDate.of(2024,1,29), l.getDueDate());
    }

    @Test
    void borrowForDaysClampedForCd() {
        CD c = library.addCD("T", "ART");
        int u = library.registerUser("u2", "e2").getId();
        Loan l = library.borrowItemForDays(u, c.getId(), 10);
        Assertions.assertEquals(LocalDate.of(2024,1,8), l.getDueDate());
    }

    @Test
    void borrowForDaysZeroThrows() {
        Book b = library.addBook("T2", "A2", "I2");
        int u = library.registerUser("u3", "e3").getId();
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowItemForDays(u, b.getId(), 0));
    }

    @Test
    void borrowForDaysInvalidItemThrows() {
        int u = library.registerUser("u4", "e4").getId();
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowItemForDays(u, 9999, 2));
    }

    @Test
    void borrowForDaysUnavailableItem() {
        Book b = library.addBook("T3", "A3", "I3");
        int u1 = library.registerUser("u5", "e5").getId();
        int u2 = library.registerUser("u6", "e6").getId();
        library.borrowItem(u1, b.getId());
        Assertions.assertThrows(libmng.service.ex.BorrowingNotAllowedException.class, () -> library.borrowItemForDays(u2, b.getId(), 2));
    }
}
