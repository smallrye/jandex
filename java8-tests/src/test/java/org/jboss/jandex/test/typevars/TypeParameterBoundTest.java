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

package org.jboss.jandex.test.typevars;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.tainting.qual.Untainted;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Indexer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TypeParameterBoundTest {
    // extends Runnable produces T::List signature, so
    // @Nullable targets index=1 (optional class bound always counts)
    public static class ListConsumer<T extends @Nullable List<?>>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }

    @Test
    public void listConsumer() throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes(ListConsumer.class));
        Assert.assertEquals(
                "T extends java.lang.Object & @Nullable java.util.List",
                info.typeParameters().get(0).toString());
    }

    // extends Runnable produces T:ArrayList signature since ArrayList is class
    public static class ArrayListConsumer<T extends @Nullable ArrayList<?>>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }

    @Test
    public void arrayListConsumer() throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes(ArrayListConsumer.class));
        Assert.assertEquals(
                "T extends @Nullable java.util.ArrayList",
                info.typeParameters().get(0).toString());
    }

    // extends Runnable produces T:ArrayList signature since ArrayList is class
    public static class SerializableListConsumer<T extends @Nullable List<?> & @Untainted Serializable>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }

    @Test
    public void serializableListConsumer() throws IOException {
        Indexer indexer = new Indexer();
        ClassInfo info = indexer.index(getClassBytes(SerializableListConsumer.class));
        Assert.assertEquals(
                "T extends java.lang.Object & @Nullable java.util.List & @Untainted java.io.Serializable",
                info.typeParameters().get(0).toString());
    }

    private InputStream getClassBytes(Class<?> klass) {
        String fileName = klass.getName();
        fileName = fileName.substring(fileName.lastIndexOf('.') + 1);
        return klass.getResourceAsStream(fileName + ".class");
    }
}
