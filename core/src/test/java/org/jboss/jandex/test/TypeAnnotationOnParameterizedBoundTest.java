package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeAnnotationOnParameterizedBoundTest {
    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TypeAnn {
        String value();
    }

    static class ClassWithRecursiveTypeParameter<@TypeAnn("rec:1") T extends @TypeAnn("rec:2") ClassWithRecursiveTypeParameter<@TypeAnn("rec:3") T>> {
    }

    static class ParameterizedClass<T> {
        class InnerParameterizedClass<U extends T> {
        }
    }

    static class ClassWithParameterizedTypeParameterBound<@TypeAnn("param:1") T extends @TypeAnn("param:2") ParameterizedClass<@TypeAnn("param:3") ParameterizedClass<@TypeAnn("param:4") String>>> {
    }

    static class ClassWithInnerParameterizedTypeParameterBound<@TypeAnn("inner:1") T extends @TypeAnn("inner:2") ParameterizedClass<@TypeAnn("inner:3") Number>.@TypeAnn("inner:4") InnerParameterizedClass<@TypeAnn("inner:5") Integer>> {
    }

    static class A<@TypeAnn("a:1") T extends @TypeAnn("a:2") List<@TypeAnn("a:3") String>> {
    }

    static class B<@TypeAnn("b:1") T extends @TypeAnn("b:2") List<@TypeAnn("b:3") ? extends @TypeAnn("b:4") Number>> {
    }

    static class C<@TypeAnn("c:1") T extends @TypeAnn("c:2") List<@TypeAnn("c:3") ? super @TypeAnn("c:4") Number>> {
    }

    static class D<@TypeAnn("d:1") T extends @TypeAnn("d:2") List<@TypeAnn("d:3") ? extends @TypeAnn("d:4") List<@TypeAnn("d:5") ? super @TypeAnn("d:6") Number>>> {
    }

    static class E<@TypeAnn("e:1") T extends @TypeAnn("e:2") List<@TypeAnn("e:3") ? super @TypeAnn("e:4") List<@TypeAnn("e:5") ? extends @TypeAnn("e:6") Number>>> {
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(ClassWithRecursiveTypeParameter.class, ClassWithParameterizedTypeParameterBound.class,
                ClassWithInnerParameterizedTypeParameterBound.class, A.class, B.class, C.class, D.class, E.class);
        test(index);
        test(IndexingUtil.roundtrip(index));
    }

    private void test(Index index) {
        ClassInfo classWithRecursiveTypeParam = index.getClassByName(ClassWithRecursiveTypeParameter.class);
        assertEquals(
                "@TypeAnn(\"rec:1\") T extends org.jboss.jandex.test.@TypeAnn(\"rec:2\") TypeAnnotationOnParameterizedBoundTest$ClassWithRecursiveTypeParameter<@TypeAnn(\"rec:3\") T>",
                classWithRecursiveTypeParam.typeParameters().get(0).toString());

        ClassInfo classWithParameterizedBound = index.getClassByName(ClassWithParameterizedTypeParameterBound.class);
        assertEquals(
                "@TypeAnn(\"param:1\") T extends org.jboss.jandex.test.@TypeAnn(\"param:2\") TypeAnnotationOnParameterizedBoundTest$ParameterizedClass<org.jboss.jandex.test.@TypeAnn(\"param:3\") TypeAnnotationOnParameterizedBoundTest$ParameterizedClass<java.lang.@TypeAnn(\"param:4\") String>>",
                classWithParameterizedBound.typeParameters().get(0).toString());

        ClassInfo classWithInnerParameterizedBound = index.getClassByName(ClassWithInnerParameterizedTypeParameterBound.class);
        assertEquals(
                "@TypeAnn(\"inner:1\") T extends org.jboss.jandex.test.@TypeAnn(\"inner:2\") TypeAnnotationOnParameterizedBoundTest$ParameterizedClass<java.lang.@TypeAnn(\"inner:3\") Number>.@TypeAnn(\"inner:4\") InnerParameterizedClass<java.lang.@TypeAnn(\"inner:5\") Integer>",
                classWithInnerParameterizedBound.typeParameters().get(0).toString());

        ClassInfo a = index.getClassByName(A.class);
        assertEquals(
                "@TypeAnn(\"a:1\") T extends java.util.@TypeAnn(\"a:2\") List<java.lang.@TypeAnn(\"a:3\") String>",
                a.typeParameters().get(0).toString());

        ClassInfo b = index.getClassByName(B.class);
        assertEquals(
                "@TypeAnn(\"b:1\") T extends java.util.@TypeAnn(\"b:2\") List<@TypeAnn(\"b:3\") ? extends java.lang.@TypeAnn(\"b:4\") Number>",
                b.typeParameters().get(0).toString());

        ClassInfo c = index.getClassByName(C.class);
        assertEquals(
                "@TypeAnn(\"c:1\") T extends java.util.@TypeAnn(\"c:2\") List<@TypeAnn(\"c:3\") ? super java.lang.@TypeAnn(\"c:4\") Number>",
                c.typeParameters().get(0).toString());

        ClassInfo d = index.getClassByName(D.class);
        assertEquals(
                "@TypeAnn(\"d:1\") T extends java.util.@TypeAnn(\"d:2\") List<@TypeAnn(\"d:3\") ? extends java.util.@TypeAnn(\"d:4\") List<@TypeAnn(\"d:5\") ? super java.lang.@TypeAnn(\"d:6\") Number>>",
                d.typeParameters().get(0).toString());

        ClassInfo e = index.getClassByName(E.class);
        assertEquals(
                "@TypeAnn(\"e:1\") T extends java.util.@TypeAnn(\"e:2\") List<@TypeAnn(\"e:3\") ? super java.util.@TypeAnn(\"e:4\") List<@TypeAnn(\"e:5\") ? extends java.lang.@TypeAnn(\"e:6\") Number>>",
                e.typeParameters().get(0).toString());
    }
}
