package test;

public record RecordWithMultipleCtorsAndCustomCanonicalCtor(int foo, String bar) {
    public RecordWithMultipleCtorsAndCustomCanonicalCtor(int foo, String bar) {
        if (foo < 0) {
            throw new IllegalArgumentException();
        }
        if (bar == null) {
            throw new IllegalArgumentException();
        }

        this.foo = foo;
        this.bar = bar;
    }

    public RecordWithMultipleCtorsAndCustomCanonicalCtor(int foo) {
        this(foo, "");
    }

    public RecordWithMultipleCtorsAndCustomCanonicalCtor(String bar) {
        this(0, bar);
    }

    public RecordWithMultipleCtorsAndCustomCanonicalCtor(String bar, int foo) {
        this(foo, bar);
    }
}
