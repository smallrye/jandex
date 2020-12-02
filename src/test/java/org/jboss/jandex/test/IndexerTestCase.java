/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020 Red Hat, Inc., and individual contributors
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

import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.junit.Test;

public class IndexerTestCase {

    @Test
    public void testIndexView() throws IOException {
        DotName org = DotName.createComponentized(null, "org");
        DotName jboss = DotName.createComponentized(org, "jboss");
        DotName jandex = DotName.createComponentized(jboss, "jandex");
        DotName test = DotName.createComponentized(jandex, "test");
        DotName indexerTestCase = DotName.createComponentized(test, IndexerTestCase.class.getSimpleName());
        DotName testName = DotName.createComponentized(indexerTestCase,
                Test$.class.getSimpleName(), true);

        Indexer indexer = new Indexer();
        InputStream stream = Test$.class.getClassLoader()
                .getResourceAsStream(Test$.class.getName().replace('.', '/') + ".class");
        ClassInfo indexedClass = indexer.index(stream);
        Index index = indexer.complete();

        assertEquals(testName, indexedClass.name());
        assertNotNull(index.getClassByName(DotName.createSimple(Test$.class.getName())));
        assertNotNull(index.getClassByName(testName));
    }

    public static class Test$ {

    }

}
