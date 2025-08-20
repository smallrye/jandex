package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.Index;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class ArrayTypeTest {

    String[][] simple;
    String[] @MyAnnotation("array") [] annotated;
    String @MyAnnotation("array") [] constituent; // just for easy comparison in the test

    String[] jjj;
    boolean[][] kkk;

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

    @Test
    public void testArrayTypeBuilder() throws IOException {
        Index index = Index.of(ArrayTypeTest.class);
        ClassInfo testClass = index.getClassByName(ArrayTypeTest.class);

        assertEquals(ArrayType.create(ClassType.create(Number.class), 3),
                ArrayType.builder(ClassType.create(Number.class), 3).build());

        Type jjjType = testClass.field("jjj").type();
        assertEquals(jjjType, ArrayType.builder(ClassType.create(String.class), 1).build());
        Type kkkType = testClass.field("kkk").type();
        assertEquals(kkkType, ArrayType.builder(PrimitiveType.BOOLEAN, 2).build());
    }

    @Test
    public void testPrecomputedNames() {
        assertEquals("[Z", ArrayType.create(PrimitiveType.BOOLEAN, 1).name().toString());
        assertEquals("[B", ArrayType.create(PrimitiveType.BYTE, 1).name().toString());
        assertEquals("[S", ArrayType.create(PrimitiveType.SHORT, 1).name().toString());
        assertEquals("[I", ArrayType.create(PrimitiveType.INT, 1).name().toString());
        assertEquals("[J", ArrayType.create(PrimitiveType.LONG, 1).name().toString());
        assertEquals("[F", ArrayType.create(PrimitiveType.FLOAT, 1).name().toString());
        assertEquals("[D", ArrayType.create(PrimitiveType.DOUBLE, 1).name().toString());
        assertEquals("[C", ArrayType.create(PrimitiveType.CHAR, 1).name().toString());

        assertEquals("[Ljava.lang.Boolean;", ArrayType.create(ClassType.BOOLEAN_CLASS, 1).name().toString());
        assertEquals("[Ljava.lang.Byte;", ArrayType.create(ClassType.BYTE_CLASS, 1).name().toString());
        assertEquals("[Ljava.lang.Short;", ArrayType.create(ClassType.SHORT_CLASS, 1).name().toString());
        assertEquals("[Ljava.lang.Integer;", ArrayType.create(ClassType.INTEGER_CLASS, 1).name().toString());
        assertEquals("[Ljava.lang.Long;", ArrayType.create(ClassType.LONG_CLASS, 1).name().toString());
        assertEquals("[Ljava.lang.Float;", ArrayType.create(ClassType.FLOAT_CLASS, 1).name().toString());
        assertEquals("[Ljava.lang.Double;", ArrayType.create(ClassType.DOUBLE_CLASS, 1).name().toString());
        assertEquals("[Ljava.lang.Character;", ArrayType.create(ClassType.CHARACTER_CLASS, 1).name().toString());

        assertEquals("[Ljava.lang.Object;", ArrayType.create(ClassType.OBJECT_TYPE, 1).name().toString());
        assertEquals("[Ljava.lang.String;", ArrayType.create(ClassType.STRING_TYPE, 1).name().toString());
        assertEquals("[Ljava.lang.Class;", ArrayType.create(ClassType.CLASS_TYPE, 1).name().toString());
        assertEquals("[Ljava.lang.annotation.Annotation;", ArrayType.create(ClassType.ANNOTATION_TYPE, 1).name().toString());

        assertEquals("[Ljava.util.List;",
                ArrayType.create(ParameterizedType.create(List.class, ClassType.create(String.class)), 1).name().toString());

        assertEquals("[Ljava.lang.Object;", ArrayType.create(TypeVariable.create("T"), 1).name().toString());
        assertEquals("[Ljava.lang.Number;",
                ArrayType.create(TypeVariable.builder("T").addBound(Number.class).build(), 1).name().toString());
    }
}
