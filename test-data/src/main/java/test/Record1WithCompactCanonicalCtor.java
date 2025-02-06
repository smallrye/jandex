package test;

public record Record1WithCompactCanonicalCtor(String foo) {
    public Record1WithCompactCanonicalCtor {
        if (foo == null) {
            throw new IllegalArgumentException();
        }
    }
}
