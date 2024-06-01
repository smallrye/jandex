package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class RecursiveTypeParametersWithAnnotationsTest {
    static class MyComparable<@MyAnnotation("1") T extends @MyAnnotation("2") MyComparable<@MyAnnotation("3") T>> {
    }

    static class MyBuilder<@MyAnnotation("1") T, @MyAnnotation("2") THIS extends @MyAnnotation("3") MyBuilder<@MyAnnotation("4") T, @MyAnnotation("5") THIS>> {
    }

    static class DeepTypeParameterReference<@MyAnnotation("1") T extends @MyAnnotation("2") Collection<@MyAnnotation("3") List<@MyAnnotation("4") Queue<@MyAnnotation("5") Map<@MyAnnotation("6") ? super @MyAnnotation("7") T[] @MyAnnotation("8") [], @MyAnnotation("9") Iterable<@MyAnnotation("10") ? extends @MyAnnotation("11") T>>>>>> {
    }

    static class RecursiveBoundInNonRecursiveTypeParameter<@MyAnnotation("1") T extends @MyAnnotation("2") RecursiveBoundInNonRecursiveTypeParameter<@MyAnnotation("3") ?, @MyAnnotation("4") U>, @MyAnnotation("5") U extends @MyAnnotation("6") Comparable<@MyAnnotation("7") U>> {
    }

    @Test
    public void myComparable() throws IOException {
        Index index = Index.of(MyComparable.class);
        myComparable(index);
        myComparable(IndexingUtil.roundtrip(index, "a802e22dcc54147f5e3a73c21883cd3f4b6fc9230aa3e53bf893d706b5a07274"));
    }

    private void myComparable(Index index) {
        ClassInfo clazz = index.getClassByName(MyComparable.class);
        List<TypeVariable> typeParams = clazz.typeParameters();
        assertEquals(1, typeParams.size());
        TypeVariable typeParam = typeParams.get(0);
        assertEquals("T", typeParam.identifier());
        assertTypeAnnotation(typeParam, "1");

        assertEquals(1, typeParam.bounds().size());
        Type bound = typeParam.bounds().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());
        assertEquals(DotName.createSimple(MyComparable.class.getName()), bound.asParameterizedType().name());
        assertEquals(1, bound.asParameterizedType().arguments().size());
        assertTypeAnnotation(bound, "2");

        Type boundTypeArg = bound.asParameterizedType().arguments().get(0);
        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, boundTypeArg.kind());
        assertEquals("T", boundTypeArg.asTypeVariableReference().identifier());
        assertNotNull(boundTypeArg.asTypeVariableReference().follow());
        assertTypeAnnotation(boundTypeArg, "3");
        assertSame(typeParam, boundTypeArg.asTypeVariableReference().follow());

        assertEquals(
                "@MyAnnotation(\"1\") T extends org.jboss.jandex.test.@MyAnnotation(\"2\") RecursiveTypeParametersWithAnnotationsTest$MyComparable<@MyAnnotation(\"3\") T>",
                typeParam.toString());
    }

    @Test
    public void myBuilder() throws IOException {
        Index index = Index.of(MyBuilder.class);
        myBuilder(index);
        myBuilder(IndexingUtil.roundtrip(index, "aa59b707e029e0e979f0dd2c2e683070da1f246e2d40639fc01b21a4f65bfbb5"));
    }

    private void myBuilder(Index index) {
        ClassInfo clazz = index.getClassByName(MyBuilder.class);
        assertEquals(2, clazz.typeParameters().size());

        {
            TypeVariable typeParam = clazz.typeParameters().get(0);
            assertEquals("T", typeParam.identifier());
            assertTypeAnnotation(typeParam, "1");

            assertEquals(1, typeParam.bounds().size());
            assertEquals(Type.Kind.CLASS, typeParam.bounds().get(0).kind());
            assertEquals(DotName.OBJECT_NAME, typeParam.bounds().get(0).name());
        }

        {
            TypeVariable typeParam = clazz.typeParameters().get(1);
            assertEquals("THIS", typeParam.identifier());
            assertTypeAnnotation(typeParam, "2");

            assertEquals(1, typeParam.bounds().size());
            Type bound = typeParam.bounds().get(0);
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());
            assertEquals(DotName.createSimple(MyBuilder.class.getName()), bound.name());
            assertTypeAnnotation(bound, "3");

            assertEquals(2, bound.asParameterizedType().arguments().size());

            {
                Type typeArg = bound.asParameterizedType().arguments().get(0);
                assertEquals(Type.Kind.TYPE_VARIABLE, typeArg.kind());
                assertEquals("T", typeArg.asTypeVariable().identifier());
                assertTypeAnnotation(typeArg, "4");

                assertEquals(1, typeArg.asTypeVariable().bounds().size());
                assertEquals(Type.Kind.CLASS, typeArg.asTypeVariable().bounds().get(0).kind());
                assertEquals(DotName.OBJECT_NAME, typeArg.asTypeVariable().bounds().get(0).name());
            }

            {
                Type typeArg = bound.asParameterizedType().arguments().get(1);
                assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, typeArg.kind());
                assertEquals("THIS", typeArg.asTypeVariableReference().identifier());
                assertTypeAnnotation(typeArg, "5");

                assertSame(typeParam, typeArg.asTypeVariableReference().follow());
            }
        }

        assertEquals("@MyAnnotation(\"1\") T", clazz.typeParameters().get(0).toString());
        assertEquals(
                "@MyAnnotation(\"2\") THIS extends org.jboss.jandex.test.@MyAnnotation(\"3\") RecursiveTypeParametersWithAnnotationsTest$MyBuilder<@MyAnnotation(\"4\") T, @MyAnnotation(\"5\") THIS>",
                clazz.typeParameters().get(1).toString());
    }

    @Test
    public void deepTypeParameterReference() throws IOException {
        Index index = Index.of(DeepTypeParameterReference.class);
        deepTypeParameterReference(index);
        deepTypeParameterReference(
                IndexingUtil.roundtrip(index, "3ed2bdeb0c4dbdae2214b414109afa5f049d9b564d06785d94d11a3cf539988f"));
    }

    private void deepTypeParameterReference(Index index) {
        ClassInfo clazz = index.getClassByName(DeepTypeParameterReference.class);
        List<TypeVariable> typeParams = clazz.typeParameters();
        assertEquals(1, typeParams.size());
        TypeVariable typeParam = typeParams.get(0);
        assertEquals("T", typeParam.identifier());
        assertEquals(1, typeParam.bounds().size());
        assertTypeAnnotation(typeParam, "1");

        Type bound = typeParam.bounds().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());
        assertEquals(DotName.createSimple(Collection.class.getName()), bound.asParameterizedType().name());
        assertEquals(1, bound.asParameterizedType().arguments().size());
        assertTypeAnnotation(bound, "2");

        Type boundTypeArg = bound.asParameterizedType().arguments().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, boundTypeArg.kind());
        assertEquals(DotName.createSimple(List.class.getName()), boundTypeArg.asParameterizedType().name());
        assertEquals(1, boundTypeArg.asParameterizedType().arguments().size());
        assertTypeAnnotation(boundTypeArg, "3");

        Type nestedTypeArg = boundTypeArg.asParameterizedType().arguments().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, nestedTypeArg.kind());
        assertEquals(DotName.createSimple(Queue.class.getName()), nestedTypeArg.asParameterizedType().name());
        assertEquals(1, nestedTypeArg.asParameterizedType().arguments().size());
        assertTypeAnnotation(nestedTypeArg, "4");

        Type nestedNestedTypeArg = nestedTypeArg.asParameterizedType().arguments().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, nestedNestedTypeArg.kind());
        assertEquals(DotName.createSimple(Map.class.getName()), nestedNestedTypeArg.asParameterizedType().name());
        assertEquals(2, nestedNestedTypeArg.asParameterizedType().arguments().size());
        assertTypeAnnotation(nestedNestedTypeArg, "5");

        {
            Type key = nestedNestedTypeArg.asParameterizedType().arguments().get(0);
            assertEquals(DotName.OBJECT_NAME, key.name()); // has lower bound, so upper bound is java.lang.Object
            assertEquals(Type.Kind.WILDCARD_TYPE, key.kind());
            assertNotNull(key.asWildcardType().superBound());
            assertTypeAnnotation(key, "6");

            Type keyBound = key.asWildcardType().superBound();
            assertEquals(Type.Kind.ARRAY, keyBound.kind());
            assertEquals(DotName.createSimple("[[Ljava.util.Collection;"), keyBound.name());
            assertEquals(1, keyBound.asArrayType().dimensions());
            assertFalse(keyBound.hasAnnotation(MyAnnotation.DOT_NAME));

            Type keyBoundComponent = keyBound.asArrayType().component();
            assertEquals(Type.Kind.ARRAY, keyBoundComponent.kind());
            assertEquals(DotName.createSimple("[Ljava.util.Collection;"), keyBoundComponent.name());
            assertEquals(1, keyBoundComponent.asArrayType().dimensions());
            assertTypeAnnotation(keyBoundComponent, "8");

            Type keyBoundComponentComponent = keyBoundComponent.asArrayType().component();
            assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, keyBoundComponentComponent.kind());
            assertEquals(DotName.createSimple(Collection.class.getName()), keyBoundComponentComponent.name());
            assertEquals("T", keyBoundComponentComponent.asTypeVariableReference().identifier());
            assertNotNull(keyBoundComponentComponent.asTypeVariableReference().follow());
            assertTypeAnnotation(keyBoundComponentComponent, "7");

            assertSame(typeParam, keyBoundComponentComponent.asTypeVariableReference().follow());
        }

        {
            Type value = nestedNestedTypeArg.asParameterizedType().arguments().get(1);
            assertEquals(DotName.createSimple(Iterable.class.getName()), value.name()); // upper bound
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, value.kind());
            assertEquals(1, value.asParameterizedType().arguments().size());
            assertTypeAnnotation(value, "9");

            Type valueTypeArg = value.asParameterizedType().arguments().get(0);
            assertEquals(Type.Kind.WILDCARD_TYPE, valueTypeArg.kind());
            assertNull(valueTypeArg.asWildcardType().superBound());
            assertTypeAnnotation(valueTypeArg, "10");

            Type valueTypeArgBound = valueTypeArg.asWildcardType().extendsBound();
            assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, valueTypeArgBound.kind());
            assertEquals(DotName.createSimple(Collection.class.getName()), valueTypeArgBound.name());
            assertEquals("T", valueTypeArgBound.asTypeVariableReference().identifier());
            assertNotNull(valueTypeArgBound.asTypeVariableReference().follow());
            assertTypeAnnotation(valueTypeArgBound, "11");

            assertSame(typeParam, valueTypeArgBound.asTypeVariableReference().follow());
        }

        assertEquals(
                "@MyAnnotation(\"1\") T extends java.util.@MyAnnotation(\"2\") Collection<java.util.@MyAnnotation(\"3\") List<java.util.@MyAnnotation(\"4\") Queue<java.util.@MyAnnotation(\"5\") Map<@MyAnnotation(\"6\") ? super @MyAnnotation(\"7\") T[] @MyAnnotation(\"8\") [], java.lang.@MyAnnotation(\"9\") Iterable<@MyAnnotation(\"10\") ? extends @MyAnnotation(\"11\") T>>>>>",
                typeParam.toString());
    }

    @Test
    public void recursiveBoundInNonRecursiveTypeParameter() throws IOException {
        Index index = Index.of(RecursiveBoundInNonRecursiveTypeParameter.class);
        recursiveBoundInNonRecursiveTypeParameter(index);
        recursiveBoundInNonRecursiveTypeParameter(
                IndexingUtil.roundtrip(index, "769a43fbda4f6fff2d4225dba7a70369273514ff9bddf08ac46238ad34acf44b"));
    }

    private void recursiveBoundInNonRecursiveTypeParameter(Index index) {
        ClassInfo clazz = index.getClassByName(RecursiveBoundInNonRecursiveTypeParameter.class);
        List<TypeVariable> typeParams = clazz.typeParameters();
        assertEquals(2, typeParams.size());
        {
            TypeVariable typeParam = typeParams.get(0);
            assertEquals("T", typeParam.identifier());
            assertEquals(1, typeParam.bounds().size());
            assertTypeAnnotation(typeParam, "1");
            Type bound = typeParam.bounds().get(0);
            assertEquals(DotName.createSimple(RecursiveBoundInNonRecursiveTypeParameter.class.getName()), bound.name());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());
            assertTypeAnnotation(bound, "2");
            assertEquals(2, bound.asParameterizedType().arguments().size());
            {
                Type boundArg = bound.asParameterizedType().arguments().get(0);
                assertEquals(Type.Kind.WILDCARD_TYPE, boundArg.kind());
                assertTypeAnnotation(boundArg, "3");
                assertEquals(Type.Kind.CLASS, boundArg.asWildcardType().extendsBound().kind());
                assertEquals(DotName.OBJECT_NAME, boundArg.asWildcardType().extendsBound().name());
                assertNull(boundArg.asWildcardType().superBound());
            }
            {
                Type boundArg = bound.asParameterizedType().arguments().get(1);
                assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, boundArg.kind());
                assertEquals("U", boundArg.asTypeVariableReference().identifier());
                assertSame(typeParams.get(1), boundArg.asTypeVariableReference().follow());
                assertTypeAnnotation(boundArg, "4");
            }
        }
        {
            TypeVariable typeParam = typeParams.get(1);
            assertEquals("U", typeParam.identifier());
            assertEquals(1, typeParam.bounds().size());
            assertTypeAnnotation(typeParam, "5");
            Type bound = typeParam.bounds().get(0);
            assertEquals(DotName.createSimple(Comparable.class.getName()), bound.name());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());
            assertTypeAnnotation(bound, "6");
            assertEquals(1, bound.asParameterizedType().arguments().size());
            {
                Type boundArg = bound.asParameterizedType().arguments().get(0);
                assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, boundArg.kind());
                assertEquals("U", boundArg.asTypeVariableReference().identifier());
                assertSame(typeParam, boundArg.asTypeVariableReference().follow());
                assertTypeAnnotation(boundArg, "7");
            }
        }

        assertEquals(
                "@MyAnnotation(\"1\") T extends org.jboss.jandex.test.@MyAnnotation(\"2\") RecursiveTypeParametersWithAnnotationsTest$RecursiveBoundInNonRecursiveTypeParameter<@MyAnnotation(\"3\") ?, @MyAnnotation(\"4\") U>",
                typeParams.get(0).toString());
        assertEquals(
                "@MyAnnotation(\"5\") U extends java.lang.@MyAnnotation(\"6\") Comparable<@MyAnnotation(\"7\") U>",
                typeParams.get(1).toString());
    }

    private void assertTypeAnnotation(Type type, String expectedValue) {
        assertTrue(type.hasAnnotation(MyAnnotation.DOT_NAME));
        assertEquals(expectedValue, type.annotation(MyAnnotation.DOT_NAME).value().asString());
    }
}
