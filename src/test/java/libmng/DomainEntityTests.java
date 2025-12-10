package libmng;

import libmng.domain.Book;
import libmng.domain.CD;
import libmng.domain.Loan;
import libmng.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

 class DomainEntityTests {
    @Test
    void bookEqualsAndHashVariants() {
        Book b1 = new Book("t","a","i");
        Assertions.assertTrue(b1.equals(b1));
        Book b2 = new Book("t","a","i");
        Assertions.assertFalse(b1.equals(b2));
        Assertions.assertNotEquals(b1.hashCode(), b2.hashCode());
        Assertions.assertFalse(b1.equals(null));
        Assertions.assertFalse(b1.equals("x"));
    }

    @Test
    void cdEqualsAndHashVariants() {
        CD c1 = new CD("t","a");
        Assertions.assertTrue(c1.equals(c1));
        CD c2 = new CD("t","a");
        Assertions.assertFalse(c1.equals(c2));
        Assertions.assertNotEquals(c1.hashCode(), c2.hashCode());
        Assertions.assertFalse(c1.equals(null));
        Assertions.assertFalse(c1.equals(1));
    }

    @Test
    void loanMarkReturned() {
        Loan l = new Loan(1, 2, LocalDate.of(2024,1,1), LocalDate.of(2024,1,10));
        Assertions.assertFalse(l.isReturned());
        l.markReturned();
        Assertions.assertTrue(l.isReturned());
    }

    @Test
    void userPaymentAccumulationAndValidation() {
        User u = new User("n","e");
        u.addPayment(5);
        u.addPayment(7);
        Assertions.assertEquals(12, u.getPaidFines());
        User u2 = new User("n2","e2");
        Assertions.assertThrows(IllegalArgumentException.class, () -> u2.addPayment(-1));
    }

    @Test
    void idSequenceIsSequentialWithinTest() {
        User u1 = new User("a", "a@e");
        User u2 = new User("b", "b@e");
        Assertions.assertEquals(u1.getId() + 1, u2.getId());
    }
}

