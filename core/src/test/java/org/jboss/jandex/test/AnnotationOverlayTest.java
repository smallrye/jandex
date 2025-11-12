package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationOverlay;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTransformation;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Declaration;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.ModuleInfo;
import org.junit.jupiter.api.Test;

public class AnnotationOverlayTest {
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
    public void directTransformation_addAnnotation() throws IOException {
        AnnotationTransformation transformation = new AnnotationTransformation() {
            @Override
            public boolean supports(AnnotationTarget.Kind kind) {
                return kind == AnnotationTarget.Kind.CLASS;
            }

            @Override
            public void apply(TransformationContext context) {
                if (context.declaration().asClass().name().toString()
                        .equals("org.jboss.jandex.test.AnnotationOverlayTest$AnnotatedClass")) {
                    context.add(AnnotationInstance.builder(MyOtherAnnotation.class).value("C1").build());
                }
            }
        };

        assertOverlay("c1_C1_cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", transformation);
    }

    @Test
    public void directTransformation_removeAnnotation() throws IOException {
        AnnotationTransformation transformation = new AnnotationTransformation() {
            @Override
            public boolean supports(AnnotationTarget.Kind kind) {
                return kind == AnnotationTarget.Kind.CLASS;
            }

            @Override
            public void apply(TransformationContext context) {
                if (context.declaration().asClass().name().toString()
                        .equals("org.jboss.jandex.test.AnnotationOverlayTest$AnnotatedClass")) {
                    context.remove(annotation -> annotation.name().equals(MyAnnotation.DOT_NAME));
                }
            }
        };

        assertOverlay("cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", transformation);
    }

    @Test
    public void directTransformation_addAndRemoveAnnotation() throws IOException {
        AnnotationTransformation transformation = new AnnotationTransformation() {
            @Override
            public boolean supports(AnnotationTarget.Kind kind) {
                return kind == AnnotationTarget.Kind.CLASS;
            }

            @Override
            public void apply(TransformationContext context) {
                if (context.declaration().asClass().name().toString()
                        .equals("org.jboss.jandex.test.AnnotationOverlayTest$AnnotatedClass")) {
                    context.remove(annotation -> annotation.name().equals(MyAnnotation.DOT_NAME));
                    context.add(AnnotationInstance.builder(MyOtherAnnotation.class).value("C2").build());
                }
            }
        };

        assertOverlay("C2_cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", transformation);
    }

    @Test
    public void builtTransformation_addAnnotation() throws IOException {
        AnnotationTransformation transformation = AnnotationTransformation.forClasses()
                .whenClass(DotName.createSimple(AnnotatedClass.class))
                .transform(ctx -> {
                    ctx.add(AnnotationInstance.builder(MyOtherAnnotation.class).value("C3").build());
                });

        assertOverlay("c1_C3_cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", transformation);
    }

    @Test
    public void builtTransformation_removeAnnotation() throws IOException {
        AnnotationTransformation transformation = AnnotationTransformation.forClasses()
                .whenClass(DotName.createSimple(AnnotatedClass.class))
                .transform(ctx -> {
                    ctx.remove(annotation -> annotation.name().equals(MyAnnotation.DOT_NAME));
                });

        assertOverlay("cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", transformation);
    }

    @Test
    public void builtTransformation_addAndRemoveAnnotation() throws IOException {
        AnnotationTransformation transformation = AnnotationTransformation.forClasses()
                .whenClass(DotName.createSimple(AnnotatedClass.class))
                .transform(ctx -> {
                    ctx.remove(annotation -> annotation.name().equals(MyAnnotation.DOT_NAME));
                    ctx.add(AnnotationInstance.builder(MyOtherAnnotation.class).value("C4").build());
                });

        assertOverlay("C4_cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", transformation);
    }

    @Test
    public void multipleNonconflictingTransformations() throws IOException {
        AnnotationTransformation transformation1 = AnnotationTransformation.forClasses()
                .whenClass(DotName.createSimple(AnnotatedClass.class))
                .transform(ctx -> {
                    ctx.add(AnnotationInstance.builder(MyOtherAnnotation.class).value("C5").build());
                });
        AnnotationTransformation transformation2 = AnnotationTransformation.forMethods()
                .whenMethod(DotName.createSimple(AnnotatedClass.class), "method")
                .transform(ctx -> {
                    ctx.add(AnnotationInstance.builder(MyOtherAnnotation.class).value("M1").build());
                });

        assertOverlay("c1_C5_cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_M1_mr1_mr2_mr3_m2_m3", transformation1, transformation2);
    }

