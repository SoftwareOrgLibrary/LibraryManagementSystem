package libmng.domain;

import java.util.Objects;
import libmng.util.IdGenerator;

public class CD implements Media {
    private final int id;
    private final String title;
    private final String artist;

    public CD(String title, String artist) {
        this.id = IdGenerator.nextId();
        this.title = title;
        this.artist = artist;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public MediaType getType() {
        return MediaType.CD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CD cd = (CD) o;
        return id == cd.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
