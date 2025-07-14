package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeAnnotationOnNestedTypeTest {
    @Test
    public void test() throws IOException {
        Indexer indexer = new Indexer();
        indexer.index(TypeAnnotationOnNestedTypeTest.class.getResourceAsStream("/test/NestedExample.class"));
        indexer.index(TypeAnnotationOnNestedTypeTest.class.getResourceAsStream("/test/NestedInNestedExample.class"));
        Index index = indexer.complete();

        test(index);
        test(IndexingUtil.roundtrip(index));
    }

    private void test(Index index) {
        ClassInfo nested = index.getClassByName("test.NestedExample");

        assertType(nested, "f1", "test.NestedExample$A<java.lang.String>");
        assertType(nested, "f2", "test.NestedExample$A<java.lang.String>.B");
        assertType(nested, "f3", "test.NestedExample$A$B$C");
        assertType(nested, "f4", "test.NestedExample$A$B$D<java.lang.String>");
        assertType(nested, "f5", "test.NestedExample$A$B$E");
        assertType(nested, "f6", "test.NestedExample$A<java.lang.String>.B.F<java.lang.String>");
        assertType(nested, "f7", "test.NestedExample$A<java.lang.String>.G<java.lang.String>");
        assertType(nested, "f8", "test.NestedExample$A$G$H");
        assertType(nested, "f9", "test.NestedExample$A$G$I<java.lang.String>");
        assertType(nested, "f10", "test.NestedExample$A<java.lang.String>.G<java.lang.String>.J");
        assertType(nested, "f11", "test.NestedExample$A<java.lang.String>.G<java.lang.String>.K<java.lang.String>");
        assertType(nested, "f12", "test.NestedExample$A$L");
        assertType(nested, "f13", "test.NestedExample$A$L$M");
        assertType(nested, "f14", "test.NestedExample$A$L$N<java.lang.String>");
        assertType(nested, "f15", "test.NestedExample$A$L$O");
        assertType(nested, "f16", "test.NestedExample$A$L$P<java.lang.String>");
        assertType(nested, "f17", "test.NestedExample$A$Q<java.lang.String>");
        assertType(nested, "f18", "test.NestedExample$A$Q$R");
        assertType(nested, "f19", "test.NestedExample$A$Q$S<java.lang.String>");
        assertType(nested, "f20", "test.NestedExample$A$Q<java.lang.String>.T");
        assertType(nested, "f21", "test.NestedExample$A$Q<java.lang.String>.U<java.lang.String>");

        assertType(nested, "af1",
                "test.@TA1(1) @TA2(2) NestedExample$A<java.lang.@TA1(3) @TA2(4) String>");
        assertType(nested, "af2",
                "test.@TA1(5) @TA2(6) NestedExample$A<java.lang.@TA1(7) @TA2(8) String>.@TA1(9) @TA2(10) B");
        assertType(nested, "af3",
                "test.@TA1(11) @TA2(12) NestedExample$A$B$C");
        assertType(nested, "af4",
                "test.@TA1(13) @TA2(14) NestedExample$A$B$D<java.lang.@TA1(15) @TA2(16) String>");
        assertType(nested, "af5",
                "test.@TA1(17) @TA2(18) NestedExample$A<java.lang.@TA1(19) @TA2(20) String>.@TA1(21) @TA2(22) B.@TA1(23) @TA2(24) E");
        assertType(nested, "af6",
                "test.@TA1(25) @TA2(26) NestedExample$A<java.lang.@TA1(27) @TA2(28) String>.@TA1(29) @TA2(30) B.@TA1(31) @TA2(32) F<java.lang.@TA1(33) @TA2(34) String>");
        assertType(nested, "af7",
                "test.@TA1(35) @TA2(36) NestedExample$A<java.lang.@TA1(37) @TA2(38) String>.@TA1(39) @TA2(40) G<java.lang.@TA1(41) @TA2(42) String>");
        assertType(nested, "af8",
                "test.@TA1(43) @TA2(44) NestedExample$A$G$H");
        assertType(nested, "af9",
                "test.@TA1(45) @TA2(46) NestedExample$A$G$I<java.lang.@TA1(47) @TA2(48) String>");
        assertType(nested, "af10",
                "test.@TA1(49) @TA2(50) NestedExample$A<java.lang.@TA1(51) @TA2(52) String>.@TA1(53) @TA2(54) G<java.lang.@TA1(55) @TA2(56) String>.@TA1(57) @TA2(58) J");
        assertType(nested, "af11",
                "test.@TA1(59) @TA2(60) NestedExample$A<java.lang.@TA1(61) @TA2(62) String>.@TA1(63) @TA2(64) G<java.lang.@TA1(65) @TA2(66) String>.@TA1(67) @TA2(68) K<java.lang.@TA1(69) @TA2(70) String>");
        assertType(nested, "af12",
                "test.@TA1(71) @TA2(72) NestedExample$A$L");
        assertType(nested, "af13",
                "test.@TA1(73) @TA2(74) NestedExample$A$L$M");
        assertType(nested, "af14",
                "test.@TA1(75) @TA2(76) NestedExample$A$L$N<java.lang.@TA1(77) @TA2(78) String>");
        assertType(nested, "af15",
                "test.@TA1(79) @TA2(80) NestedExample$A$L.@TA1(81) @TA2(82) O");
        assertType(nested, "af16",
                "test.@TA1(83) @TA2(84) NestedExample$A$L.@TA1(85) @TA2(86) P<java.lang.@TA1(87) @TA2(88) String>");
        assertType(nested, "af17",
                "test.@TA1(89) @TA2(90) NestedExample$A$Q<java.lang.@TA1(91) @TA2(92) String>");
        assertType(nested, "af18",
                "test.@TA1(93) @TA2(94) NestedExample$A$Q$R");
        assertType(nested, "af19",
                "test.@TA1(95) @TA2(96) NestedExample$A$Q$S<java.lang.@TA1(97) @TA2(98) String>");
        assertType(nested, "af20",
                "test.@TA1(99) @TA2(100) NestedExample$A$Q<java.lang.@TA1(101) @TA2(102) String>.T");
        assertType(nested, "af21",
                "test.@TA1(103) @TA2(104) NestedExample$A$Q<java.lang.@TA1(105) @TA2(106) String>.@TA1(107) @TA2(108) U<java.lang.@TA1(109) @TA2(110) String>");

        ClassInfo nestedInNested = index.getClassByName("test.NestedInNestedExample");

        assertType(nestedInNested, "f",
                "test.NestedInNestedExample$A<test.NestedInNestedExample$D<java.lang.String>.E<java.lang.String>.F<java.lang.String>>.B<test.NestedInNestedExample$D<java.lang.String>.E<java.lang.String>.F<java.lang.String>>.C<test.NestedInNestedExample$D<java.lang.String>.E<java.lang.String>.F<java.lang.String>>");

        assertType(nestedInNested, "af",
                "test.@TA1(1) @TA2(2) NestedInNestedExample$A<test.@TA1(3) @TA2(4) NestedInNestedExample$D<java.lang.@TA1(5) @TA2(6) String>.@TA1(7) @TA2(8) E<java.lang.@TA1(9) @TA2(10) String>.@TA1(11) @TA2(12) F<java.lang.@TA1(13) @TA2(14) String>>.@TA1(15) @TA2(16) B<test.@TA1(17) @TA2(18) NestedInNestedExample$D<java.lang.@TA1(19) @TA2(20) String>.@TA1(21) @TA2(22) E<java.lang.@TA1(23) @TA2(24) String>.@TA1(25) @TA2(26) F<java.lang.@TA1(27) @TA2(28) String>>.@TA1(29) @TA2(30) C<test.@TA1(31) @TA2(32) NestedInNestedExample$D<java.lang.@TA1(33) @TA2(34) String>.@TA1(35) @TA2(36) E<java.lang.@TA1(37) @TA2(38) String>.@TA1(39) @TA2(40) F<java.lang.@TA1(41) @TA2(42) String>>");
    }

    private void assertType(ClassInfo clazz, String fieldName, String str) {
        assertEquals(str, clazz.field(fieldName).type().toString());
    }
}
