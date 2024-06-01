package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeToStringTest {
    interface TestInterface<T> {
    }

    static class TestClass<A extends Exception, B extends A, C extends B>
            extends ArrayList<C> implements TestInterface<B> {

        C field1;

        List<? extends C> field2;

        C[][] field3;

        <D extends C, E extends D> List<E> method(TestClass<A, B, C> this, E[][] param1, List<? super E> param2) throws D {
            return null;
        }

        class InnerClass<F extends C, G extends F> implements TestInterface<G> {
            G field;
        }
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(TestClass.class, TestClass.InnerClass.class);
        test(index);
        test(IndexingUtil.roundtrip(index, "05d34649c84c8958e5852f1874d6c3d922d91f546e1fc28421ec05f540da703f"));
    }

    private void test(Index index) {
        ClassInfo clazz = index.getClassByName(TestClass.class);
        assertNotNull(clazz);
        assertEquals(3, clazz.typeParameters().size());
        assertA(clazz.typeParameters().get(0));
        assertB(clazz.typeParameters().get(1));
        assertC(clazz.typeParameters().get(2));

        Type superClass = clazz.superClassType();
        assertEquals("java.util.ArrayList<C>", superClass.toString());
        assertC(superClass.asParameterizedType().arguments().get(0));

        assertEquals(1, clazz.interfaceTypes().size());
        Type superInterface = clazz.interfaceTypes().get(0);
        assertEquals("org.jboss.jandex.test.TypeToStringTest$TestInterface<B>", superInterface.toString());
        assertB(superInterface.asParameterizedType().arguments().get(0));

        FieldInfo field1 = clazz.field("field1");
        assertNotNull(field1);
        assertEquals("C org.jboss.jandex.test.TypeToStringTest$TestClass.field1", field1.toString());
        assertC(field1.type());

        FieldInfo field2 = clazz.field("field2");
        assertNotNull(field2);
        assertEquals("java.util.List<? extends C> org.jboss.jandex.test.TypeToStringTest$TestClass.field2", field2.toString());
        assertEquals("java.util.List<? extends C>", field2.type().toString());
        assertEquals("? extends C", field2.type().asParameterizedType().arguments().get(0).toString());
        assertC(field2.type().asParameterizedType().arguments().get(0).asWildcardType().extendsBound());

        FieldInfo field3 = clazz.field("field3");
        assertNotNull(field3);
        assertEquals("C[][] org.jboss.jandex.test.TypeToStringTest$TestClass.field3", field3.toString());
        assertEquals("C[][]", field3.type().toString());
        assertC(field3.type().asArrayType().component());

        MethodInfo method = clazz.firstMethod("method");
        assertNotNull(method);
        assertEquals("java.util.List<E> method(E[][] param1, java.util.List<? super E> param2) throws D", method.toString());

        assertEquals(2, method.typeParameters().size());
        assertD(method.typeParameters().get(0));
        assertE(method.typeParameters().get(1));

        Type returnType = method.returnType();
        assertNotNull(returnType);
        assertEquals("java.util.List<E>", returnType.toString());
        assertE(returnType.asParameterizedType().arguments().get(0));

        Type receiver = method.receiverType();
        assertNotNull(receiver);
        assertEquals("org.jboss.jandex.test.TypeToStringTest$TestClass<A, B, C>", receiver.toString());
        assertA(receiver.asParameterizedType().arguments().get(0));
        assertB(receiver.asParameterizedType().arguments().get(1));
        assertC(receiver.asParameterizedType().arguments().get(2));

        assertEquals(2, method.parametersCount());
        assertEquals("E[][]", method.parameterType(0).toString());
        assertE(method.parameterType(0).asArrayType().component());
        assertEquals("java.util.List<? super E>", method.parameterType(1).toString());
        assertEquals("? super E", method.parameterType(1).asParameterizedType().arguments().get(0).toString());
        assertE(method.parameterType(1).asParameterizedType().arguments().get(0).asWildcardType().superBound());

        assertEquals(1, method.exceptions().size());
        assertD(method.exceptions().get(0));

        ClassInfo inner = index.getClassByName(TestClass.InnerClass.class);
        assertNotNull(inner);
        assertEquals(2, inner.typeParameters().size());
        assertF(inner.typeParameters().get(0));
        assertG(inner.typeParameters().get(1));

        assertEquals(1, inner.interfaceTypes().size());
        Type innerSuperInterface = inner.interfaceTypes().get(0);
        assertEquals("org.jboss.jandex.test.TypeToStringTest$TestInterface<G>", innerSuperInterface.toString());
        assertG(innerSuperInterface.asParameterizedType().arguments().get(0).asTypeVariable());

        FieldInfo innerField = inner.field("field");
        assertNotNull(innerField);
        assertEquals("G org.jboss.jandex.test.TypeToStringTest$TestClass$InnerClass.field", innerField.toString());
        assertEquals("G extends F", innerField.type().toString());
        assertG(innerField.type().asTypeVariable());
    }

    private void assertA(Type a) {
        assertEquals("A extends java.lang.Exception", a.toString());
    }

    private void assertB(Type b) {
        assertEquals("B extends A", b.toString());
        assertA(b.asTypeVariable().bounds().get(0));
    }

    private void assertC(Type c) {
        assertEquals("C extends B", c.toString());
        assertB(c.asTypeVariable().bounds().get(0));
    }

    private void assertD(Type d) {
        assertEquals("D extends C", d.toString());
        assertC(d.asTypeVariable().bounds().get(0));
    }

    private void assertE(Type e) {
        assertEquals("E extends D", e.toString());
        assertD(e.asTypeVariable().bounds().get(0));
    }

    private void assertF(Type f) {
        assertEquals("F extends C", f.toString());
        assertC(f.asTypeVariable().bounds().get(0));
    }

    private void assertG(Type g) {
        assertEquals("G extends F", g.toString());
        assertF(g.asTypeVariable().bounds().get(0));
    }
}
