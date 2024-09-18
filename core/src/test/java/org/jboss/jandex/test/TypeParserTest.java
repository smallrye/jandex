package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.VoidType;
import org.jboss.jandex.WildcardType;
import org.junit.jupiter.api.Test;

public class TypeParserTest {
    @Test
    public void testVoid() {
        assertCorrect("void", VoidType.VOID);
        assertCorrect(" void", VoidType.VOID);
        assertCorrect("void ", VoidType.VOID);
        assertCorrect(" void ", VoidType.VOID);
    }

    @Test
    public void testPrimitive() {
        assertCorrect("boolean", PrimitiveType.BOOLEAN);
        assertCorrect(" byte", PrimitiveType.BYTE);
        assertCorrect("short ", PrimitiveType.SHORT);
        assertCorrect(" int ", PrimitiveType.INT);
        assertCorrect("\tlong", PrimitiveType.LONG);
        assertCorrect("float\t", PrimitiveType.FLOAT);
        assertCorrect("\tdouble\t", PrimitiveType.DOUBLE);
        assertCorrect(" \n char \n ", PrimitiveType.CHAR);
    }

    @Test
    public void testPrimitiveArray() {
        assertCorrect("boolean[]", ArrayType.create(PrimitiveType.BOOLEAN, 1));
        assertCorrect("byte [][]", ArrayType.create(PrimitiveType.BYTE, 2));
        assertCorrect("short [] [] []", ArrayType.create(PrimitiveType.SHORT, 3));
        assertCorrect("int [ ] [ ] [ ] [ ]", ArrayType.create(PrimitiveType.INT, 4));
        assertCorrect("long   [][][]", ArrayType.create(PrimitiveType.LONG, 3));
        assertCorrect(" float[][]", ArrayType.create(PrimitiveType.FLOAT, 2));
        assertCorrect(" double [] ", ArrayType.create(PrimitiveType.DOUBLE, 1));
        assertCorrect(" char [ ][ ]  ", ArrayType.create(PrimitiveType.CHAR, 2));
    }

    @Test
    public void testClass() {
        assertCorrect("java.lang.Object", ClassType.OBJECT_TYPE);
        assertCorrect("java.lang.String", ClassType.create(DotName.STRING_NAME));

        assertCorrect(" java.lang.Boolean", ClassType.BOOLEAN_CLASS);
        assertCorrect("java.lang.Byte ", ClassType.BYTE_CLASS);
        assertCorrect(" java.lang.Short ", ClassType.SHORT_CLASS);
        assertCorrect("\tjava.lang.Integer", ClassType.INTEGER_CLASS);
        assertCorrect("java.lang.Long\t", ClassType.LONG_CLASS);
        assertCorrect("\tjava.lang.Float\t", ClassType.FLOAT_CLASS);
        assertCorrect("   java.lang.Double", ClassType.DOUBLE_CLASS);
        assertCorrect("java.lang.Character   ", ClassType.CHARACTER_CLASS);
    }

    @Test
    public void testClassArray() {
        assertCorrect("java.lang.Object[]", ArrayType.create(ClassType.OBJECT_TYPE, 1));
        assertCorrect("java.lang.String[][]", ArrayType.create(ClassType.create(DotName.STRING_NAME), 2));

        assertCorrect("java.lang.Boolean[][][]", ArrayType.create(ClassType.BOOLEAN_CLASS, 3));
        assertCorrect("java.lang.Byte[][][][]", ArrayType.create(ClassType.BYTE_CLASS, 4));
        assertCorrect("java.lang.Short[][][]", ArrayType.create(ClassType.SHORT_CLASS, 3));
        assertCorrect("java.lang.Integer[][]", ArrayType.create(ClassType.INTEGER_CLASS, 2));
        assertCorrect("java.lang.Long[]", ArrayType.create(ClassType.LONG_CLASS, 1));
        assertCorrect("java.lang.Float[][]", ArrayType.create(ClassType.FLOAT_CLASS, 2));
        assertCorrect("java.lang.Double[][][]", ArrayType.create(ClassType.DOUBLE_CLASS, 3));
        assertCorrect("java.lang.Character[][][][]", ArrayType.create(ClassType.CHARACTER_CLASS, 4));
    }

