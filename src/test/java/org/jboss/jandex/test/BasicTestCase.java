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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.junit.Assert;
import org.junit.Test;

public class BasicTestCase {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FieldAnnotation {

    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ParameterAnnotation {

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
    public @interface MethodAnnotation1 {}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation2 {}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation3 {}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation4 {}

    public @interface NestedAnnotation {
        float value();
    }

    @TestAnnotation(name = "Test", override = "somethingelse", ints = { 1, 2, 3, 4, 5 }, klass = Void.class, nested = @NestedAnnotation(1.34f), nestedArray = {
            @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) }, enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
    public class DummyClass implements Serializable {
        void doSomething(int x, long y, Long foo){}
        void doSomething(int x, long y){}

        @FieldAnnotation
        private int x;

        @MethodAnnotation1
        @MethodAnnotation2
        @MethodAnnotation4
        void doSomething(int x, long y, String foo){}
        
        public class Nested {
            public Nested(int noAnnotation) {}
            public Nested(@ParameterAnnotation byte annotated) {}
        }
    }

    public enum Enum {
        A(1), B(2);
        
        private Enum(int noAnnotation) {}
        private Enum(@ParameterAnnotation byte annotated) {}
    }
    
    @TestAnnotation(name = "Test", ints = { 1, 2, 3, 4, 5 }, klass = Void.class, nested = @NestedAnnotation(1.34f), nestedArray = {
        @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) }, enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
    public static class NestedA implements Serializable {
    }

    @TestAnnotation(name = "Test", ints = { 1, 2, 3, 4, 5 }, klass = Void.class, nested = @NestedAnnotation(1.34f), nestedArray = {
        @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) }, enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
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

        static {
            anonymousStaticClass = new Object() {}.getClass();
        }
        {
            anonymousInnerClass = new Object() {}.getClass();
        }
    }

