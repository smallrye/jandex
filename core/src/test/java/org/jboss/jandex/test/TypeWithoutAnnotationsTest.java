package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.junit.jupiter.api.Test;

public class TypeWithoutAnnotationsTest {
    interface TestMethods<X> {
        @MyAnnotation("1")
        int primitive();

        @MyAnnotation("2")
        String clazz();

        @MyAnnotation("3")
        List<String> parameterized();

        @MyAnnotation("4")
        String[][] array();

        @MyAnnotation("5")
        List<@MyAnnotation("6") ? extends @MyAnnotation("7") Number> @MyAnnotation("8") [] genericArray();

        // annotations are present to make sure the `ArrayType` has multiple levels of nesting
        @MyAnnotation("9")
        String[] @MyAnnotation("10") [][] @MyAnnotation("11") [] annotatedArray();

        @MyAnnotation("12")
        X typeParameter();

        @MyAnnotation("13")
        <Y extends @MyAnnotation("14") Number> Y typeParameterWithSingleBound();

        @MyAnnotation("15")
        <Y extends @MyAnnotation("16") Number & @MyAnnotation("17") Comparable<@MyAnnotation("18") Y>> Y typeParameterWithMultipleBounds();

        @MyAnnotation("19")
        <Y extends @MyAnnotation("20") Comparable<@MyAnnotation("21") Y>> Y typeParameterWithSingleParameterizedBound();

        @MyAnnotation("22")
        <Y extends @MyAnnotation("23") Comparable<@MyAnnotation("24") Y> & @MyAnnotation("25") Serializable> Y typeParameterWithMultipleBoundsFirstParameterized();

        @MyAnnotation("26")
        <Y extends @MyAnnotation("27") Serializable & @MyAnnotation("28") Comparable<@MyAnnotation("29") Y>> Y typeParameterWithMultipleBoundsSecondParameterized();

        @MyAnnotation("30")
        List<@MyAnnotation("31") ?> unboundedWildcard();

        @MyAnnotation("32")
        List<@MyAnnotation("33") ? extends @MyAnnotation("34") Number> wildcardWithUpperBound();

        @MyAnnotation("35")
        List<@MyAnnotation("36") ? super @MyAnnotation("37") String> wildcardWithLowerBound();

        @MyAnnotation("38")
        List<@MyAnnotation("39") ? extends @MyAnnotation("40") X> wildcardWithUnboundedTypeParameterAsUpperBound();

        @MyAnnotation("41")
        <@MyAnnotation("42") Y extends @MyAnnotation("43") Number> List<@MyAnnotation("44") ? extends @MyAnnotation("45") Y> wildcardWithBoundedTypeParameterAsUpperBound();

        @MyAnnotation("46")
        <@MyAnnotation("47") Y extends @MyAnnotation("48") Number> List<@MyAnnotation("49") ? super @MyAnnotation("50") Y> wildcardWithBoundedTypeParameterAsLowerBound();
    }

    static class NestedClass<@MyAnnotation("51") T> {
        class InnerClass<@MyAnnotation("52") U extends @MyAnnotation("53") T> {
        }
    }

    @Test
    public void test() throws IOException, ReflectiveOperationException {
        // intentionally _not_ indexing `NestedClass`, so that the type parameter bound of `InnerClass` is unresolved
        Index index = Index.of(TestMethods.class, NestedClass.InnerClass.class);

        ClassInfo clazz = index.getClassByName(TestMethods.class);
        assertEquals("int",
                withoutAnnotations(clazz, "primitive"));
        assertEquals("java.lang.String",
                withoutAnnotations(clazz, "clazz"));
        assertEquals("java.util.List<java.lang.String>",
                withoutAnnotations(clazz, "parameterized"));
        assertEquals("java.lang.String[][]",
                withoutAnnotations(clazz, "array"));
        assertEquals("java.util.List<? extends java.lang.Number>[]",
                withoutAnnotations(clazz, "genericArray"));
        assertEquals("java.lang.String[][][][]",
                withoutAnnotations(clazz, "annotatedArray"));
        assertEquals("X",
                withoutAnnotations(clazz, "typeParameter"));
        assertEquals("Y extends java.lang.Number",
                withoutAnnotations(clazz, "typeParameterWithSingleBound"));
        assertEquals("Y extends java.lang.Number & java.lang.Comparable<Y>",
                withoutAnnotations(clazz, "typeParameterWithMultipleBounds"));
        assertEquals("Y extends java.lang.Comparable<Y>",
                withoutAnnotations(clazz, "typeParameterWithSingleParameterizedBound"));
        assertEquals("Y extends java.lang.Comparable<Y> & java.io.Serializable",
                withoutAnnotations(clazz, "typeParameterWithMultipleBoundsFirstParameterized"));
        assertEquals("Y extends java.io.Serializable & java.lang.Comparable<Y>",
                withoutAnnotations(clazz, "typeParameterWithMultipleBoundsSecondParameterized"));
        assertEquals("? extends java.lang.Object",
                firstTypeArgumentWithoutAnnotations(clazz, "unboundedWildcard"));
        assertEquals("? extends java.lang.Number",
                firstTypeArgumentWithoutAnnotations(clazz, "wildcardWithUpperBound"));
        assertEquals("? super java.lang.String",
                firstTypeArgumentWithoutAnnotations(clazz, "wildcardWithLowerBound"));
        assertEquals("? extends X",
                firstTypeArgumentWithoutAnnotations(clazz, "wildcardWithUnboundedTypeParameterAsUpperBound"));
        assertEquals("? extends Y",
                firstTypeArgumentWithoutAnnotations(clazz, "wildcardWithBoundedTypeParameterAsUpperBound"));
        assertEquals("? super Y",
                firstTypeArgumentWithoutAnnotations(clazz, "wildcardWithBoundedTypeParameterAsLowerBound"));

        TypeVariable u = index.getClassByName(NestedClass.InnerClass.class).typeParameters().get(0);
        assertEquals("U extends T", withoutAnnotations(u));
    }

    private String withoutAnnotations(ClassInfo clazz, String method) throws ReflectiveOperationException {
        Type jandexType = clazz.firstMethod(method).returnType();
        return withoutAnnotations(jandexType);
    }

    private String firstTypeArgumentWithoutAnnotations(ClassInfo clazz, String method) throws ReflectiveOperationException {
        Type jandexType = clazz.firstMethod(method).returnType().asParameterizedType().arguments().get(0);
        return withoutAnnotations(jandexType);
    }

    private String withoutAnnotations(Type type) throws ReflectiveOperationException {
        Method withoutAnnotations = Type.class.getDeclaredMethod("withoutAnnotations");
        withoutAnnotations.setAccessible(true);
        return withoutAnnotations.invoke(type).toString();
    }
}
