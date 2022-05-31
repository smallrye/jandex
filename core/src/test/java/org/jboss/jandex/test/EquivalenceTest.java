package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.EquivalenceKey;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EquivalenceTest {
    static class A<T extends Appendable> {
        int a;

        void a(String str, int[] i, List<? extends Number> listUpperBound, List<? super Integer> listLowerBound) {
        }

        <U extends Number & CharSequence & Serializable> U b(U arg, U[] args, List<Class<?>> listUnbounded) {
            return null;
        }

        private T c() {
            return null;
        }
    }

    static class B {
        static class A<T extends Appendable> {
            int a;

            public void a(String str, int[] i, List<? extends Number> listUpperBound, List<? super Integer> listLowerBound) {
            }

            <U extends Number & CharSequence & Serializable> U b(U arg, U[] args, List<Class<?>> listUnbounded) {
                return null;
            }

            T c() {
                return null;
            }
        }

        double z;

        double z(short arg) {
            return 0.0;
        }
    }

    static class Types {
        void voidMethod() {
        }

        int primitiveMethod() {
            return 0;
        }

        String classMethod() {
            return null;
        }

        List<String> parameterizedTypeMethod() {
            return null;
        }

        <T> T typeVariableMethod() {
            return null;
        }

        <T extends Number> T typeVariableWithBoundMethod() {
            return null;
        }

        <T extends Serializable & Comparable<T>> T typeVariableWithMultipleBoundsMethod() {
            return null;
        }

        int[] primitiveArrayMethod() {
            return null;
        }

        String[] classArrayMethod() {
            return null;
        }

        List<String>[] parameterizedTypeArrayMethod() {
            return null;
        }

        <T> T[] typeVariableArrayMethod() {
            return null;
        }

        List<?> unboundedWildcardMethod() {
            return null;
        }

        List<? extends Number> wildcardWithUpperBoundMethod() {
            return null;
        }

        List<? super String> wildcardWithLowerBoundMethod() {
            return null;
        }
    }

    private Index index;

    @BeforeEach
    public void setUp() throws IOException {
        index = Index.of(A.class, B.class, B.A.class, Types.class);
    }

    @Test
    public void classes() {
        ClassInfo a1 = index.getClassByName(DotName.createSimple(A.class.getName()));
        ClassInfo a2 = index.getClassByName(DotName.createSimple(B.A.class.getName()));
        ClassInfo b = index.getClassByName(DotName.createSimple(B.class.getName()));

        EquivalenceKey keyA1 = EquivalenceKey.of(a1);
        EquivalenceKey keyA2 = EquivalenceKey.of(a2);
        EquivalenceKey keyB = EquivalenceKey.of(b);

        assertEquals(keyA1, EquivalenceKey.of(a1));
        assertEquals(keyA1.hashCode(), EquivalenceKey.of(a1).hashCode());
        assertEquals("class org.jboss.jandex.test.EquivalenceTest$A", keyA1.toString());

        assertNotEquals(keyA1, keyA2);
        assertEquals("class org.jboss.jandex.test.EquivalenceTest$B$A", keyA2.toString());

        assertNotEquals(keyA1, keyB);
        assertEquals("class org.jboss.jandex.test.EquivalenceTest$B", keyB.toString());

        Map<DotName, List<AnnotationInstance>> annotations = new HashMap<>();
        annotations.put(MyAnnotation.DOT_NAME,
                Collections.singletonList(AnnotationInstance.create(MyAnnotation.DOT_NAME, null, new AnnotationValue[0])));
        ClassInfo bWithAnnotations = ClassInfo.create(b.name(), b.superName(), b.flags(), b.interfaces(), annotations,
                b.hasNoArgsConstructor());
        EquivalenceKey keyBWithAnnotations = EquivalenceKey.of(bWithAnnotations);

        assertNotEquals(b, bWithAnnotations);
        assertEquals(keyB, keyBWithAnnotations);
        assertEquals(keyB.hashCode(), keyBWithAnnotations.hashCode());
        assertEquals(keyB.toString(), keyBWithAnnotations.toString());
    }

    @Test
    public void methods() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ClassInfo class1 = index.getClassByName(DotName.createSimple(A.class.getName()));
        ClassInfo class2 = index.getClassByName(DotName.createSimple(B.A.class.getName()));

        MethodInfo a1 = class1.firstMethod("a");
        MethodInfo a2 = class2.firstMethod("a");

        EquivalenceKey keyA1 = EquivalenceKey.of(a1);
        EquivalenceKey keyA2 = EquivalenceKey.of(a2);

        assertEquals(keyA1, EquivalenceKey.of(a1));
        assertEquals(keyA1.hashCode(), EquivalenceKey.of(a1).hashCode());
        assertEquals(
                "method org.jboss.jandex.test.EquivalenceTest$A#a(java.lang.String, int[], java.util.List<? extends java.lang.Number>, java.util.List<? super java.lang.Integer>) -> void",
                keyA1.toString());

        assertNotEquals(keyA1, keyA2);
        assertEquals(
                "method org.jboss.jandex.test.EquivalenceTest$B$A#a(java.lang.String, int[], java.util.List<? extends java.lang.Number>, java.util.List<? super java.lang.Integer>) -> void",
                keyA2.toString());

        MethodInfo b1 = class1.firstMethod("b");
        MethodInfo b2 = class2.firstMethod("b");

        EquivalenceKey keyB1 = EquivalenceKey.of(b1);
        EquivalenceKey keyB2 = EquivalenceKey.of(b2);

        assertEquals(keyB1, EquivalenceKey.of(b1));
        assertEquals(keyB1.hashCode(), EquivalenceKey.of(b1).hashCode());
        assertEquals(
                "method org.jboss.jandex.test.EquivalenceTest$A#b(U, U[], java.util.List<java.lang.Class<?>>) -> U where U extends java.lang.Number & java.lang.CharSequence & java.io.Serializable",
                keyB1.toString());

        assertNotEquals(keyB1, keyB2);
        assertEquals(
                "method org.jboss.jandex.test.EquivalenceTest$B$A#b(U, U[], java.util.List<java.lang.Class<?>>) -> U where U extends java.lang.Number & java.lang.CharSequence & java.io.Serializable",
                keyB2.toString());

        MethodInfo c1 = class1.firstMethod("c");
        MethodInfo c2 = class2.firstMethod("c");

        EquivalenceKey keyC1 = EquivalenceKey.of(c1);
        EquivalenceKey keyC2 = EquivalenceKey.of(c2);

        assertEquals(keyC1, EquivalenceKey.of(c1));
        assertEquals(keyC1.hashCode(), EquivalenceKey.of(c1).hashCode());
        assertEquals("method org.jboss.jandex.test.EquivalenceTest$A#c() -> T where T extends java.lang.Appendable",
                keyC1.toString());

        assertNotEquals(keyC1, keyC2);
        assertEquals("method org.jboss.jandex.test.EquivalenceTest$B$A#c() -> T where T extends java.lang.Appendable",
                keyC2.toString());

        MethodInfo z = index.getClassByName(DotName.createSimple(B.class.getName())).firstMethod("z");
        MethodInfo zWithAnnotations = MethodInfo.create(z.declaringClass(), z.name(),
                z.parameterTypes().toArray(Type.EMPTY_ARRAY),
                z.returnType(), z.flags());
        List<AnnotationInstance> annotations = Collections.singletonList(
                AnnotationInstance.create(MyAnnotation.DOT_NAME, null, new AnnotationValue[0]));
        Method setAnnotations = zWithAnnotations.getClass().getDeclaredMethod("setAnnotations", List.class);
        setAnnotations.setAccessible(true);
        setAnnotations.invoke(zWithAnnotations, annotations);

        EquivalenceKey keyZ = EquivalenceKey.of(z);
        EquivalenceKey keyZWithAnnotations = EquivalenceKey.of(zWithAnnotations);

        assertNotEquals(z, zWithAnnotations);
        assertEquals(keyZ, keyZWithAnnotations);
        assertEquals(keyZ.hashCode(), keyZWithAnnotations.hashCode());
        assertEquals(keyZ.toString(), keyZWithAnnotations.toString());
    }

    @Test
    public void methodParameters() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ClassInfo class1 = index.getClassByName(DotName.createSimple(A.class.getName()));
        ClassInfo class2 = index.getClassByName(DotName.createSimple(B.A.class.getName()));

        for (short i = 0; i < 4; i++) {
            MethodParameterInfo a1 = MethodParameterInfo.create(class1.firstMethod("a"), i);
            MethodParameterInfo a2 = MethodParameterInfo.create(class2.firstMethod("a"), i);

            EquivalenceKey keyA1 = EquivalenceKey.of(a1);
            EquivalenceKey keyA2 = EquivalenceKey.of(a2);

            assertEquals(keyA1, EquivalenceKey.of(a1));
            assertEquals(keyA1.hashCode(), EquivalenceKey.of(a1).hashCode());
            assertEquals("parameter " + i
                    + " of method org.jboss.jandex.test.EquivalenceTest$A#a(java.lang.String, int[], java.util.List<? extends java.lang.Number>, java.util.List<? super java.lang.Integer>) -> void",
                    keyA1.toString());

            assertNotEquals(keyA1, keyA2);
            assertEquals("parameter " + i
                    + " of method org.jboss.jandex.test.EquivalenceTest$B$A#a(java.lang.String, int[], java.util.List<? extends java.lang.Number>, java.util.List<? super java.lang.Integer>) -> void",
                    keyA2.toString());
        }

        for (short i = 0; i < 3; i++) {
            MethodParameterInfo b1 = MethodParameterInfo.create(class1.firstMethod("b"), i);
            MethodParameterInfo b2 = MethodParameterInfo.create(class2.firstMethod("b"), i);

            EquivalenceKey keyB1 = EquivalenceKey.of(b1);
            EquivalenceKey keyB2 = EquivalenceKey.of(b2);

            assertEquals(keyB1, EquivalenceKey.of(b1));
            assertEquals(keyB1.hashCode(), EquivalenceKey.of(b1).hashCode());
            assertEquals("parameter " + i
                    + " of method org.jboss.jandex.test.EquivalenceTest$A#b(U, U[], java.util.List<java.lang.Class<?>>) -> U where U extends java.lang.Number & java.lang.CharSequence & java.io.Serializable",
                    keyB1.toString());

            assertNotEquals(keyB1, keyB2);
            assertEquals("parameter " + i
                    + " of method org.jboss.jandex.test.EquivalenceTest$B$A#b(U, U[], java.util.List<java.lang.Class<?>>) -> U where U extends java.lang.Number & java.lang.CharSequence & java.io.Serializable",
                    keyB2.toString());
        }

        MethodInfo z = index.getClassByName(DotName.createSimple(B.class.getName())).firstMethod("z");
        MethodInfo zWithAnnotations = MethodInfo.create(z.declaringClass(), z.name(),
                z.parameterTypes().toArray(Type.EMPTY_ARRAY),
                z.returnType(), z.flags());
        List<AnnotationInstance> annotations = Collections.singletonList(
                AnnotationInstance.create(MyAnnotation.DOT_NAME,
                        MethodParameterInfo.create(z, (short) 0), new AnnotationValue[0]));
        Method setAnnotations = zWithAnnotations.getClass().getDeclaredMethod("setAnnotations", List.class);
        setAnnotations.setAccessible(true);
        setAnnotations.invoke(zWithAnnotations, annotations);

        EquivalenceKey keyZ = EquivalenceKey.of(z);
        EquivalenceKey keyZWithAnnotations = EquivalenceKey.of(zWithAnnotations);

        assertNotEquals(z, zWithAnnotations);
        assertEquals(keyZ, keyZWithAnnotations);
        assertEquals(keyZ.hashCode(), keyZWithAnnotations.hashCode());
        assertEquals(keyZ.toString(), keyZWithAnnotations.toString());

        EquivalenceKey keyZP = EquivalenceKey.of(MethodParameterInfo.create(z, (short) 0));
        EquivalenceKey keyZPWithAnnotations = EquivalenceKey.of(MethodParameterInfo.create(zWithAnnotations, (short) 0));

        assertEquals(keyZP, keyZPWithAnnotations);
        assertEquals(keyZP.hashCode(), keyZPWithAnnotations.hashCode());
        assertEquals(keyZP.toString(), keyZPWithAnnotations.toString());
    }

    @Test
    public void fields() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        FieldInfo a1 = index.getClassByName(DotName.createSimple(A.class.getName())).field("a");
        FieldInfo a2 = index.getClassByName(DotName.createSimple(B.A.class.getName())).field("a");

        EquivalenceKey keyA1 = EquivalenceKey.of(a1);
        EquivalenceKey keyA2 = EquivalenceKey.of(a2);

        assertEquals(keyA1, EquivalenceKey.of(a1));
        assertEquals(keyA1.hashCode(), EquivalenceKey.of(a1).hashCode());
        assertEquals("field org.jboss.jandex.test.EquivalenceTest$A#a of type int", keyA1.toString());

        assertNotEquals(keyA1, keyA2);
        assertEquals("field org.jboss.jandex.test.EquivalenceTest$B$A#a of type int", keyA2.toString());

        FieldInfo z = index.getClassByName(DotName.createSimple(B.class.getName())).field("z");
        FieldInfo zWithAnnotations = FieldInfo.create(z.declaringClass(), z.name(), z.type(), z.flags());
        List<AnnotationInstance> annotations = Collections.singletonList(
                AnnotationInstance.create(MyAnnotation.DOT_NAME, null, new AnnotationValue[0]));
        Method setAnnotations = zWithAnnotations.getClass().getDeclaredMethod("setAnnotations", List.class);
        setAnnotations.setAccessible(true);
        setAnnotations.invoke(zWithAnnotations, annotations);

        EquivalenceKey keyZ = EquivalenceKey.of(z);
        EquivalenceKey keyZWithAnnotations = EquivalenceKey.of(zWithAnnotations);

        assertNotEquals(z, zWithAnnotations);
        assertEquals(keyZ, keyZWithAnnotations);
        assertEquals(keyZ.hashCode(), keyZWithAnnotations.hashCode());
        assertEquals(keyZ.toString(), keyZWithAnnotations.toString());
    }

    @Test
    public void mixed() {
        ClassInfo a1 = index.getClassByName(DotName.createSimple(A.class.getName()));
        ClassInfo a2 = index.getClassByName(DotName.createSimple(B.A.class.getName()));

        MethodInfo ma1 = a1.firstMethod("a");
        MethodInfo ma2 = a2.firstMethod("a");
        FieldInfo fa1 = a1.field("a");
        FieldInfo fa2 = a2.field("a");

        assertNotEquals(a1, ma1);
        assertNotEquals(a1, ma2);
        assertNotEquals(a1, fa1);
        assertNotEquals(a1, fa2);
        assertNotEquals(a2, ma1);
        assertNotEquals(a2, ma2);
        assertNotEquals(a2, fa1);
        assertNotEquals(a2, fa2);
        assertNotEquals(ma1, fa1);
        assertNotEquals(ma1, fa2);
        assertNotEquals(ma2, fa1);
        assertNotEquals(ma2, fa2);
    }

    @Test
    public void typeToString() {
        ClassInfo types = index.getClassByName(Types.class);

        assertEquals("void", equivalenceKeyStringOf(types, "voidMethod"));
        assertEquals("int", equivalenceKeyStringOf(types, "primitiveMethod"));
        assertEquals("java.lang.String", equivalenceKeyStringOf(types, "classMethod"));
        assertEquals("java.util.List<java.lang.String>", equivalenceKeyStringOf(types, "parameterizedTypeMethod"));
        assertEquals("T extends java.lang.Object", equivalenceKeyStringOf(types, "typeVariableMethod"));
        assertEquals("T extends java.lang.Number", equivalenceKeyStringOf(types, "typeVariableWithBoundMethod"));
        assertEquals("T extends java.io.Serializable & java.lang.Comparable<T>",
                equivalenceKeyStringOf(types, "typeVariableWithMultipleBoundsMethod"));
        assertEquals("int[]", equivalenceKeyStringOf(types, "primitiveArrayMethod"));
        assertEquals("java.lang.String[]", equivalenceKeyStringOf(types, "classArrayMethod"));
        assertEquals("java.util.List<java.lang.String>[]", equivalenceKeyStringOf(types, "parameterizedTypeArrayMethod"));
        assertEquals("T[] where T extends java.lang.Object", equivalenceKeyStringOf(types, "typeVariableArrayMethod"));

        assertEquals("?", EquivalenceKey.of(types.firstMethod("unboundedWildcardMethod")
                .returnType().asParameterizedType().arguments().get(0)).toString());
        assertEquals("? extends java.lang.Number", EquivalenceKey.of(types.firstMethod("wildcardWithUpperBoundMethod")
                .returnType().asParameterizedType().arguments().get(0)).toString());
        assertEquals("? super java.lang.String", EquivalenceKey.of(types.firstMethod("wildcardWithLowerBoundMethod")
                .returnType().asParameterizedType().arguments().get(0)).toString());
    }

    private static String equivalenceKeyStringOf(ClassInfo typesClass, String methodName) {
        return EquivalenceKey.of(typesClass.firstMethod(methodName).returnType()).toString();
    }
}
