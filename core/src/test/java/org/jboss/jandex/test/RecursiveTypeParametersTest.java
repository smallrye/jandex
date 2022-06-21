package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class RecursiveTypeParametersTest {
    static class MyComparable<T extends Comparable<T>> {
    }

    static class DeepTypeParameterReference<T extends Collection<List<Queue<Map<? super T[][], Iterable<? extends T>>>>>> {
    }

    abstract static class MyEnum<T extends MyEnum<T>> implements Comparable<T> {
        abstract int ordinal();

        @Override
        public int compareTo(T other) {
            return Integer.compare(this.ordinal(), other.ordinal());
        }
    }

    abstract static class MyBuilder<T, THIS extends MyBuilder<T, THIS>> {
        abstract T build();

        final THIS self() {
            return (THIS) this;
        }

        final THIS with(Consumer<THIS> action) {
            action.accept(self());
            return self();
        }
    }

    interface Score<S extends Score<S>> {
    }

    static class ScoreManager<S extends Score<S>> {
    }

    static class ScoreManagerFactory {
        <S extends Score<S>> ScoreManager<S> newScoreManager() {
            return new ScoreManager<>();
        }
    }

    // ---

    @Test
    public void myComparable() throws IOException {
        Index index = Index.of(MyComparable.class);
        myComparable(index);
        myComparable(IndexingUtil.roundtrip(index));
    }

    private void myComparable(Index index) {
        ClassInfo clazz = index.getClassByName(MyComparable.class);
        List<TypeVariable> typeParams = clazz.typeParameters();
        assertEquals(1, typeParams.size());
        TypeVariable typeParam = typeParams.get(0);
        assertEquals("T", typeParam.identifier());
        assertEquals(1, typeParam.bounds().size());
        Type bound = typeParam.bounds().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());
        assertEquals(DotName.createSimple(Comparable.class.getName()), bound.asParameterizedType().name());
        assertEquals(1, bound.asParameterizedType().arguments().size());
        Type boundTypeArg = bound.asParameterizedType().arguments().get(0);
        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, boundTypeArg.kind());
        assertEquals("T", boundTypeArg.asTypeVariableReference().identifier());
        assertNotNull(boundTypeArg.asTypeVariableReference().follow());
        assertSame(typeParam, boundTypeArg.asTypeVariableReference().follow());
        assertEquals("T extends java.lang.Comparable<T>", typeParam.toString());
    }

    @Test
    public void deepTypeParameterReference() throws IOException {
        Index index = Index.of(DeepTypeParameterReference.class);
        deepTypeParameterReference(index);
        deepTypeParameterReference(IndexingUtil.roundtrip(index));
    }

    private void deepTypeParameterReference(Index index) {
        ClassInfo clazz = index.getClassByName(DeepTypeParameterReference.class);
        List<TypeVariable> typeParams = clazz.typeParameters();
        assertEquals(1, typeParams.size());
        TypeVariable typeParam = typeParams.get(0);
        assertEquals("T", typeParam.identifier());
        assertEquals(1, typeParam.bounds().size());

        Type bound = typeParam.bounds().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());
        assertEquals(DotName.createSimple(Collection.class.getName()), bound.asParameterizedType().name());
        assertEquals(1, bound.asParameterizedType().arguments().size());

        Type boundTypeArg = bound.asParameterizedType().arguments().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, boundTypeArg.kind());
        assertEquals(DotName.createSimple(List.class.getName()), boundTypeArg.asParameterizedType().name());
        assertEquals(1, boundTypeArg.asParameterizedType().arguments().size());

        Type nestedTypeArg = boundTypeArg.asParameterizedType().arguments().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, nestedTypeArg.kind());
        assertEquals(DotName.createSimple(Queue.class.getName()), nestedTypeArg.asParameterizedType().name());
        assertEquals(1, nestedTypeArg.asParameterizedType().arguments().size());

        Type nestedNestedTypeArg = nestedTypeArg.asParameterizedType().arguments().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, nestedNestedTypeArg.kind());
        assertEquals(DotName.createSimple(Map.class.getName()), nestedNestedTypeArg.asParameterizedType().name());
        assertEquals(2, nestedNestedTypeArg.asParameterizedType().arguments().size());

        {
            Type key = nestedNestedTypeArg.asParameterizedType().arguments().get(0);
            assertEquals(DotName.OBJECT_NAME, key.name()); // has lower bound, so upper bound is java.lang.Object
            assertEquals(Type.Kind.WILDCARD_TYPE, key.kind());
            assertNotNull(key.asWildcardType().superBound());

            Type keyBound = key.asWildcardType().superBound();
            assertEquals(Type.Kind.ARRAY, keyBound.kind());
            assertEquals(DotName.createSimple("[[Ljava.util.Collection;"), keyBound.name());
            assertEquals(2, keyBound.asArrayType().dimensions());

            Type keyBoundComponent = keyBound.asArrayType().component();
            assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, keyBoundComponent.kind());
            assertEquals(DotName.createSimple(Collection.class.getName()), keyBoundComponent.name());
            assertEquals("T", keyBoundComponent.asTypeVariableReference().identifier());
            assertNotNull(keyBoundComponent.asTypeVariableReference().follow());
            assertSame(typeParam, keyBoundComponent.asTypeVariableReference().follow());
        }

        {
            Type value = nestedNestedTypeArg.asParameterizedType().arguments().get(1);
            assertEquals(DotName.createSimple(Iterable.class.getName()), value.name()); // upper bound
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, value.kind());
            assertEquals(1, value.asParameterizedType().arguments().size());

            Type valueTypeArg = value.asParameterizedType().arguments().get(0);
            assertEquals(Type.Kind.WILDCARD_TYPE, valueTypeArg.kind());
            assertNull(valueTypeArg.asWildcardType().superBound());

            Type valueTypeArgBound = valueTypeArg.asWildcardType().extendsBound();
            assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, valueTypeArgBound.kind());
            assertEquals(DotName.createSimple(Collection.class.getName()), valueTypeArgBound.name());
            assertEquals("T", valueTypeArgBound.asTypeVariableReference().identifier());
            assertNotNull(valueTypeArgBound.asTypeVariableReference().follow());
            assertSame(typeParam, valueTypeArgBound.asTypeVariableReference().follow());
        }

        assertEquals(
                "T extends java.util.Collection<java.util.List<java.util.Queue<java.util.Map<? super T[][], java.lang.Iterable<? extends T>>>>>",
                typeParam.toString());
    }

    @Test
    public void myEnum() throws IOException {
        Index index = Index.of(MyEnum.class);
        myEnum(index);
        myEnum(IndexingUtil.roundtrip(index));
    }

    private void myEnum(Index index) {
        ClassInfo clazz = index.getClassByName(MyEnum.class);
        List<TypeVariable> typeParams = clazz.typeParameters();
        assertEquals(1, typeParams.size());
        TypeVariable typeParam = typeParams.get(0);
        assertEquals("T", typeParam.identifier());
        assertEquals(1, typeParam.bounds().size());
        Type bound = typeParam.bounds().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());
        assertEquals(DotName.createSimple(MyEnum.class.getName()), bound.asParameterizedType().name());
        assertEquals(1, bound.asParameterizedType().arguments().size());
        Type boundTypeArg = bound.asParameterizedType().arguments().get(0);
        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, boundTypeArg.kind());
        assertEquals("T", boundTypeArg.asTypeVariableReference().identifier());
        assertNotNull(boundTypeArg.asTypeVariableReference().follow());
        assertSame(typeParam, boundTypeArg.asTypeVariableReference().follow());

        assertEquals("T extends org.jboss.jandex.test.RecursiveTypeParametersTest$MyEnum<T>", typeParam.toString());
    }

    @Test
    public void myBuilder() throws IOException {
        Index index = Index.of(MyBuilder.class);
        myBuilder(index);
        myBuilder(IndexingUtil.roundtrip(index));
    }

    private void myBuilder(Index index) {
        ClassInfo clazz = index.getClassByName(MyBuilder.class);
        List<TypeVariable> typeParams = clazz.typeParameters();
        assertEquals(2, typeParams.size());
        {
            TypeVariable typeParam = typeParams.get(0);
            assertEquals("T", typeParam.identifier());
            assertEquals(1, typeParam.bounds().size());
            assertEquals(DotName.OBJECT_NAME, typeParam.bounds().get(0).name());
        }
        {
            TypeVariable typeParam = typeParams.get(1);
            assertMyBuilderRecursiveTypeParameter(typeParam);
        }

        {
            MethodInfo method = clazz.firstMethod("self");
            assertEquals(Type.Kind.TYPE_VARIABLE, method.returnType().kind());
            assertMyBuilderRecursiveTypeParameter(method.returnType().asTypeVariable());
        }

        {
            MethodInfo method = clazz.firstMethod("with");
            assertEquals(Type.Kind.TYPE_VARIABLE, method.returnType().kind());
            assertMyBuilderRecursiveTypeParameter(method.returnType().asTypeVariable());

            Type parameter = method.parameterType(0);
            assertEquals(DotName.createSimple(Consumer.class.getName()), parameter.name());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, parameter.kind());
            assertEquals(1, parameter.asParameterizedType().arguments().size());
            Type typeArgument = parameter.asParameterizedType().arguments().get(0);
            assertEquals(Type.Kind.TYPE_VARIABLE, typeArgument.kind());
            assertMyBuilderRecursiveTypeParameter(typeArgument.asTypeVariable());
        }

        assertEquals("T", typeParams.get(0).toString());
        assertEquals(
                "THIS extends org.jboss.jandex.test.RecursiveTypeParametersTest$MyBuilder<T, THIS>",
                typeParams.get(1).toString());
    }

    private void assertMyBuilderRecursiveTypeParameter(TypeVariable typeVariable) {
        assertEquals("THIS", typeVariable.identifier());
        assertEquals(1, typeVariable.bounds().size());
        Type bound = typeVariable.bounds().get(0);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());
        assertEquals(DotName.createSimple(MyBuilder.class.getName()), bound.asParameterizedType().name());
        assertEquals(2, bound.asParameterizedType().arguments().size());
        {
            Type boundTypeArg = bound.asParameterizedType().arguments().get(0);
            assertEquals(Type.Kind.TYPE_VARIABLE, boundTypeArg.kind());
            assertEquals("T", boundTypeArg.asTypeVariable().identifier());
            assertEquals(1, boundTypeArg.asTypeVariable().bounds().size());
            assertEquals(DotName.OBJECT_NAME, boundTypeArg.asTypeVariable().bounds().get(0).name());
        }
        {
            Type boundTypeArg = bound.asParameterizedType().arguments().get(1);
            assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, boundTypeArg.kind());
            assertEquals("THIS", boundTypeArg.asTypeVariableReference().identifier());
            assertNotNull(boundTypeArg.asTypeVariableReference().follow());
            assertSame(typeVariable, boundTypeArg.asTypeVariableReference().follow());
        }
    }

    @Test
    public void score() throws IOException {
        Index index = Index.of(Score.class, ScoreManager.class, ScoreManagerFactory.class);
        score(index);
        score(IndexingUtil.roundtrip(index));
    }

    private void score(Index index) {
        ClassInfo scoreManagerFactory = index.getClassByName(ScoreManagerFactory.class);
        MethodInfo newScoreManager = scoreManagerFactory.method("newScoreManager");
        Type type = newScoreManager.returnType();
        assertEquals(DotName.createSimple(ScoreManager.class.getName()), type.name());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        assertScoreTypeVariable(type.asParameterizedType().arguments().get(0));

        ClassInfo scoreManager = index.getClassByName(ScoreManager.class);
        assertEquals(1, scoreManager.typeParameters().size());
        assertScoreTypeVariable(scoreManager.typeParameters().get(0));

        ClassInfo score = index.getClassByName(Score.class);
        assertEquals(1, score.typeParameters().size());
        assertScoreTypeVariable(score.typeParameters().get(0));
    }

    private void assertScoreTypeVariable(Type typeVariable) {
        // S extends Score<S>
        assertEquals(DotName.createSimple(Score.class.getName()), typeVariable.name());
        assertEquals(Type.Kind.TYPE_VARIABLE, typeVariable.kind());
        assertEquals("S", typeVariable.asTypeVariable().identifier());

        // Score<S>
        Type bound = typeVariable.asTypeVariable().bounds().get(0);
        assertEquals(DotName.createSimple(Score.class.getName()), bound.name());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, bound.kind());

        // S, which is a reference to S extends Score<S>
        Type boundTypeArg = bound.asParameterizedType().arguments().get(0);
        assertEquals(DotName.createSimple(Score.class.getName()), boundTypeArg.name());
        assertEquals(Type.Kind.TYPE_VARIABLE_REFERENCE, boundTypeArg.kind());

        assertSame(typeVariable, boundTypeArg.asTypeVariableReference().follow());

        assertEquals("S extends org.jboss.jandex.test.RecursiveTypeParametersTest$Score<S>", typeVariable.toString());
    }
}
