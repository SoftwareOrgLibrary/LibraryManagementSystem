package libmng.time;

import java.time.LocalDate;

public class RealTimeProvider implements TimeProvider {
    @Override
    public LocalDate now() {
        return LocalDate.now();
    }
}

