package test;


import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RepeatableAnnotationsExample {

    @Repeatable(AlphaContainer.class)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Alpha {

        int value();

    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface AlphaContainer {

        Alpha[] value();

    }

    @Alpha(0)
    static class MyAnnotated {

        @Alpha(-1)
        @Alpha(-2)
        int myField;

        @Alpha(-3)
        int anotherField;

        @Alpha(1)
        public void foo(@Alpha(11) @Alpha(12) String fooName) {
        }

        @Alpha(2)
        @Alpha(3)
        public void bar(@Alpha(10) String barName) {
        }

    }
}
