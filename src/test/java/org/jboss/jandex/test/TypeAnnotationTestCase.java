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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.junit.Test;

public class TypeAnnotationTestCase {

    @Test
    public void testIndexer() throws IOException {
        Index index = buildIndex();
        verifyTypeAnnotations(index);
    }

    @Test
    public void testReadWrite() throws IOException {
        Index index = buildIndex();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new IndexWriter(baos).write(index);

        index = new IndexReader(new ByteArrayInputStream(baos.toByteArray())).read();

        verifyTypeAnnotations(index);
    }

    private Index buildIndex() throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream("test/TExample.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("test/VExample$O1$O2$O3$Nested.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("test/VExample.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("test/VExample$1Fun.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("test/RecordExample.class");
        indexer.index(stream);
        stream = getClass().getClassLoader().getResourceAsStream("test/RecordExample$NestedEmptyRecord.class");
        indexer.index(stream);
        ModuleInfoTestCase.indexAvailableModuleInfo(indexer);

        return indexer.complete();
    }

    private void verifyTypeAnnotations(Index index) {
        ClassInfo clazz = index.getClassByName(DotName.createSimple("test.TExample"));
        verifyTExampleFieldTypes(clazz);

        ClassInfo methodClass = index.getClassByName(DotName.createSimple("test.VExample"));
        ClassInfo localClass = index.getClassByName(DotName.createSimple("test.VExample$1Fun"));

        // Test methods on VExample
        verifyVExampleParameterTypes(methodClass, methodClass);
        verifyVExampleReturnTypes(methodClass, methodClass);

        // Test methods on a nested local class
        verifyVExampleParameterTypes(methodClass, localClass);
        verifyVExampleReturnTypes(methodClass, localClass);

        clazz = index.getClassByName(DotName.createSimple("test.VExample$O1$O2$O3$Nested"));
        verifyTypeParametersAndArguments(methodClass, clazz);
    }

    private void verifyTExampleFieldTypes(ClassInfo clazz) {
        //  @A Map<@B ? extends @C String, @D List<@E Object>> bar1;
        FieldInfo field = clazz.field("bar1");
        assertNotNull(field);
        Type type = field.type();
        verifyBar1(clazz, type);

        // @I String @F [] @G [] @H [] bar2;
        field = clazz.field("bar2");
        assertNotNull(field);
        type = field.type();
        verifyBar2(clazz, type);

        // @I String @F [][] @H [] bar3;
        field = clazz.field("bar3");
        assertNotNull(field);
        type = field.type();
        verifyBar3(clazz, type);

        // @M O1.@L O2.@K O3.@J Nested bar4;
        field = clazz.field("bar4");
        assertNotNull(field);
        type = field.type();
        verifyBar4(clazz, type);

        // @A Map<@B Comparable<@F Object @C [] @D [] @E []>, @G List<@H Document>> bar5;
        field = clazz.field("bar5");
        assertNotNull(field);
        type = field.type();
        verifyBar5(clazz, type);

        // @H O1.@E O2<@F S, @G T>.@D O3.@A Nested<@B U, @C V> bar6;
        field = clazz.field("bar6");
        assertNotNull(field);
        type = field.type();
        verifyBar6(clazz, type);

        // T1 & T2 = static
        // private T1.T2.@Foo T3.T4 theField2;
        type = clazz.field("theField2").type();
        type = type.asParameterizedType().owner();
        verifyHasAnnotation(DotName.createSimple("test.Foo"), type);
        assertEquals(Type.Kind.CLASS, type.kind());
    }

    private void verifyTypeParametersAndArguments(ClassInfo referenceClass, ClassInfo clazz) {
        List<TypeVariable> parameters = clazz.typeParameters();
        Type superClass = clazz.superClassType();
        List<Type> arguments = superClass.asParameterizedType().arguments();

        Type type = parameters.get(0);
        verifyHasAnnotation(nestName(referenceClass, "C"), type);
        assertEquals(Type.Kind.TYPE_VARIABLE, type.kind());
        assertEquals("R", type.asTypeVariable().identifier());
        type = type.asTypeVariable().bounds().get(0);
        verifyHasAnnotation(nestName(referenceClass, "H"), type);
        assertEquals(Type.Kind.TYPE_VARIABLE, type.kind());
        assertEquals("SU", type.asTypeVariable().identifier());
        type = type.asTypeVariable().bounds().get(0);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName("java.lang.CharSequence", type);

        type = parameters.get(1);
        verifyHasAnnotation(nestName(referenceClass, "D"), type);
        assertEquals(Type.Kind.TYPE_VARIABLE, type.kind());
        assertEquals("SU", type.asTypeVariable().identifier());
        type = type.asTypeVariable().bounds().get(0);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName("java.lang.CharSequence", type);

        type = arguments.get(0);
        assertEquals(Type.Kind.TYPE_VARIABLE, type.kind());
        assertEquals("R", type.asTypeVariable().identifier());
        assertEquals(0, type.annotations().size());
        type = type.asTypeVariable().bounds().get(0);
        assertEquals(0, type.annotations().size());
        assertEquals(Type.Kind.TYPE_VARIABLE, type.kind());
        assertEquals("SU", type.asTypeVariable().identifier());
        type = type.asTypeVariable().bounds().get(0);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName("java.lang.CharSequence", type);

        type = arguments.get(1);
        verifyHasAnnotation(nestName(referenceClass, "A"), type);
        verifyName(nestName(referenceClass, "S"), type);
    }

    private void verifyVExampleParameterTypes(ClassInfo referenceClass, ClassInfo declaringClass) {
        MethodInfo method = declaringClass.firstMethod("bar1");
        assertNotNull(method);
        Type type = method.parameters().get(0);
        verifyBar1(referenceClass, type);

        method = declaringClass.firstMethod("bar2");
        assertNotNull(method);
        type = method.parameters().get(0);
        verifyBar2(referenceClass, type);

        method = declaringClass.firstMethod("bar3");
        assertNotNull(method);
        type = method.parameters().get(0);
        verifyBar3(referenceClass, type);

        method = declaringClass.firstMethod("bar4");
        assertNotNull(method);
        type = method.parameters().get(0);
        verifyBar4(referenceClass, type);

        method = declaringClass.firstMethod("bar5");
        assertNotNull(method);
        type = method.parameters().get(0);
        verifyBar5(referenceClass, type);

        method = declaringClass.firstMethod("bar6");
        assertNotNull(method);
        type = method.parameters().get(0);
        verifyBar6(referenceClass, type);

        // Verify receiver
        method = declaringClass.firstMethod("receiverTest");
        assertNotNull(method);
        type = method.receiverType();
        assertEquals(type.name(), declaringClass.name());
        verifyHasAnnotation(nestName(referenceClass, "A"), type);
    }

    private void verifyVExampleReturnTypes(ClassInfo referenceClass, ClassInfo declaringClass) {
        MethodInfo method = declaringClass.firstMethod("foo1");
        assertNotNull(method);
        Type type = method.returnType();
        verifyBar1(referenceClass, type);

        method = declaringClass.firstMethod("foo2");
        assertNotNull(method);
        type = method.returnType();
        verifyBar2(referenceClass, type);

        method = declaringClass.firstMethod("foo3");
        assertNotNull(method);
        type = method.returnType();
        verifyBar3(referenceClass, type);

        method = declaringClass.firstMethod("foo4");
        assertNotNull(method);
        type = method.returnType();
        verifyBar4(referenceClass, type);

        method = declaringClass.firstMethod("foo5");
        assertNotNull(method);
        type = method.returnType();
        verifyBar5(referenceClass, type);

        method = declaringClass.firstMethod("foo6");
        assertNotNull(method);
        type = method.returnType();
        verifyBar6(referenceClass, type);
    }

    private void verifyBar6(ClassInfo clazz, Type type) {
        ParameterizedType pType;
        verifyHasAnnotation(nestName(clazz, "A"), type);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        verifyName(nestName(clazz, "O1", "O2", "O3", "Nested"), type);
        pType = type.asParameterizedType();
        type = pType.arguments().get(0);
        verifyHasAnnotation(nestName(clazz, "B"), type);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName(nestName(clazz, "U"), type);
        type = pType.arguments().get(1);
        verifyHasAnnotation(nestName(clazz, "C"), type);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName(nestName(clazz, "V"), type);
        type = pType.owner();
        verifyHasAnnotation(nestName(clazz, "D"), type);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        verifyName(nestName(clazz, "O1", "O2", "O3"), type);
        type = type.asParameterizedType().owner();
        verifyHasAnnotation(nestName(clazz, "E"), type);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        verifyName(nestName(clazz, "O1", "O2"), type);
        pType = type.asParameterizedType();
        type = pType.arguments().get(0);
        verifyHasAnnotation(nestName(clazz, "F"), type);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName(nestName(clazz, "S"), type);
        type = pType.arguments().get(1);
        verifyHasAnnotation(nestName(clazz, "G"), type);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName(nestName(clazz, "T"), type);
        type = pType.owner();
        verifyHasAnnotation(nestName(clazz, "H"), type);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName(nestName(clazz, "O1"), type);
    }

    private void verifyBar5(ClassInfo clazz, Type type) {
        ParameterizedType pType;
        verifyHasAnnotation(nestName(clazz, "A"), type);
        assertEquals(DotName.createSimple("java.util.Map"), type.name());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        pType = type.asParameterizedType();
        type = pType.arguments().get(0);
        verifyHasAnnotation(nestName(clazz, "B"), type);
        verifyName("java.lang.Comparable", type);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        type = type.asParameterizedType().arguments().get(0);
        verifyHasAnnotation(nestName(clazz, "C"), type);
        assertEquals(Type.Kind.ARRAY, type.kind());
        assertEquals(1, type.asArrayType().dimensions());
        type = type.asArrayType().component();
        verifyHasAnnotation(nestName(clazz, "D"), type);
        assertEquals(Type.Kind.ARRAY, type.kind());
        assertEquals(1, type.asArrayType().dimensions());
        type = type.asArrayType().component();
        verifyHasAnnotation(nestName(clazz, "E"), type);
        assertEquals(Type.Kind.ARRAY, type.kind());
        assertEquals(1, type.asArrayType().dimensions());
        type = type.asArrayType().component();
        verifyHasAnnotation(nestName(clazz, "F"), type);
        verifyName("java.lang.Object", type);
        type = pType.arguments().get(1);
        verifyHasAnnotation(nestName(clazz, "G"), type);
        verifyName("java.util.List", type);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        type = type.asParameterizedType().arguments().get(0);
        verifyHasAnnotation(nestName(clazz, "H"), type);
        verifyName(nestName(clazz, "Document"), type);
    }

    private void verifyBar4(ClassInfo clazz, Type type) {
        verifyHasAnnotation(nestName(clazz, "J"), type);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        verifyName(nestName(clazz, "O1", "O2", "O3", "Nested"), type);

        type = type.asParameterizedType().owner();
        verifyHasAnnotation(nestName(clazz, "K"), type);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        verifyName(nestName(clazz, "O1", "O2", "O3"), type);
        type = type.asParameterizedType().owner();
        verifyHasAnnotation(nestName(clazz, "L"), type);
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        verifyName(nestName(clazz, "O1", "O2"), type);
        type = type.asParameterizedType().owner();
        verifyHasAnnotation(nestName(clazz, "M"), type);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName(nestName(clazz, "O1"), type);
    }

    private void verifyBar3(ClassInfo clazz, Type type) {
        verifyHasAnnotation(nestName(clazz, "F"), type);
        assertEquals(Type.Kind.ARRAY, type.kind());
        assertEquals(2, type.asArrayType().dimensions());
        type = type.asArrayType().component();
        verifyHasAnnotation(nestName(clazz, "H"), type);
        assertEquals(Type.Kind.ARRAY, type.kind());
        assertEquals(1, type.asArrayType().dimensions());
        type = type.asArrayType().component();
        verifyHasAnnotation(nestName(clazz, "I"), type);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName("java.lang.String", type);
    }

    private void verifyBar2(ClassInfo clazz, Type type) {
        verifyHasAnnotation(nestName(clazz, "F"), type);
        assertEquals(Type.Kind.ARRAY, type.kind());
        assertEquals(1, type.asArrayType().dimensions());
        type = type.asArrayType().component();
        verifyHasAnnotation(nestName(clazz, "G"), type);
        assertEquals(Type.Kind.ARRAY, type.kind());
        assertEquals(1, type.asArrayType().dimensions());
        type = type.asArrayType().component();
        verifyHasAnnotation(nestName(clazz, "H"), type);
        assertEquals(Type.Kind.ARRAY, type.kind());
        assertEquals(1, type.asArrayType().dimensions());
        type = type.asArrayType().component();
        verifyHasAnnotation(nestName(clazz, "I"), type);
        assertEquals(Type.Kind.CLASS, type.kind());
        verifyName("java.lang.String", type);
    }

    private void verifyBar1(ClassInfo clazz, Type type) {
        verifyHasAnnotation(nestName(clazz, "A"), type);
        assertEquals(DotName.createSimple("java.util.Map"), type.name());
        assertEquals(Type.Kind.PARAMETERIZED_TYPE, type.kind());
        ParameterizedType pType = type.asParameterizedType();
        type = pType.arguments().get(0);
        assertEquals(Type.Kind.WILDCARD_TYPE, type.kind());
        verifyHasAnnotation(nestName(clazz, "B"), type);
        type = type.asWildcardType().extendsBound();
        verifyName("java.lang.String", type);
        verifyHasAnnotation(nestName(clazz, "C"), type);
        type = pType.arguments().get(1);
        verifyHasAnnotation(nestName(clazz, "D"), type);
        verifyName("java.util.List", type);
        type = type.asParameterizedType().arguments().get(0);
        verifyHasAnnotation(nestName(clazz, "E"), type);
        verifyName("java.lang.Object", type);
    }

    private DotName nestName(ClassInfo clazz, String... names) {
        DotName name = clazz.name();
        for (String local : names) {
            name = DotName.createComponentized(name, local, true);
        }

        return name;
    }

    private void verifyHasAnnotation(DotName name, Type type) {
        assertTrue(hasAnnotation0(name, type));
    }

    private void verifyName(String name, Type type) {
        DotName simple = DotName.createSimple(name);
        assertEquals(simple, type.name());
    }

    private void verifyName(DotName name, Type type) {
        assertEquals(name, type.name());
    }

    private boolean hasAnnotation0(DotName name, Type type) {
        for (AnnotationInstance instance : type.annotations()) {
            if (instance.name().equals(name)) {
                return true;
            }
        }

        return false;
    }


}
