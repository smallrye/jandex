package test;

public class InnerClassTypeAnnotationsExample {

    public class Other {
    }

    public class NoGenericsConstruct {
        private final Other o;
        private final byte b1;
        private final byte b2;

        NoGenericsConstruct(@Nullable Other o, byte b1, @Nullable byte b2) {
            this.o = o;
            this.b1 = b1;
            this.b2 = b2;
        }
    }

    public static class StaticNoGenericsConstruct {
        private final Other o;
        private final byte b1;
        private final byte b2;

        StaticNoGenericsConstruct(@Nullable Other o, byte b1, @Nullable byte b2) {
            this.o = o;
            this.b1 = b1;
            this.b2 = b2;
        }
    }

    public class GenericsConstruct {
        private final Other o;
        private final byte b1;
        private final byte b2;

        <T extends Other> GenericsConstruct(@Nullable T o, byte b1, byte b2) {
            this.o = o;
            this.b1 = b1;
            this.b2 = b2;
        }
    }
}
