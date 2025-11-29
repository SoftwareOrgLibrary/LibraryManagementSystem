package libmng;

import java.time.LocalDate;

import libmng.domain.Book;
import libmng.notify.EmailNotifier;
import libmng.notify.EmailServer;
import libmng.repo.InMemoryAdminRepository;
import libmng.repo.InMemoryLoanRepository;
import libmng.repo.InMemoryMediaRepository;
import libmng.repo.InMemoryUserRepository;
import libmng.service.AuthService;
import libmng.service.LibraryService;
import libmng.service.ReminderService;
import libmng.service.SessionManager;
import libmng.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ReminderTests {
    InMemoryAdminRepository admins;
    InMemoryMediaRepository media;
    InMemoryUserRepository users;
    InMemoryLoanRepository loans;
    SessionManager sessions;
    AuthService auth;
    LibraryService library;
    ReminderService reminders;
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
        reminders = new ReminderService(users, loans, media, time);
        auth.login("admin", "secret");
    }

    @Test
    void sendsEmailForOverdue() {
        Book b = library.addBook("A", "B", "I");
        int userId = library.registerUser("u", "u@example.com").getId();
        library.borrowItem(userId, b.getId());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,2,1));
        EmailServer server = Mockito.mock(EmailServer.class);
        EmailNotifier notifier = new EmailNotifier(server);
        reminders.sendReminders(notifier);
        ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        Mockito.verify(server).send(Mockito.eq("u@example.com"), subject.capture(), body.capture());
        assert body.getValue().contains("You have 1 overdue book(s).");
    }

    @Test
    void sendsCountForTwoOverdues() {
        Book b1 = library.addBook("A", "B", "I1");
        Book b2 = library.addBook("A2", "B2", "I2");
        int uid = library.registerUser("u", "u@e").getId();
        library.borrowItem(uid, b1.getId());
        library.borrowItem(uid, b2.getId());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,3,1));
        EmailServer server = Mockito.mock(EmailServer.class);
        EmailNotifier notifier = new EmailNotifier(server);
        reminders.sendReminders(notifier);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        Mockito.verify(server).send(Mockito.anyString(), Mockito.anyString(), body.capture());
        assert body.getValue().contains("You have 2 overdue book(s).");
    }

    @Test
    void noOverduesSendsNothing() {
        Book b = library.addBook("A", "B", "I");
        int uid = library.registerUser("u", "e").getId();
        library.borrowItem(uid, b.getId());
        Mockito.when(time.now()).thenReturn(LocalDate.of(2024,1,10));
        EmailServer server = Mockito.mock(EmailServer.class);
        EmailNotifier notifier = new EmailNotifier(server);
        reminders.sendReminders(notifier);
        Mockito.verifyNoInteractions(server);
    }
}

