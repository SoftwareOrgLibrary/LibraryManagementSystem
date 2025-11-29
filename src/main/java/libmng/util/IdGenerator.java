package libmng.util;

public final class IdGenerator {
    private static int SEQ = 0;
    private IdGenerator() {}
    public static int nextId() {
        SEQ += 1;
        return SEQ;
    }
}

