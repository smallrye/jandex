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

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TransitiveTypeParameterBoundsTest {
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

    static class ClassWithTransitiveTypeParameterBounds<@Ann1 A extends @Ann2 Number, @Ann3 B extends @Ann4 A, @Ann5 C extends @Ann6 B> {
        <@Ann7 D extends @Ann8 C, @Ann9 E extends @Ann10 D> Class<?> method() {
            class LocalClass<@Ann11 F extends @Ann12 E> {
                <@Ann13 G extends @Ann14 F> void localMethod() {
                }
            }

            return LocalClass.class;
        }

        class InnerClass<@Ann15 H extends @Ann16 C> {
            <@Ann17 I extends @Ann18 H> void innerMethod() {
            }

            class InnerInnerClass<@Ann19 J extends @Ann20 C> {
                <@Ann21 K extends @Ann22 C> void innerInnerMethod() {
                }
            }
        }

        static class NestedClass<@Ann23 A extends @Ann24 Number, @Ann25 B extends @Ann26 A> {
        }

        static <@Ann27 A extends @Ann28 Number, @Ann29 B extends @Ann30 A> Class<?> staticMethod() {
            class StaticLocalClass<@Ann31 C extends @Ann32 B> {
            }
            return StaticLocalClass.class;
        }
    }

    @Test
    public void test() throws IOException {
        @SuppressWarnings("rawtypes")
        Class<?> localClass = new ClassWithTransitiveTypeParameterBounds().method();
        Class<?> staticLocalClass = ClassWithTransitiveTypeParameterBounds.staticMethod();

        Index index = Index.of(localClass, staticLocalClass,
                ClassWithTransitiveTypeParameterBounds.class,
                ClassWithTransitiveTypeParameterBounds.InnerClass.class,
                ClassWithTransitiveTypeParameterBounds.InnerClass.InnerInnerClass.class,
                ClassWithTransitiveTypeParameterBounds.NestedClass.class);

        doTest(localClass, staticLocalClass, index);

        doTest(localClass, staticLocalClass,
                IndexingUtil.roundtrip(index, "1f9b30896412c22f57881e7f129b05b6b6f9accb28c7a55ade68ea1fc6e9da10"));
    }

    private void doTest(Class<?> localClassObject, Class<?> staticLocalClassObject, Index index) {
        ClassInfo clazz = index.getClassByName(ClassWithTransitiveTypeParameterBounds.class);
        assertNotNull(clazz);
        assertEquals(3, clazz.typeParameters().size());
        assertA(clazz.typeParameters().get(0), Ann1.class);
        assertB(clazz.typeParameters().get(1), Ann3.class);
        assertC(clazz.typeParameters().get(2), Ann5.class);

        MethodInfo method = clazz.firstMethod("method");
        assertNotNull(method);
        assertEquals(2, method.typeParameters().size());
        assertD(method.typeParameters().get(0), Ann7.class);
        assertE(method.typeParameters().get(1), Ann9.class);

        ClassInfo localClass = index.getClassByName(localClassObject);
        assertNotNull(localClass);
        assertEquals(1, localClass.typeParameters().size());
        assertF(localClass.typeParameters().get(0), Ann11.class);

        MethodInfo localMethod = localClass.firstMethod("localMethod");
        assertNotNull(localMethod);
        assertEquals(1, localMethod.typeParameters().size());
        assertG(localMethod.typeParameters().get(0), Ann13.class);

        ClassInfo innerClass = index.getClassByName(ClassWithTransitiveTypeParameterBounds.InnerClass.class);
        assertNotNull(innerClass);
        assertEquals(1, innerClass.typeParameters().size());
        assertH(innerClass.typeParameters().get(0), Ann15.class);

        MethodInfo innerMethod = innerClass.firstMethod("innerMethod");
        assertNotNull(innerMethod);
        assertEquals(1, innerMethod.typeParameters().size());
        assertI(innerMethod.typeParameters().get(0), Ann17.class);

        ClassInfo innerInnerClass = index
                .getClassByName(ClassWithTransitiveTypeParameterBounds.InnerClass.InnerInnerClass.class);
        assertNotNull(innerInnerClass);
        assertEquals(1, innerInnerClass.typeParameters().size());
        assertJ(innerInnerClass.typeParameters().get(0), Ann19.class);

        MethodInfo innerInnerMethod = innerInnerClass.firstMethod("innerInnerMethod");
        assertNotNull(innerInnerMethod);
        assertEquals(1, innerInnerMethod.typeParameters().size());
        assertK(innerInnerMethod.typeParameters().get(0), Ann21.class);

        ClassInfo nestedClass = index.getClassByName(ClassWithTransitiveTypeParameterBounds.NestedClass.class);
        assertNotNull(nestedClass);
        assertEquals(2, nestedClass.typeParameters().size());
        assertStaticClassA(nestedClass.typeParameters().get(0), Ann23.class);
        assertStaticClassB(nestedClass.typeParameters().get(1), Ann25.class);

        MethodInfo staticMethod = clazz.firstMethod("staticMethod");
        assertNotNull(staticMethod);
        assertEquals(2, staticMethod.typeParameters().size());
        assertStaticMethodA(staticMethod.typeParameters().get(0), Ann27.class);
        assertStaticMethodB(staticMethod.typeParameters().get(1), Ann29.class);

        ClassInfo staticLocalClass = index.getClassByName(staticLocalClassObject);
        assertNotNull(staticLocalClass);
        assertEquals(1, staticLocalClass.typeParameters().size());
        assertStaticMethodC(staticLocalClass.typeParameters().get(0), Ann31.class);
    }

    private static void assertA(TypeVariable a, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("A", a.identifier());
        assertTrue(a.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, a.bounds().size());
        assertEquals(Type.Kind.CLASS, a.bounds().get(0).kind());
        assertEquals("java.lang.Number", a.bounds().get(0).asClassType().name().toString());
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
        assertC(d.bounds().get(0).asTypeVariable(), Ann8.class);
    }

    private static void assertE(TypeVariable e, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("E", e.identifier());
        assertTrue(e.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, e.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, e.bounds().get(0).kind());
        assertD(e.bounds().get(0).asTypeVariable(), Ann10.class);
    }

    private static void assertF(TypeVariable f, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("F", f.identifier());
        assertTrue(f.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, f.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, f.bounds().get(0).kind());
        assertE(f.bounds().get(0).asTypeVariable(), Ann12.class);
    }

    private static void assertG(TypeVariable g, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("G", g.identifier());
        assertTrue(g.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, g.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, g.bounds().get(0).kind());
        assertF(g.bounds().get(0).asTypeVariable(), Ann14.class);
    }

    private static void assertH(TypeVariable h, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("H", h.identifier());
        assertTrue(h.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, h.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, h.bounds().get(0).kind());
        assertC(h.bounds().get(0).asTypeVariable(), Ann16.class);
    }

    private static void assertI(TypeVariable i, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("I", i.identifier());
        assertTrue(i.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, i.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, i.bounds().get(0).kind());
        assertH(i.bounds().get(0).asTypeVariable(), Ann18.class);
    }

    private static void assertJ(TypeVariable j, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("J", j.identifier());
        assertTrue(j.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, j.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, j.bounds().get(0).kind());
        assertC(j.bounds().get(0).asTypeVariable(), Ann20.class);
    }

    private static void assertK(TypeVariable k, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("K", k.identifier());
        assertTrue(k.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, k.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, k.bounds().get(0).kind());
        assertC(k.bounds().get(0).asTypeVariable(), Ann22.class);
    }

    private static void assertStaticClassA(TypeVariable a, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("A", a.identifier());
        assertTrue(a.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, a.bounds().size());
        assertEquals(Type.Kind.CLASS, a.bounds().get(0).kind());
        assertEquals("java.lang.Number", a.bounds().get(0).asClassType().name().toString());
        assertTrue(a.bounds().get(0).hasAnnotation(DotName.createSimple(Ann24.class.getName())));
    }

    private static void assertStaticClassB(TypeVariable b, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("B", b.identifier());
        assertTrue(b.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, b.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, b.bounds().get(0).kind());
        assertStaticClassA(b.bounds().get(0).asTypeVariable(), Ann26.class);
    }

    private static void assertStaticMethodA(TypeVariable a, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("A", a.identifier());
        assertTrue(a.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, a.bounds().size());
        assertEquals(Type.Kind.CLASS, a.bounds().get(0).kind());
        assertEquals("java.lang.Number", a.bounds().get(0).asClassType().name().toString());
        assertTrue(a.bounds().get(0).hasAnnotation(DotName.createSimple(Ann28.class.getName())));
    }

    private static void assertStaticMethodB(TypeVariable b, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("B", b.identifier());
        assertTrue(b.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, b.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, b.bounds().get(0).kind());
        assertStaticMethodA(b.bounds().get(0).asTypeVariable(), Ann30.class);
    }

    private static void assertStaticMethodC(TypeVariable c, Class<? extends Annotation> expectedAnnotation) {
        assertEquals("C", c.identifier());
        assertTrue(c.hasAnnotation(DotName.createSimple(expectedAnnotation.getName())));
        assertEquals(1, c.bounds().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, c.bounds().get(0).kind());
        assertStaticMethodB(c.bounds().get(0).asTypeVariable(), Ann32.class);
    }
}