    @Test
    public void testParameterizedType() {
        assertCorrect("java.util.List<java.lang.Integer>",
                ParameterizedType.builder(List.class).addArgument(ClassType.INTEGER_CLASS).build());
        assertCorrect("java.util.Map<java.lang.Integer, int[]>",
                ParameterizedType.builder(Map.class)
                        .addArgument(ClassType.INTEGER_CLASS)
                        .addArgument(ArrayType.create(PrimitiveType.INT, 1))
                        .build());

        assertCorrect("java.util.List<? extends java.lang.Integer>",
                ParameterizedType.builder(List.class)
                        .addArgument(WildcardType.createUpperBound(ClassType.INTEGER_CLASS))
                        .build());
        assertCorrect("java.util.Map<? super int[][], java.util.List<?>>",
                ParameterizedType.builder(Map.class)
                        .addArgument(WildcardType.createLowerBound(ArrayType.create(PrimitiveType.INT, 2)))
                        .addArgument(ParameterizedType.builder(List.class).addArgument(WildcardType.UNBOUNDED).build())
                        .build());
    }

    @Test
    public void testParameterizedTypeArray() {
        assertCorrect("java.util.List<java.lang.Integer>[]",
                ArrayType.create(ParameterizedType.builder(List.class).addArgument(ClassType.INTEGER_CLASS).build(), 1));
        assertCorrect("java.util.Map<java.lang.Integer, int[]>[][]",
                ArrayType.create(ParameterizedType.builder(Map.class)
                        .addArgument(ClassType.INTEGER_CLASS)
                        .addArgument(ArrayType.create(PrimitiveType.INT, 1))
                        .build(), 2));
    }

    @Test
    public void testIncorrect() {
        assertIncorrect("");
        assertIncorrect(" ");
        assertIncorrect("\t");
        assertIncorrect("    ");
        assertIncorrect("  \n  ");

        assertIncorrect(".");
        assertIncorrect(",");
        assertIncorrect("[");
        assertIncorrect("]");
        assertIncorrect("<");
        assertIncorrect(">");

        assertIncorrect("int.");
        assertIncorrect("int,");
        assertIncorrect("int[");
        assertIncorrect("int]");
        assertIncorrect("int[[]");
        assertIncorrect("int[][");
        assertIncorrect("int[]]");
        assertIncorrect("int[0]");
        assertIncorrect("int<");
        assertIncorrect("int>");
        assertIncorrect("int<>");

        assertIncorrect("java.util.List<");
        assertIncorrect("java.util.List<>");
        assertIncorrect("java.util.List<java.lang.Integer");
        assertIncorrect("java.util.List<java.lang.Integer>>");
        assertIncorrect("java.util.List<java.util.List<java.lang.Integer");
        assertIncorrect("java.util.List<java.util.List<java.lang.Integer>");
        assertIncorrect("java.util.List<java.util.List<java.lang.Integer>>>");

        assertIncorrect("java.util.List<int>");
        assertIncorrect("java.util.Map<int, long>");

        assertIncorrect("java.lang.Integer.");
        assertIncorrect("java .lang.Integer");
        assertIncorrect("java. lang.Integer");
        assertIncorrect("java . lang.Integer");
        assertIncorrect(".java.lang.Integer");
        assertIncorrect(".java.lang.Integer.");

        assertIncorrect("java.lang.Integer[");
        assertIncorrect("java.lang.Integer[[]");
        assertIncorrect("java.lang.Integer[][");
        assertIncorrect("java.lang.Integer[]]");
        assertIncorrect("java.lang.Integer[0]");
    }

    private void assertCorrect(String str, Type expectedType) {
        assertEquals(expectedType, Type.parse(str));
    }

    private void assertIncorrect(String str) {
        assertThrows(IllegalArgumentException.class, () -> Type.parse(str));
    }
}
