package libmng.time;

import java.time.LocalDate;

public interface TimeProvider {
    LocalDate now();
}

