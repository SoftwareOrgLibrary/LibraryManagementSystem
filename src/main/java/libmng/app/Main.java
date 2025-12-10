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
                    logger.info(USERNAME_PROMPT);
                    String u = br.readLine();
                    logger.info(PASSWORD_PROMPT);
                    String p = br.readLine();
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
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(String.format("Error: %s", e.getMessage()));
                }
            }
        }
    }

    private static void printMainHeader() {

        logger.info("Library Management System");

    }

    private static void printMainMenu() {
        logger.info("1. Admin Login");
        logger.info("2. User Login  (optional – future phases)");
        logger.info("3. Sign Up");
        logger.info("4. Exit");
        logger.info(SEPARATOR);
        logger.info(ENTER_CHOICE);
    }

    private static void adminMenu(BufferedReader br, AuthService auth, LibraryService lib, ReminderService reminders, SessionManager sessions, InMemoryUserRepository users, InMemoryLoanRepository loans) throws Exception {
        while (true) {

            logger.info("Admin Menu");

            logger.info("1. Add Book");
            logger.info("2. Search Books");
            logger.info("3. Register New User");
            logger.info("4. Unregister User");
            logger.info("5. Borrow Item for User");
            logger.info("6. Return Item");
            logger.info("7. Pay Fine for User");
            logger.info("8. View User Fines");
            logger.info("9. Send Overdue Reminders");
            logger.info("10. Logout");
            logger.info(SEPARATOR);
            logger.info(ENTER_CHOICE);
            String c = br.readLine();
            if (c == null) break;
            c = c.trim();
            if (c.equals("10")) { auth.logout(); break; }
            if (c.equals("1")) {
                logger.info(TITLE_PROMPT); String t = br.readLine();
                logger.info(AUTHOR_PROMPT); String a = br.readLine();
                logger.info(ISBN_PROMPT); String i = br.readLine();
                try { 
                    Book b = lib.addBook(t, a, i); 
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Added book id=%d", b.getId()));
                    }
                } catch (AuthorizationException e) { 
                    logger.info(ADMIN_REQUIRED); 
                }
            } else if (c.equals("2")) {
                logger.info(QUERY_PROMPT); String q = br.readLine();
                List<Media> results = lib.search(q);
                if (logger.isLoggable(Level.INFO)) {
                    for (Media m : results) {
                        logger.info(String.format("%s | id=%d | title=%s", m.getType(), m.getId(), m.getTitle()));
                    }
                }
            } else if (c.equals("3")) {
                logger.info(NAME_PROMPT); String n = br.readLine();
                logger.info(EMAIL_PROMPT); String e = br.readLine();
                User u = lib.registerUser(n, e);
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(String.format("User id=%d", u.getId()));
                }
            } else if (c.equals("4")) {
                logger.info(USER_ID_PROMPT); int id = Integer.parseInt(br.readLine());
                try { lib.unregisterUser("admin", id); logger.info("Unregistered"); } catch (AuthorizationException ex) { logger.info(ADMIN_REQUIRED); } catch (Exception ex) { logger.info(BLOCKED); }
            } else if (c.equals("5")) {
                logger.info(USER_ID_PROMPT); int uid = Integer.parseInt(br.readLine());
                logger.info(ITEM_ID_PROMPT); int iid = Integer.parseInt(br.readLine());
                try { 
                    Loan l = lib.borrowItem(uid, iid); 
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Borrowed, due=%s", l.getDueDate()));
                    }
                } catch (BorrowingNotAllowedException ex) { 
                    logger.info(BLOCKED); 
                } catch (Exception ex) { 
                    logger.info(ERROR); 
                }
            } else if (c.equals("6")) {
                logger.info(ITEM_ID_PROMPT); int iid = Integer.parseInt(br.readLine());
                lib.returnItem(iid);
                logger.info("Returned if active");
            } else if (c.equals("7")) {
                logger.info(USER_ID_PROMPT); int uid = Integer.parseInt(br.readLine());
                logger.info(AMOUNT_PROMPT); int amt = Integer.parseInt(br.readLine());
                try { lib.payFine(uid, amt); logger.info("Paid"); } catch (Exception ex) { logger.info(ERROR); }
            } else if (c.equals("8")) {
                logger.info(USER_ID_PROMPT); int uid = Integer.parseInt(br.readLine());
                try { 
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Outstanding=%d", lib.outstandingFines(uid)));
                    }
                } catch (Exception ex) { 
                    logger.info(ERROR); 
                }
            } else if (c.equals("9")) {
                EmailServer server = new EmailServer() { 
                    public void send(String to, String subject, String body) { 
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info(String.format("Email -> %s | %s | %s", to, subject, body));
                        }
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

        logger.info(USER_ID_PROMPT);
        String s = br.readLine();
        if (s == null) return;
        int uid;
        try { uid = Integer.parseInt(s.trim()); } catch (Exception e) { logger.info("Invalid id"); return; }
        User u = users.findById(uid);
        if (u == null) { logger.info("Not found"); return; }
        sessions.userLogin(uid);
        while (true) {

            logger.info("User Menu");

            logger.info("1. Search for Media");
            logger.info("2. Borrow Book");
            logger.info("3. Borrow for N days");
            logger.info("4. Borrow CD");
            logger.info("5. View My Loans");
            logger.info("6. Pay My Fines");
            logger.info("7. Logout");

            logger.info(SEPARATOR);
            logger.info(ENTER_CHOICE);
            String c = br.readLine();
            if (c == null) break;
            c = c.trim();
            if (c.equals("7")) { sessions.userLogout(); break; }
            if (c.equals("1")) {
                logger.info(QUERY_PROMPT); String q = br.readLine();
                List<Media> results = lib.search(q);
                if (logger.isLoggable(Level.INFO)) {
                    for (Media m : results) {
                        logger.info(String.format("%s | id=%d | title=%s", m.getType(), m.getId(), m.getTitle()));
                    }
                }
            } else if (c.equals("2") || c.equals("4")) {
                logger.info(ITEM_ID_PROMPT); int iid = Integer.parseInt(br.readLine());
                try { 
                    Loan l = lib.borrowItem(uid, iid); 
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Borrowed, due=%s", l.getDueDate()));
                    }
                } catch (BorrowingNotAllowedException ex) { 
                    logger.info(BLOCKED); 
                } catch (Exception ex) { 
                    logger.info(ERROR); 
                }
            } else if (c.equals("5")) {
                List<Loan> list = loans.findActiveByUserId(uid);
                if (logger.isLoggable(Level.INFO)) {
                    for (Loan l : list) {
                        logger.info(String.format("Loan id=%d | item=%d | due=%s", l.getId(), l.getItemId(), l.getDueDate()));
                    }
                }
            } else if (c.equals("6")) {
                int due = lib.outstandingFines(uid);
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(String.format("Outstanding=%d", due));
                }
                logger.info(AMOUNT_PROMPT); int amt = Integer.parseInt(br.readLine());
                try { lib.payFine(uid, amt); logger.info("Paid"); } catch (Exception ex) { logger.info(ERROR); }
            } else if (c.equals("3")) {
                logger.info(ITEM_ID_PROMPT); int iid = Integer.parseInt(br.readLine());
                logger.info(DAYS_PROMPT); int days = Integer.parseInt(br.readLine());
                try { 
                    Loan l = lib.borrowItemForDays(uid, iid, days); 
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Borrowed, due=%s", l.getDueDate()));
                    }
                } catch (BorrowingNotAllowedException ex) { 
                    logger.info(BLOCKED); 
                } catch (Exception ex) { 
                    logger.info(ERROR); 
                }
            }
        }
    }

    private static void signUpMenu(BufferedReader br, LibraryService lib, InMemoryAdminRepository admins) throws Exception {
        while (true) {

            logger.info("SIGN UP");

            logger.info("1. Create User Account");
            logger.info("2. Create Admin Account");
            logger.info("3. Back");
            logger.info(SEPARATOR);
            logger.info(ENTER_CHOICE);
            String c = br.readLine();
            if (c == null) break;
            c = c.trim();
            if (c.equals("3")) break;
            if (c.equals("1")) {
                logger.info(NAME_PROMPT); String n = br.readLine();
                logger.info(EMAIL_PROMPT); String e = br.readLine();
                User u = lib.registerUser(n, e);
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(String.format("User created. id=%d", u.getId()));
                }
            } else if (c.equals("2")) {
                logger.info(USERNAME_PROMPT); String u = br.readLine();
                logger.info(PASSWORD_PROMPT); String p = br.readLine();
                logger.info(EMAIL_PROMPT); String mail = br.readLine();
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
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Verification code sent to %s", mail));
                    }
                }
                catch (RuntimeException ex) { 
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Failed to send email: %s", ex.getMessage()));
                    }
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
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(String.format("Admin account created for '%s'", u));
                    }
                }
            }
        }
    }
}
