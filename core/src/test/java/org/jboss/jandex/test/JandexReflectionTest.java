package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

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
    public void loadType() throws IOException {
        // intentionally _not_ indexing `NestedClass`, so that the type parameter bound of `InnerClass` is unresolved
        Index index = Index.of(TestMethods.class, NestedClass.InnerClass.class);

        ClassInfo clazz = index.getClassByName(TestMethods.class);
        assertEquals(typeReflective(clazz, "nothing"), type(clazz, "nothing"));
        assertEquals(typeReflective(clazz, "primitive"), type(clazz, "primitive"));
        assertEquals(typeReflective(clazz, "clazz"), type(clazz, "clazz"));
        assertEquals(typeReflective(clazz, "parameterized"), type(clazz, "parameterized"));
        assertEquals(typeReflective(clazz, "array"), type(clazz, "array"));
        assertEquals(typeReflective(clazz, "genericArray"), type(clazz, "genericArray"));
        assertEquals(typeReflective(clazz, "annotatedArray"), type(clazz, "annotatedArray"));
        assertTypeVariable(type(clazz, "typeParameter"), tv -> {
            assertEquals("X", tv.getName());
            assertEquals(1, tv.getBounds().length);
            assertEquals(Object.class, tv.getBounds()[0]);
        });
        assertTypeVariable(type(clazz, "typeParameterWithSingleBound"), tv -> {
            assertEquals("Y", tv.getName());
            assertEquals(1, tv.getBounds().length);
            assertEquals(Number.class, tv.getBounds()[0]);
        });
        assertTypeVariable(type(clazz, "typeParameterWithMultipleBounds"), tv -> {
            assertEquals("Y", tv.getName());
            assertEquals(2, tv.getBounds().length);
            assertEquals(Number.class, tv.getBounds()[0]);
            assertInstanceOf(java.lang.reflect.ParameterizedType.class, tv.getBounds()[1]);
            java.lang.reflect.ParameterizedType bound = (java.lang.reflect.ParameterizedType) tv.getBounds()[1];
            assertEquals(Comparable.class, bound.getRawType());
            assertEquals(1, bound.getActualTypeArguments().length);
            assertInstanceOf(java.lang.reflect.TypeVariable.class, bound.getActualTypeArguments()[0]);
            assertEquals("Y", ((java.lang.reflect.TypeVariable<?>) bound.getActualTypeArguments()[0]).getName());
        });
        assertTypeVariable(type(clazz, "typeParameterWithSingleParameterizedBound"), tv -> {
            assertEquals("Y", tv.getName());
            assertEquals(1, tv.getBounds().length);
            assertInstanceOf(java.lang.reflect.ParameterizedType.class, tv.getBounds()[0]);
            java.lang.reflect.ParameterizedType bound = (java.lang.reflect.ParameterizedType) tv.getBounds()[0];
            assertEquals(Comparable.class, bound.getRawType());
            assertEquals(1, bound.getActualTypeArguments().length);
            assertInstanceOf(java.lang.reflect.TypeVariable.class, bound.getActualTypeArguments()[0]);
            assertEquals("Y", ((java.lang.reflect.TypeVariable<?>) bound.getActualTypeArguments()[0]).getName());
        });
        assertTypeVariable(type(clazz, "typeParameterWithMultipleBoundsFirstParameterized"), tv -> {
            assertEquals("Y", tv.getName());
            assertEquals(2, tv.getBounds().length);
            assertInstanceOf(java.lang.reflect.ParameterizedType.class, tv.getBounds()[0]);
            java.lang.reflect.ParameterizedType bound = (java.lang.reflect.ParameterizedType) tv.getBounds()[0];
            assertEquals(Comparable.class, bound.getRawType());
            assertEquals(1, bound.getActualTypeArguments().length);
            assertInstanceOf(java.lang.reflect.TypeVariable.class, bound.getActualTypeArguments()[0]);
            assertEquals("Y", ((java.lang.reflect.TypeVariable<?>) bound.getActualTypeArguments()[0]).getName());
            assertEquals(Serializable.class, tv.getBounds()[1]);
        });
        assertTypeVariable(type(clazz, "typeParameterWithMultipleBoundsSecondParameterized"), tv -> {
            assertEquals("Y", tv.getName());
            assertEquals(2, tv.getBounds().length);
            assertEquals(Serializable.class, tv.getBounds()[0]);
            assertInstanceOf(java.lang.reflect.ParameterizedType.class, tv.getBounds()[1]);
            java.lang.reflect.ParameterizedType bound = (java.lang.reflect.ParameterizedType) tv.getBounds()[1];
            assertEquals(Comparable.class, bound.getRawType());
            assertEquals(1, bound.getActualTypeArguments().length);
            assertInstanceOf(java.lang.reflect.TypeVariable.class, bound.getActualTypeArguments()[0]);
            assertEquals("Y", ((java.lang.reflect.TypeVariable<?>) bound.getActualTypeArguments()[0]).getName());
        });
        assertEquals(firstTypeArgumentReflective(clazz, "unboundedWildcard"), firstTypeArgument(clazz, "unboundedWildcard"));
        assertEquals(firstTypeArgumentReflective(clazz, "wildcardWithUpperBound"),
                firstTypeArgument(clazz, "wildcardWithUpperBound"));
        assertEquals(firstTypeArgumentReflective(clazz, "wildcardWithLowerBound"),
                firstTypeArgument(clazz, "wildcardWithLowerBound"));
        assertWildcardType(firstTypeArgument(clazz, "wildcardWithUnboundedTypeParameterAsUpperBound"), wt -> {
            assertEquals(1, wt.getUpperBounds().length);
            assertTypeVariable(wt.getUpperBounds()[0], tv -> {
                assertEquals("X", tv.getName());
                assertEquals(1, tv.getBounds().length);
                assertEquals(Object.class, tv.getBounds()[0]);
            });
            assertEquals(0, wt.getLowerBounds().length);
        });
        assertWildcardType(firstTypeArgument(clazz, "wildcardWithBoundedTypeParameterAsUpperBound"), wt -> {
            assertEquals(1, wt.getUpperBounds().length);
            assertTypeVariable(wt.getUpperBounds()[0], tv -> {
                assertEquals("Y", tv.getName());
                assertEquals(1, tv.getBounds().length);
                assertEquals(Number.class, tv.getBounds()[0]);
            });
            assertEquals(0, wt.getLowerBounds().length);
        });
        assertWildcardType(firstTypeArgument(clazz, "wildcardWithBoundedTypeParameterAsLowerBound"), wt -> {
            assertEquals(1, wt.getUpperBounds().length);
            assertEquals(Object.class, wt.getUpperBounds()[0]);
            assertEquals(1, wt.getLowerBounds().length);
            assertTypeVariable(wt.getLowerBounds()[0], tv -> {
                assertEquals("Y", tv.getName());
                assertEquals(1, tv.getBounds().length);
                assertEquals(Number.class, tv.getBounds()[0]);
            });
        });

        TypeVariable u = index.getClassByName(NestedClass.InnerClass.class).typeParameters().get(0);
        java.lang.reflect.Type uReflective = JandexReflection.loadType(u);
        assertInstanceOf(java.lang.reflect.TypeVariable.class, uReflective);
        assertEquals("U", ((java.lang.reflect.TypeVariable<?>) uReflective).getName());
        assertEquals(1, ((java.lang.reflect.TypeVariable<?>) uReflective).getBounds().length);

        assertEquals(Type.Kind.UNRESOLVED_TYPE_VARIABLE, u.bounds().get(0).kind());
        UnresolvedTypeVariable t = u.bounds().get(0).asUnresolvedTypeVariable();
        java.lang.reflect.Type tReflective = JandexReflection.loadType(t);
        assertInstanceOf(java.lang.reflect.TypeVariable.class, tReflective);
        assertEquals("T", ((java.lang.reflect.TypeVariable<?>) tReflective).getName());
        assertEquals(0, ((java.lang.reflect.TypeVariable<?>) tReflective).getBounds().length);
    }

    private void assertTypeVariable(java.lang.reflect.Type type, Consumer<java.lang.reflect.TypeVariable<?>> assertion) {
        assertInstanceOf(java.lang.reflect.TypeVariable.class, type);
        assertion.accept((java.lang.reflect.TypeVariable<?>) type);
    }

    private void assertWildcardType(java.lang.reflect.Type type, Consumer<java.lang.reflect.WildcardType> assertion) {
        assertInstanceOf(java.lang.reflect.WildcardType.class, type);
        assertion.accept((java.lang.reflect.WildcardType) type);
    }

    private java.lang.reflect.Type type(ClassInfo clazz, String method) {
        Type jandexType = clazz.firstMethod(method).returnType();
        return JandexReflection.loadType(jandexType);
    }

    private java.lang.reflect.Type typeReflective(ClassInfo clazz, String method) {
        Class<?> classReflective = JandexReflection.loadClass(clazz);
        for (java.lang.reflect.Method m : classReflective.getDeclaredMethods()) {
            if (method.equals(m.getName())) {
                return m.getGenericReturnType();
            }
        }
        throw new IllegalArgumentException(clazz + " does not have method '" + method + "()'");
    }

    private java.lang.reflect.Type firstTypeArgument(ClassInfo clazz, String method) {
        Type jandexType = clazz.firstMethod(method).returnType().asParameterizedType().arguments().get(0);
        return JandexReflection.loadType(jandexType);
    }

    private java.lang.reflect.Type firstTypeArgumentReflective(ClassInfo clazz, String method) {
        java.lang.reflect.Type methodType = typeReflective(clazz, method);
        return ((java.lang.reflect.ParameterizedType) methodType).getActualTypeArguments()[0];
    }

    @Test
    public void loadRawType() throws IOException {
        // intentionally _not_ indexing `NestedClass`, so that the type parameter bound of `InnerClass` is unresolved
        Index index = Index.of(TestMethods.class, NestedClass.InnerClass.class);

        ClassInfo clazz = index.getClassByName(TestMethods.class);
        assertEquals(void.class, rawType(clazz, "nothing"));
        assertEquals(int.class, rawType(clazz, "primitive"));
        assertEquals(String.class, rawType(clazz, "clazz"));
        assertEquals(List.class, rawType(clazz, "parameterized"));
        assertEquals(String[][].class, rawType(clazz, "array"));
        assertEquals(List[].class, rawType(clazz, "genericArray"));
        assertEquals(String[][][][].class, rawType(clazz, "annotatedArray"));
        assertEquals(Object.class, rawType(clazz, "typeParameter"));
        assertEquals(Number.class, rawType(clazz, "typeParameterWithSingleBound"));
        assertEquals(Number.class, rawType(clazz, "typeParameterWithMultipleBounds"));
        assertEquals(Comparable.class, rawType(clazz, "typeParameterWithSingleParameterizedBound"));
        assertEquals(Comparable.class, rawType(clazz, "typeParameterWithMultipleBoundsFirstParameterized"));
        assertEquals(Serializable.class, rawType(clazz, "typeParameterWithMultipleBoundsSecondParameterized"));
        assertEquals(Object.class, firstRawTypeArgument(clazz, "unboundedWildcard"));
        assertEquals(Number.class, firstRawTypeArgument(clazz, "wildcardWithUpperBound"));
        assertEquals(Object.class, firstRawTypeArgument(clazz, "wildcardWithLowerBound"));
        assertEquals(Object.class, firstRawTypeArgument(clazz, "wildcardWithUnboundedTypeParameterAsUpperBound"));
        assertEquals(Number.class, firstRawTypeArgument(clazz, "wildcardWithBoundedTypeParameterAsUpperBound"));
        assertEquals(Object.class, firstRawTypeArgument(clazz, "wildcardWithBoundedTypeParameterAsLowerBound"));

        TypeVariable u = index.getClassByName(NestedClass.InnerClass.class).typeParameters().get(0);
        assertEquals(Object.class, JandexReflection.loadRawType(u));

        assertEquals(Type.Kind.UNRESOLVED_TYPE_VARIABLE, u.bounds().get(0).kind());
        UnresolvedTypeVariable t = u.bounds().get(0).asUnresolvedTypeVariable();
        assertEquals(Object.class, JandexReflection.loadRawType(t));
    }

    private Class<?> rawType(ClassInfo clazz, String method) {
        Type jandexType = clazz.firstMethod(method).returnType();
        return JandexReflection.loadRawType(jandexType);
    }

    private Class<?> firstRawTypeArgument(ClassInfo clazz, String method) {
        Type jandexType = clazz.firstMethod(method).returnType().asParameterizedType().arguments().get(0);
        return JandexReflection.loadRawType(jandexType);
    }

    @Test
    public void loadClass() throws IOException {
        Index index = Index.of(JandexReflectionTest.class);

        assertEquals(JandexReflectionTest.class, JandexReflection.loadClass(index.getClassByName(JandexReflectionTest.class)));
    }
}
