package test;

public record Record2WithCompactCanonicalCtor(int foo, String bar) {
    public Record2WithCompactCanonicalCtor {
        if (foo < 0) {
            throw new IllegalArgumentException();
        }
        if (bar == null) {
            throw new IllegalArgumentException();
        }
    }
}
