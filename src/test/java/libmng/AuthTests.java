package libmng;

import java.time.LocalDate;

import libmng.repo.InMemoryAdminRepository;
import libmng.repo.InMemoryLoanRepository;
import libmng.repo.InMemoryMediaRepository;
import libmng.repo.InMemoryUserRepository;
import libmng.service.AuthService;
import libmng.service.LibraryService;
import libmng.service.SessionManager;
import libmng.service.ex.AuthenticationException;
import libmng.service.ex.AuthorizationException;
import libmng.time.TimeProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

 class AuthTests {
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
    void validLogin() {
        auth.login("admin", "secret");
        Assertions.assertTrue(sessions.isAdminLoggedIn());
    }

    @Test
    void invalidLogin() {
        Assertions.assertThrows(AuthenticationException.class, () -> auth.login("admin", "bad"));
        Assertions.assertFalse(sessions.isAdminLoggedIn());
    }

    @Test
    void invalidUsername() {
        Assertions.assertThrows(AuthenticationException.class, () -> auth.login("nope", "secret"));
    }

    @Test
    void logoutClearsSession() {
        auth.login("admin","secret");
        auth.logout();
        Assertions.assertFalse(sessions.isAdminLoggedIn());
    }

    @Test
    void adminOnlyActions() {
        Assertions.assertThrows(AuthorizationException.class, () -> library.addBook("t", "a", "i"));
        auth.login("admin", "secret");
        library.addBook("t", "a", "i");
        auth.logout();
        Assertions.assertThrows(AuthorizationException.class, () -> library.addBook("t2", "a2", "i2"));
    }
}

