package libmng;

import java.time.LocalDate;
import java.util.List;

import libmng.domain.Book;
import libmng.domain.CD;
import libmng.domain.Media;
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

public class LibrarySearchTests {
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
    void addAndSearchBook() {
        Book b = library.addBook("Clean Code", "Robert", "123");
        List<Media> byTitle = library.search("Clean");
        Assertions.assertTrue(byTitle.contains(b));
        List<Media> byAuthor = library.search("Robert");
        Assertions.assertTrue(byAuthor.contains(b));
        List<Media> byIsbn = library.search("123");
        Assertions.assertTrue(byIsbn.contains(b));
    }

    @Test
    void searchCdByTitle() {
        CD c = library.addCD("Thriller", "MJ");
        List<Media> results = library.search("Thrill");
        Assertions.assertTrue(results.contains(c));
    }

    @Test
    void searchIgnoresCase() {
        Book b = library.addBook("Java", "Author", "XYZ");
        List<Media> results = library.search("java");
        Assertions.assertTrue(results.contains(b));
    }
}

