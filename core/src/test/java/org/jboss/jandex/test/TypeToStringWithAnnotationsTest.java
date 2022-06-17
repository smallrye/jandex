package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeToStringWithAnnotationsTest {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann1 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann2 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann3 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann4 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann5 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann6 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann7 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann8 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann9 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann10 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann11 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann12 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann13 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann14 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann15 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann16 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann17 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann18 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann19 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann20 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann21 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann22 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann23 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann24 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann25 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann26 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann27 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann28 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann29 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann30 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann31 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann32 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann33 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann34 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann35 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann36 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann37 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann38 {
    }

    interface TestInterface<T> {
    }

    static class TestClass<@Ann1 A extends @Ann2 Exception, @Ann3 B extends @Ann4 A, @Ann5 C extends @Ann6 B>
            extends @Ann7 ArrayList<@Ann8 C> implements @Ann9 TestInterface<@Ann10 B> {

        @Ann11
        C field1;

        @Ann12
        List<@Ann13 ? extends @Ann14 C> field2;

        @Ann15
        C[] @Ann16 [] field3;

        <@Ann17 D extends @Ann18 C, @Ann19 E extends @Ann20 D> @Ann21 List<@Ann22 E> method(
                @Ann23 TestClass<@Ann24 A, @Ann25 B, @Ann26 C> this,
                @Ann27 E[] @Ann28 [] param1,
                @Ann29 List<@Ann30 ? super @Ann31 E> param2)
                throws @Ann32 D {
            return null;
        }

        class InnerClass<@Ann33 F extends @Ann34 C, @Ann35 G extends @Ann36 F> implements TestInterface<@Ann37 G> {
            @Ann38
            G field;
        }
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(TestClass.class, TestClass.InnerClass.class);
        test(index);
        test(IndexingUtil.roundtrip(index));
    }

    private void test(Index index) {
        ClassInfo clazz = index.getClassByName(TestClass.class);
        assertNotNull(clazz);
        assertEquals(3, clazz.typeParameters().size());
        assertA(clazz.typeParameters().get(0), Ann1.class);
        assertB(clazz.typeParameters().get(1), Ann3.class);
        assertC(clazz.typeParameters().get(2), Ann5.class);

        Type superClass = clazz.superClassType();
        assertEquals("java.util.@Ann7 ArrayList<@Ann8 C>", superClass.toString());
        assertC(superClass.asParameterizedType().arguments().get(0), Ann8.class);

        assertEquals(1, clazz.interfaceTypes().size());
        Type superInterface = clazz.interfaceTypes().get(0);
        assertEquals(
                "org.jboss.jandex.test.@Ann9 TypeToStringWithAnnotationsTest$TestInterface<@Ann10 B>",
                superInterface.toString());
        assertB(superInterface.asParameterizedType().arguments().get(0), Ann10.class);

        FieldInfo field1 = clazz.field("field1");
        assertNotNull(field1);
        assertEquals("@Ann11 C org.jboss.jandex.test.TypeToStringWithAnnotationsTest$TestClass.field1", field1.toString());
        assertC(field1.type(), Ann11.class);

        FieldInfo field2 = clazz.field("field2");
        assertNotNull(field2);
        assertEquals(
                "java.util.@Ann12 List<@Ann13 ? extends @Ann14 C> org.jboss.jandex.test.TypeToStringWithAnnotationsTest$TestClass.field2",
                field2.toString());
        assertEquals("java.util.@Ann12 List<@Ann13 ? extends @Ann14 C>", field2.type().toString());
        assertEquals("@Ann13 ? extends @Ann14 C", field2.type().asParameterizedType().arguments().get(0).toString());
        assertC(field2.type().asParameterizedType().arguments().get(0).asWildcardType().extendsBound(), Ann14.class);

        FieldInfo field3 = clazz.field("field3");
        assertNotNull(field3);
        assertEquals(
                "@Ann15 C[] @Ann16 [] org.jboss.jandex.test.TypeToStringWithAnnotationsTest$TestClass.field3",
                field3.toString());
        assertEquals("@Ann15 C[] @Ann16 []", field3.type().toString());
        assertEquals("@Ann15 C @Ann16 []", field3.type().asArrayType().component().toString());
        assertC(field3.type().asArrayType().component().asArrayType().component(), Ann15.class);

        MethodInfo method = clazz.firstMethod("method");
        assertNotNull(method);
        assertEquals(
                "java.util.@Ann21 List<@Ann22 E> method(org.jboss.jandex.test.@Ann23 TypeToStringWithAnnotationsTest$TestClass<@Ann24 A, @Ann25 B, @Ann26 C> this, @Ann27 E[] @Ann28 [] param1, java.util.@Ann29 List<@Ann30 ? super @Ann31 E> param2) throws @Ann32 D",
                method.toString());

        assertEquals(2, method.typeParameters().size());
        assertD(method.typeParameters().get(0), Ann17.class);
        assertE(method.typeParameters().get(1), Ann19.class);

        Type returnType = method.returnType();
        assertNotNull(returnType);
        assertEquals("java.util.@Ann21 List<@Ann22 E>", returnType.toString());
        assertE(returnType.asParameterizedType().arguments().get(0), Ann22.class);

        Type receiver = method.receiverType();
        assertNotNull(receiver);
        assertEquals(
                "org.jboss.jandex.test.@Ann23 TypeToStringWithAnnotationsTest$TestClass<@Ann24 A, @Ann25 B, @Ann26 C>",
                receiver.toString());
        assertA(receiver.asParameterizedType().arguments().get(0), Ann24.class);
        assertB(receiver.asParameterizedType().arguments().get(1), Ann25.class);
        assertC(receiver.asParameterizedType().arguments().get(2), Ann26.class);

        assertEquals(2, method.parametersCount());
        assertEquals("@Ann27 E[] @Ann28 []", method.parameterType(0).toString());
        assertEquals("@Ann27 E @Ann28 []", method.parameterType(0).asArrayType().component().toString());
        assertE(method.parameterType(0).asArrayType().component().asArrayType().component(), Ann27.class);
        assertEquals("java.util.@Ann29 List<@Ann30 ? super @Ann31 E>", method.parameterType(1).toString());
        assertEquals("@Ann30 ? super @Ann31 E", method.parameterType(1).asParameterizedType().arguments().get(0).toString());
        assertE(method.parameterType(1).asParameterizedType().arguments().get(0).asWildcardType().superBound(), Ann31.class);

        assertEquals(1, method.exceptions().size());
        assertD(method.exceptions().get(0), Ann32.class);

        ClassInfo inner = index.getClassByName(TestClass.InnerClass.class);
        assertNotNull(inner);
        assertEquals(2, inner.typeParameters().size());
        assertF(inner.typeParameters().get(0), Ann33.class);
        assertG(inner.typeParameters().get(1), Ann35.class);

        assertEquals(1, inner.interfaceTypes().size());
        Type innerSuperInterface = inner.interfaceTypes().get(0);
        assertEquals(
                "org.jboss.jandex.test.TypeToStringWithAnnotationsTest$TestInterface<@Ann37 G>",
                innerSuperInterface.toString());
        assertG(innerSuperInterface.asParameterizedType().arguments().get(0).asTypeVariable(), Ann37.class);

        FieldInfo innerField = inner.field("field");
        assertNotNull(innerField);
        assertEquals(
                "@Ann38 G org.jboss.jandex.test.TypeToStringWithAnnotationsTest$TestClass$InnerClass.field",
                innerField.toString());
        assertEquals("@Ann38 G extends @Ann36 F", innerField.type().toString());
        assertG(innerField.type().asTypeVariable(), Ann38.class);
    }

    private static void assertA(Type a, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("@" + expectedAnnotation.getSimpleName() + " A extends java.lang.@Ann2 Exception", a.toString());
    }

    private static void assertB(Type b, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("@" + expectedAnnotation.getSimpleName() + " B extends @Ann4 A", b.toString());
        assertA(b.asTypeVariable().bounds().get(0), Ann4.class);
    }

    private static void assertC(Type c, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("@" + expectedAnnotation.getSimpleName() + " C extends @Ann6 B", c.toString());
        assertB(c.asTypeVariable().bounds().get(0), Ann6.class);
    }

    private static void assertD(Type d, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("@" + expectedAnnotation.getSimpleName() + " D extends @Ann18 C", d.toString());
        assertC(d.asTypeVariable().bounds().get(0), Ann18.class);
    }

    private static void assertE(Type e, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("@" + expectedAnnotation.getSimpleName() + " E extends @Ann20 D", e.toString());
        assertD(e.asTypeVariable().bounds().get(0), Ann20.class);
    }

    private static void assertF(Type f, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("@" + expectedAnnotation.getSimpleName() + " F extends @Ann34 C", f.toString());
        assertC(f.asTypeVariable().bounds().get(0), Ann34.class);
    }

    private static void assertG(Type g, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("@" + expectedAnnotation.getSimpleName() + " G extends @Ann36 F", g.toString());
        assertF(g.asTypeVariable().bounds().get(0), Ann36.class);
    }
}
