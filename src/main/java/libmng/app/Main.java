package libmng.app;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import libmng.domain.Book;
import libmng.domain.Admin;
import libmng.domain.CD;
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
                    System.out.print("Username: ");
                    String u = br.readLine();
                    System.out.print("Password: ");
                    String p = br.readLine();
                    try {
                        authService.login(u, p);
                        adminMenu(br, authService, libraryService, reminderService, sessionManager, userRepository, loanRepository);
                    } catch (Exception e) {
                        System.out.println("Login failed");
                    }
                } else if (c.equals("2")) {
                    userLoginFlow(br, libraryService, sessionManager, userRepository, loanRepository);
                } else if (c.equals("3")) {
                    signUpMenu(br, libraryService, adminRepository);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printMainHeader() {

        System.out.println("Library Management System");

    }

    private static void printMainMenu() {
        System.out.println("1. Admin Login");
        System.out.println("2. User Login  (optional – future phases)");
        System.out.println("3. Sign Up");
        System.out.println("4. Exit");
        System.out.println("------------------------------------");
        System.out.print("Enter choice: ");
    }

    private static void adminMenu(BufferedReader br, AuthService auth, LibraryService lib, ReminderService reminders, SessionManager sessions, InMemoryUserRepository users, InMemoryLoanRepository loans) throws Exception {
        while (true) {

            System.out.println("Admin Menu");

            System.out.println("1. Add Book");
            System.out.println("2. Search Books");
            System.out.println("3. Register New User");
            System.out.println("4. Unregister User");
            System.out.println("5. Borrow Item for User");
            System.out.println("6. Return Item");
            System.out.println("7. Pay Fine for User");
            System.out.println("8. View User Fines");
            System.out.println("9. Send Overdue Reminders");
            System.out.println("10. Logout");
            System.out.println("------------------------------------");
            System.out.print("Enter choice: ");
            String c = br.readLine();
            if (c == null) break;
            c = c.trim();
            if (c.equals("10")) { auth.logout(); break; }
            if (c.equals("1")) {
                System.out.print("Title: "); String t = br.readLine();
                System.out.print("Author: "); String a = br.readLine();
                System.out.print("ISBN: "); String i = br.readLine();
                try { Book b = lib.addBook(t, a, i); System.out.println("Added book id=" + b.getId()); } catch (AuthorizationException e) { System.out.println("Admin required"); }
            } else if (c.equals("2")) {
                System.out.print("Query: "); String q = br.readLine();
                List<Media> results = lib.search(q);
                for (Media m : results) System.out.println(m.getType() + " | id=" + m.getId() + " | title=" + m.getTitle());
            } else if (c.equals("3")) {
                System.out.print("Name: "); String n = br.readLine();
                System.out.print("Email: "); String e = br.readLine();
                User u = lib.registerUser(n, e);
                System.out.println("User id=" + u.getId());
            } else if (c.equals("4")) {
                System.out.print("User id: "); int id = Integer.parseInt(br.readLine());
                try { lib.unregisterUser("admin", id); System.out.println("Unregistered"); } catch (AuthorizationException ex) { System.out.println("Admin required"); } catch (Exception ex) { System.out.println("Blocked"); }
            } else if (c.equals("5")) {
                System.out.print("User id: "); int uid = Integer.parseInt(br.readLine());
                System.out.print("Item id: "); int iid = Integer.parseInt(br.readLine());
                try { Loan l = lib.borrowItem(uid, iid); System.out.println("Borrowed, due=" + l.getDueDate()); } catch (BorrowingNotAllowedException ex) { System.out.println("Blocked"); } catch (Exception ex) { System.out.println("Error"); }
            } else if (c.equals("6")) {
                System.out.print("Item id: "); int iid = Integer.parseInt(br.readLine());
                lib.returnItem(iid);
                System.out.println("Returned if active");
            } else if (c.equals("7")) {
                System.out.print("User id: "); int uid = Integer.parseInt(br.readLine());
                System.out.print("Amount: "); int amt = Integer.parseInt(br.readLine());
                try { lib.payFine(uid, amt); System.out.println("Paid"); } catch (Exception ex) { System.out.println("Error"); }
            } else if (c.equals("8")) {
                System.out.print("User id: "); int uid = Integer.parseInt(br.readLine());
                try { System.out.println("Outstanding=" + lib.outstandingFines(uid)); } catch (Exception ex) { System.out.println("Error"); }
            } else if (c.equals("9")) {
                EmailServer server = new EmailServer() { public void send(String to, String subject, String body) { System.out.println("Email -> " + to + " | " + subject + " | " + body); } };
                EmailNotifier notifier = new EmailNotifier(server);
                reminders.sendReminders(notifier);
                System.out.println("Reminders sent");
            }
        }
    }

    private static void userLoginFlow(BufferedReader br, LibraryService lib, SessionManager sessions, InMemoryUserRepository users, InMemoryLoanRepository loans) throws Exception {

        System.out.println("User Menu");

        System.out.print("User id: ");
        String s = br.readLine();
        if (s == null) return;
        int uid;
        try { uid = Integer.parseInt(s.trim()); } catch (Exception e) { System.out.println("Invalid id"); return; }
        User u = users.findById(uid);
        if (u == null) { System.out.println("Not found"); return; }
        sessions.userLogin(uid);
        while (true) {

            System.out.println("User Menu");

            System.out.println("1. Search for Media");
            System.out.println("2. Borrow Book");
            System.out.println("3. Borrow for N days");
            System.out.println("4. Borrow CD");
            System.out.println("5. View My Loans");
            System.out.println("6. Pay My Fines");
            System.out.println("7. Logout");

            System.out.println("------------------------------------");
            System.out.print("Enter choice: ");
            String c = br.readLine();
            if (c == null) break;
            c = c.trim();
            if (c.equals("7")) { sessions.userLogout(); break; }
            if (c.equals("1")) {
                System.out.print("Query: "); String q = br.readLine();
                List<Media> results = lib.search(q);
                for (Media m : results) System.out.println(m.getType() + " | id=" + m.getId() + " | title=" + m.getTitle());
            } else if (c.equals("2") || c.equals("4")) {
                System.out.print("Item id: "); int iid = Integer.parseInt(br.readLine());
                try { Loan l = lib.borrowItem(uid, iid); System.out.println("Borrowed, due=" + l.getDueDate()); } catch (BorrowingNotAllowedException ex) { System.out.println("Blocked"); } catch (Exception ex) { System.out.println("Error"); }
            } else if (c.equals("5")) {
                List<Loan> list = loans.findActiveByUserId(uid);
                for (Loan l : list) System.out.println("Loan id=" + l.getId() + " | item=" + l.getItemId() + " | due=" + l.getDueDate());
            } else if (c.equals("6")) {
                int due = lib.outstandingFines(uid);
                System.out.println("Outstanding=" + due);
                System.out.print("Amount: "); int amt = Integer.parseInt(br.readLine());
                try { lib.payFine(uid, amt); System.out.println("Paid"); } catch (Exception ex) { System.out.println("Error"); }
            } else if (c.equals("3")) {
                System.out.print("Item id: "); int iid = Integer.parseInt(br.readLine());
                System.out.print("Days: "); int days = Integer.parseInt(br.readLine());
                try { Loan l = lib.borrowItemForDays(uid, iid, days); System.out.println("Borrowed, due=" + l.getDueDate()); } catch (BorrowingNotAllowedException ex) { System.out.println("Blocked"); } catch (Exception ex) { System.out.println("Error"); }
            }
        }
    }

    private static void signUpMenu(BufferedReader br, LibraryService lib, InMemoryAdminRepository admins) throws Exception {
        while (true) {

            System.out.println("SIGN UP");

            System.out.println("1. Create User Account");
            System.out.println("2. Create Admin Account");
            System.out.println("3. Back");
            System.out.println("------------------------------------");
            System.out.print("Enter choice: ");
            String c = br.readLine();
            if (c == null) break;
            c = c.trim();
            if (c.equals("3")) break;
            if (c.equals("1")) {
                System.out.print("Name: "); String n = br.readLine();
                System.out.print("Email: "); String e = br.readLine();
                User u = lib.registerUser(n, e);
                System.out.println("User created. id=" + u.getId());
            } else if (c.equals("2")) {
                System.out.print("Username: "); String u = br.readLine();
                System.out.print("Password: "); String p = br.readLine();
                System.out.print("Email: "); String mail = br.readLine();
                String code = String.valueOf(100000 + (int)(Math.random()*900000));
                EmailServer smtp = new SmtpEmailServer(
                        "smtp.gmail.com", 587, true,
                        "shahd10121@gmail.com",
                        "mpmv nrmp jqdz wsia"
                );
                String subject = "Admin Verification Code";
                String body = "Your verification code is: " + code + "\nIf you did not request this, ignore this email.";
                try { smtp.send(mail, subject, body); System.out.println("Verification code sent to " + mail); }
                catch (RuntimeException ex) { System.out.println("Failed to send email: " + ex.getMessage()); continue; }
                boolean ok = false;
                for (int attempts = 1; attempts <= 2; attempts++) {
                    System.out.print("Enter verification code (6 digits): ");
                    String entered = br.readLine();
                    if (entered != null) entered = entered.trim();
                    if (code.equals(entered)) { ok = true; break; }
                    else if (attempts < 2) System.out.println("Incorrect. Try again.");
                }
                if (!ok) {
                    System.out.println("Verification failed. Signup canceled.");
                } else {
                    admins.save(new Admin(u, p, mail));
                    System.out.println("Admin account created for '" + u + "'");
                }
            }
        }
    }
}
