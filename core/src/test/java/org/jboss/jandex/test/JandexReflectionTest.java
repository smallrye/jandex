package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.JandexReflection;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.UnresolvedTypeVariable;
import org.junit.jupiter.api.Test;

public class JandexReflectionTest {
    interface TestMethods<X> {
        void nothing();

        int primitive();

        String clazz();

        List<String> parameterized();

        String[][] array();

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

    static class SimpleClass {
    }

    @Test
    public void test() throws IOException {
        // intentionally _not_ indexing `NestedClass`, so that the type parameter bound of `InnerClass` is unresolved
        Index index = Index.of(TestMethods.class, NestedClass.InnerClass.class, SimpleClass.class);

        ClassInfo clazz = index.getClassByName(TestMethods.class);
        assertEquals(void.class, type(clazz, "nothing"));
        assertEquals(int.class, type(clazz, "primitive"));
        assertEquals(String.class, type(clazz, "clazz"));
        assertEquals(List.class, type(clazz, "parameterized"));
        assertEquals(String[][].class, type(clazz, "array"));
        assertEquals(String[][][][].class, type(clazz, "annotatedArray"));
        assertEquals(Object.class, type(clazz, "typeParameter"));
        assertEquals(Number.class, type(clazz, "typeParameterWithSingleBound"));
        assertEquals(Number.class, type(clazz, "typeParameterWithMultipleBounds"));
        assertEquals(Comparable.class, type(clazz, "typeParameterWithSingleParameterizedBound"));
        assertEquals(Comparable.class, type(clazz, "typeParameterWithMultipleBoundsFirstParameterized"));
        assertEquals(Serializable.class, type(clazz, "typeParameterWithMultipleBoundsSecondParameterized"));
        assertEquals(Object.class, firstTypeArgument(clazz, "unboundedWildcard"));
        assertEquals(Number.class, firstTypeArgument(clazz, "wildcardWithUpperBound"));
        assertEquals(Object.class, firstTypeArgument(clazz, "wildcardWithLowerBound"));
        assertEquals(Object.class, firstTypeArgument(clazz, "wildcardWithUnboundedTypeParameterAsUpperBound"));
        assertEquals(Number.class, firstTypeArgument(clazz, "wildcardWithBoundedTypeParameterAsUpperBound"));
        assertEquals(Object.class, firstTypeArgument(clazz, "wildcardWithBoundedTypeParameterAsLowerBound"));

        TypeVariable u = index.getClassByName(NestedClass.InnerClass.class).typeParameters().get(0);
        assertEquals(Type.Kind.UNRESOLVED_TYPE_VARIABLE, u.bounds().get(0).kind());
        UnresolvedTypeVariable t = u.bounds().get(0).asUnresolvedTypeVariable();
        assertEquals(Object.class, JandexReflection.loadRawType(t));

        assertEquals(SimpleClass.class, JandexReflection.loadClass(index.getClassByName(SimpleClass.class)));
    }

    private Class<?> type(ClassInfo clazz, String method) {
        Type jandexType = clazz.firstMethod(method).returnType();
        return JandexReflection.loadRawType(jandexType);
    }

    private Class<?> firstTypeArgument(ClassInfo clazz, String method) {
        Type jandexType = clazz.firstMethod(method).returnType().asParameterizedType().arguments().get(0);
        return JandexReflection.loadRawType(jandexType);
    }
}
