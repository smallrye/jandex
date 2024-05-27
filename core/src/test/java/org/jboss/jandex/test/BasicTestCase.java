/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class BasicTestCase {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FieldAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ParameterAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
    public @interface TypeUseAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {
        String name();

        int[] ints();

        String other() default "something";

        String override() default "override-me";

        long longValue();

        Class<?> klass();

        NestedAnnotation nested();

        ElementType[] enums();

        NestedAnnotation[] nestedArray();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation1 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation2 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation3 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation4 {
    }

    public @interface NestedAnnotation {
        float value();
    }

    @Retention(RetentionPolicy.CLASS)
    @Target({ ElementType.TYPE, ElementType.PARAMETER, ElementType.TYPE_USE })
    @interface RuntimeInvisible {
        String placement();
    }

    @RuntimeInvisible(placement = "class")
    class RuntimeInvisibleTarget {
        @SuppressWarnings("unused")
        @MethodAnnotation1
        void execute(
                @ParameterAnnotation @RuntimeInvisible(placement = "arg") List<@TypeUseAnnotation @RuntimeInvisible(placement = "type use") String> arg) {
        }
    }

    // @formatter:off
    @TestAnnotation(name = "Test", override = "somethingelse", ints = { 1, 2, 3, 4, 5 }, klass = Void.class,
            nested = @NestedAnnotation(1.34f), nestedArray = { @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) },
            enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
    // @formatter:on
    public class DummyClass implements Serializable {
        void doSomething(int x, long y, Long foo) {
        }

        void doSomething(int x, long y) {
        }

        @FieldAnnotation
        private int x;

        @MethodAnnotation1
        @MethodAnnotation2
        @MethodAnnotation4
        void doSomething(int x, long y, String foo) {
        }

        public class Nested {
            public Nested(int noAnnotation) {
            }

            public Nested(@ParameterAnnotation byte annotated) {
            }
        }
    }

    public enum Enum {
        A(1),
        B(2);

        private Enum(int noAnnotation) {
        }

        private Enum(@ParameterAnnotation byte annotated) {
        }
    }

    public enum EnumWithGenericConstructor {
        INSTANCE(Collections.singletonList(""));

        <T> EnumWithGenericConstructor(List<T> list) {
        }
    }

    // @formatter:off
    @TestAnnotation(name = "Test", ints = { 1, 2, 3, 4, 5 }, klass = Void.class, nested = @NestedAnnotation(1.34f),
            nestedArray = { @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) },
            enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
    // @formatter:on
    public static class NestedA implements Serializable {
    }

    // @formatter:off
    @TestAnnotation(name = "Test", ints = { 1, 2, 3, 4, 5 }, klass = Void.class, nested = @NestedAnnotation(1.34f),
            nestedArray = { @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) },
            enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
    // @formatter:on
    public static class NestedB implements Serializable {

        NestedB(Integer foo) {
        }
    }

    public static class NestedC implements Serializable {
    }

    public class NestedD implements Serializable {
    }

    public static class NoEnclosureAnonTest {
        static Class<?> anonymousStaticClass;
        Class<?> anonymousInnerClass;

        // @formatter:off
        static {
            anonymousStaticClass = new Object() {}.getClass();
        }
        {
            anonymousInnerClass = new Object() {}.getClass();
        }
        // @formatter:on
    }

    public static class ApiClass {
        public static void superApi() {
        }
    }

    public static class ApiUser {
        public void f() {
            ApiClass.superApi();
        }
    }

    @Test
    public void testIndexer() throws IOException {
        Indexer indexer = new Indexer();
        indexer.indexClass(DummyClass.class);
        indexer.indexClass(TestAnnotation.class);
        indexer.indexClass(DummyClass.Nested.class);
        indexer.indexClass(Enum.class);
        indexer.indexClass(EnumWithGenericConstructor.class);
        Index index = indexer.complete();

        verifyDummy(index, true);
        index.printSubclasses();
    }

    @Test
    public void testIndexOfDirectory() throws IOException, URISyntaxException {
        URL testLocation = getClass().getResource(getClass().getSimpleName() + ".class");
        File testDirectory = new File(testLocation.toURI().resolve("."));
        int expectedCount = testDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".class");
            }
        }).length;
        Index index = Index.of(testDirectory);
        assertEquals(expectedCount, index.getKnownClasses().size());
    }

    @Test
    public void testIndexOfEmptyDirectory() throws IOException, URISyntaxException {
        File tempDir = null;

        try {
            tempDir = File.createTempFile("temp", ".dir");
            tempDir.delete();
            tempDir.mkdir();
            Index index = Index.of(tempDir);
            assertEquals(0, index.getKnownClasses().size());
        } finally {
            if (tempDir != null) {
                tempDir.delete();
            }
        }
    }

    @Test
    public void testIndexOfDirectoryNonClassFile() throws IOException, URISyntaxException {
        File tempDir = null;
        File temp = null;

        try {
            tempDir = File.createTempFile("temp", ".dir");
            tempDir.delete();
            tempDir.mkdir();
            temp = File.createTempFile("dummy", ".tmp", tempDir);
            Index index = Index.of(temp.getParentFile());
            assertEquals(0, index.getKnownClasses().size());
        } finally {
            if (temp != null) {
                temp.delete();
            }
            if (tempDir != null) {
                tempDir.delete();
            }
        }
    }

    @Test
    public void testIndexOfClassFile() throws IOException, URISyntaxException {
        final URL testLocation = getClass().getResource(getClass().getSimpleName() + ".class");
        final File thisClassFile = new File(testLocation.toURI());
        Index index = Index.of(thisClassFile);
        assertEquals(1, index.getKnownClasses().size());
    }

    @Test
    public void testIndexOfNonClassFile() throws IOException {
        File tempDir = null;
        File tempFile = null;

        try {
            tempDir = File.createTempFile("temp", ".dir");
            tempDir.delete();
            tempDir.mkdir();
            tempFile = File.createTempFile("dummy", ".tmp", tempDir);

            File temp = tempFile;
            assertThrows(IllegalArgumentException.class, () -> {
                Index.of(temp);
            });
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
            if (tempDir != null) {
                tempDir.delete();
            }
        }
    }

    @Test
    public void testIndexOfNullDirectory() {
        assertThrows(IllegalArgumentException.class, () -> {
            Index.of((File) null);
        });
    }

    @Test
    public void testWriteRead() throws IOException {
        Indexer indexer = new Indexer();
        indexer.indexClass(DummyClass.class);
        indexer.indexClass(TestAnnotation.class);
        indexer.indexClass(DummyClass.Nested.class);
        indexer.indexClass(Enum.class);
        indexer.indexClass(EnumWithGenericConstructor.class);
        Index index = indexer.complete();

        index = IndexingUtil.roundtrip(index);

        verifyDummy(index, true);
    }

    @Test
    public void testWriteReadPreviousVersion() throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName()
                .replace('.', '/') + ".class");
        indexer.index(stream);
        Index index = indexer.complete();

        index = IndexingUtil.roundtrip(index);

        assertFalse(index.getClassByName(DotName.createSimple(DummyClass.class.getName())).hasNoArgsConstructor());
    }

    @Test
    public void testWriteReadNestingVersions() throws IOException {
        verifyWriteReadNesting(8, ClassInfo.NestingType.TOP_LEVEL);
        verifyWriteReadNesting(-1, ClassInfo.NestingType.ANONYMOUS);
    }

    private void verifyWriteReadNesting(int version, ClassInfo.NestingType expectedNoEncloseAnon) throws IOException {
        Class<?> noEncloseInstance = new NoEnclosureAnonTest().anonymousInnerClass;

        // @formatter:off
        Class<?> plainAnon = new Object() {}.getClass();
        // @formatter:on

        class Named {
        }

        Indexer indexer = new Indexer();
        indexClass(NestedC.class, indexer);
        indexClass(BasicTestCase.class, indexer);
        indexClass(NoEnclosureAnonTest.anonymousStaticClass, indexer);
        indexClass(noEncloseInstance, indexer);
        indexClass(plainAnon, indexer);
        indexClass(Named.class, indexer);
        Index index = indexer.complete();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int ignore = (version == -1) ? new IndexWriter(baos).write(index) : new IndexWriter(baos).write(index, version);

        index = new IndexReader(new ByteArrayInputStream(baos.toByteArray())).read();
        assertEquals(ClassInfo.NestingType.INNER,
                index.getClassByName(DotName.createSimple(NestedC.class.getName())).nestingType());
        assertEquals(ClassInfo.NestingType.TOP_LEVEL,
                index.getClassByName(DotName.createSimple(BasicTestCase.class.getName())).nestingType());
        assertEquals(ClassInfo.NestingType.ANONYMOUS,
                index.getClassByName(DotName.createSimple(plainAnon.getName())).nestingType());
        assertEquals(ClassInfo.NestingType.LOCAL,
                index.getClassByName(DotName.createSimple(Named.class.getName())).nestingType());
        assertEquals(expectedNoEncloseAnon,
                index.getClassByName(DotName.createSimple(noEncloseInstance.getName())).nestingType());
        assertEquals(expectedNoEncloseAnon,
                index.getClassByName(DotName.createSimple(NoEnclosureAnonTest.anonymousStaticClass.getName())).nestingType());
    }

    private void indexClass(Class<?> klass, Indexer indexer) throws IOException {
        InputStream stream;
        stream = getClass().getClassLoader().getResourceAsStream(klass.getName().replace('.', '/') + ".class");
        indexer.index(stream);
    }

    @Test
    public void testHasNoArgsConstructor() throws IOException {
        assertHasNoArgsConstructor(DummyClass.class, false);
        assertHasNoArgsConstructor(NestedA.class, true);
        assertHasNoArgsConstructor(NestedB.class, false);
        assertHasNoArgsConstructor(NestedC.class, true);
        assertHasNoArgsConstructor(DummyTopLevel.class, true);
        assertHasNoArgsConstructor(DummyTopLevelWithoutNoArgsConstructor.class, false);
    }

    @Test
    public void testStaticInner() throws IOException {
        assertFlagSet(NestedC.class, Modifier.STATIC, true);
        assertNesting(NestedC.class, ClassInfo.NestingType.INNER, true);

        assertFlagSet(NestedD.class, Modifier.STATIC, false);
        assertNesting(NestedC.class, ClassInfo.NestingType.INNER, true);

        assertNesting(BasicTestCase.class, ClassInfo.NestingType.INNER, false);
        assertFlagSet(BasicTestCase.class, Modifier.STATIC, false);
    }

    @Test
    public void testSimpleName() throws IOException {
        class MyLocal {
        }
        assertEquals("NestedC", getIndexForClasses(NestedC.class)
                .getClassByName(DotName.createSimple(NestedC.class.getName())).simpleName());
        assertEquals("BasicTestCase", getIndexForClasses(BasicTestCase.class)
                .getClassByName(DotName.createSimple(BasicTestCase.class.getName())).simpleName());
        assertEquals("MyLocal", getIndexForClasses(MyLocal.class)
                .getClassByName(DotName.createSimple(MyLocal.class.getName())).simpleName());
        assertEquals("String", getIndexForClasses(String.class)
                .getClassByName(DotName.createSimple(String.class.getName())).simpleName());
        // @formatter:off
        Class<?> anon = new Object() {}.getClass();
        // @formatter:on
        assertNull(getIndexForClasses(anon).getClassByName(DotName.createSimple(anon.getName())).simpleName());
    }

    @Test
    public void testAnon() throws IOException {
        Runnable blah = new Runnable() {
            @Override
            public void run() {
                System.out.println("blah");
            }
        };

        assertNesting(blah.getClass(), ClassInfo.NestingType.ANONYMOUS, true);

        NoEnclosureAnonTest nestedTest = new NoEnclosureAnonTest();
        assertNesting(nestedTest.anonymousInnerClass, ClassInfo.NestingType.ANONYMOUS, true);
        assertNesting(NoEnclosureAnonTest.anonymousStaticClass, ClassInfo.NestingType.ANONYMOUS, true);
    }

    @Test
    public void testNamedLocal() throws IOException {
        class Something {
            public void run() {
                System.out.println("blah");
            }
        }

        assertNesting(Something.class, ClassInfo.NestingType.LOCAL, true);
    }

    @Test
    public void testNullStream() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            Indexer indexer = new Indexer();
            indexer.index(null);
        });
    }

    @Test
    public void testNullClass() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            Indexer indexer = new Indexer();
            indexer.indexClass(null);
        });
    }

    @Test
    public void testRuntimeInvisiblePresentInV11() throws Exception {
        // Check @RuntimeInvisible not available to reflection APIs
        Class<RuntimeInvisibleTarget> testTarget = RuntimeInvisibleTarget.class;
        assertEquals(0, testTarget.getDeclaredAnnotations().length);

        Parameter[] executeParams = testTarget.getDeclaredMethod("execute", List.class).getParameters();
        assertEquals(1, executeParams[0].getDeclaredAnnotations().length);

        AnnotatedParameterizedType apt = (AnnotatedParameterizedType) executeParams[0].getAnnotatedType();
        AnnotatedType argGenericType = apt.getAnnotatedActualTypeArguments()[0];
        assertEquals(1, argGenericType.getDeclaredAnnotations().length);

        Index index = testClassConstantSerialisation(Index.of(testTarget), 11);
        DotName annoName = DotName.createSimple(RuntimeInvisible.class.getName());
        List<AnnotationInstance> rtInvisible = index.getAnnotations(annoName);

        assertEquals(4, rtInvisible.size());
        Map<String, List<AnnotationInstance>> placements = new HashMap<>(3);

        for (AnnotationInstance a : rtInvisible) {
            assertFalse(a.runtimeVisible());
            String placement = a.value("placement").asString();
            placements.compute(placement, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(a);
                return v;
            });
        }

        assertEquals(1, placements.get("class").size());
        // @RuntimeInvisible recorded on both method parameter and method parameter type
        assertEquals(2, placements.get("arg").size());
        assertEquals(1, placements.get("type use").size());
    }

    @Test
    public void testRuntimeInvisibleAbsentFromV10() throws IOException {
        Class<RuntimeInvisibleTarget> testTarget = RuntimeInvisibleTarget.class;
        Index index = testClassConstantSerialisation(Index.of(testTarget), 10);
        DotName annoName = DotName.createSimple(RuntimeInvisible.class.getName());
        List<AnnotationInstance> rtInvisible = index.getAnnotations(annoName);

        assertEquals(0, rtInvisible.size());

        annoName = DotName.createSimple(MethodAnnotation1.class.getName());
        List<AnnotationInstance> rtVisible = index.getAnnotations(annoName);
        assertEquals(1, rtVisible.size());

        for (AnnotationInstance a : rtVisible) {
            assertTrue(a.runtimeVisible());
        }
    }

    private void verifyDummy(Index index, boolean v2features) {
        AnnotationInstance instance = index.getAnnotations(DotName.createSimple(TestAnnotation.class.getName())).get(0);

        // Verify values
        assertEquals("Test", instance.value("name").asString());
        assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, instance.value("ints").asIntArray());
        assertEquals(Void.class.getName(), instance.value("klass").asClass().name().toString());
        assertEquals(1.34f, instance.value("nested").asNested().value().asFloat(), 0.0);
        assertEquals(3.14f, instance.value("nestedArray").asNestedArray()[0].value().asFloat(), 0.0);
        assertEquals(2.27f, instance.value("nestedArray").asNestedArray()[1].value().asFloat(), 0.0);
        assertEquals(ElementType.TYPE.name(), instance.value("enums").asEnumArray()[0]);
        assertEquals(ElementType.PACKAGE.name(), instance.value("enums").asEnumArray()[1]);
        assertEquals(10, instance.value("longValue").asLong());

        // Verify target
        assertEquals(DummyClass.class.getName(), instance.target().toString());
        List<ClassInfo> implementors = index.getKnownDirectImplementors(DotName.createSimple(Serializable.class.getName()));
        assertEquals(1, implementors.size());
        assertEquals(implementors.get(0).name(), DotName.createSimple(DummyClass.class.getName()));

        implementors = index.getKnownDirectImplementors(DotName.createSimple(InputStream.class.getName()));
        assertEquals(0, implementors.size());

        if (v2features) {
            // Verify classAnnotations
            ClassInfo clazz = (ClassInfo) instance.target();
            assertTrue(clazz.declaredAnnotations().contains(instance));
            assertEquals(1, clazz.declaredAnnotations().size());

            // Verify method annotations
            MethodInfo method = clazz.method("doSomething", PrimitiveType.INT, PrimitiveType.LONG,
                    Type.create(DotName.createSimple("java.lang.String"), Type.Kind.CLASS));

            // Verify default value
            assertEquals("something", instance.valueWithDefault(index, "other").asString());
            assertEquals("somethingelse", instance.valueWithDefault(index, "override").asString());
            assertEquals("override-me", index.getClassByName(instance.name()).method("override").defaultValue().asString());

            List<AnnotationValue> annotationValues = instance.valuesWithDefaults(index);
            AnnotationValue otherValue = null;
            AnnotationValue overrideValue = null;
            for (AnnotationValue value : annotationValues) {
                if ("other".equals(value.name())) {
                    otherValue = value;
                } else if ("override".equals(value.name())) {
                    overrideValue = value;
                }
            }

            assertEquals(9, annotationValues.size());
            assertEquals("something", otherValue.asString());
            assertEquals("somethingelse", overrideValue.asString());

            assertNotNull(method);
            assertFalse(method.isConstructor());
            assertEquals(3, method.annotations().size());
            assertEquals(MethodAnnotation1.class.getName(),
                    method.annotation(DotName.createSimple(MethodAnnotation1.class.getName())).name().toString());
            assertEquals(MethodAnnotation2.class.getName(),
                    method.annotation(DotName.createSimple(MethodAnnotation2.class.getName())).name().toString());
            assertEquals(MethodAnnotation4.class.getName(),
                    method.annotation(DotName.createSimple(MethodAnnotation4.class.getName())).name().toString());
            assertFalse(method.hasAnnotation(DotName.createSimple(MethodAnnotation3.class.getName())));

            assertEquals("x", method.parameterName(0));
            assertEquals("y", method.parameterName(1));
            assertEquals("foo", method.parameterName(2));

            MethodInfo method2 = clazz.method("doSomething", Arrays.asList(PrimitiveType.INT, PrimitiveType.LONG,
                    ClassType.create("java.lang.String")));
            assertEquals(method, method2);

            ClassInfo nested = index.getClassByName(DotName.createSimple(DummyClass.Nested.class.getName()));
            assertNotNull(nested);
            MethodInfo nestedConstructor1 = nested.method("<init>", PrimitiveType.INT);
            assertNotNull(nestedConstructor1);
            assertTrue(nestedConstructor1.isConstructor());
            assertEquals(1, nestedConstructor1.parametersCount());
            assertEquals(1, nestedConstructor1.parameterTypes().size());
            assertEquals(1, nestedConstructor1.parameters().size());
            assertEquals("noAnnotation", nestedConstructor1.parameterName(0));

            MethodInfo nestedConstructor2 = nested.method("<init>", PrimitiveType.BYTE);
            assertNotNull(nestedConstructor2);
            assertTrue(nestedConstructor2.isConstructor());
            // synthetic param counts here
            assertEquals(1, nestedConstructor2.parametersCount());
            assertEquals(1, nestedConstructor2.parameterTypes().size());
            assertEquals(1, nestedConstructor2.parameters().size());
            assertEquals("annotated", nestedConstructor2.parameterName(0));

            AnnotationInstance paramAnnotation = nestedConstructor2
                    .annotation(DotName.createSimple(ParameterAnnotation.class.getName()));
            assertNotNull(paramAnnotation);
            assertEquals(Kind.METHOD_PARAMETER, paramAnnotation.target().kind());
            assertEquals("annotated", paramAnnotation.target().asMethodParameter().name());
            assertEquals(0, paramAnnotation.target().asMethodParameter().position());

            List<MethodParameterInfo> nestedConstructor2Parameters = nestedConstructor2.parameters();
            MethodParameterInfo nestedParamAnnotated = nestedConstructor2Parameters.get(0);
            assertEquals("annotated", nestedParamAnnotated.name());
            assertEquals(0, nestedParamAnnotated.position());
            assertTrue(nestedParamAnnotated.hasAnnotation(DotName.createSimple(ParameterAnnotation.class.getName())));
            assertNotNull(nestedParamAnnotated.annotation(DotName.createSimple(ParameterAnnotation.class.getName())));
            assertEquals(1, nestedParamAnnotated.annotations().size());
            assertTrue(nestedParamAnnotated.type().annotations().isEmpty());
            assertNull(nestedParamAnnotated.type().annotation(DotName.createSimple(ParameterAnnotation.class.getName())));

            ClassInfo enumClass = index.getClassByName(DotName.createSimple(Enum.class.getName()));
            assertNotNull(enumClass);
            MethodInfo enumConstructor1 = enumClass.method("<init>", PrimitiveType.INT);
            assertNotNull(enumConstructor1);
            assertTrue(enumConstructor1.isConstructor());
            assertEquals(1, enumConstructor1.parametersCount());
            assertEquals("noAnnotation", enumConstructor1.parameterName(0));

            MethodInfo enumConstructor2 = enumClass.method("<init>", PrimitiveType.BYTE);
            assertNotNull(enumConstructor2);
            assertTrue(enumConstructor2.isConstructor());
            assertEquals(1, enumConstructor2.parametersCount());
            assertEquals("annotated", enumConstructor2.parameterName(0));

            paramAnnotation = enumConstructor2.annotation(DotName.createSimple(ParameterAnnotation.class.getName()));
            assertNotNull(paramAnnotation);
            assertEquals(Kind.METHOD_PARAMETER, paramAnnotation.target().kind());
            assertEquals("annotated", paramAnnotation.target().asMethodParameter().name());
            assertEquals(0, paramAnnotation.target().asMethodParameter().position());

            ClassInfo enumWithGenericConstructorClass = index.getClassByName(EnumWithGenericConstructor.class.getName());
            assertNotNull(enumWithGenericConstructorClass);
            MethodInfo ctor = enumWithGenericConstructorClass.firstMethod("<init>");
            assertNotNull(ctor);
            assertTrue(ctor.isConstructor());
            assertEquals(1, ctor.parametersCount());
            assertEquals(Type.Kind.PARAMETERIZED_TYPE, ctor.parameterType(0).kind());
            assertEquals("java.util.List", ctor.parameterType(0).asParameterizedType().name().toString());
            assertEquals(1, ctor.parameterType(0).asParameterizedType().arguments().size());
            assertEquals(Type.Kind.TYPE_VARIABLE, ctor.parameterType(0).asParameterizedType().arguments().get(0).kind());
            assertEquals("T", ctor.parameterType(0).asParameterizedType().arguments().get(0).asTypeVariable().identifier());
            assertEquals("list", ctor.parameterName(0));
        }

        // Verify hasNoArgsConstructor
        assertFalse(index.getClassByName(DotName.createSimple(DummyClass.class.getName())).hasNoArgsConstructor());
    }

    private void assertHasNoArgsConstructor(Class<?> clazz, boolean result) throws IOException {
        ClassInfo classInfo = getIndexForClasses(clazz).getClassByName(DotName.createSimple(clazz.getName()));
        assertNotNull(classInfo);
        assertEquals(result, classInfo.hasNoArgsConstructor());
    }

    private void assertFlagSet(Class<?> clazz, int flag, boolean result) throws IOException {
        ClassInfo classInfo = getIndexForClasses(clazz).getClassByName(DotName.createSimple(clazz.getName()));
        assertNotNull(classInfo);
        assertTrue((classInfo.flags() & flag) == (result ? flag : 0));
    }

    private void assertNesting(Class<?> clazz, ClassInfo.NestingType nesting, boolean result) throws IOException {
        ClassInfo classInfo = getIndexForClasses(clazz).getClassByName(DotName.createSimple(clazz.getName()));
        assertNotNull(classInfo);
        if (result) {
            assertEquals(nesting, classInfo.nestingType());
        } else {
            assertNotEquals(nesting, classInfo.nestingType());
        }
    }

    static Index getIndexForClasses(Class<?>... classes) throws IOException {
        return Index.of(classes);
    }

    static ClassInfo getClassInfo(Class<?> clazz) throws IOException {
        return getIndexForClasses(clazz).getClassByName(DotName.createSimple(clazz.getName()));
    }

    @Test
    public void testClassConstantIndexing() throws IOException, URISyntaxException {
        Index index = getIndexForClasses(DummyClass.class, ApiClass.class, ApiUser.class);
        DotName apiClassDotName = DotName.createSimple(ApiClass.class.getName());
        List<ClassInfo> users = index.getKnownUsers(apiClassDotName);
        assertEquals(2, users.size());
        ClassInfo apiUserClassInfo = index.getClassByName(DotName.createSimple(ApiUser.class.getName()));
        assertTrue(users.contains(apiUserClassInfo));
        ClassInfo apiClassInfo = index.getClassByName(apiClassDotName);
        assertTrue(users.contains(apiClassInfo));

        Index readIndex = testClassConstantSerialisation(index, -1);
        List<ClassInfo> readUsers = readIndex.getKnownUsers(apiClassDotName);
        assertEquals(2, readUsers.size());
        ClassInfo readApiUserClassInfo = readIndex.getClassByName(DotName.createSimple(ApiUser.class.getName()));
        assertTrue(readUsers.contains(readApiUserClassInfo));
        ClassInfo readApiClassInfo = readIndex.getClassByName(apiClassDotName);
        assertTrue(readUsers.contains(readApiClassInfo));

        Index readOldIndex = testClassConstantSerialisation(index, 9);
        assertEquals(0, readOldIndex.getKnownUsers(apiClassDotName).size());

        Index allClasses = index(getClass().getProtectionDomain().getCodeSource().getLocation(),
                Index.class.getProtectionDomain().getCodeSource().getLocation());
        System.err.println("Indexed " + allClasses.getKnownClasses().size() + " classes");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.err.println("V9 size: " + new IndexWriter(baos).write(index, 9));
        baos = new ByteArrayOutputStream();
        System.err.println("V10 size: " + new IndexWriter(baos).write(index));
    }

    private Index index(URL... locations) throws URISyntaxException, IOException {
        final Indexer indexer = new Indexer();
        final ClassLoader cl = BasicTestCase.class.getClassLoader();

        for (URL url : locations) {
            final Path path = Paths.get(url.toURI());
            Files.walkFileTree(path, new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String name = file.toString();
                    if (name.endsWith(".class")) {
                        InputStream stream = cl.getResourceAsStream(path.relativize(file).toString());
                        indexer.index(stream);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

            });
        }

        return indexer.complete();
    }

    private Index testClassConstantSerialisation(Index index, int version) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int ignore = (version == -1) ? new IndexWriter(baos).write(index) : new IndexWriter(baos).write(index, version);

        return new IndexReader(new ByteArrayInputStream(baos.toByteArray())).read();
    }
}
