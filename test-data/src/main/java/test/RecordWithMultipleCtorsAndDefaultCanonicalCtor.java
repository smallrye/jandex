package test;

public record RecordWithMultipleCtorsAndDefaultCanonicalCtor(int foo, String bar) {
    RecordWithMultipleCtorsAndDefaultCanonicalCtor(int foo) {
        this(foo, "");
    }

    public RecordWithMultipleCtorsAndDefaultCanonicalCtor(String bar) {
        this(0, bar);
    }

    public RecordWithMultipleCtorsAndDefaultCanonicalCtor(String bar, int foo) {
        this(foo, bar);
    }
}
