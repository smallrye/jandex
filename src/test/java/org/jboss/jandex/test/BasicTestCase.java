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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
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
        
        void method(final int capture) {
            class Local{
                Local(int noAnnotation){}
                Local(@ParameterAnnotation byte annotated){}
                
                int f() {
                    return capture;
                }
            }
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

    public static class NestedTest {
        
        static {
            new Object() {};
        }
        {
            new Object() {};
        }
        
        NestedTest(int noAnnotation){}
        NestedTest(@ParameterAnnotation byte annotated){}
        
        static void staticMethod() {
            class Local{
                Local(int noAnnotation){}
                Local(@ParameterAnnotation byte annotated){}
            }
        }
    }

    public class NestedD implements Serializable {
    }

    @Test
    public void testIndexer() throws IOException {
        Index index = index();

        verifyDummy(index, true);
        index.printSubclasses();
    }

    private Index index() throws IOException {
        Indexer indexer = new Indexer();

        InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(TestAnnotation.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(DummyClass.Nested.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(Enum.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream(NestedTest.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("org/jboss/jandex/test/BasicTestCase$NestedTest$1Local.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("org/jboss/jandex/test/BasicTestCase$DummyClass$1Local.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("org/jboss/jandex/test/BasicTestCase$NestedTest$1.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("org/jboss/jandex/test/BasicTestCase$NestedTest$2.class");
        indexer.index(stream);
        
        return indexer.complete();
    }

    @Test
    public void testWriteRead() throws IOException {
        Index index = index();

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
        assertInner(NestedC.class, true);

        assertFlagSet(NestedD.class, Modifier.STATIC, false);
        assertInner(NestedC.class, true);

        assertInner(BasicTestCase.class, false);
        assertFlagSet(BasicTestCase.class, Modifier.STATIC, false);
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
            // synthetic param does not counts here
            assertEquals("noAnnotation", nestedConstructor1.parameterName(0));
            assertEquals(PrimitiveType.INT, nestedConstructor1.realParameter(0));

            MethodInfo nestedConstructor2 = nested.method("<init>", 
                  Type.create(DotName.createSimple(DummyClass.class.getName()), Type.Kind.CLASS), PrimitiveType.BYTE);
            assertNotNull(nestedConstructor2);
            // synthetic param counts here
            assertEquals(2, nestedConstructor2.parameters().size());
            // synthetic param does not counts here
            assertEquals("annotated", nestedConstructor2.parameterName(0));
            assertEquals(PrimitiveType.BYTE, nestedConstructor2.realParameter(0));
            
            AnnotationInstance paramAnnotation = nestedConstructor2.annotation(DotName.createSimple(ParameterAnnotation.class.getName()));
            assertNotNull(paramAnnotation);
            assertEquals(Kind.METHOD_PARAMETER, paramAnnotation.target().kind());
            assertEquals("annotated", paramAnnotation.target().asMethodParameter().name());
            assertEquals(PrimitiveType.BYTE, paramAnnotation.target().asMethodParameter().type());
            assertEquals(0, paramAnnotation.target().asMethodParameter().position());
            
            ClassInfo enumClass = index.getClassByName(DotName.createSimple(Enum.class.getName()));
            assertNotNull(enumClass);
            // synthetic param counts here (for ECJ)
            MethodInfo enumConstructor1 = enumClass.method("<init>", 
                  Type.create(DotName.createSimple("java.lang.String"), Type.Kind.CLASS), PrimitiveType.INT, PrimitiveType.INT);
            if(enumConstructor1 == null) {
                enumConstructor1 = enumClass.method("<init>", PrimitiveType.INT);
                assertNotNull(enumConstructor1);
                // synthetic param does not count here
                assertEquals(1, enumConstructor1.parameters().size());
            }else {
                // synthetic param counts here
                assertEquals(3, enumConstructor1.parameters().size());
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
            }else {
                // synthetic param counts here
                assertEquals(3, enumConstructor2.parameters().size());
            }
            // synthetic param does not counts here
            assertEquals("annotated", enumConstructor2.parameterName(0));
            
            paramAnnotation = enumConstructor2.annotation(DotName.createSimple(ParameterAnnotation.class.getName()));
            assertNotNull(paramAnnotation);
            assertEquals(Kind.METHOD_PARAMETER, paramAnnotation.target().kind());
            assertEquals("annotated", paramAnnotation.target().asMethodParameter().name());
            assertEquals(0, paramAnnotation.target().asMethodParameter().position());
            
            // test the synthetic param count
            
            // inner
            assertEquals(1, nestedConstructor1.syntheticParameterCount());

            // static inner
            ClassInfo staticInner = index.getClassByName(DotName.createSimple(NestedTest.class.getName()));
            MethodInfo staticInnerConstructor = staticInner.method("<init>", PrimitiveType.BYTE);
            assertEquals(0, staticInnerConstructor.syntheticParameterCount());

            // local
            ClassInfo local = index.getClassByName(DotName.createSimple("org.jboss.jandex.test.BasicTestCase$DummyClass$1Local"));
            MethodInfo localConstructor = local.method("<init>", Type.create(DotName.createSimple(DummyClass.class.getName()), Type.Kind.CLASS), 
                                                       PrimitiveType.BYTE,
                                                       // capture param
                                                       PrimitiveType.INT);
            try {
                assertEquals(1, localConstructor.syntheticParameterCount());
                fail("Exception not thrown");
            }catch(UnsupportedOperationException x) {}

            // static local
            ClassInfo staticLocal = index.getClassByName(DotName.createSimple("org.jboss.jandex.test.BasicTestCase$NestedTest$1Local"));
            MethodInfo staticLocalConstructor = staticLocal.method("<init>", PrimitiveType.BYTE);
            try {
                assertEquals(0, staticLocalConstructor.syntheticParameterCount());
                fail("Exception not thrown");
            }catch(UnsupportedOperationException x) {}

            // Tests disabled for anon because the code doesn't look like they're supported
//            // anon
//            ClassInfo anon = index.getClassByName(DotName.createSimple("org.jboss.jandex.test.BasicTestCase$NestedTest$2"));
//            MethodInfo anonConstructor = anon.method("<init>", Type.create(DotName.createSimple(NestedTest.class.getName()), Type.Kind.CLASS));
//            assertEquals(1, anonConstructor.syntheticParameterCount(index));
//
//            // static anon
//            ClassInfo staticAnon = index.getClassByName(DotName.createSimple("org.jboss.jandex.test.BasicTestCase$NestedTest$1"));
//            MethodInfo staticAnonConstructor = staticAnon.method("<init>");
//            assertEquals(0, staticAnonConstructor.syntheticParameterCount(index));
        }

        // Verify hasNoArgsConstructor
        assertFalse(index.getClassByName(DotName.createSimple(DummyClass.class.getName())).hasNoArgsConstructor());
    }

    private void assertHasNoArgsConstructor(Class<?> clazz, boolean result) throws IOException {
        ClassInfo classInfo = getIndexForClass(clazz).getClassByName(DotName.createSimple(clazz.getName()));
        assertNotNull(classInfo);
        if (result) {
            assertTrue(classInfo.hasNoArgsConstructor());
        } else {
            assertFalse(classInfo.hasNoArgsConstructor());
        }
    }

    private void assertFlagSet(Class<?> clazz, int flag, boolean result) throws IOException {
        ClassInfo classInfo = getIndexForClass(clazz).getClassByName(DotName.createSimple(clazz.getName()));
        assertNotNull(classInfo);
        assertTrue((classInfo.flags() & flag) == (result ? flag : 0));
    }

    private void assertInner(Class<?> clazz, boolean result) throws IOException {
        ClassInfo classInfo = getIndexForClass(clazz).getClassByName(DotName.createSimple(clazz.getName()));
        assertNotNull(classInfo);
        assertTrue((classInfo.nestingType() == ClassInfo.NestingType.INNER) == result);
    }

    private Index getIndexForClass(Class<?> clazz) throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        return indexer.complete();
    }

}
