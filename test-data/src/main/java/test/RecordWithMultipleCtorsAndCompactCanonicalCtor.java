package test;

public record RecordWithMultipleCtorsAndCompactCanonicalCtor(int foo, String bar) {
    public RecordWithMultipleCtorsAndCompactCanonicalCtor {
        if (foo < 0) {
            throw new IllegalArgumentException();
        }
        if (bar == null) {
            throw new IllegalArgumentException();
        }
    }

    public RecordWithMultipleCtorsAndCompactCanonicalCtor(int foo) {
        this(foo, "");
    }

    public RecordWithMultipleCtorsAndCompactCanonicalCtor(String bar) {
        this(0, bar);
    }

    public RecordWithMultipleCtorsAndCompactCanonicalCtor(String bar, int foo) {
        this(foo, bar);
    }
}
