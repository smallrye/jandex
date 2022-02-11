package org.jboss.jandex.test.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

public class IndexingUtil {
    public static ClassInfo indexSingle(Class<?> clazz) throws IOException {
        return Index.of(clazz).getKnownClasses().iterator().next();
    }

    public static ClassInfo indexSingle(byte[] classData) throws IOException {
        return indexSingle(new ByteArrayInputStream(classData));
    }

    public static ClassInfo indexSingle(InputStream classData) throws IOException {
        Indexer indexer = new Indexer();
        indexer.index(classData);
        Index index = indexer.complete();
        assert index.getKnownClasses().size() == 1;
        return index.getKnownClasses().iterator().next();
    }

    public static Index roundtrip(Index index) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new IndexWriter(bytes).write(index);
        return new IndexReader(new ByteArrayInputStream(bytes.toByteArray())).read();
    }
}
