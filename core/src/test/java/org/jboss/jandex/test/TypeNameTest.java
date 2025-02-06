package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.UnresolvedTypeVariable;
import org.junit.jupiter.api.Test;

public class TypeNameTest {
    interface TestMethods<X> {
        void nothing();

        int primitive();

        String clazz();

        List<String> parameterized();

        String[][] array();

        List<? extends Number>[] genericArray();

        // annotations are present to make sure the `ArrayType` has multiple levels of nesting
        @MyAnnotation("1")
        String[] @MyAnnotation("2") [][] @MyAnnotation("3") [] annotatedArray();

        X typeParameter();

        <Y extends Number> Y typeParameterWithSingleBound();

        <Y extends Number & Comparable<Y>> Y typeParameterWithMultipleBounds();

        <Y extends Comparable<Y>> Y typeParameterWithSingleParameterizedBound();

        <Y extends Comparable<Y> & Serializable> Y typeParameterWithMultipleBoundsFirstParameterized();

        <Y extends Serializable & Comparable<Y>> Y typeParameterWithMultipleBoundsSecondParameterized();

        List<?> unboundedWildcard();

        List<? extends Number> wildcardWithUpperBound();

        List<? super String> wildcardWithLowerBound();

        List<? extends X> wildcardWithUnboundedTypeParameterAsUpperBound();

        <Y extends Number> List<? extends Y> wildcardWithBoundedTypeParameterAsUpperBound();

        <Y extends Number> List<? super Y> wildcardWithBoundedTypeParameterAsLowerBound();
    }

    static class NestedClass<T> {
        class InnerClass<U extends T> {
        }
    }

    @Test
    public void test() throws IOException {
        // intentionally _not_ indexing `NestedClass`, so that the type parameter bound of `InnerClass` is unresolved
        Index index = Index.of(TestMethods.class, NestedClass.InnerClass.class);

        ClassInfo clazz = index.getClassByName(TestMethods.class);
        assertEquals("void", typeName(clazz, "nothing"));
        assertEquals("int", typeName(clazz, "primitive"));
        assertEquals("java.lang.String", typeName(clazz, "clazz"));
        assertEquals("java.util.List", typeName(clazz, "parameterized"));
        assertEquals("[[Ljava.lang.String;", typeName(clazz, "array"));
        assertEquals("[Ljava.util.List;", typeName(clazz, "genericArray"));
        assertEquals("[[[[Ljava.lang.String;", typeName(clazz, "annotatedArray"));
        assertEquals("java.lang.Object", typeName(clazz, "typeParameter"));
        assertEquals("java.lang.Number", typeName(clazz, "typeParameterWithSingleBound"));
        assertEquals("java.lang.Number", typeName(clazz, "typeParameterWithMultipleBounds"));
        assertEquals("java.lang.Comparable", typeName(clazz, "typeParameterWithSingleParameterizedBound"));
        assertEquals("java.lang.Comparable", typeName(clazz, "typeParameterWithMultipleBoundsFirstParameterized"));
        assertEquals("java.io.Serializable", typeName(clazz, "typeParameterWithMultipleBoundsSecondParameterized"));
        assertEquals("java.lang.Object", firstTypeArgumentName(clazz, "unboundedWildcard"));
        assertEquals("java.lang.Number", firstTypeArgumentName(clazz, "wildcardWithUpperBound"));
        assertEquals("java.lang.Object", firstTypeArgumentName(clazz, "wildcardWithLowerBound"));
        assertEquals("java.lang.Object", firstTypeArgumentName(clazz, "wildcardWithUnboundedTypeParameterAsUpperBound"));
        assertEquals("java.lang.Number", firstTypeArgumentName(clazz, "wildcardWithBoundedTypeParameterAsUpperBound"));
        assertEquals("java.lang.Object", firstTypeArgumentName(clazz, "wildcardWithBoundedTypeParameterAsLowerBound"));

        TypeVariable u = index.getClassByName(NestedClass.InnerClass.class).typeParameters().get(0);
        assertEquals("java.lang.Object", u.name().toString());
        assertEquals(Type.Kind.UNRESOLVED_TYPE_VARIABLE, u.bounds().get(0).kind());
        UnresolvedTypeVariable t = u.bounds().get(0).asUnresolvedTypeVariable();
        assertEquals("java.lang.Object", t.name().toString());
    }

    private String typeName(ClassInfo clazz, String method) {
        return clazz.firstMethod(method).returnType().name().toString();
    }

    private String firstTypeArgumentName(ClassInfo clazz, String method) {
        return clazz.firstMethod(method).returnType().asParameterizedType().arguments().get(0).name().toString();
    }
}