    @Test
    public void multipleConflictingTransformations_firstBeforeSecond() throws IOException {
        AnnotationTransformation transformation1 = AnnotationTransformation.forClasses()
                .priority(10)
                .whenClass(DotName.createSimple(AnnotatedClass.class))
                .transform(ctx -> {
                    ctx.remove(annotation -> annotation.name().equals(MyAnnotation.DOT_NAME));
                });
        AnnotationTransformation transformation2 = AnnotationTransformation.forClasses()
                .priority(1)
                .whenClass(DotName.createSimple(AnnotatedClass.class))
                .whenAnyMatch(MyAnnotation.DOT_NAME)
                .transform(ctx -> {
                    ctx.add(AnnotationInstance.builder(MyOtherAnnotation.DOT_NAME).value("C6").build());
                });

        assertOverlay("cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", transformation1, transformation2);
    }

    @Test
    public void multipleConflictingTransformations_secondBeforeFirst() throws IOException {
        AnnotationTransformation transformation1 = AnnotationTransformation.forClasses()
                .priority(1)
                .whenClass(DotName.createSimple(AnnotatedClass.class))
                .transform(ctx -> {
                    ctx.remove(annotation -> annotation.name().equals(MyAnnotation.DOT_NAME));
                });
        AnnotationTransformation transformation2 = AnnotationTransformation.forClasses()
                .priority(10)
                .whenClass(DotName.createSimple(AnnotatedClass.class))
                .whenAnyMatch(MyAnnotation.DOT_NAME)
                .transform(ctx -> {
                    ctx.add(AnnotationInstance.builder(MyOtherAnnotation.DOT_NAME).value("C7").build());
                });

        assertOverlay("C7_cr1_cr2_cr3_f1_fr1_fr2_fr3_m1_mr1_mr2_mr3_m2_m3", transformation1, transformation2);
    }

