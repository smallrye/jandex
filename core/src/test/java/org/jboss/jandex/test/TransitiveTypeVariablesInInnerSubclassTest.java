package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeTarget;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TransitiveTypeVariablesInInnerSubclassTest {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface Ann {
    }

    static class TestClass<T> {
        class Foo {
            void foo(@Ann T value) {
            }
        }

        class Bar extends Foo {
            void bar(@Ann T value) {
            }
        }

        class Baz extends Bar {
            void baz(@Ann T value) {
            }
        }
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(TestClass.class, TestClass.Foo.class, TestClass.Bar.class, TestClass.Baz.class);
        doTest(index);
        doTest(IndexingUtil.roundtrip(index));
    }

    private void doTest(Index index) {
        assertClass(index.getClassByName(TestClass.Foo.class), "foo");
        assertClass(index.getClassByName(TestClass.Bar.class), "bar");
        assertClass(index.getClassByName(TestClass.Baz.class), "baz");

        Collection<AnnotationInstance> allAnnotations = index.getAnnotations(Ann.class);
        assertEquals(3, allAnnotations.size());
        for (AnnotationInstance ann : allAnnotations) {
            assertTypeVariableAnnotation(ann);
        }
    }

    private void assertClass(ClassInfo clazz, String methodName) {
        Collection<AnnotationInstance> annotationsFromClass = clazz.annotations(Ann.class);
        assertEquals(1, annotationsFromClass.size());
        assertTypeVariableAnnotation(annotationsFromClass.iterator().next());

        MethodInfo method = clazz.firstMethod(methodName);
        Collection<AnnotationInstance> annotationsFromMethod = method.annotations(Ann.class);
        assertEquals(1, annotationsFromMethod.size());
        assertTypeVariableAnnotation(annotationsFromMethod.iterator().next());
    }

    private void assertTypeVariableAnnotation(AnnotationInstance ann) {
        assertEquals(AnnotationTarget.Kind.TYPE, ann.target().kind());
        TypeTarget typeTarget = ann.target().asType();

        assertEquals(Type.Kind.TYPE_VARIABLE, typeTarget.target().kind());
        TypeVariable typeVariable = typeTarget.target().asTypeVariable();
        assertEquals("T", typeVariable.identifier());
        assertEquals(1, typeVariable.annotations().size());
        assertEquals(Ann.class.getName(), typeVariable.annotations().get(0).name().toString());

        assertEquals(TypeTarget.Usage.METHOD_PARAMETER, typeTarget.usage());
        assertEquals(0, typeTarget.asMethodParameterType().position());

        assertEquals(AnnotationTarget.Kind.METHOD, typeTarget.enclosingTarget().kind());
    }
}
