/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jandex.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.junit.Test;

public class BasicTestCase {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {
        String name();
        int[] ints();

        long longValue();
        Class<?> klass();
        NestedAnnotation nested();
        ElementType[] enums();
        NestedAnnotation[] nestedArray();
    }

    public @interface NestedAnnotation {
        float value();
    }

    @TestAnnotation(name = "Test", ints = { 1, 2, 3, 4, 5 }, klass = Void.class, nested = @NestedAnnotation(1.34f), nestedArray = {
            @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) }, enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
    public class DummyClass implements Serializable {
    }

    @Test
    public void testIndexer() throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        Index index = indexer.complete();

        verifyDummy(index);
    }

    @Test
    public void testWriteRead() throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        Index index = indexer.complete();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new IndexWriter(baos).write(index);

        index = new IndexReader(new ByteArrayInputStream(baos.toByteArray())).read();

        verifyDummy(index);
    }

    private void verifyDummy(Index index) {
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
        List<ClassInfo> implementors = index.getKnownImplementors(DotName.createSimple(Serializable.class.getName()));
        assertEquals(1, implementors.size());
        assertEquals(implementors.get(0).name(), DotName.createSimple(DummyClass.class.getName()));

        implementors = index.getKnownImplementors(DotName.createSimple(InputStream.class.getName()));
        assertEquals(0, implementors.size());
    }

}
