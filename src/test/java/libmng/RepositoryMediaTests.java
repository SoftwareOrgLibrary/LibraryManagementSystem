package libmng;

import libmng.domain.Book;
import libmng.domain.CD;
import libmng.domain.Media;
import libmng.repo.InMemoryMediaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RepositoryMediaTests {
    @Test
    void saveFindSearchReplaceAndNotFound() {
        InMemoryMediaRepository repo = new InMemoryMediaRepository();
        Book b = new Book("t","a","i");
        repo.saveBook(b);
        repo.saveBook(b);
        Assertions.assertEquals(b, repo.findById(b.getId()));
        Assertions.assertEquals(b, repo.findBookByIsbn("i"));
        Assertions.assertNull(repo.findBookByIsbn("x"));
        List<Media> results = repo.search("t");
        Assertions.assertTrue(results.contains(b));
        CD c = new CD("cd","artist");
        repo.saveCD(c);
        repo.saveCD(c);
        Assertions.assertEquals(c, repo.findById(c.getId()));
        Assertions.assertTrue(repo.search("artist").contains(c));
        Assertions.assertTrue(repo.search("nope").isEmpty());
        Assertions.assertNull(repo.findById(9999));
    }

    @Test
    void bookSearchWithNullFieldsAndCdNullArtist() {
        InMemoryMediaRepository repo = new InMemoryMediaRepository();
        Book b1 = new Book(null, "AuthorOnly", null);
        Book b2 = new Book("TitleOnly", null, null);
        Book b3 = new Book(null, null, "ISBNONLY");
        repo.saveBook(b1);
        repo.saveBook(b2);
        repo.saveBook(b3);
        Assertions.assertTrue(repo.search("authoronly").contains(b1));
        Assertions.assertTrue(repo.search("titleonly").contains(b2));
        Assertions.assertTrue(repo.search("isbnonly").contains(b3));
        CD c = new CD("CDTitle", null);
        repo.saveCD(c);
        Assertions.assertTrue(repo.search("cdtitle").contains(c));
        Assertions.assertTrue(repo.search("nope").isEmpty());
    }

    @Test
    void findBookByIsbnSingle() {
        InMemoryMediaRepository repo = new InMemoryMediaRepository();
        Book b = new Book("t", "a", "ISBN-1");
        repo.saveBook(b);
        Assertions.assertEquals(b, repo.findBookByIsbn("ISBN-1"));
    }
}

