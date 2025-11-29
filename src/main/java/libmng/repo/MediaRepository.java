package libmng.repo;

import java.util.List;

import libmng.domain.Book;
import libmng.domain.CD;
import libmng.domain.Media;

public interface MediaRepository {
    void saveBook(Book book);
    void saveCD(CD cd);
    Media findById(int id);
    Book findBookByIsbn(String isbn);
    List<Media> search(String query);
}