    private void assertOverlay(String expectedValues, AnnotationTransformation... transformations) throws IOException {
        Index index = Index.of(AnnotatedSuperClass.class, AnnotatedClass.class, MyAnnotation.class,
                MyOtherAnnotation.class, MyRepeatableAnnotation.class, MyRepeatableAnnotation.List.class,
                MyClassRetainedAnnotation.class, MyInheritedAnnotation.class, MyNotInheritedAnnotation.class);

        for (boolean inheritedAnnotations : Arrays.asList(true, false)) {
            for (boolean runtimeAnnotationsOnly : Arrays.asList(true, false)) {
                AnnotationOverlay.Builder builder = AnnotationOverlay.builder(index, Arrays.asList(transformations));
                if (inheritedAnnotations) {
                    builder.inheritedAnnotations();
                }
                if (runtimeAnnotationsOnly) {
                    builder.runtimeAnnotationsOnly();
                }
                AnnotationOverlay overlay = builder.build();

                StringBuilder values = new StringBuilder();

                ClassInfo clazz = index.getClassByName(AnnotatedClass.class);
                assertNotNull(clazz);

                assertFalse(overlay.hasAnnotation(clazz, MyNotInheritedAnnotation.class));
                assertNull(overlay.annotation(clazz, MyNotInheritedAnnotation.class));
                assertEquals(0, overlay.annotationsWithRepeatable(clazz, MyNotInheritedAnnotation.class).size());

                if (inheritedAnnotations) {
                    assertTrue(overlay.hasAnnotation(clazz, MyInheritedAnnotation.class));
                    assertNotNull(overlay.annotation(clazz, MyInheritedAnnotation.class));
                    assertNotNull(overlay.annotation(clazz, MyInheritedAnnotation.class).target());
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
                        assertNotNull(overlay.annotation(declaration, MyAnnotation.DOT_NAME).target());
                        values.append(overlay.annotation(declaration, MyAnnotation.DOT_NAME).value().asString()).append("_");
                    }
                    if (overlay.hasAnnotation(declaration, MyOtherAnnotation.DOT_NAME)) {
                        assertNotNull(overlay.annotation(declaration, MyOtherAnnotation.DOT_NAME).target());
                        values.append(overlay.annotation(declaration, MyOtherAnnotation.DOT_NAME).value().asString())
                                .append("_");
                    }
                    if (declaration != method) {
                        if (overlay.hasAnnotation(declaration, MyRepeatableAnnotation.DOT_NAME)) {
                            assertNotNull(overlay.annotation(declaration, MyRepeatableAnnotation.DOT_NAME).target());
                            values.append(overlay.annotation(declaration, MyRepeatableAnnotation.DOT_NAME).value().asString())
                                    .append("_");
                        }
                        if (overlay.hasAnnotation(declaration, MyRepeatableAnnotation.List.DOT_NAME)) {
                            AnnotationInstance annotation = overlay.annotation(declaration,
                                    MyRepeatableAnnotation.List.DOT_NAME);
                            assertNotNull(annotation.target());
                            for (AnnotationInstance nestedAnnotation : annotation.value().asNestedArray()) {
                                values.append(nestedAnnotation.value().asString()).append("_");
                            }
                        }
                    } else { // just to test `annotationsWithRepeatable`, no other reason
                        for (AnnotationInstance annotation : overlay.annotationsWithRepeatable(declaration,
                                MyRepeatableAnnotation.DOT_NAME)) {
                            assertNotNull(annotation.target());
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
                        assertNotNull(overlay.annotation(declaration, MyClassRetainedAnnotation.class).target());
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

    /**
     * Tests that accessing annotations on the {@code Object} class does not attempt
     * to retrieve the (non-existing) superclass.
     */
    @Test
    public void accessAnnotationsOnTheObjectClass() throws IOException {
        // create a trivial index and an overlay with inherited annotations
        Index index = Index.of(Object.class);
        ClassInfo clazz = index.getClassByName(Object.class);

        AnnotationOverlay overlay = AnnotationOverlay.builder(new MyIndexWrapper(index), Collections.emptyList())
                .inheritedAnnotations()
                .build();

        // Note that if there was a failure, it would be an assertion error from MyIndexWrapper
        assertFalse(overlay.hasAnnotation(clazz, MyInheritedAnnotation.class));
        assertFalse(overlay.hasAnyAnnotation(clazz, MyInheritedAnnotation.class));
        assertNull(overlay.annotation(clazz, MyInheritedAnnotation.class));
        assertEquals(0, overlay.annotationsWithRepeatable(clazz, MyRepeatableAnnotation.class).size());
        assertNotNull(overlay.annotations(clazz)); // usually empty, but there's `@AOTSafeClassInitializer` on JDK 26-ea
    }

    static class MyIndexWrapper implements IndexView {
        private final IndexView delegate;

        public MyIndexWrapper(IndexView delegate) {
            this.delegate = delegate;
        }

        @Override
        public Collection<ClassInfo> getKnownClasses() {
            return delegate.getKnownClasses();
        }

        @Override
        public ClassInfo getClassByName(DotName className) {
            if (className == null) {
                fail("IndexView#getClassByName should never be invoked with null parameter!");
            }
            return delegate.getClassByName(className);
        }

        @Override
        public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
            return delegate.getKnownDirectSubclasses(className);
        }

        @Override
        public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
            return delegate.getAllKnownSubclasses(className);
        }

        @Override
        public Collection<ClassInfo> getKnownDirectSubinterfaces(DotName interfaceName) {
            return delegate.getKnownDirectSubinterfaces(interfaceName);
        }

        @Override
        public Collection<ClassInfo> getAllKnownSubinterfaces(DotName interfaceName) {
            return delegate.getAllKnownSubinterfaces(interfaceName);
        }

        @Override
        public Collection<ClassInfo> getKnownDirectImplementations(DotName interfaceName) {
            return delegate.getKnownDirectImplementations(interfaceName);
        }

        @Override
        public Collection<ClassInfo> getAllKnownImplementations(DotName interfaceName) {
            return delegate.getAllKnownImplementations(interfaceName);
        }

        @Override
        public Collection<ClassInfo> getKnownDirectImplementors(DotName interfaceName) {
            return delegate.getKnownDirectImplementors(interfaceName);
        }

        @Override
        public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
            return delegate.getAllKnownImplementors(interfaceName);
        }

        @Override
        public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
            return delegate.getAnnotations(annotationName);
        }

        @Override
        public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index) {
            return delegate.getAnnotationsWithRepeatable(annotationName, this);
        }

        @Override
        public Collection<ModuleInfo> getKnownModules() {
            return delegate.getKnownModules();
        }

        @Override
        public ModuleInfo getModuleByName(DotName moduleName) {
            return delegate.getModuleByName(moduleName);
        }

        @Override
        public Collection<ClassInfo> getKnownUsers(DotName className) {
            return delegate.getKnownUsers(className);
        }

        @Override
        public Collection<ClassInfo> getClassesInPackage(DotName packageName) {
            return delegate.getClassesInPackage(packageName);
        }

        @Override
        public Set<DotName> getSubpackages(DotName packageName) {
            return delegate.getSubpackages(packageName);
        }
    }
}
