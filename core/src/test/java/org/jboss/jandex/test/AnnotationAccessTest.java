package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

public class AnnotationAccessTest {
    @MyRepeatableAnnotation("cr1")
    @MyRepeatableAnnotation.List({
            @MyRepeatableAnnotation("cr2"),
            @MyRepeatableAnnotation("cr3")
    })
    @MyAnnotation("c1")
    static class AnnotatedClass<@MyAnnotation("c2") T extends @MyAnnotation("c3") Number> {
        @MyRepeatableAnnotation("fr1")
        @MyRepeatableAnnotation.List({
                @MyRepeatableAnnotation("fr2"),
                @MyRepeatableAnnotation("fr3")
        })
        @MyAnnotation("f1")
        Map<@MyAnnotation("f2") String, @MyAnnotation("f3") List<? extends @MyAnnotation("f4") Number>> field;

        @MyRepeatableAnnotation("mr1")
        @MyRepeatableAnnotation.List({
                @MyRepeatableAnnotation("mr2"),
                @MyRepeatableAnnotation("mr3")
        })
        @MyAnnotation("m1")
        void method(
                @MyAnnotation("m2") Map<@MyAnnotation("m3") String, @MyAnnotation("m4") List<? extends @MyAnnotation("m5") Number>> param,
                @MyAnnotation("m6") int @MyAnnotation("m7") [] @MyAnnotation("m8") [] otherParam) {
        }
    }

    private void verify(Collection<AnnotationInstance> annotations, DotName filter, String... expectedValues) {
        Set<String> presentValues = new HashSet<>();
        for (AnnotationInstance annotation : annotations) {
            if (filter.equals(annotation.name())) {
                presentValues.add(annotation.value().asString());
            }
        }

        assertEquals(new HashSet<>(Arrays.asList(expectedValues)), presentValues);
    }

