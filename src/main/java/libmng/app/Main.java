package libmng.app;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import libmng.domain.Book;
import libmng.domain.Admin;

import libmng.domain.Loan;
import libmng.domain.Media;
import libmng.domain.User;
import libmng.notify.EmailServer;
import libmng.notify.EmailNotifier;
import libmng.notify.SmtpEmailServer;
import libmng.repo.InMemoryAdminRepository;
import libmng.repo.InMemoryLoanRepository;
import libmng.repo.InMemoryMediaRepository;
import libmng.repo.InMemoryUserRepository;
import libmng.service.AuthService;
import libmng.service.LibraryService;
import libmng.service.ReminderService;
import libmng.service.SessionManager;
import libmng.service.ex.AuthorizationException;
import libmng.service.ex.BorrowingNotAllowedException;
import libmng.time.RealTimeProvider;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    // Constants for repeated strings
    private static final String SEPARATOR = "------------------------------------";
    private static final String ENTER_CHOICE = "Enter choice: ";
    private static final String EMAIL_PROMPT = "Email: ";
    private static final String USER_ID_PROMPT = "User id: ";
    private static final String ITEM_ID_PROMPT = "Item id: ";
    private static final String BLOCKED = "Blocked";
    private static final String ERROR = "Error";
    private static final String QUERY_PROMPT = "Query: ";
    private static final String ADMIN_REQUIRED = "Admin required";
    private static final String PASSWORD_PROMPT = "Password: ";
    private static final String USERNAME_PROMPT = "Username: ";
    private static final String TITLE_PROMPT = "Title: ";
    private static final String AUTHOR_PROMPT = "Author: ";
    private static final String ISBN_PROMPT = "ISBN: ";
    private static final String NAME_PROMPT = "Name: ";
    private static final String AMOUNT_PROMPT = "Amount: ";
    private static final String DAYS_PROMPT = "Days: ";
    
    // Helper methods to reduce duplication
    private static String readInput(BufferedReader br, String prompt) throws Exception {
        logger.info(prompt);
        return br.readLine();
    }
    
    private static int readIntInput(BufferedReader br, String prompt) throws Exception {
        logger.info(prompt);
        return Integer.parseInt(br.readLine());
    }
    
    private static void displayMenu(String title, String[] options) {
        logger.info(title);
        for (String option : options) {
            logger.info(option);
        }
        logger.info(SEPARATOR);
        logger.info(ENTER_CHOICE);
    }
    
    private static void displaySearchResults(List<Media> results) {
        if (logger.isLoggable(Level.INFO)) {
            for (Media m : results) {
                logger.info(String.format("%s | id=%d | title=%s", m.getType(), m.getId(), m.getTitle()));
            }
        }
    }
    
    private static void displayLoans(List<Loan> loans) {
        if (logger.isLoggable(Level.INFO)) {
            for (Loan l : loans) {
                logger.info(String.format("Loan id=%d | item=%d | due=%s", l.getId(), l.getItemId(), l.getDueDate()));
            }
        }
    }
    
    private static void logInfoFormat(String format, Object... args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format(format, args));
        }
    }
    
    private static void handleBorrowItem(LibraryService lib, int uid, int iid) {
        try {
            Loan l = lib.borrowItem(uid, iid);
            logInfoFormat("Borrowed, due=%s", l.getDueDate());
        } catch (BorrowingNotAllowedException ex) {
            logger.info(BLOCKED);
        } catch (Exception ex) {
            logger.info(ERROR);
        }
    }
    
    private static void handleBorrowItemForDays(LibraryService lib, int uid, int iid, int days) {
        try {
            Loan l = lib.borrowItemForDays(uid, iid, days);
            logInfoFormat("Borrowed, due=%s", l.getDueDate());
        } catch (BorrowingNotAllowedException ex) {
            logger.info(BLOCKED);
        } catch (Exception ex) {
            logger.info(ERROR);
        }
    }
    
    public static void main(String[] args) {
        InMemoryMediaRepository mediaRepository = new InMemoryMediaRepository();
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryLoanRepository loanRepository = new InMemoryLoanRepository();
        InMemoryAdminRepository adminRepository = new InMemoryAdminRepository();
        SessionManager sessionManager = new SessionManager();
        AuthService authService = new AuthService(adminRepository, sessionManager);
        LibraryService libraryService = new LibraryService(mediaRepository, userRepository, loanRepository, new RealTimeProvider(), sessionManager);
        ReminderService reminderService = new ReminderService(userRepository, loanRepository, mediaRepository, new RealTimeProvider());

        if (args != null && args.length > 0 && "print".equalsIgnoreCase(args[0])) {
            printMainHeader();
            printMainMenu();
            return;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                printMainHeader();
                printMainMenu();
                String c = br.readLine();
                if (c == null) break;
                c = c.trim();
                if (c.equals("4")) break;
                if (c.equals("1")) {
                    String u = readInput(br, USERNAME_PROMPT);
                    String p = readInput(br, PASSWORD_PROMPT);
                    try {
                        authService.login(u, p);
                        adminMenu(br, authService, libraryService, reminderService, sessionManager, userRepository, loanRepository);
                    } catch (Exception e) {
                        logger.info("Login failed");
                    }
                } else if (c.equals("2")) {
                    userLoginFlow(br, libraryService, sessionManager, userRepository, loanRepository);
                } else if (c.equals("3")) {
                    signUpMenu(br, libraryService, adminRepository);
                }
            } catch (Exception e) {
                logInfoFormat("Error: %s", e.getMessage());
            }
        }
    }

    private static void printMainHeader() {

        logger.info("Library Management System");

    }

    private static void printMainMenu() {
        displayMenu("", new String[]{
            "1. Admin Login",
            "2. User Login  (optional – future phases)",
            "3. Sign Up",
            "4. Exit"
        });
    }

    private static void adminMenu(BufferedReader br, AuthService auth, LibraryService lib, ReminderService reminders, SessionManager sessions, InMemoryUserRepository users, InMemoryLoanRepository loans) throws Exception {
        while (true) {
            displayMenu("Admin Menu", new String[]{
                "1. Add Book",
                "2. Search Books",
                "3. Register New User",
                "4. Unregister User",
                "5. Borrow Item for User",
                "6. Return Item",
                "7. Pay Fine for User",
                "8. View User Fines",
                "9. Send Overdue Reminders",
                "10. Logout"
            });
            String c = br.readLine();
            if (c == null) break;
            c = c.trim();
            if (c.equals("10")) { auth.logout(); break; }
            if (c.equals("1")) {
                String t = readInput(br, TITLE_PROMPT);
                String a = readInput(br, AUTHOR_PROMPT);
                String i = readInput(br, ISBN_PROMPT);
                try {
                    Book b = lib.addBook(t, a, i);
                    logInfoFormat("Added book id=%d", b.getId());
                } catch (AuthorizationException e) {
                    logger.info(ADMIN_REQUIRED);
                }
            } else if (c.equals("2")) {
                String q = readInput(br, QUERY_PROMPT);
                List<Media> results = lib.search(q);
                displaySearchResults(results);
            } else if (c.equals("3")) {
                String n = readInput(br, NAME_PROMPT);
                String e = readInput(br, EMAIL_PROMPT);
                User u = lib.registerUser(n, e);
                logInfoFormat("User id=%d", u.getId());
            } else if (c.equals("4")) {
                int id = readIntInput(br, USER_ID_PROMPT);
                try {
                    lib.unregisterUser("admin", id);
                    logger.info("Unregistered");
                } catch (AuthorizationException ex) {
                    logger.info(ADMIN_REQUIRED);
                } catch (Exception ex) {
                    logger.info(BLOCKED);
                }
            } else if (c.equals("5")) {
                int uid = readIntInput(br, USER_ID_PROMPT);
                int iid = readIntInput(br, ITEM_ID_PROMPT);
                handleBorrowItem(lib, uid, iid);
            } else if (c.equals("6")) {
                int iid = readIntInput(br, ITEM_ID_PROMPT);
                lib.returnItem(iid);
                logger.info("Returned if active");
            } else if (c.equals("7")) {
                int uid = readIntInput(br, USER_ID_PROMPT);
                int amt = readIntInput(br, AMOUNT_PROMPT);
                try {
                    lib.payFine(uid, amt);
                    logger.info("Paid");
                } catch (Exception ex) {
                    logger.info(ERROR);
                }
            } else if (c.equals("8")) {
                int uid = readIntInput(br, USER_ID_PROMPT);
                try {
                    logInfoFormat("Outstanding=%d", lib.outstandingFines(uid));
                } catch (Exception ex) {
                    logger.info(ERROR);
                }
            } else if (c.equals("9")) {
                EmailServer server = new EmailServer() {
                    public void send(String to, String subject, String body) {
                        logInfoFormat("Email -> %s | %s | %s", to, subject, body);
                    }
                };
                EmailNotifier notifier = new EmailNotifier(server);
                reminders.sendReminders(notifier);
                logger.info("Reminders sent");
            }
        }
    }

    private static void userLoginFlow(BufferedReader br, LibraryService lib, SessionManager sessions, InMemoryUserRepository users, InMemoryLoanRepository loans) throws Exception {
        logger.info("User Menu");
        String s = readInput(br, USER_ID_PROMPT);
        if (s == null) return;
        int uid;
        try {
            uid = Integer.parseInt(s.trim());
        } catch (Exception e) {
            logger.info("Invalid id");
            return;
        }
        User u = users.findById(uid);
        if (u == null) {
            logger.info("Not found");
            return;
        }
        sessions.userLogin(uid);
        while (true) {
            displayMenu("User Menu", new String[]{
                "1. Search for Media",
                "2. Borrow Book",
                "3. Borrow for N days",
                "4. Borrow CD",
                "5. View My Loans",
                "6. Pay My Fines",
                "7. Logout"
            });
            String c = br.readLine();
            if (c == null) break;
            c = c.trim();
            if (c.equals("7")) { sessions.userLogout(); break; }
            if (c.equals("1")) {
                String q = readInput(br, QUERY_PROMPT);
                List<Media> results = lib.search(q);
                displaySearchResults(results);
            } else if (c.equals("2") || c.equals("4")) {
                int iid = readIntInput(br, ITEM_ID_PROMPT);
                handleBorrowItem(lib, uid, iid);
            } else if (c.equals("5")) {
                List<Loan> list = loans.findActiveByUserId(uid);
                displayLoans(list);
            } else if (c.equals("6")) {
                int due = lib.outstandingFines(uid);
                logInfoFormat("Outstanding=%d", due);
                int amt = readIntInput(br, AMOUNT_PROMPT);
                try {
                    lib.payFine(uid, amt);
                    logger.info("Paid");
                } catch (Exception ex) {
                    logger.info(ERROR);
                }
            } else if (c.equals("3")) {
                int iid = readIntInput(br, ITEM_ID_PROMPT);
                int days = readIntInput(br, DAYS_PROMPT);
                handleBorrowItemForDays(lib, uid, iid, days);
            }
        }
    }

    private static void signUpMenu(BufferedReader br, LibraryService lib, InMemoryAdminRepository admins) throws Exception {
        while (true) {
            displayMenu("SIGN UP", new String[]{
                "1. Create User Account",
                "2. Create Admin Account",
                "3. Back"
            });
            String c = br.readLine();
            if (c == null) break;
            c = c.trim();
            if (c.equals("3")) break;
            if (c.equals("1")) {
                String n = readInput(br, NAME_PROMPT);
                String e = readInput(br, EMAIL_PROMPT);
                User u = lib.registerUser(n, e);
                logInfoFormat("User created. id=%d", u.getId());
            } else if (c.equals("2")) {
                String u = readInput(br, USERNAME_PROMPT);
                String p = readInput(br, PASSWORD_PROMPT);
                String mail = readInput(br, EMAIL_PROMPT);
                SecureRandom secureRandom = new SecureRandom();
                String code = String.valueOf(100000 + secureRandom.nextInt(900000));
                EmailServer smtp = new SmtpEmailServer(
                        "smtp.gmail.com", 587, true,
                        "shahd10121@gmail.com",
                        "mpmv nrmp jqdz wsia"
                );
                String subject = "Admin Verification Code";
                String body = String.format("Your verification code is: %s\nIf you did not request this, ignore this email.", code);
                try {
                    smtp.send(mail, subject, body);
                    logInfoFormat("Verification code sent to %s", mail);
                } catch (RuntimeException ex) {
                    logInfoFormat("Failed to send email: %s", ex.getMessage());
                    continue;
                }
                boolean ok = false;
                for (int attempts = 1; attempts <= 2; attempts++) {
                    logger.info("Enter verification code (6 digits): ");
                    String entered = br.readLine();
                    if (entered != null) entered = entered.trim();
                    if (code.equals(entered)) { ok = true; break; }
                    else if (attempts < 2) logger.info("Incorrect. Try again.");
                }
                if (!ok) {
                    logger.info("Verification failed. Signup canceled.");
                } else {
                    admins.save(new Admin(u, p, mail));
                    logInfoFormat("Admin account created for '%s'", u);
                }
            }
        }
    }
}
