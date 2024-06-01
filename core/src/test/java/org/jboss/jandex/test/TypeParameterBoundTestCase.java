/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021 Red Hat, Inc., and individual contributors
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.TypeParameterBoundTypeTarget;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class TypeParameterBoundTestCase {

    @Test
    public void listConsumer() throws IOException {
        ClassInfo info = Index.singleClass(getClassBytes("test/TypeParameterBoundExample$ListConsumer.class"));
        assertEquals(
                "T extends java.util.@Nullable List<?>",
                info.typeParameters().get(0).toString());
    }

    @Test
    public void arrayListConsumer() throws IOException {
        ClassInfo info = Index.singleClass(getClassBytes("test/TypeParameterBoundExample$ArrayListConsumer.class"));
        assertEquals(
                "T extends java.util.@Nullable ArrayList<?>",
                info.typeParameters().get(0).toString());
    }

    @Test
    public void serializableListConsumer() throws IOException {
        ClassInfo info = Index.singleClass(getClassBytes("test/TypeParameterBoundExample$SerializableListConsumer.class"));
        assertEquals(
                "T extends java.util.@Nullable List<?> & java.io.@Untainted Serializable",
                info.typeParameters().get(0).toString());
    }

    @Test
    public void classExtendsOnInner() throws IOException {
        ClassInfo info = Index.singleClass(getClassBytes("test/TypeParameterBoundExample$IteratorSupplier.class"));
        assertEquals("java.util.function.Supplier<java.util.function.Consumer<java.lang.@Nullable Object[]>>",
                info.interfaceTypes().get(0).toString());
    }

    @Test
    public void classExtendsOnAnonInInner() throws IOException {
        ClassInfo info = Index.singleClass(getClassBytes("test/TypeParameterBoundExample$IteratorSupplier$1.class"));
        assertEquals("java.util.function.Consumer<java.lang.@Nullable Object[]>",
                info.interfaceTypes().get(0).toString());
    }

    @Test
    public void classExtendsNestAnonExtendsInner() throws IOException {
        ClassInfo info = Index.singleClass(getClassBytes("test/TypeParameterBoundExample$Nest1$Nest2$Nest3$1$1.class"));
        assertEquals(
                "test.TypeParameterBoundExample$Nest1<java.lang.String>.Nest2<java.lang.Object[]>.Nest3<java.lang.@Nullable Integer>",
                info.superClassType().toString());
        info = Index.singleClass(getClassBytes("test/TypeParameterBoundExample$Nest1$Nest2$Nest3$1$2.class"));
        assertEquals(
                "test.TypeParameterBoundExample$Nest1<java.lang.String>.Nest2<java.lang.@Nullable Object[]>.Nest3<java.lang.Integer>",
                info.superClassType().toString());
    }

    @Test
    public void serializableListConsumerDA() throws IOException {
        Indexer indexer = new Indexer();
        String klass = "TypeParameterBoundExample$SerializableListConsumerDoubleA";
        indexer.index(getClassBytes("test/" + klass + ".class"));
        Index index = indexer.complete();

        ClassInfo info = index.getKnownClasses().iterator().next();

        assertNotNull(info);
        verifySerializableListConsumerDA(info);

        index = IndexingUtil.roundtrip(index, "c484af54959afdf1e4b674a1ba1092abd832b4d9fdee18eecf6c88e3b262183e");
        info = index.getClassByName(DotName.createSimple("test." + klass));

        assertNotNull(info);
        verifySerializableListConsumerDA(info);
    }

    private void verifySerializableListConsumerDA(ClassInfo info) {
        assertEquals(
                "T extends java.util.@Nullable @Untainted List<?> & java.io.@Untainted Serializable",
                info.typeParameters().get(0).toString());

        List<AnnotationInstance> annotationInstances = info.annotationsMap().get(DotName.createSimple("test.Nullable"));
        assertEquals(0, annotationInstances.get(0).target().asType().asTypeParameterBound().boundPosition());

        annotationInstances = info.annotationsMap().get(DotName.createSimple("test.Untainted"));
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (AnnotationInstance instance : annotationInstances) {
            TypeParameterBoundTypeTarget target = instance.target().asType().asTypeParameterBound();
            list.add(target.boundPosition());
            assertEquals(0, target.position());
        }
        assertEquals(2, list.size());
        assertTrue(list.contains(0) && list.contains(1));
    }

    private InputStream getClassBytes(String klass) {
        return getClass().getClassLoader().getResourceAsStream(klass);
    }
}
