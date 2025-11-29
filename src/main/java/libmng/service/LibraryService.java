package libmng.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import libmng.domain.Book;
import libmng.domain.CD;
import libmng.domain.Loan;
import libmng.domain.Media;
import libmng.domain.MediaType;
import libmng.domain.User;
import libmng.fine.FineCalculator;
import libmng.repo.LoanRepository;
import libmng.repo.MediaRepository;
import libmng.repo.UserRepository;
import libmng.service.ex.AuthorizationException;
import libmng.service.ex.BorrowingNotAllowedException;
import libmng.service.ex.OperationNotAllowedException;
import libmng.time.TimeProvider;

public class LibraryService {
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final TimeProvider timeProvider;
    private final SessionManager sessionManager;
    private final FineCalculator fineCalculator;

    public LibraryService(MediaRepository mediaRepository, UserRepository userRepository, LoanRepository loanRepository, TimeProvider timeProvider, SessionManager sessionManager) {
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.timeProvider = timeProvider;
        this.sessionManager = sessionManager;
        this.fineCalculator = new FineCalculator(mediaRepository);
    }

    public Book addBook(String title, String author, String isbn) {
        if (!sessionManager.isAdminLoggedIn()) throw new AuthorizationException("admin required");
        Book b = new Book(title, author, isbn);
        mediaRepository.saveBook(b);
        return b;
    }

    public CD addCD(String title, String artist) {
        if (!sessionManager.isAdminLoggedIn()) throw new AuthorizationException("admin required");
        CD c = new CD(title, artist);
        mediaRepository.saveCD(c);
        return c;
    }

    public List<Media> search(String query) {
        return mediaRepository.search(query);
    }

    public User registerUser(String name, String email) {
        User u = new User(name, email);
        userRepository.save(u);
        return u;
    }

    public Loan borrowItem(int userId, int itemId) {
        User u = userRepository.findById(userId);
        if (u == null) throw new IllegalArgumentException("user");
        if (!canBorrow(u)) throw new BorrowingNotAllowedException("borrow blocked");
        if (loanRepository.findActiveByItemId(itemId) != null) throw new BorrowingNotAllowedException("item unavailable");
        LocalDate now = timeProvider.now();
        Media m = mediaRepository.findById(itemId);
        if (m == null) throw new IllegalArgumentException("item");
        int days = m.getType() == MediaType.BOOK ? 28 : 7;
        Loan l = new Loan(u.getId(), itemId, now, now.plus(days, ChronoUnit.DAYS));
        loanRepository.save(l);
        return l;
    }

    public Loan borrowItemForDays(int userId, int itemId, int daysRequested) {
        if (daysRequested <= 0) throw new IllegalArgumentException("days");
        User u = userRepository.findById(userId);
        if (u == null) throw new IllegalArgumentException("user");
        if (!canBorrow(u)) throw new BorrowingNotAllowedException("borrow blocked");
        if (loanRepository.findActiveByItemId(itemId) != null) throw new BorrowingNotAllowedException("item unavailable");
        LocalDate now = timeProvider.now();
        Media m = mediaRepository.findById(itemId);
        if (m == null) throw new IllegalArgumentException("item");
        int maxDays = m.getType() == MediaType.BOOK ? 28 : 7;
        int days = Math.min(daysRequested, maxDays);
        LocalDate due = now.plus(days, ChronoUnit.DAYS);
        if (!due.isAfter(now)) throw new IllegalArgumentException("due date");
        Loan l = new Loan(u.getId(), itemId, now, due);
        loanRepository.save(l);
        return l;
    }

    public void returnItem(int itemId) {
        Loan loan = loanRepository.findActiveByItemId(itemId);
        if (loan != null) loanRepository.markReturned(loan.getId());
    }

    public boolean isOverdue(Loan loan) {
        return timeProvider.now().isAfter(loan.getDueDate());
    }

    public int outstandingFines(int userId) {
        User u = userRepository.findById(userId);
        if (u == null) throw new IllegalArgumentException("user");
        List<Loan> active = loanRepository.findActiveByUserId(u.getId());
        int sum = 0;
        LocalDate now = timeProvider.now();
        for (Loan l : active) sum += fineCalculator.fineForLoan(l, now);
        int outstanding = sum - u.getPaidFines();
        return Math.max(outstanding, 0);
    }

    public void payFine(int userId, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount");
        User u = userRepository.findById(userId);
        if (u == null) throw new IllegalArgumentException("user");
        u.addPayment(amount);
    }

    public boolean canBorrow(User user) {
        List<Loan> active = loanRepository.findActiveByUserId(user.getId());
        LocalDate now = timeProvider.now();
        for (Loan l : active) if (isOverdue(l)) return false;
        return outstandingFines(user.getId()) == 0;
    }

    public List<User> usersWithOverdues() {
        List<User> users = userRepository.findAll();
        List<User> result = new ArrayList<>();
        for (User u : users) {
            List<Loan> active = loanRepository.findActiveByUserId(u.getId());
            boolean any = false;
            for (Loan l : active) if (isOverdue(l)) { any = true; break; }
            if (any) result.add(u);
        }
        return result;
    }

    public int overdueCount(int userId) {
        List<Loan> active = loanRepository.findActiveByUserId(userId);
        int c = 0;
        for (Loan l : active) if (isOverdue(l)) c++;
        return c;
    }

    public void unregisterUser(String adminUser, int targetUserId) {
        if (!sessionManager.isAdminLoggedIn()) throw new AuthorizationException("admin required");
        List<Loan> active = loanRepository.findActiveByUserId(targetUserId);
        for (Loan l : active) if (!l.isReturned()) throw new OperationNotAllowedException("active loans");
        if (outstandingFines(targetUserId) > 0) throw new OperationNotAllowedException("unpaid fines");
        userRepository.deleteById(targetUserId);
    }
}
