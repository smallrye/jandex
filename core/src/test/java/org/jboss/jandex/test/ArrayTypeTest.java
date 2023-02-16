package org.jboss.jandex.test;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayTypeTest {
    private String[][] simple;
    private String[] @MyAnnotation("array") [] annotated;

    private String @MyAnnotation("array") [] constituent; // just for easy comparison in the test

    @Test
    public void test() throws IOException {
        Index index = Index.of(ArrayTypeTest.class);
        doTest(index);
        doTest(IndexingUtil.roundtrip(index));
    }

    private void doTest(Index index) {
        ClassInfo clazz = index.getClassByName(ArrayTypeTest.class);
        assertSimpleType(clazz.field("simple").type());
        assertAnnotatedType(clazz.field("annotated").type(), clazz.field("constituent").type());
    }

    private void assertSimpleType(Type type) {
        assertEquals(Type.Kind.ARRAY, type.kind());
        ArrayType array = type.asArrayType();

        assertEquals("java.lang.String[][]", array.toString());

        // Jandex compressed representation
        assertEquals(2, array.dimensions());
        assertEquals(ClassType.create(String.class), array.constituent());

        // Java language representation
        assertEquals(2, array.deepDimensions());
        assertEquals(ClassType.create(String.class), array.elementType());
        assertEquals(ArrayType.create(ClassType.create(String.class), 1), array.componentType());
        assertEquals(Type.Kind.ARRAY, array.componentType().kind());
        assertEquals(1, array.componentType().asArrayType().dimensions());
        assertEquals(ClassType.create(String.class), array.componentType().asArrayType().elementType());
        assertEquals(ClassType.create(String.class), array.componentType().asArrayType().componentType());
    }

    private void assertAnnotatedType(Type type, Type constituent) {
        assertEquals(Type.Kind.ARRAY, type.kind());
        ArrayType array = type.asArrayType();

        assertEquals("java.lang.String[] @MyAnnotation(\"array\") []", array.toString());

        // Jandex compressed representation
        assertEquals(1, array.dimensions());
        assertEquals(constituent, array.constituent());
        assertEquals(Type.Kind.ARRAY, array.constituent().kind());
        assertEquals(1, array.constituent().asArrayType().dimensions());
        assertEquals(ClassType.create(String.class), array.constituent().asArrayType().constituent());

        // Java language representation
        assertEquals(2, array.deepDimensions());
        assertEquals(ClassType.create(String.class), array.elementType());
        assertEquals(constituent, array.componentType());
        assertEquals(Type.Kind.ARRAY, array.componentType().kind());
        assertEquals(1, array.componentType().asArrayType().dimensions());
        assertEquals(ClassType.create(String.class), array.componentType().asArrayType().elementType());
        assertEquals(ClassType.create(String.class), array.componentType().asArrayType().componentType());
    }
}
