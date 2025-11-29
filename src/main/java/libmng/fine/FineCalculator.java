package libmng.fine;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import libmng.domain.Loan;
import libmng.domain.Media;
import libmng.domain.MediaType;
import libmng.repo.MediaRepository;

public class FineCalculator {
    private final MediaRepository mediaRepository;
    private final FineStrategy bookStrategy = new BookFineStrategy();
    private final FineStrategy cdStrategy = new CDFineStrategy();

    public FineCalculator(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public int fineForLoan(Loan loan, LocalDate now) {
        if (!now.isAfter(loan.getDueDate())) return 0;
        long days = ChronoUnit.DAYS.between(loan.getDueDate(), now);
        Media media = mediaRepository.findById(loan.getItemId());
        if (media == null) return 0;
        MediaType type = media.getType();
        if (type == MediaType.BOOK) return bookStrategy.calculateFine((int) days);
        if (type == MediaType.CD) return cdStrategy.calculateFine((int) days);
        return 0;
    }
}
