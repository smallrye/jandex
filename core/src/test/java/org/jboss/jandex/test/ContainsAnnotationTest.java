package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class ContainsAnnotationTest {
    static class AnnotatedField {
        @MyAnnotation("f")
        int field;
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(AnnotatedField.class);
        testIndex(index);
        testIndex(IndexingUtil.roundtrip(index));
    }

    private static void testIndex(Index index) {
        assertTrue(index.containsAnnotation(MyAnnotation.DOT_NAME));
        assertTrue(index.containsAnnotation(MyAnnotation.class));
        assertFalse(index.containsAnnotation(Deprecated.class));
    }

    @Test
    public void emptyIndex() throws IOException {
        Index index = Index.of(new Class<?>[0]);
        assertFalse(index.containsAnnotation(MyAnnotation.DOT_NAME));
    }
}
