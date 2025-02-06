package test;

public record Record1WithCustomCanonicalCtor(String foo) {
    public Record1WithCustomCanonicalCtor(String foo) {
        if (foo == null) {
            throw new IllegalArgumentException();
        }

        this.foo = foo;
    }
}
