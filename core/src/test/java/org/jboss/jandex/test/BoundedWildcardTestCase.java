package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

public class BoundedWildcardTestCase {

    public static class TestSubject<T extends Number> implements Comparable<T> {
        @Override
        public int compareTo(T o) {
            return 0;
        }
    }

    @Test
    public void testIndexer() throws IOException {
        Indexer indexer = new Indexer();
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(TestSubject.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        Index index = indexer.complete();
        verifyMethodTypeVariable(index);
    }

    private void verifyMethodTypeVariable(Index index) {
        ClassInfo classInfo = index.getClassByName(DotName.createSimple(TestSubject.class.getName()));
        assertNotNull(
                classInfo.method("compareTo", Type.create(DotName.createSimple(Number.class.getName()), Type.Kind.CLASS)));
    }
}
