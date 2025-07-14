package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class NestedInNestedExample {
    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface TA1 {
        int value();
    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface TA2 {
        int value();
    }

    static class A<T> {
        class B<U> {
            class C<V> {
            }
        }
    }

    static class D<T> {
        class E<U> {
            class F<V> {
            }
        }
    }

    A<D<String>.E<String>.F<String>>.B<D<String>.E<String>.F<String>>.C<D<String>.E<String>.F<String>> f;

    // @formatter:off
    @TA1(1) @TA2(2) A<@TA1(3) @TA2(4) D<@TA1(5) @TA2(6) String>.@TA1(7) @TA2(8) E<@TA1(9) @TA2(10) String>.@TA1(11) @TA2(12) F<@TA1(13) @TA2(14) String>>
            .@TA1(15) @TA2(16) B<@TA1(17) @TA2(18) D<@TA1(19) @TA2(20) String>.@TA1(21) @TA2(22) E<@TA1(23) @TA2(24) String>.@TA1(25) @TA2(26) F<@TA1(27) @TA2(28) String>>
            .@TA1(29) @TA2(30) C<@TA1(31) @TA2(32) D<@TA1(33) @TA2(34) String>.@TA1(35) @TA2(36) E<@TA1(37) @TA2(38) String>.@TA1(39) @TA2(40) F<@TA1(41) @TA2(42) String>> af;
    // @formatter:on
}
