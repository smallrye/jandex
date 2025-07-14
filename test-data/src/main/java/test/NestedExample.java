package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class NestedExample {
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

    static class A<X> {
        class B {
            static class C {
            }

            static class D<Y> {
            }

            class E {
            }

            class F<Y> {
            }
        }

        class G<Y> {
            static class H {
            }

            static class I<Z> {
            }

            class J {
            }

            class K<Z> {
            }
        }

        static class L {
            static class M {
            }

            static class N<Y> {
            }

            class O {
            }

            class P<Y> {
            }
        }

        static class Q<Y> {
            static class R {
            }

            static class S<Z> {
            }

            class T {
            }

            class U<Z> {
            }
        }
    }

    A<String> f1;
    A<String>.B f2;
    A.B.C f3;
    A.B.D<String> f4;
    A.B.E f5;
    A<String>.B.F<String> f6;
    A<String>.G<String> f7;
    A.G.H f8;
    A.G.I<String> f9;
    A<String>.G<String>.J f10;
    A<String>.G<String>.K<String> f11;
    A.L f12;
    A.L.M f13;
    A.L.N<String> f14;
    A.L.O f15;
    A.L.P<String> f16;
    A.Q<String> f17;
    A.Q.R f18;
    A.Q.S<String> f19;
    A.Q<String>.T f20;
    A.Q<String>.U<String> f21;

    // @formatter:off
    @TA1(1) @TA2(2) A<@TA1(3) @TA2(4) String> af1;
    @TA1(5) @TA2(6) A<@TA1(7) @TA2(8) String>.@TA1(9) @TA2(10) B af2;
    A.B.@TA1(11) @TA2(12) C af3;
    A.B.@TA1(13) @TA2(14) D<@TA1(15) @TA2(16) String> af4;
    @TA1(17) @TA2(18) A<@TA1(19) @TA2(20) String>.@TA1(21) @TA2(22) B.@TA1(23) @TA2(24) E af5;
    @TA1(25) @TA2(26) A<@TA1(27) @TA2(28) String>.@TA1(29) @TA2(30) B.@TA1(31) @TA2(32) F<@TA1(33) @TA2(34) String> af6;
    @TA1(35) @TA2(36) A<@TA1(37) @TA2(38) String>.@TA1(39) @TA2(40) G<@TA1(41) @TA2(42) String> af7;
    A.G.@TA1(43) @TA2(44) H af8;
    A.G.@TA1(45) @TA2(46) I<@TA1(47) @TA2(48) String> af9;
    @TA1(49) @TA2(50) A<@TA1(51) @TA2(52) String>.@TA1(53) @TA2(54) G<@TA1(55) @TA2(56) String>.@TA1(57) @TA2(58) J af10;
    @TA1(59) @TA2(60) A<@TA1(61) @TA2(62) String>.@TA1(63) @TA2(64) G<@TA1(65) @TA2(66) String>.@TA1(67) @TA2(68) K<@TA1(69) @TA2(70) String> af11;
    A.@TA1(71) @TA2(72) L af12;
    A.L.@TA1(73) @TA2(74) M af13;
    A.L.@TA1(75) @TA2(76) N<@TA1(77) @TA2(78) String> af14;
    A.@TA1(79) @TA2(80) L.@TA1(81) @TA2(82) O af15;
    A.@TA1(83) @TA2(84) L.@TA1(85) @TA2(86) P<@TA1(87) @TA2(88) String> af16;
    A.@TA1(89) @TA2(90) Q<@TA1(91) @TA2(92) String> af17;
    A.Q.@TA1(93) @TA2(94) R af18;
    A.Q.@TA1(95) @TA2(96) S<@TA1(97) @TA2(98) String> af19;
    A.@TA1(99) @TA2(100) Q<@TA1(101) @TA2(102) String>.T af20;
    A.@TA1(103) @TA2(104) Q<@TA1(105) @TA2(106) String>.@TA1(107) @TA2(108) U<@TA1(109) @TA2(110) String> af21;
    // @formatter:on
}
