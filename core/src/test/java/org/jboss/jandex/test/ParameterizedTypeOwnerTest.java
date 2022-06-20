package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class ParameterizedTypeOwnerTest {
    class A {
        class B {
        }
    }

    class C<T> {
        class D {
        }
    }

    class E<T extends E<T>> {
        class F {
        }

        class G extends E.F {
        }

        class H extends @MyAnnotation("e") E.F {
        }

        class I extends E.@MyAnnotation("f") F {
        }

        class J extends E<T>.F {
        }

        class K extends @MyAnnotation("e") E<T>.F {
        }

        class L extends E<@MyAnnotation("e.t") T>.F {
        }

        class M extends E<T>.@MyAnnotation("f") F {
        }
    }

    class EImpl extends E<EImpl> {
    }

    class N {
        class O<T> {
        }
    }

    private A.B aaa;
    private @MyAnnotation("a") A.B bbb;
    private A.@MyAnnotation("b") B ccc;

    private C<String>.D ddd;
    private @MyAnnotation("c") C<String>.D eee;
    private C<@MyAnnotation("c.t") String>.D fff;
    private C<String>.@MyAnnotation("d") D ggg;

    private E<EImpl>.F hhh;
    private @MyAnnotation("e") E<EImpl>.F iii;
    private E<@MyAnnotation("e.t") EImpl>.F jjj;
    private E<EImpl>.@MyAnnotation("f") F kkk;

    private N.O<String> lll;
    private @MyAnnotation("n") N.O<String> mmm;
    private N.@MyAnnotation("o") O<String> nnn;
    private N.O<@MyAnnotation("o.t") String> ooo;

    @Test
    public void test() throws IOException {
        Index index = Index.of(A.class, A.B.class, C.class, C.D.class, E.class, E.F.class, E.G.class, E.H.class,
                E.I.class, E.J.class, E.K.class, E.L.class, E.M.class, EImpl.class, ParameterizedTypeOwnerTest.class);

        test(index);
        test(IndexingUtil.roundtrip(index));
    }

    private void test(Index index) {
        ClassInfo clazz = index.getClassByName(ParameterizedTypeOwnerTest.class);

        Type aaa = clazz.field("aaa").type();
        assertEquals(Type.Kind.CLASS, aaa.kind());
        assertEquals("org.jboss.jandex.test.ParameterizedTypeOwnerTest$A$B", aaa.toString());

        Type bbb = clazz.field("bbb").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals("org.jboss.jandex.test.@MyAnnotation(\"a\") ParameterizedTypeOwnerTest$A.B", bbb.toString());
        assertNotNull(bbb.asParameterizedType().owner());
        assertEquals(Type.Kind.CLASS, bbb.asParameterizedType().owner().kind());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"a\") ParameterizedTypeOwnerTest$A",
                bbb.asParameterizedType().owner().toString());

        Type ccc = clazz.field("ccc").type();
        assertEquals(Type.Kind.CLASS, ccc.kind());
        assertEquals("org.jboss.jandex.test.@MyAnnotation(\"b\") ParameterizedTypeOwnerTest$A$B", ccc.toString());

        Type ddd = clazz.field("ddd").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals("org.jboss.jandex.test.ParameterizedTypeOwnerTest$C<java.lang.String>.D", ddd.toString());
        assertNotNull(ddd.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$C<java.lang.String>",
                ddd.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, ddd.asParameterizedType().owner().kind());
        assertNull(ddd.asParameterizedType().owner().asParameterizedType().owner());

        Type eee = clazz.field("eee").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"c\") ParameterizedTypeOwnerTest$C<java.lang.String>.D",
                eee.toString());
        assertNotNull(eee.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"c\") ParameterizedTypeOwnerTest$C<java.lang.String>",
                eee.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, eee.asParameterizedType().owner().kind());
        assertNull(eee.asParameterizedType().owner().asParameterizedType().owner());

        Type fff = clazz.field("fff").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$C<java.lang.@MyAnnotation(\"c.t\") String>.D",
                fff.toString());
        assertNotNull(fff.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$C<java.lang.@MyAnnotation(\"c.t\") String>",
                fff.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, fff.asParameterizedType().owner().kind());
        assertNull(fff.asParameterizedType().owner().asParameterizedType().owner());

        Type ggg = clazz.field("ggg").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$C<java.lang.String>.@MyAnnotation(\"d\") D",
                ggg.toString());
        assertNotNull(ggg.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$C<java.lang.String>",
                ggg.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, ggg.asParameterizedType().owner().kind());
        assertNull(ggg.asParameterizedType().owner().asParameterizedType().owner());

        Type hhh = clazz.field("hhh").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<org.jboss.jandex.test.ParameterizedTypeOwnerTest$EImpl>.F",
                hhh.toString());
        assertNotNull(hhh.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<org.jboss.jandex.test.ParameterizedTypeOwnerTest$EImpl>",
                hhh.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, hhh.asParameterizedType().owner().kind());
        assertNull(hhh.asParameterizedType().owner().asParameterizedType().owner());

        Type iii = clazz.field("iii").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"e\") ParameterizedTypeOwnerTest$E<org.jboss.jandex.test.ParameterizedTypeOwnerTest$EImpl>.F",
                iii.toString());
        assertNotNull(iii.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"e\") ParameterizedTypeOwnerTest$E<org.jboss.jandex.test.ParameterizedTypeOwnerTest$EImpl>",
                iii.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, iii.asParameterizedType().owner().kind());
        assertNull(iii.asParameterizedType().owner().asParameterizedType().owner());

        Type jjj = clazz.field("jjj").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<org.jboss.jandex.test.@MyAnnotation(\"e.t\") ParameterizedTypeOwnerTest$EImpl>.F",
                jjj.toString());
        assertNotNull(jjj.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<org.jboss.jandex.test.@MyAnnotation(\"e.t\") ParameterizedTypeOwnerTest$EImpl>",
                jjj.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, jjj.asParameterizedType().owner().kind());
        assertNull(jjj.asParameterizedType().owner().asParameterizedType().owner());

        Type kkk = clazz.field("kkk").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<org.jboss.jandex.test.ParameterizedTypeOwnerTest$EImpl>.@MyAnnotation(\"f\") F",
                kkk.toString());
        assertNotNull(kkk.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<org.jboss.jandex.test.ParameterizedTypeOwnerTest$EImpl>",
                kkk.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, kkk.asParameterizedType().owner().kind());
        assertNull(kkk.asParameterizedType().owner().asParameterizedType().owner());

        Type lll = clazz.field("lll").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$N$O<java.lang.String>",
                lll.toString());
        assertNull(lll.asParameterizedType().owner());

        Type mmm = clazz.field("mmm").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"n\") ParameterizedTypeOwnerTest$N.O<java.lang.String>",
                mmm.toString());
        assertNotNull(mmm.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"n\") ParameterizedTypeOwnerTest$N",
                mmm.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.CLASS, mmm.asParameterizedType().owner().kind());

        Type nnn = clazz.field("nnn").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"o\") ParameterizedTypeOwnerTest$N$O<java.lang.String>",
                nnn.toString());
        assertNull(nnn.asParameterizedType().owner());

        Type ooo = clazz.field("ooo").type();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bbb.kind());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$N$O<java.lang.@MyAnnotation(\"o.t\") String>",
                ooo.toString());
        assertNull(ooo.asParameterizedType().owner());

        // ---

        Type g = index.getClassByName(E.G.class).superClassType();
        assertEquals(Type.Kind.CLASS, g.kind());
        assertEquals("org.jboss.jandex.test.ParameterizedTypeOwnerTest$E$F", g.toString());

        Type h = index.getClassByName(E.H.class).superClassType();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, h.kind());
        assertEquals("org.jboss.jandex.test.@MyAnnotation(\"e\") ParameterizedTypeOwnerTest$E.F", h.toString());
        assertNotNull(h.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"e\") ParameterizedTypeOwnerTest$E",
                h.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.CLASS, h.asParameterizedType().owner().kind());

        Type i = index.getClassByName(E.I.class).superClassType();
        assertEquals(Type.Kind.CLASS, i.kind());
        assertEquals("org.jboss.jandex.test.@MyAnnotation(\"f\") ParameterizedTypeOwnerTest$E$F", i.toString());

        Type j = index.getClassByName(E.J.class).superClassType();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, j.kind());
        assertEquals("org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>.F", j.toString());
        assertNotNull(j.asParameterizedType().owner());
        assertEquals("org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>", j.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, j.asParameterizedType().owner().kind());
        assertNull(j.asParameterizedType().owner().asParameterizedType().owner());
        assertEquals(1, j.asParameterizedType().owner().asParameterizedType().arguments().size());
        assertEquals(
                "T extends org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>",
                j.asParameterizedType().owner().asParameterizedType().arguments().get(0).toString());
        assertEquals(Type.Kind.TYPE_VARIABLE, j.asParameterizedType().owner().asParameterizedType().arguments().get(0).kind());
        assertEquals(1, j.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().size());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, j.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).kind());
        assertEquals(1, j.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().size());
        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, j.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().get(0).kind());

        Type k = index.getClassByName(E.K.class).superClassType();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, k.kind());
        assertEquals("org.jboss.jandex.test.@MyAnnotation(\"e\") ParameterizedTypeOwnerTest$E<T>.F", k.toString());
        assertNotNull(k.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.@MyAnnotation(\"e\") ParameterizedTypeOwnerTest$E<T>",
                k.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, k.asParameterizedType().owner().kind());
        assertNull(k.asParameterizedType().owner().asParameterizedType().owner());
        assertEquals(1, k.asParameterizedType().owner().asParameterizedType().arguments().size());
        assertEquals(
                "T extends org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>",
                k.asParameterizedType().owner().asParameterizedType().arguments().get(0).toString());
        assertEquals(Type.Kind.TYPE_VARIABLE, k.asParameterizedType().owner().asParameterizedType().arguments().get(0).kind());
        assertEquals(1, k.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().size());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>",
                k.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                        .asTypeVariable().bounds().get(0).toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, k.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).kind());
        assertEquals(1, k.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().size());
        assertEquals("T", k.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().get(0).toString());
        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, k.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().get(0).kind());

        Type l = index.getClassByName(E.L.class).superClassType();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, l.kind());
        assertEquals("org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<@MyAnnotation(\"e.t\") T>.F", l.toString());
        assertNotNull(l.asParameterizedType().owner());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<@MyAnnotation(\"e.t\") T>",
                l.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, l.asParameterizedType().owner().kind());
        assertNull(l.asParameterizedType().owner().asParameterizedType().owner());
        assertEquals(1, l.asParameterizedType().owner().asParameterizedType().arguments().size());
        assertEquals(
                "@MyAnnotation(\"e.t\") T extends org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>",
                l.asParameterizedType().owner().asParameterizedType().arguments().get(0).toString());
        assertEquals(Type.Kind.TYPE_VARIABLE, l.asParameterizedType().owner().asParameterizedType().arguments().get(0).kind());
        assertEquals(1, l.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().size());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>",
                k.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                        .asTypeVariable().bounds().get(0).toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, l.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).kind());
        assertEquals(1, l.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().size());
        assertEquals("T", l.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().get(0).toString());
        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, l.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().get(0).kind());

        Type m = index.getClassByName(E.M.class).superClassType();
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, m.kind());
        assertEquals("org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>.@MyAnnotation(\"f\") F", m.toString());
        assertNotNull(m.asParameterizedType().owner());
        assertEquals("org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>", m.asParameterizedType().owner().toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, m.asParameterizedType().owner().kind());
        assertNull(m.asParameterizedType().owner().asParameterizedType().owner());
        assertEquals(1, m.asParameterizedType().owner().asParameterizedType().arguments().size());
        assertEquals(
                "T extends org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>",
                m.asParameterizedType().owner().asParameterizedType().arguments().get(0).toString());
        assertEquals(Type.Kind.TYPE_VARIABLE, m.asParameterizedType().owner().asParameterizedType().arguments().get(0).kind());
        assertEquals(1, m.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().size());
        assertEquals(
                "org.jboss.jandex.test.ParameterizedTypeOwnerTest$E<T>",
                k.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                        .asTypeVariable().bounds().get(0).toString());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, m.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).kind());
        assertEquals(1, m.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().size());
        assertEquals("T", m.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().get(0).toString());
        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, m.asParameterizedType().owner().asParameterizedType().arguments().get(0)
                .asTypeVariable().bounds().get(0).asParameterizedType().arguments().get(0).kind());
    }
}
