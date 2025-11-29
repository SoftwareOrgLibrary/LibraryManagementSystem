package libmng;

import java.time.LocalDate;

import libmng.domain.Book;
import libmng.repo.InMemoryAdminRepository;
import libmng.repo.InMemoryLoanRepository;
import libmng.repo.InMemoryMediaRepository;
import libmng.repo.InMemoryUserRepository;
import libmng.service.AuthService;
import libmng.service.LibraryService;
import libmng.service.SessionManager;
import libmng.service.ex.AuthorizationException;
import libmng.time.TimeProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LibraryAdminAndInvalidTests {
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
    }

    @Test
    void restrictionsAndUnregister() {
        auth.login("admin", "secret");
        Book b = library.addBook("A", "B", "I");
        int userId = library.registerUser("u", "u@example.com").getId();
        library.borrowItem(userId, b.getId());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,3,1));
        Assertions.assertThrows(libmng.service.ex.BorrowingNotAllowedException.class, () -> library.borrowItem(userId, b.getId()));
        Assertions.assertThrows(libmng.service.ex.OperationNotAllowedException.class, () -> library.unregisterUser("admin", userId));
        library.payFine(userId, 1000);
        library.returnItem(b.getId());
        Assertions.assertDoesNotThrow(() -> library.unregisterUser("admin", userId));
    }

    @Test
    void borrowInvalidItemThrows() {
        auth.login("admin","secret");
        int uid = library.registerUser("u","e").getId();
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowItem(uid, 9999));
    }

    @Test
    void borrowWithInvalidUserThrows() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.borrowItem(9999, 1));
    }

    @Test
    void outstandingFinesInvalidUserThrows() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.outstandingFines(9999));
    }

    @Test
    void payFineInvalidUserThrows() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> library.payFine(9999, 10));
    }

    @Test
    void unregisterRequiresAdmin() {
        int uid = library.registerUser("u", "e").getId();
        Assertions.assertThrows(AuthorizationException.class, () -> library.unregisterUser("admin", uid));
        auth.login("admin", "secret");
        Assertions.assertDoesNotThrow(() -> library.unregisterUser("admin", uid));
    }

    @Test
    void addCdRequiresAdmin() {
        Assertions.assertThrows(AuthorizationException.class, () -> library.addCD("t", "a"));
        auth.login("admin", "secret");
        Assertions.assertDoesNotThrow(() -> library.addCD("t", "a"));
    }
}