    @Test
    public void testIndexer() throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(TestAnnotation.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(DummyClass.Nested.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(Enum.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        Index index = indexer.complete();

        verifyDummy(index, true);
        index.printSubclasses();
    }

    @Test
    public void testWriteRead() throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(TestAnnotation.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(DummyClass.Nested.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(Enum.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        Index index = indexer.complete();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new IndexWriter(baos).write(index);

        index = new IndexReader(new ByteArrayInputStream(baos.toByteArray())).read();

        verifyDummy(index, true);
    }

    @Test
    public void testWriteReadPreviousVersion() throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        Index index = indexer.complete();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new IndexWriter(baos).write(index, (byte)2);

        index = new IndexReader(new ByteArrayInputStream(baos.toByteArray())).read();
        assertFalse(index.getClassByName(DotName.createSimple(DummyClass.class.getName())).hasNoArgsConstructor());
    }

    @Test
    public void testWriteReadNestingVersions() throws IOException {
        verifyWriteReadNesting(8, ClassInfo.NestingType.TOP_LEVEL);
        verifyWriteReadNesting(-1, ClassInfo.NestingType.ANONYMOUS);
    }

    private void verifyWriteReadNesting(int version, ClassInfo.NestingType expectedNoEncloseAnon) throws IOException {
        Class<?> noEncloseInstance = new NoEnclosureAnonTest().anonymousInnerClass;
        Class<?> plainAnon = new Object(){}.getClass();
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
        assertEquals(ClassInfo.NestingType.INNER, index.getClassByName(DotName.createSimple(NestedC.class.getName())).nestingType());
        assertEquals(ClassInfo.NestingType.TOP_LEVEL, index.getClassByName(DotName.createSimple(BasicTestCase.class.getName())).nestingType());
        assertEquals(ClassInfo.NestingType.ANONYMOUS, index.getClassByName(DotName.createSimple(plainAnon.getName())).nestingType());
        assertEquals(ClassInfo.NestingType.LOCAL, index.getClassByName(DotName.createSimple(Named.class.getName())).nestingType());
        assertEquals(expectedNoEncloseAnon, index.getClassByName(DotName.createSimple(noEncloseInstance.getName())).nestingType());
        assertEquals(expectedNoEncloseAnon, index.getClassByName(DotName.createSimple(NoEnclosureAnonTest.anonymousStaticClass.getName())).nestingType());
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
    public void testSimpleName() throws IOException  {
        class MyLocal{}
        assertEquals("NestedC", getIndexForClass(NestedC.class)
                                .getClassByName(DotName.createSimple(NestedC.class.getName())
                                ).simpleName());
        assertEquals("BasicTestCase", getIndexForClass(BasicTestCase.class)
                                        .getClassByName(DotName.createSimple(BasicTestCase.class.getName())
                                        ).simpleName());
        assertEquals("MyLocal", getIndexForClass(MyLocal.class)
                                        .getClassByName(DotName.createSimple(MyLocal.class.getName())
                                        ).simpleName());
        Class<?> anon = new Object(){}.getClass();
        assertEquals(null, getIndexForClass(anon)
                                        .getClassByName(DotName.createSimple(anon.getName())
                                        ).simpleName());
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

    private void verifyDummy(Index index, boolean v2features) {
        AnnotationInstance instance = index.getAnnotations(DotName.createSimple(TestAnnotation.class.getName())).get(0);

        // Verify values
        assertEquals("Test", instance.value("name").asString());
        assertTrue(Arrays.equals(new int[] {1,2,3,4,5}, instance.value("ints").asIntArray()));
        assertEquals(Void.class.getName(), instance.value("klass").asClass().name().toString());
        assertTrue(1.34f == instance.value("nested").asNested().value().asFloat());
        assertTrue(3.14f == instance.value("nestedArray").asNestedArray()[0].value().asFloat());
        assertTrue(2.27f == instance.value("nestedArray").asNestedArray()[1].value().asFloat());
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
            assertTrue(clazz.classAnnotations().contains(instance));
            assertEquals(1, clazz.classAnnotations().size());

            // Verify method annotations
            MethodInfo method = clazz.method("doSomething", PrimitiveType.INT, PrimitiveType.LONG, Type.create(DotName.createSimple("java.lang.String"), Type.Kind.CLASS));

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
            assertEquals(3, method.annotations().size());
            assertEquals(MethodAnnotation1.class.getName(), method.annotation(DotName.createSimple(MethodAnnotation1.class.getName())).name().toString());
            assertEquals(MethodAnnotation2.class.getName(), method.annotation(DotName.createSimple(MethodAnnotation2.class.getName())).name().toString());
            assertEquals(MethodAnnotation4.class.getName(), method.annotation(DotName.createSimple(MethodAnnotation4.class.getName())).name().toString());
            assertFalse(method.hasAnnotation(DotName.createSimple(MethodAnnotation3.class.getName())));
            
            assertEquals("x", method.parameterName(0));
            assertEquals("y", method.parameterName(1));
            assertEquals("foo", method.parameterName(2));
            
            ClassInfo nested = index.getClassByName(DotName.createSimple(DummyClass.Nested.class.getName()));
            assertNotNull(nested);
            // synthetic param counts here
            MethodInfo nestedConstructor1 = nested.method("<init>", 
                  Type.create(DotName.createSimple(DummyClass.class.getName()), Type.Kind.CLASS), PrimitiveType.INT);
            assertNotNull(nestedConstructor1);
            // synthetic param counts here
            assertEquals(2, nestedConstructor1.parameters().size());
            assertEquals(2, nestedConstructor1.parametersList().size());
            // synthetic param does not counts here
            assertEquals("noAnnotation", nestedConstructor1.parameterName(0));

            List<MethodParameterInfo> nestedInit0ParametersList = nestedConstructor1.parametersList();
            MethodParameterInfo nestedParamNoAnnotation = nestedInit0ParametersList.get(0);
            assertEquals("noAnnotation", nestedInit0ParametersList.get(0).name());
            assertEquals(0, nestedParamNoAnnotation.position());
            assertEquals(0, nestedParamNoAnnotation.annotations().size());
            assertNull(nestedParamNoAnnotation.annotation(DotName.createSimple(ParameterAnnotation.class.getName())));
            assertFalse(nestedParamNoAnnotation.hasAnnotation(DotName.createSimple(ParameterAnnotation.class.getName())));

            MethodInfo nestedConstructor2 = nested.method("<init>", 
                  Type.create(DotName.createSimple(DummyClass.class.getName()), Type.Kind.CLASS), PrimitiveType.BYTE);
            assertNotNull(nestedConstructor2);
            // synthetic param counts here
            assertEquals(2, nestedConstructor2.parameters().size());
            assertEquals(2, nestedConstructor2.parametersList().size());
            // synthetic param does not counts here
            assertEquals("annotated", nestedConstructor2.parameterName(0));

            AnnotationInstance paramAnnotation = nestedConstructor2.annotation(DotName.createSimple(ParameterAnnotation.class.getName()));
            assertNotNull(paramAnnotation);
            assertEquals(Kind.METHOD_PARAMETER, paramAnnotation.target().kind());
            assertEquals("annotated", paramAnnotation.target().asMethodParameter().name());
            assertEquals(0, paramAnnotation.target().asMethodParameter().position());

            List<MethodParameterInfo> nestedInit1ParametersList = nestedConstructor2.parametersList();
            MethodParameterInfo nestedParamAnnotated = nestedInit1ParametersList.get(0);
            assertEquals("annotated", nestedParamAnnotated.name());
            assertEquals(0, nestedParamAnnotated.position());
            assertEquals(1, nestedParamAnnotated.annotations().size());
            assertNotNull(nestedParamAnnotated.annotation(DotName.createSimple(ParameterAnnotation.class.getName())));
            assertTrue(nestedParamAnnotated.hasAnnotation(DotName.createSimple(ParameterAnnotation.class.getName())));
            
            ClassInfo enumClass = index.getClassByName(DotName.createSimple(Enum.class.getName()));
            assertNotNull(enumClass);
            // synthetic param counts here (for ECJ)
            MethodInfo enumConstructor1 = enumClass.method("<init>", 
                  Type.create(DotName.createSimple("java.lang.String"), Type.Kind.CLASS), PrimitiveType.INT, PrimitiveType.INT);
            if(enumConstructor1 == null) {
                enumConstructor1 = enumClass.method("<init>", PrimitiveType.INT);
                assertNotNull(enumConstructor1);
                // synthetic param does not found here
                assertEquals(1, enumConstructor1.parameters().size());
                assertEquals(1, enumConstructor1.parametersList().size());
            }else {
                // synthetic param counts here
                assertEquals(3, enumConstructor1.parameters().size());
                assertEquals(3, enumConstructor1.parametersList().size());
            }
            // synthetic param does not counts here
            assertEquals("noAnnotation", enumConstructor1.parameterName(0));

            MethodInfo enumConstructor2 = enumClass.method("<init>", 
                  Type.create(DotName.createSimple("java.lang.String"), Type.Kind.CLASS), PrimitiveType.INT, PrimitiveType.BYTE);
            if(enumConstructor2 == null) {
                enumConstructor2 = enumClass.method("<init>", PrimitiveType.BYTE);
                assertNotNull(enumConstructor2);
                // synthetic param does not found here
                assertEquals(1, enumConstructor2.parameters().size());
                assertEquals(1, enumConstructor2.parametersList().size());
            }else {
                // synthetic param counts here
                assertEquals(3, enumConstructor2.parameters().size());
                assertEquals(3, enumConstructor2.parametersList().size());
            }
            // synthetic param does not counts here
            assertEquals("annotated", enumConstructor2.parameterName(0));
            
            paramAnnotation = enumConstructor2.annotation(DotName.createSimple(ParameterAnnotation.class.getName()));
            assertNotNull(paramAnnotation);
            assertEquals(Kind.METHOD_PARAMETER, paramAnnotation.target().kind());
            assertEquals("annotated", paramAnnotation.target().asMethodParameter().name());
            assertEquals(0, paramAnnotation.target().asMethodParameter().position());
        }

        // Verify hasNoArgsConstructor
        assertFalse(index.getClassByName(DotName.createSimple(DummyClass.class.getName())).hasNoArgsConstructor());
    }

    private void assertHasNoArgsConstructor(Class<?> clazz, boolean result) throws IOException {
        ClassInfo classInfo = getIndexForClass(clazz).getClassByName(DotName.createSimple(clazz.getName()));
        assertNotNull(classInfo);
        assertThat(classInfo.hasNoArgsConstructor(), is(result));
    }

    private void assertFlagSet(Class<?> clazz, int flag, boolean result) throws IOException {
        ClassInfo classInfo = getIndexForClass(clazz).getClassByName(DotName.createSimple(clazz.getName()));
        assertNotNull(classInfo);
        assertTrue((classInfo.flags() & flag) == (result ? flag : 0));
    }

    private void assertNesting(Class<?> clazz, ClassInfo.NestingType nesting, boolean result) throws IOException {
        ClassInfo classInfo = getIndexForClass(clazz).getClassByName(DotName.createSimple(clazz.getName()));
        assertNotNull(classInfo);
        assertThat(classInfo.nestingType(), result ? is(nesting) : not(nesting));
    }

    private Index getIndexForClass(Class<?> clazz) throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        return indexer.complete();
    }

}
