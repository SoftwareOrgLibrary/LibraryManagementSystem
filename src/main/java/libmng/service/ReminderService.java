package libmng.service;

import java.util.List;

import libmng.domain.Loan;
import libmng.domain.User;
import libmng.notify.Notifier;
import libmng.repo.LoanRepository;
import libmng.repo.MediaRepository;
import libmng.repo.UserRepository;
import libmng.time.TimeProvider;

public class ReminderService {
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final MediaRepository mediaRepository;
    private final TimeProvider timeProvider;

    public ReminderService(UserRepository userRepository, LoanRepository loanRepository, MediaRepository mediaRepository, TimeProvider timeProvider) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.mediaRepository = mediaRepository;
        this.timeProvider = timeProvider;
    }

    public void sendReminders(Notifier notifier) {
        List<User> users = userRepository.findAll();
        for (User u : users) {
            int count = 0;
            for (Loan l : loanRepository.findActiveByUserId(u.getId())) {
                if (timeProvider.now().isAfter(l.getDueDate())) count++;
            }
            if (count > 0) notifier.send(u, "You have " + count + " overdue book(s).");
        }
    }
}

