package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TransitiveTypeVariablesTest {
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
        test(IndexingUtil.roundtrip(index, "940b75404bb6eaeab1e0befa017531391a7ba85ab41601e5f77a9fbedff4f401"));
    }

    private void test(Index index) {
        ClassInfo clazz = index.getClassByName(TestClass.class);
        assertNotNull(clazz);
        assertEquals(3, clazz.typeParameters().size());
        assertA(clazz.typeParameters().get(0), Ann1.class);
        assertB(clazz.typeParameters().get(1), Ann3.class);
        assertC(clazz.typeParameters().get(2), Ann5.class);

        Type superClass = clazz.superClassType();
        assertNotNull(superClass);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, superClass.kind());
        assertEquals(DotName.createSimple(ArrayList.class.getName()), superClass.name());
        assertEquals(1, superClass.asParameterizedType().arguments().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, superClass.asParameterizedType().arguments().get(0).kind());
        assertTrue(superClass.hasAnnotation(DotName.createSimple(Ann7.class.getName())));
        assertC(superClass.asParameterizedType().arguments().get(0).asTypeVariable(), Ann8.class);

        assertEquals(1, clazz.interfaceTypes().size());
        Type superInterface = clazz.interfaceTypes().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, superInterface.kind());
        assertEquals(DotName.createSimple(TestInterface.class.getName()), superInterface.name());
        assertEquals(1, superInterface.asParameterizedType().arguments().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, superInterface.asParameterizedType().arguments().get(0).kind());
        assertTrue(superInterface.hasAnnotation(DotName.createSimple(Ann9.class.getName())));
        assertB(superInterface.asParameterizedType().arguments().get(0).asTypeVariable(), Ann10.class);

        FieldInfo field1 = clazz.field("field1");
        assertNotNull(field1);
        assertEquals(Type.Kind.TYPE_VARIABLE, field1.type().kind());
        assertC(field1.type().asTypeVariable(), Ann11.class);

        FieldInfo field2 = clazz.field("field2");
        assertNotNull(field2);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, field2.type().kind());
        assertEquals(DotName.createSimple(List.class.getName()), field2.type().asParameterizedType().name());
        assertEquals(1, field2.type().asParameterizedType().arguments().size());
        assertEquals(Type.Kind.WILDCARD_TYPE, field2.type().asParameterizedType().arguments().get(0).kind());
        assertEquals(Type.Kind.TYPE_VARIABLE,
                field2.type().asParameterizedType().arguments().get(0).asWildcardType().extendsBound().kind());
        assertTrue(field2.type().hasAnnotation(DotName.createSimple(Ann12.class.getName())));
        assertTrue(field2.type().asParameterizedType().arguments().get(0)
                .hasAnnotation(DotName.createSimple(Ann13.class.getName())));
        assertC(field2.type().asParameterizedType().arguments().get(0).asWildcardType().extendsBound().asTypeVariable(),
                Ann14.class);

        FieldInfo field3 = clazz.field("field3");
        assertNotNull(field3);
        assertEquals(Type.Kind.ARRAY, field3.type().kind());
        assertEquals(Type.Kind.ARRAY, field3.type().asArrayType().component().kind());
        assertEquals(Type.Kind.TYPE_VARIABLE, field3.type().asArrayType().component().asArrayType().component().kind());
        assertTrue(field3.type().annotations().isEmpty());
        assertTrue(field3.type().asArrayType().component().hasAnnotation(DotName.createSimple(Ann16.class.getName())));
        assertC(field3.type().asArrayType().component().asArrayType().component().asTypeVariable(), Ann15.class);

        MethodInfo method = clazz.firstMethod("method");
        assertNotNull(method);
        assertEquals(2, method.typeParameters().size());
        assertD(method.typeParameters().get(0), Ann17.class);
        assertE(method.typeParameters().get(1), Ann19.class);

        Type receiver = method.receiverType();
        assertNotNull(receiver);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, receiver.kind());
        assertEquals(3, receiver.asParameterizedType().arguments().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, receiver.asParameterizedType().arguments().get(0).kind());
        assertEquals(Type.Kind.TYPE_VARIABLE, receiver.asParameterizedType().arguments().get(1).kind());
        assertEquals(Type.Kind.TYPE_VARIABLE, receiver.asParameterizedType().arguments().get(2).kind());
        assertTrue(receiver.hasAnnotation(DotName.createSimple(Ann23.class.getName())));
        assertA(receiver.asParameterizedType().arguments().get(0).asTypeVariable(), Ann24.class);
        assertB(receiver.asParameterizedType().arguments().get(1).asTypeVariable(), Ann25.class);
        assertC(receiver.asParameterizedType().arguments().get(2).asTypeVariable(), Ann26.class);

        assertEquals(2, method.parametersCount());

        Type param1 = method.parameterType(0);
        assertEquals(Type.Kind.ARRAY, param1.kind());
        assertEquals(Type.Kind.ARRAY, param1.asArrayType().component().kind());
        assertEquals(Type.Kind.TYPE_VARIABLE, param1.asArrayType().component().asArrayType().component().kind());
        assertTrue(param1.annotations().isEmpty());
        assertTrue(param1.asArrayType().component().hasAnnotation(DotName.createSimple(Ann28.class.getName())));
        assertE(param1.asArrayType().component().asArrayType().component().asTypeVariable(), Ann27.class);

        Type param2 = method.parameterType(1);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, param2.kind());
        assertEquals(1, param2.asParameterizedType().arguments().size());
        assertEquals(Type.Kind.WILDCARD_TYPE, param2.asParameterizedType().arguments().get(0).kind());
        assertNotNull(param2.asParameterizedType().arguments().get(0).asWildcardType().superBound());
        assertEquals(Type.Kind.TYPE_VARIABLE,
                param2.asParameterizedType().arguments().get(0).asWildcardType().superBound().kind());
        assertTrue(param2.hasAnnotation(DotName.createSimple(Ann29.class.getName())));
        assertTrue(param2.asParameterizedType().arguments().get(0).hasAnnotation(DotName.createSimple(Ann30.class.getName())));
        assertE(param2.asParameterizedType().arguments().get(0).asWildcardType().superBound().asTypeVariable(), Ann31.class);

        assertEquals(1, method.exceptions().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, method.exceptions().get(0).kind());
        assertD(method.exceptions().get(0).asTypeVariable(), Ann32.class);

        ClassInfo inner = index.getClassByName(TestClass.InnerClass.class);
        assertNotNull(inner);
        assertEquals(2, inner.typeParameters().size());
        assertF(inner.typeParameters().get(0), Ann33.class);
        assertG(inner.typeParameters().get(1), Ann35.class);

        assertEquals(1, inner.interfaceTypes().size());
        Type innerSuperInterface = inner.interfaceTypes().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, innerSuperInterface.kind());
        assertEquals(DotName.createSimple(TestInterface.class.getName()), innerSuperInterface.name());
        assertEquals(1, innerSuperInterface.asParameterizedType().arguments().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, innerSuperInterface.asParameterizedType().arguments().get(0).kind());
        assertG(innerSuperInterface.asParameterizedType().arguments().get(0).asTypeVariable(), Ann37.class);

        FieldInfo innerField = inner.field("field");
        assertNotNull(innerField);
        assertEquals(Type.Kind.TYPE_VARIABLE, innerField.type().kind());
        assertG(innerField.type().asTypeVariable(), Ann38.class);
    }

    private static void assertA(TypeVariable a, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("A", a.identifier());
        assertTrue(a.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, a.bounds().size());
        assertEquals(Type.Kind.CLASS, a.bounds().get(0).kind());
        assertEquals("java.lang.Exception", a.bounds().get(0).asClassType().name().toString());
        assertTrue(a.bounds().get(0).hasAnnotation(DotName.createSimple(Ann2.class.getName())));
    }

    private static void assertB(TypeVariable b, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("B", b.identifier());
        assertTrue(b.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, b.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, b.bounds().get(0).kind());
        assertA(b.bounds().get(0).asTypeVariable(), Ann4.class);
    }

    private static void assertC(TypeVariable c, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("C", c.identifier());
        assertTrue(c.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, c.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, c.bounds().get(0).kind());
        assertB(c.bounds().get(0).asTypeVariable(), Ann6.class);
    }

    private static void assertD(TypeVariable d, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("D", d.identifier());
        assertTrue(d.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, d.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, d.bounds().get(0).kind());
        assertC(d.bounds().get(0).asTypeVariable(), Ann18.class);
    }

    private static void assertE(TypeVariable e, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("E", e.identifier());
        assertTrue(e.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, e.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, e.bounds().get(0).kind());
        assertD(e.bounds().get(0).asTypeVariable(), Ann20.class);
    }

    private static void assertF(TypeVariable f, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("F", f.identifier());
        assertTrue(f.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, f.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, f.bounds().get(0).kind());
        assertC(f.bounds().get(0).asTypeVariable(), Ann34.class);
    }

    private static void assertG(TypeVariable g, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("G", g.identifier());
        assertTrue(g.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, g.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, g.bounds().get(0).kind());
        assertF(g.bounds().get(0).asTypeVariable(), Ann36.class);
    }
}
