package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeEqualityTest {
    static class A<T, U extends Comparable<U>, V extends List<W>, W extends List<V>> {
        void voidMethod() {
        }

        int primitive;
        String clazz;
        int[] arrayOfPrimitive;
        String[][] arrayOfClass;
        List<String> parameterized;
        Map<String, List<String>>[] arrayOfParameterized;
        T typeVariable;
        T[] arrayOfTypeVariable;
        U recursiveTypeVariable;
        U[] arrayOfRecursiveTypeVariable;
        W indirectlyRecursiveTypeVariable;
        W[] arrayOfIndirectlyRecursiveTypeVariable;
        List<? extends Number> parameterizedWithClassWildcard;
        List<? extends Number[]> parameterizedWithArrayWildcard;
        List<? extends T> parameterizedWithTypeVariableWildcard;
        List<? extends T[]> parameterizedWithTypeVariableArrayWildcard;
        List<? extends U> parameterizedWithRecursiveTypeVariableWildcard;
        List<? extends U[]> parameterizedWithRecursiveTypeVariableArrayWildcard;
        List<? extends W> parameterizedWithIndirectlyRecursiveTypeVariableWildcard;
        List<? extends W[]> parameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard;
        Map<?, List<? super Number>>[] arrayOfParameterizedWithClassWildcard;
        Map<?, List<? super Number[]>>[] arrayOfParameterizedWithArrayWildcard;
        Map<?, List<? super T>>[] arrayOfParameterizedWithTypeVariableWildcard;
        Map<?, List<? super T[]>>[] arrayOfParameterizedWithTypeVariableArrayWildcard;
        Map<?, List<? super U>>[] arrayOfParameterizedWithRecursiveTypeVariableWildcard;
        Map<?, List<? super U[]>>[] arrayOfParameterizedWithRecursiveTypeVariableArrayWildcard;
        Map<?, List<? super W>>[] arrayOfParameterizedWithIndirectlyRecursiveTypeVariableWildcard;
        Map<?, List<? super W[]>>[] arrayOfParameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard;
    }

    static class B<T, U extends Comparable<U>, V extends List<W>, W extends List<V>> {
        void voidMethod() {
        }

        int primitive;
        String clazz;
        int[] arrayOfPrimitive;
        String[][] arrayOfClass;
        List<String> parameterized;
        Map<String, List<String>>[] arrayOfParameterized;
        T typeVariable;
        T[] arrayOfTypeVariable;
        U recursiveTypeVariable;
        U[] arrayOfRecursiveTypeVariable;
        W indirectlyRecursiveTypeVariable;
        W[] arrayOfIndirectlyRecursiveTypeVariable;
        List<? extends Number> parameterizedWithClassWildcard;
        List<? extends Number[]> parameterizedWithArrayWildcard;
        List<? extends T> parameterizedWithTypeVariableWildcard;
        List<? extends T[]> parameterizedWithTypeVariableArrayWildcard;
        List<? extends U> parameterizedWithRecursiveTypeVariableWildcard;
        List<? extends U[]> parameterizedWithRecursiveTypeVariableArrayWildcard;
        List<? extends W> parameterizedWithIndirectlyRecursiveTypeVariableWildcard;
        List<? extends W[]> parameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard;
        Map<?, List<? super Number>>[] arrayOfParameterizedWithClassWildcard;
        Map<?, List<? super Number[]>>[] arrayOfParameterizedWithArrayWildcard;
        Map<?, List<? super T>>[] arrayOfParameterizedWithTypeVariableWildcard;
        Map<?, List<? super T[]>>[] arrayOfParameterizedWithTypeVariableArrayWildcard;
        Map<?, List<? super U>>[] arrayOfParameterizedWithRecursiveTypeVariableWildcard;
        Map<?, List<? super U[]>>[] arrayOfParameterizedWithRecursiveTypeVariableArrayWildcard;
        Map<?, List<? super W>>[] arrayOfParameterizedWithIndirectlyRecursiveTypeVariableWildcard;
        Map<?, List<? super W[]>>[] arrayOfParameterizedWithIndirectlyRecursiveTypeVariableArrayWildcard;
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

        assertEquals(a.firstMethod("voidMethod").returnType(),
                b.firstMethod("voidMethod").returnType());
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