    @Test
    public void test() throws IOException {
        DotName myAnn = MyAnnotation.DOT_NAME;
        DotName myRepAnn = MyRepeatableAnnotation.DOT_NAME;
        DotName className = DotName.createSimple(AnnotatedClass.class.getName());

        Index index = Index.of(MyAnnotation.class, MyRepeatableAnnotation.class, MyRepeatableAnnotation.List.class,
                AnnotatedClass.class);

        // ecj also puts the `MyRepeatableAnnotation` and `MyRepeatableAnnotation.List` annotations
        // on the _type_ of `AnnotatedClass.field`, contrary to the `@Target` declarations

        {
            ClassInfo clazz = index.getClassByName(className);
            assertTrue(clazz.hasAnnotation(myAnn));
            assertNotNull(clazz.annotation(myAnn));
            assertEquals(18, clazz.annotations(myAnn).size());
            assertEquals(18, clazz.annotationsWithRepeatable(myAnn, index).size());
            assertTrue(clazz.annotations().size() == 24 || clazz.annotations().size() == 28);
            assertTrue(clazz.hasDeclaredAnnotation(myAnn));
            assertNotNull(clazz.declaredAnnotation(myAnn));
            assertEquals(1, clazz.declaredAnnotationsWithRepeatable(myAnn, index).size());
            assertEquals(3, clazz.declaredAnnotations().size());
            verify(clazz.declaredAnnotations(), myAnn, "c1");
            assertEquals(9, clazz.annotationsWithRepeatable(myRepAnn, index).size());
            assertEquals(3, clazz.declaredAnnotationsWithRepeatable(myRepAnn, index).size());
            verify(clazz.declaredAnnotationsWithRepeatable(myRepAnn, index), myRepAnn, "cr1", "cr2", "cr3");
        }

        {
            ClassInfo clazz = index.getClassByName(className);
            FieldInfo field = clazz.field("field");
            assertTrue(field.hasAnnotation(myAnn));
            assertNotNull(field.annotation(myAnn));
            assertEquals(5, field.annotations(myAnn).size());
            assertEquals(5, field.annotationsWithRepeatable(myAnn, index).size());
            assertTrue(field.annotations().size() == 7 || field.annotations().size() == 11);
            assertTrue(field.hasDeclaredAnnotation(myAnn));
            assertNotNull(field.declaredAnnotation(myAnn));
            assertEquals(1, field.declaredAnnotationsWithRepeatable(myAnn, index).size());
            assertEquals(3, field.declaredAnnotations().size());
            verify(field.declaredAnnotations(), myAnn, "f1");
            assertEquals(3, field.annotationsWithRepeatable(myRepAnn, index).size());
            assertEquals(3, field.declaredAnnotationsWithRepeatable(myRepAnn, index).size());
            verify(field.declaredAnnotationsWithRepeatable(myRepAnn, index), myRepAnn, "fr1", "fr2", "fr3");
        }

        {
            ClassInfo clazz = index.getClassByName(className);
            FieldInfo field = clazz.field("field");
            Type type = field.type();
            assertTrue(type.hasAnnotation(myAnn));
            assertNotNull(type.annotation(myAnn));
            assertEquals(1, type.annotationsWithRepeatable(myAnn, index).size());
            assertTrue(type.annotations().size() == 1 || type.annotations().size() == 5);
            verify(type.annotations(), myAnn, "f1");
        }

        {
            ClassInfo clazz = index.getClassByName(className);
            MethodInfo method = clazz.firstMethod("method");
            assertTrue(method.hasAnnotation(myAnn));
            assertNotNull(method.annotation(myAnn));
            assertEquals(10, method.annotations(myAnn).size());
            assertEquals(10, method.annotationsWithRepeatable(myAnn, index).size());
            assertEquals(12, method.annotations().size());
            assertTrue(method.hasDeclaredAnnotation(myAnn));
            assertNotNull(method.declaredAnnotation(myAnn));
            assertEquals(1, method.declaredAnnotationsWithRepeatable(myAnn, index).size());
            assertEquals(3, method.declaredAnnotations().size());
            verify(method.declaredAnnotations(), myAnn, "m1");
            assertEquals(3, method.annotationsWithRepeatable(myRepAnn, index).size());
            assertEquals(3, method.declaredAnnotationsWithRepeatable(myRepAnn, index).size());
            verify(method.declaredAnnotationsWithRepeatable(myRepAnn, index), myRepAnn, "mr1", "mr2", "mr3");
        }

        {
            ClassInfo clazz = index.getClassByName(className);
            MethodInfo method = clazz.firstMethod("method");
            MethodParameterInfo param = method.parameters().get(0);
            assertTrue(param.hasAnnotation(myAnn));
            assertNotNull(param.annotation(myAnn));
            assertEquals(5, param.annotations(myAnn).size());
            assertEquals(5, param.annotationsWithRepeatable(myAnn, index).size());
            assertEquals(5, param.annotations().size());
            assertTrue(param.hasDeclaredAnnotation(myAnn));
            assertNotNull(param.declaredAnnotation(myAnn));
            assertEquals(1, param.declaredAnnotationsWithRepeatable(myAnn, index).size());
            assertEquals(1, param.declaredAnnotations().size());
            verify(param.declaredAnnotations(), myAnn, "m2");
        }

        {
            ClassInfo clazz = index.getClassByName(className);
            MethodInfo method = clazz.firstMethod("method");
            MethodParameterInfo param = method.parameters().get(0);
            Type type = param.type();
            assertTrue(type.hasAnnotation(myAnn));
            assertNotNull(type.annotation(myAnn));
            assertEquals(1, type.annotationsWithRepeatable(myAnn, index).size());
            assertEquals(1, type.annotations().size());
            verify(type.annotations(), myAnn, "m2");
        }

        {
            ClassInfo clazz = index.getClassByName(className);
            MethodInfo method = clazz.firstMethod("method");
            MethodParameterInfo param = method.parameters().get(1);
            assertTrue(param.hasAnnotation(myAnn));
            assertNotNull(param.annotation(myAnn));
            assertEquals(4, param.annotations(myAnn).size());
            assertEquals(4, param.annotationsWithRepeatable(myAnn, index).size());
            assertEquals(4, param.annotations().size());
            assertTrue(param.hasDeclaredAnnotation(myAnn));
            assertNotNull(param.declaredAnnotation(myAnn));
            assertEquals(1, param.declaredAnnotationsWithRepeatable(myAnn, index).size());
            assertEquals(1, param.declaredAnnotations().size());
            verify(param.declaredAnnotations(), myAnn, "m6");
        }

        {
            ClassInfo clazz = index.getClassByName(className);
            MethodInfo method = clazz.firstMethod("method");
            MethodParameterInfo param = method.parameters().get(1);
            Type type = param.type();
            assertTrue(type.hasAnnotation(myAnn));
            assertNotNull(type.annotation(myAnn));
            assertEquals(1, type.annotationsWithRepeatable(myAnn, index).size());
            assertEquals(1, type.annotations().size());
            verify(type.annotations(), myAnn, "m7");
        }
    }
}
