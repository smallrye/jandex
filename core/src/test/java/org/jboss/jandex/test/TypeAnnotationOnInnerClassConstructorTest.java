package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeTarget;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeAnnotationOnInnerClassConstructorTest {
    @MyAnnotation("top-level")
    TypeAnnotationOnInnerClassConstructorTest() {
    }

    class InnerClass {
        @MyAnnotation("inner")
        public InnerClass() {
        }

        class InnerInnerClass {
            @MyAnnotation("inner-inner")
            public InnerInnerClass() {
            }
        }
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(TypeAnnotationOnInnerClassConstructorTest.class, InnerClass.class,
                InnerClass.InnerInnerClass.class);
        test(index);
        test(IndexingUtil.roundtrip(index));
    }

    private void test(Index index) {
        testConstructor(index.getClassByName(TypeAnnotationOnInnerClassConstructorTest.class), "top-level");
        testConstructor(index.getClassByName(InnerClass.class), "inner");
        testConstructor(index.getClassByName(InnerClass.InnerInnerClass.class), "inner-inner");

        for (AnnotationInstance annotation : index.getAnnotations(MyAnnotation.DOT_NAME)) {
            assertTrue(annotation.target().kind() == AnnotationTarget.Kind.METHOD
                    || annotation.target().kind() == AnnotationTarget.Kind.TYPE);

            if (annotation.target().kind() == AnnotationTarget.Kind.TYPE) {
                TypeTarget typeAnnotationTarget = annotation.target().asType();
                assertEquals(AnnotationTarget.Kind.METHOD, typeAnnotationTarget.enclosingTarget().kind());
                assertTrue(typeAnnotationTarget.enclosingTarget().asMethod().isConstructor());
                assertEquals(Type.Kind.VOID, typeAnnotationTarget.target().kind());
            }
        }
    }

    private void testConstructor(ClassInfo clazz, String annotationValue) {
        assertEquals(1, clazz.constructors().size());

        MethodInfo ctor = clazz.constructors().get(0);
        assertTrue(ctor.hasAnnotation(MyAnnotation.DOT_NAME));
        assertEquals(annotationValue, ctor.annotation(MyAnnotation.DOT_NAME).value().asString());

        Type ctorType = ctor.returnType();
        assertTrue(ctorType.hasAnnotation(MyAnnotation.DOT_NAME));
        assertEquals(annotationValue, ctorType.annotation(MyAnnotation.DOT_NAME).value().asString());
    }
}
