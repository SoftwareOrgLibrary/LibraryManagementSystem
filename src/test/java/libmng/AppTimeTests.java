package libmng;

import libmng.app.Main;
import libmng.time.RealTimeProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

 class AppTimeTests {
    @Test
    void runMain() {
        Main.main(new String[]{"print"});
    }

    @Test
    void returnsToday() {
        LocalDate today = LocalDate.now();
        Assertions.assertEquals(today, new RealTimeProvider().now());
    }
}
