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

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeParameterBoundTypeTarget;
import org.jboss.jandex.TypeVariable;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TypeParameterBoundTestCase {

    @Test
    public void listConsumer() throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes("test/TypeParameterBoundExample$ListConsumer.class"));
        Assert.assertEquals(
                "T extends @Nullable java.util.List",
                info.typeParameters().get(0).toString());
    }



    @Test
    public void arrayListConsumer() throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes("test/TypeParameterBoundExample$ArrayListConsumer.class"));
        Assert.assertEquals(
                "T extends @Nullable java.util.ArrayList",
                info.typeParameters().get(0).toString());
    }



    @Test
    public void serializableListConsumer() throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes("test/TypeParameterBoundExample$SerializableListConsumer.class"));
        Assert.assertEquals(
                "T extends @Nullable java.util.List & @Untainted java.io.Serializable",
                info.typeParameters().get(0).toString());
    }


    @Test
    public void classExtendsOnInner() throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes("test/TypeParameterBoundExample$IteratorSupplier.class"));
        Assert.assertEquals("java.util.function.Supplier<java.util.function.Consumer<@Nullable java.lang.Object[]>>",
                            info.interfaceTypes().get(0).toString());
    }

    @Test
    public void classExtendsOnAnonInInner() throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes("test/TypeParameterBoundExample$IteratorSupplier$1.class"));
        Assert.assertEquals("java.util.function.Consumer<@Nullable java.lang.Object[]>",
                           info.interfaceTypes().get(0).toString());
    }

    @Test
    public void classExtendsNestAnonExtendsInner() throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes("test/TypeParameterBoundExample$Nest1$Nest2$Nest3$1$1.class"));
        Assert.assertEquals("test.TypeParameterBoundExample$Nest1<java.lang.String>.Nest2<java.lang.Object[]>.Nest3<@Nullable java.lang.Integer>",
                    info.superClassType().toString());
        info = indexer.index(getClassBytes("test/TypeParameterBoundExample$Nest1$Nest2$Nest3$1$2.class"));
        Assert.assertEquals("test.TypeParameterBoundExample$Nest1<java.lang.String>.Nest2<@Nullable java.lang.Object[]>.Nest3<java.lang.Integer>",
                           info.superClassType().toString());
    }

    @Test
    public void serializableListConsumerDA() throws IOException {
        Indexer indexer = new Indexer();
        String klass = "TypeParameterBoundExample$SerializableListConsumerDoubleA";
        ClassInfo info = indexer.index(getClassBytes("test/" + klass + ".class"));
        Index index = indexer.complete();

        Assert.assertNotNull(info);
        verifySerializableListConsumerDA(info);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new IndexWriter(baos).write(index);
        index = new IndexReader(new ByteArrayInputStream(baos.toByteArray())).read();
        info = index.getClassByName(DotName.createSimple("test." + klass));

        Assert.assertNotNull(info);
        verifySerializableListConsumerDA(info);
    }

    private void verifySerializableListConsumerDA(ClassInfo info) {
        Assert.assertEquals(
                "T extends @Nullable @Untainted java.util.List & @Untainted java.io.Serializable",
                info.typeParameters().get(0).toString());

        List<AnnotationInstance> annotationInstances = info.annotations().get(DotName.createSimple("test.Nullable"));
        Assert.assertEquals(0,  annotationInstances.get(0).target().asType().asTypeParameterBound().boundPosition());

        annotationInstances = info.annotations().get(DotName.createSimple("test.Untainted"));
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (AnnotationInstance instance : annotationInstances) {
            TypeParameterBoundTypeTarget target = instance.target().asType().asTypeParameterBound();
            list.add(target.boundPosition());
            Assert.assertEquals(0, target.position());
        }
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains(0) && list.contains(1));
    }

    private InputStream getClassBytes(String klass) {
        return getClass().getClassLoader().getResourceAsStream(klass);
    }
}
