package test;

public record Record2WithCustomCanonicalCtor(int foo, String bar) {
    public Record2WithCustomCanonicalCtor(int foo, String bar) {
        if (foo < 0) {
            throw new IllegalArgumentException();
        }
        if (bar == null) {
            throw new IllegalArgumentException();
        }

        this.foo = foo;
        this.bar = bar;
    }
}
