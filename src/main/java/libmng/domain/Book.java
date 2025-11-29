package libmng.domain;

import java.util.Objects;
import libmng.util.IdGenerator;

public class Book implements Media {
    private final int id;
    private final String title;
    private final String author;
    private final String isbn;

    public Book(String title, String author, String isbn) {
        this.id = IdGenerator.nextId();
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    @Override
    public MediaType getType() {
        return MediaType.BOOK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return id == book.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
