package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Declaration;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.MutableAnnotationOverlay;
import org.junit.jupiter.api.Test;

public class MutableAnnotationOverlayTest {
    @Retention(RetentionPolicy.CLASS)
    @interface MyClassRetainedAnnotation {
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface MyInheritedAnnotation {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface MyNotInheritedAnnotation {
        String value();
    }

    @MyInheritedAnnotation("i")
    @MyNotInheritedAnnotation("ni")
    static class AnnotatedSuperClass {
    }

    @MyAnnotation("c1")
    @MyRepeatableAnnotation("cr1")
    @MyRepeatableAnnotation.List({
            @MyRepeatableAnnotation("cr2"),
            @MyRepeatableAnnotation("cr3")
    })
    @MyClassRetainedAnnotation
    static class AnnotatedClass extends AnnotatedSuperClass {
        @MyAnnotation("f1")
        @MyRepeatableAnnotation("fr1")
        @MyRepeatableAnnotation.List({
                @MyRepeatableAnnotation("fr2"),
                @MyRepeatableAnnotation("fr3")
        })
        @MyClassRetainedAnnotation
        Map<String, List<Number>> field;

        @MyAnnotation("m1")
        @MyRepeatableAnnotation("mr1")
        @MyRepeatableAnnotation.List({
                @MyRepeatableAnnotation("mr2"),
                @MyRepeatableAnnotation("mr3")
        })
        @MyClassRetainedAnnotation
        void method(@MyAnnotation("m2") @MyClassRetainedAnnotation Map<String, List<Number>> param,
                @MyAnnotation("m3") @MyClassRetainedAnnotation int[] otherParam) {
        }
    }

    @Test
    public void addAnnotation() throws IOException {
        assertOverlay("c1_C1_cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", (index, overlay) -> {
            ClassInfo clazz = index.getClassByName("org.jboss.jandex.test.MutableAnnotationOverlayTest$AnnotatedClass");
            overlay.addAnnotation(clazz, AnnotationInstance.builder(MyOtherAnnotation.class).value("C1").build());
        });
    }

    @Test
    public void removeAnnotation() throws IOException {
        assertOverlay("cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", (index, overlay) -> {
            ClassInfo clazz = index.getClassByName("org.jboss.jandex.test.MutableAnnotationOverlayTest$AnnotatedClass");
            overlay.removeAnnotations(clazz, annotation -> annotation.name().equals(MyAnnotation.DOT_NAME));
        });
    }

    @Test
    public void addAndRemoveAnnotation() throws IOException {
        assertOverlay("C2_cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", (index, overlay) -> {
            ClassInfo clazz = index.getClassByName("org.jboss.jandex.test.MutableAnnotationOverlayTest$AnnotatedClass");
            overlay.removeAnnotations(clazz, annotation -> annotation.name().equals(MyAnnotation.DOT_NAME));
            overlay.addAnnotation(clazz, AnnotationInstance.builder(MyOtherAnnotation.class).value("C2").build());
        });
    }

    private void assertOverlay(String expectedValues, BiConsumer<IndexView, MutableAnnotationOverlay> action)
            throws IOException {
        Index index = Index.of(AnnotatedSuperClass.class, AnnotatedClass.class, MyAnnotation.class,
                MyOtherAnnotation.class, MyRepeatableAnnotation.class, MyRepeatableAnnotation.List.class,
                MyClassRetainedAnnotation.class, MyInheritedAnnotation.class, MyNotInheritedAnnotation.class);

        for (boolean inheritedAnnotations : Arrays.asList(true, false)) {
            for (boolean runtimeAnnotationsOnly : Arrays.asList(true, false)) {
                MutableAnnotationOverlay.Builder builder = MutableAnnotationOverlay.builder(index);
                if (inheritedAnnotations) {
                    builder.inheritedAnnotations();
                }
                if (runtimeAnnotationsOnly) {
                    builder.runtimeAnnotationsOnly();
                }
                MutableAnnotationOverlay overlay = builder.build();

                action.accept(index, overlay);

                StringBuilder values = new StringBuilder();

                ClassInfo clazz = index.getClassByName(AnnotatedClass.class);
                assertNotNull(clazz);

                assertFalse(overlay.hasAnnotation(clazz, MyNotInheritedAnnotation.class));
                assertNull(overlay.annotation(clazz, MyNotInheritedAnnotation.class));
                assertEquals(0, overlay.annotationsWithRepeatable(clazz, MyNotInheritedAnnotation.class).size());

                if (inheritedAnnotations) {
                    assertTrue(overlay.hasAnnotation(clazz, MyInheritedAnnotation.class));
                    assertNotNull(overlay.annotation(clazz, MyInheritedAnnotation.class));
                    assertEquals("i", overlay.annotation(clazz, MyInheritedAnnotation.class).value().asString());
                    assertEquals(1, overlay.annotationsWithRepeatable(clazz, MyInheritedAnnotation.class).size());
                } else {
                    assertFalse(overlay.hasAnnotation(clazz, MyInheritedAnnotation.class));
                    assertNull(overlay.annotation(clazz, MyInheritedAnnotation.class));
                    assertEquals(0, overlay.annotationsWithRepeatable(clazz, MyInheritedAnnotation.class).size());
                }

                FieldInfo field = clazz.field("field");
                assertNotNull(field);

                MethodInfo method = clazz.firstMethod("method");
                assertNotNull(method);

                MethodParameterInfo parameter1 = method.parameters().get(0);
                assertNotNull(parameter1);

                MethodParameterInfo parameter2 = method.parameters().get(1);
                assertNotNull(parameter2);

                for (Declaration declaration : Arrays.asList(clazz, field, method, parameter1, parameter2)) {
                    if (overlay.hasAnnotation(declaration, MyAnnotation.DOT_NAME)) {
                        values.append(overlay.annotation(declaration, MyAnnotation.DOT_NAME).value().asString()).append("_");
                    }
                    if (overlay.hasAnnotation(declaration, MyOtherAnnotation.DOT_NAME)) {
                        values.append(overlay.annotation(declaration, MyOtherAnnotation.DOT_NAME).value().asString())
                                .append("_");
                    }
                    if (declaration != method) {
                        if (overlay.hasAnnotation(declaration, MyRepeatableAnnotation.DOT_NAME)) {
                            values.append(overlay.annotation(declaration, MyRepeatableAnnotation.DOT_NAME).value().asString())
                                    .append("_");
                        }
                        if (overlay.hasAnnotation(declaration, MyRepeatableAnnotation.List.DOT_NAME)) {
                            AnnotationInstance annotation = overlay.annotation(declaration,
                                    MyRepeatableAnnotation.List.DOT_NAME);
                            for (AnnotationInstance nestedAnnotation : annotation.value().asNestedArray()) {
                                values.append(nestedAnnotation.value().asString()).append("_");
                            }
                        }
                    } else { // just to test `annotationsWithRepeatable`, no other reason
                        for (AnnotationInstance annotation : overlay.annotationsWithRepeatable(declaration,
                                MyRepeatableAnnotation.DOT_NAME)) {
                            values.append(annotation.value().asString()).append("_");
                        }
                    }

                    if (runtimeAnnotationsOnly) {
                        assertFalse(overlay.hasAnnotation(declaration, MyClassRetainedAnnotation.class));
                        assertNull(overlay.annotation(declaration, MyClassRetainedAnnotation.class));
                        assertEquals(0, overlay.annotationsWithRepeatable(declaration, MyClassRetainedAnnotation.class).size());
                    } else {
                        assertTrue(overlay.hasAnnotation(declaration, MyClassRetainedAnnotation.class));
                        assertNotNull(overlay.annotation(declaration, MyClassRetainedAnnotation.class));
                        assertEquals(1, overlay.annotationsWithRepeatable(declaration, MyClassRetainedAnnotation.class).size());
                    }
                }

                if (values.length() > 0) {
                    values.deleteCharAt(values.length() - 1);
                }

                assertEquals(expectedValues, values.toString());
            }
        }
    }
}
