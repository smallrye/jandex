package test;

public record RecordWithDifferentVisibilityAnnotation(@MyAnnotation("foo") int foo, @MyClassAnnotation("bar") String bar) {
}
