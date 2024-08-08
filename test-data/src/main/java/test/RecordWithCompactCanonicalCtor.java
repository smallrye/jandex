package test;

public record RecordWithCompactCanonicalCtor(int foo, String bar) {
    public RecordWithCompactCanonicalCtor {
        if (foo < 0) {
            throw new IllegalArgumentException();
        }
        if (bar == null) {
            throw new IllegalArgumentException();
        }
    }
}
