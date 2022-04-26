package test;

// supplement to org.jboss.jandex.test.ImplicitDeclarationsExample
public class ImplicitDeclarationsExampleSupplement {
    public record SimpleRecord(@MyAnnotation("record: str") String str, @MyAnnotation("record: num") int num) {
        // implicitly declares a canonical constructor, component fields and accessor methods
    }

    public record RecordWithCompactConstructor(@MyAnnotation("record: str") String str, @MyAnnotation("record: num") int num) {
        // compact constructor implicitly declares parameters
        public RecordWithCompactConstructor {
        }
    }
}
