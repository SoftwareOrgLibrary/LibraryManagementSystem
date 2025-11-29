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
import libmng.service.ex.BorrowingNotAllowedException;
import libmng.time.TimeProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LibraryFinesTests {
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
    void payFineFlow() {
        Book b = library.addBook("A", "B", "I");
        int userId = library.registerUser("u", "u@example.com").getId();
        Loan l = library.borrowItem(userId, b.getId());
        Mockito.when(time.now()).thenReturn(l.getDueDate().plusDays(3));
        int due = library.outstandingFines(userId);
        Assertions.assertEquals(30, due);
        library.payFine(userId, 10);
        Assertions.assertEquals(20, library.outstandingFines(userId));
        Assertions.assertThrows(BorrowingNotAllowedException.class, () -> library.borrowItem(userId, b.getId()));
        library.payFine(userId, 20);
        Assertions.assertEquals(0, library.outstandingFines(userId));
        library.returnItem(b.getId());
        Book b2 = library.addBook("X", "Y", "Z");
        Assertions.assertDoesNotThrow(() -> library.borrowItem(userId, b2.getId()));
    }

    @Test
    void cdFineRate() {
        CD c = library.addCD("t", "a");
        int userId = library.registerUser("u2", "u2@example.com").getId();
        Loan l = library.borrowItem(userId, c.getId());
        Mockito.when(time.now()).thenReturn(l.getDueDate().plusDays(2));
        Assertions.assertEquals(40, library.outstandingFines(userId));
    }

    @Test
    void bookNoOverdueFineZero() {
        Book b = library.addBook("t", "a", "i");
        int u = library.registerUser("u", "e").getId();
        library.borrowItem(u, b.getId());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,1,28));
        Assertions.assertEquals(0, library.outstandingFines(u));
    }

    @Test
    void cdNoOverdueFineZero() {
        CD c = library.addCD("t", "a");
        int u = library.registerUser("u2", "e2").getId();
        library.borrowItem(u, c.getId());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,1,7));
        Assertions.assertEquals(0, library.outstandingFines(u));
    }

    @Test
    void stillBlockedUntilReturn() {
        Book b = library.addBook("t", "a", "i");
        int u = library.registerUser("u", "e").getId();
        library.borrowItem(u, b.getId());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,2,1));
        int fine = library.outstandingFines(u);
        library.payFine(u, fine);
        Assertions.assertThrows(BorrowingNotAllowedException.class, () -> library.borrowItem(u, b.getId()));
    }

    @Test
    void finesAcrossMedia() {
        Book b = library.addBook("b", "a", "i");
        CD c = library.addCD("t", "a");
        int uid = library.registerUser("u", "u@e").getId();
        library.borrowItem(uid, b.getId());
        library.borrowItem(uid, c.getId());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,2,5));
        int fine = library.outstandingFines(uid);
        Assertions.assertEquals(630, fine);
    }

    @Test
    void overpaymentClampedToZero() {
        Book b = library.addBook("tb", "ab", "ib");
        int u = library.registerUser("u3", "e3").getId();
        Loan l = library.borrowItem(u, b.getId());
        Mockito.when(time.now()).thenReturn(l.getDueDate().plusDays(1));
        int due = library.outstandingFines(u);
        library.payFine(u, due + 100);
        Assertions.assertEquals(0, library.outstandingFines(u));
    }

    @Test
    void borrowForDaysBlockedByOverdue() {
        Book b1 = library.addBook("t1","a1","i1");
        Book b2 = library.addBook("t2","a2","i2");
        int u = library.registerUser("user","mail").getId();
        Loan l = library.borrowItem(u, b1.getId());
        Mockito.when(time.now()).thenReturn(l.getDueDate().plusDays(1));
        Assertions.assertThrows(BorrowingNotAllowedException.class, () -> library.borrowItemForDays(u, b2.getId(), 2));
    }

    @Test
    void paymentValidation() {
        int uid = library.registerUser("u", "e").getId();
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.payFine(uid, -1));
        int uid2 = library.registerUser("u2", "e2").getId();
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.payFine(uid2, 0));
    }

    @Test
    void overdueUsersAndCount() {
        Book b1 = library.addBook("t1", "a1", "i1");
        Book b2 = library.addBook("t2", "a2", "i2");
        int u = library.registerUser("u", "u@e").getId();
        library.borrowItem(u, b1.getId());
        library.borrowItem(u, b2.getId());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,2,10));
        Assertions.assertEquals(2, library.overdueCount(u));
        Assertions.assertTrue(library.usersWithOverdues().stream().anyMatch(us -> us.getId() == u));
    }
}
