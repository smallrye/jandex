package org.jboss.jandex.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.jboss.jandex.ClassInfo.NestingType.ANONYMOUS;
import static org.jboss.jandex.ClassInfo.NestingType.INNER;
import static org.jboss.jandex.ClassInfo.NestingType.TOP_LEVEL;
import static org.junit.Assert.assertThat;

public class IndexReaderTest {

    private static final int V2_VERSION = 6;

    public @interface NestedAnnotation {
    }

    public static class NoEnclosureAnonTest {
        static Class<?> anonymousStaticClass = new Object() {
        }.getClass();

        Class<?> anonymousInnerClass = new Object() {
        }.getClass();
    }

    @Test
    public void index_v2_should_detect_correct_nesting_type() throws IOException {
        Object anonClass = new Object() {
        };
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Indexer indexer = new Indexer();
        indexClass(DummyTopLevel.class, indexer);
        indexClass(NestedAnnotation.class, indexer);
        indexClass(anonClass.getClass(), indexer);
        indexClass(NoEnclosureAnonTest.class, indexer);
        indexClass(NoEnclosureAnonTest.anonymousStaticClass, indexer);
        indexClass(new NoEnclosureAnonTest().anonymousInnerClass, indexer);

        // Build index
        Index index = indexer.complete();

        // Write index
        IndexWriter iw = new IndexWriter(baos);
        iw.write(index, V2_VERSION);

        // Read index
        IndexReader reader = new IndexReader(new ByteArrayInputStream(baos.toByteArray()));
        index = reader.read();

        assertThat(getClassInfo(index, anonClass.getClass()).nestingType(), is(ANONYMOUS));
        assertThat(getClassInfo(index, new NoEnclosureAnonTest().anonymousInnerClass).nestingType(), is(ANONYMOUS));
        assertThat(getClassInfo(index, NoEnclosureAnonTest.class).nestingType(), is(INNER));
        assertThat(getClassInfo(index, NoEnclosureAnonTest.anonymousStaticClass).nestingType(), is(ANONYMOUS));
        assertThat(getClassInfo(index, NestedAnnotation.class).nestingType(), is(INNER));
        assertThat(getClassInfo(index, DummyTopLevel.class).nestingType(), is(TOP_LEVEL));
    }

    private void indexClass(Class<?> clazz, Indexer indexer) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
        indexer.index(stream);
    }

    private ClassInfo getClassInfo(Index index, Class<?> clazz) {
        return index.getClassByName(DotName.createSimple(clazz.getName()));
    }

}
