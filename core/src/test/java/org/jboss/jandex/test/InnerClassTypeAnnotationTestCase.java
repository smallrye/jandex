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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class InnerClassTypeAnnotationTestCase {

    @Test
    public void testNoGenericsConstructIndex() throws IOException {
        verifyIndex("NoGenericsConstruct", 0, 2);
    }

    @Test
    public void testNoGenericsConstructRW() throws IOException {
        verifyRW("NoGenericsConstruct", 0, 2);
    }

    @Test
    public void testStaticNoGenericsConstructIndex() throws IOException {
        verifyIndex("StaticNoGenericsConstruct", 0, 2);
    }

    @Test
    public void testStaticNoGenericsConstructRW() throws IOException {
        verifyRW("StaticNoGenericsConstruct", 0, 2);
    }

    @Test
    public void testGenericsConstructIndex() throws IOException {
        verifyIndex("GenericsConstruct", 0, -1);
    }

    @Test
    public void testGenericsConstructRW() throws IOException {
        verifyRW("GenericsConstruct", 0, -1);
    }

    private void verifyIndex(String name, int pos1, int pos2) throws IOException {
        Index index = buildIndex(name);

        verifyTypeAnnotations(index, name, pos1);
        if (pos2 != -1) {
            verifyTypeAnnotations(index, name, pos2);
        }
    }

    private void verifyRW(String name, int pos1, int pos2) throws IOException {
        Index index = buildIndex(name);
        index = IndexingUtil.roundtrip(index);

        verifyTypeAnnotations(index, name, pos1);
        if (pos2 != -1) {
            verifyTypeAnnotations(index, name, pos2);
        }
    }

    private Index buildIndex(String name) throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("test/InnerClassTypeAnnotationsExample$" + name + ".class");
        indexer.index(stream);
        return indexer.complete();
    }

    private void verifyTypeAnnotations(Index index, String name, int pos) {
        ClassInfo clazz = index.getClassByName(DotName.createSimple("test.InnerClassTypeAnnotationsExample$" + name));
        assertTrue(clazz.constructors().get(0).parameterTypes().get(pos).hasAnnotation(DotName.createSimple("test.Nullable")));
    }
}
