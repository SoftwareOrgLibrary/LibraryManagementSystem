package libmng.repo;

import java.util.ArrayList;
import java.util.List;

import libmng.domain.Book;
import libmng.domain.CD;
import libmng.domain.Media;

public class InMemoryMediaRepository implements MediaRepository {
    private final List<Media> items = new ArrayList<>();

    @Override
    public void saveBook(Book book) {
        Media existing = findById(book.getId());
        if (existing == null) items.add(book);
        else replace(existing, book);
    }

    @Override
    public void saveCD(CD cd) {
        Media existing = findById(cd.getId());
        if (existing == null) items.add(cd);
        else replace(existing, cd);
    }

    private void replace(Media oldItem, Media newItem) {
        int idx = -1;
        for (int i = 0; i < items.size(); i++) if (items.get(i).getId() == oldItem.getId()) { idx = i; break; }
        if (idx >= 0) items.set(idx, newItem);
    }

    @Override
    public Media findById(int id) {
        for (Media m : items) if (m.getId() == id) return m;
        return null;
    }

    @Override
    public Book findBookByIsbn(String isbn) {
        for (Media m : items) {
            if (m instanceof Book) {
                Book b = (Book) m;
                if (b.getIsbn() != null && b.getIsbn().equals(isbn)) return b;
            }
        }
        return null;
    }

    @Override
    public List<Media> search(String query) {
        String q = query.toLowerCase();
        List<Media> results = new ArrayList<>();
        for (Media m : items) {
            if (m instanceof Book) {
                Book b = (Book) m;
                if ((b.getTitle() != null && b.getTitle().toLowerCase().contains(q))
                        || (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(q))
                        || (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(q))) {
                    results.add(b);
                }
            } else if (m instanceof CD) {
                CD c = (CD) m;
                if ((c.getTitle() != null && c.getTitle().toLowerCase().contains(q))
                        || (c.getArtist() != null && c.getArtist().toLowerCase().contains(q))) {
                    results.add(c);
                }
            }
        }
        return results;
    }
}
