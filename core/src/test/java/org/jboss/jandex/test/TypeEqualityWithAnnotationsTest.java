package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeEqualityWithAnnotationsTest {
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

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann39 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann40 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann41 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann42 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann43 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann44 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann45 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann46 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann47 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann48 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann49 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann50 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann51 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann52 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann53 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann54 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann55 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann56 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann57 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann58 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann59 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann60 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann61 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann62 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann63 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann64 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann65 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann66 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann67 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann68 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann69 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann70 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann71 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann72 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann73 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann74 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann75 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann76 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann77 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann78 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann79 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann80 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann81 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann82 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann83 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann84 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann85 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann86 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann87 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann88 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann89 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann90 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann91 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann92 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann93 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann94 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann95 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann96 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann97 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann98 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann99 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann100 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann101 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann102 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann103 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann104 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann105 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann106 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann107 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann108 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann109 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann110 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann111 {
    }

    static class A<@Ann1 T, @Ann2 U extends Comparable<@Ann3 U>, @Ann4 V extends @Ann5 List<@Ann6 W>, @Ann7 W extends @Ann8 List<@Ann9 V>> {
        @Ann10
        int primitive;
        @Ann11
        String clazz;
        @Ann12
        int @Ann13 [] arrayOfPrimitive;
        @Ann14
        String @Ann15 [] @Ann16 [] arrayOfClass;
        @Ann17
        List<@Ann18 String> parameterized;
        @Ann19
        Map<@Ann20 String, @Ann21 List<@Ann22 String>> @Ann23 [] arrayOfParameterized;
        @Ann24
        T typeVariable;
        @Ann25
        T @Ann26 [] arrayOfTypeVariable;
        @Ann27
        U recursiveTypeVariable;
        @Ann28
        U @Ann29 [] arrayOfRecursiveTypeVariable;
        @Ann30
        W indirectlyRecursiveTypeVariable;
        @Ann31
        W @Ann32 [] arrayOfIndirectlyRecursiveTypeVariable;
        @Ann33
        List<@Ann34 ? extends @Ann35 Number> parameterizedWithClassWildcard;
        @Ann36
        List<@Ann37 ? extends @Ann38 Number @Ann39 []> parameterizedWithArrayWildcard;
        @Ann40
        List<@Ann41 ? extends @Ann42 T> parameterizedWithTypeVariableWildcard;
        @Ann43
        List<@Ann44 ? extends @Ann45 T @Ann46 []> parameterizedWithTypeVariableArrayWildcard;
        @Ann47
        List<@Ann48 ? extends @Ann49 U> parameterizedWithRecursiveTypeVariableWildcard;
        @Ann50
        List<@Ann51 ? extends @Ann52 U @Ann53 []> parameterizedWithRecursiveTypeVariableArrayWildcard;
        @Ann54
        List<@Ann55 ? extends @Ann56 W> parameterizedWithIndirectlyRecursiveTypeVariableWildcard;
        @Ann57
        List<@Ann58 ? extends @Ann59 W @Ann60 []> parameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard;
        @Ann61
        Map<@Ann62 ?, @Ann63 List<@Ann64 ? super @Ann65 Number>> @Ann66 [] arrayOfParameterizedWithClassWildcard;
        @Ann67
        Map<@Ann68 ?, @Ann69 List<@Ann70 ? super @Ann71 Number[]>> @Ann72 [] arrayOfParameterizedWithArrayWildcard;
        @Ann73
        Map<@Ann74 ?, @Ann75 List<@Ann76 ? super @Ann77 T>> @Ann78 [] arrayOfParameterizedWithTypeVariableWildcard;
        @Ann79
        Map<@Ann80 ?, @Ann81 List<@Ann82 ? super @Ann83 T @Ann84 []>> @Ann85 [] arrayOfParameterizedWithTypeVariableArrayWildcard;
        @Ann86
        Map<@Ann87 ?, @Ann88 List<@Ann89 ? super @Ann90 U>> @Ann91 [] arrayOfParameterizedWithRecursiveTypeVariableWildcard;
        @Ann92
        Map<@Ann93 ?, @Ann94 List<@Ann95 ? super @Ann96 U @Ann97 []>> @Ann98 [] arrayOfParameterizedWithRecursiveTypeVariableArrayWildcard;
        @Ann99
        Map<@Ann100 ?, @Ann101 List<@Ann102 ? super @Ann103 W>> @Ann104 [] arrayOfParameterizedWithIndirectlyRecursiveTypeVariableWildcard;
        @Ann105
        Map<@Ann106 ?, @Ann107 List<@Ann108 ? super @Ann109 W @Ann110 []>> @Ann111 [] arrayOfParameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard;
    }

    static class B<@Ann1 T, @Ann2 U extends Comparable<@Ann3 U>, @Ann4 V extends @Ann5 List<@Ann6 W>, @Ann7 W extends @Ann8 List<@Ann9 V>> {
        @Ann10
        int primitive;
        @Ann11
        String clazz;
        @Ann12
        int @Ann13 [] arrayOfPrimitive;
        @Ann14
        String @Ann15 [] @Ann16 [] arrayOfClass;
        @Ann17
        List<@Ann18 String> parameterized;
        @Ann19
        Map<@Ann20 String, @Ann21 List<@Ann22 String>> @Ann23 [] arrayOfParameterized;
        @Ann24
        T typeVariable;
        @Ann25
        T @Ann26 [] arrayOfTypeVariable;
        @Ann27
        U recursiveTypeVariable;
        @Ann28
        U @Ann29 [] arrayOfRecursiveTypeVariable;
        @Ann30
        W indirectlyRecursiveTypeVariable;
        @Ann31
        W @Ann32 [] arrayOfIndirectlyRecursiveTypeVariable;
        @Ann33
        List<@Ann34 ? extends @Ann35 Number> parameterizedWithClassWildcard;
        @Ann36
        List<@Ann37 ? extends @Ann38 Number @Ann39 []> parameterizedWithArrayWildcard;
        @Ann40
        List<@Ann41 ? extends @Ann42 T> parameterizedWithTypeVariableWildcard;
        @Ann43
        List<@Ann44 ? extends @Ann45 T @Ann46 []> parameterizedWithTypeVariableArrayWildcard;
        @Ann47
        List<@Ann48 ? extends @Ann49 U> parameterizedWithRecursiveTypeVariableWildcard;
        @Ann50
        List<@Ann51 ? extends @Ann52 U @Ann53 []> parameterizedWithRecursiveTypeVariableArrayWildcard;
        @Ann54
        List<@Ann55 ? extends @Ann56 W> parameterizedWithIndirectlyRecursiveTypeVariableWildcard;
        @Ann57
        List<@Ann58 ? extends @Ann59 W @Ann60 []> parameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard;
        @Ann61
        Map<@Ann62 ?, @Ann63 List<@Ann64 ? super @Ann65 Number>> @Ann66 [] arrayOfParameterizedWithClassWildcard;
        @Ann67
        Map<@Ann68 ?, @Ann69 List<@Ann70 ? super @Ann71 Number[]>> @Ann72 [] arrayOfParameterizedWithArrayWildcard;
        @Ann73
        Map<@Ann74 ?, @Ann75 List<@Ann76 ? super @Ann77 T>> @Ann78 [] arrayOfParameterizedWithTypeVariableWildcard;
        @Ann79
        Map<@Ann80 ?, @Ann81 List<@Ann82 ? super @Ann83 T @Ann84 []>> @Ann85 [] arrayOfParameterizedWithTypeVariableArrayWildcard;
        @Ann86
        Map<@Ann87 ?, @Ann88 List<@Ann89 ? super @Ann90 U>> @Ann91 [] arrayOfParameterizedWithRecursiveTypeVariableWildcard;
        @Ann92
        Map<@Ann93 ?, @Ann94 List<@Ann95 ? super @Ann96 U @Ann97 []>> @Ann98 [] arrayOfParameterizedWithRecursiveTypeVariableArrayWildcard;
        @Ann99
        Map<@Ann100 ?, @Ann101 List<@Ann102 ? super @Ann103 W>> @Ann104 [] arrayOfParameterizedWithIndirectlyRecursiveTypeVariableWildcard;
        @Ann105
        Map<@Ann106 ?, @Ann107 List<@Ann108 ? super @Ann109 W @Ann110 []>> @Ann111 [] arrayOfParameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard;
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(A.class, B.class);
        test(index);
        test(IndexingUtil.roundtrip(index));
    }

    private void test(Index index) {
        ClassInfo a = index.getClassByName(A.class);
        ClassInfo b = index.getClassByName(B.class);

        assertEquals(a.field("primitive").type(),
                b.field("primitive").type());
        assertEquals(a.field("clazz").type(),
                b.field("clazz").type());
        assertEquals(a.field("arrayOfPrimitive").type(),
                b.field("arrayOfPrimitive").type());
        assertEquals(a.field("arrayOfClass").type(),
                b.field("arrayOfClass").type());
        assertEquals(a.field("parameterized").type(),
                b.field("parameterized").type());
        assertEquals(a.field("arrayOfParameterized").type(),
                b.field("arrayOfParameterized").type());
        assertEquals(a.field("typeVariable").type(),
                b.field("typeVariable").type());
        assertEquals(a.field("arrayOfTypeVariable").type(),
                b.field("arrayOfTypeVariable").type());
        assertEquals(a.field("recursiveTypeVariable").type(),
                b.field("recursiveTypeVariable").type());
        assertEquals(a.field("arrayOfRecursiveTypeVariable").type(),
                b.field("arrayOfRecursiveTypeVariable").type());
        assertEquals(a.field("indirectlyRecursiveTypeVariable").type(),
                b.field("indirectlyRecursiveTypeVariable").type());
        assertEquals(a.field("arrayOfIndirectlyRecursiveTypeVariable").type(),
                b.field("arrayOfIndirectlyRecursiveTypeVariable").type());
        assertEquals(a.field("parameterizedWithClassWildcard").type(),
                b.field("parameterizedWithClassWildcard").type());
        assertEquals(a.field("parameterizedWithArrayWildcard").type(),
                b.field("parameterizedWithArrayWildcard").type());
        assertEquals(a.field("parameterizedWithTypeVariableWildcard").type(),
                b.field("parameterizedWithTypeVariableWildcard").type());
        assertEquals(a.field("parameterizedWithTypeVariableArrayWildcard").type(),
                b.field("parameterizedWithTypeVariableArrayWildcard").type());
        assertEquals(a.field("parameterizedWithRecursiveTypeVariableWildcard").type(),
                b.field("parameterizedWithRecursiveTypeVariableWildcard").type());
        assertEquals(a.field("parameterizedWithRecursiveTypeVariableArrayWildcard").type(),
                b.field("parameterizedWithRecursiveTypeVariableArrayWildcard").type());
        assertEquals(a.field("parameterizedWithIndirectlyRecursiveTypeVariableWildcard").type(),
                b.field("parameterizedWithIndirectlyRecursiveTypeVariableWildcard").type());
        assertEquals(a.field("parameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard").type(),
                b.field("parameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard").type());
        assertEquals(a.field("arrayOfParameterizedWithClassWildcard").type(),
                b.field("arrayOfParameterizedWithClassWildcard").type());
        assertEquals(a.field("arrayOfParameterizedWithArrayWildcard").type(),
                b.field("arrayOfParameterizedWithArrayWildcard").type());
        assertEquals(a.field("arrayOfParameterizedWithTypeVariableWildcard").type(),
                b.field("arrayOfParameterizedWithTypeVariableWildcard").type());
        assertEquals(a.field("arrayOfParameterizedWithTypeVariableArrayWildcard").type(),
                b.field("arrayOfParameterizedWithTypeVariableArrayWildcard").type());
        assertEquals(a.field("arrayOfParameterizedWithRecursiveTypeVariableWildcard").type(),
                b.field("arrayOfParameterizedWithRecursiveTypeVariableWildcard").type());
        assertEquals(a.field("arrayOfParameterizedWithRecursiveTypeVariableArrayWildcard").type(),
                b.field("arrayOfParameterizedWithRecursiveTypeVariableArrayWildcard").type());
        assertEquals(a.field("arrayOfParameterizedWithIndirectlyRecursiveTypeVariableWildcard").type(),
                b.field("arrayOfParameterizedWithIndirectlyRecursiveTypeVariableWildcard").type());
        assertEquals(a.field("arrayOfParameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard").type(),
                b.field("arrayOfParameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard").type());
    }
}
