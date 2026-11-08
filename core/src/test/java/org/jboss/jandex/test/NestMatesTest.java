package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class NestMatesTest {
    private static final String HOST = "test.NestMatesExample";
    private static final String A = "test.NestMatesExample$A";
    private static final String B = "test.NestMatesExample$A$B";
    private static final String C = "test.NestMatesExample$C";
    private static final String UNRELATED = "test.expr.Value";

    @Test
    public void test() throws IOException {
        Index index = buildIndex();

        doTest(index);
        doTest(IndexingUtil.roundtrip(index));
    }

    @Test
    public void testOlderPersistentFormat() throws IOException {
        Index index = buildIndex();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new IndexWriter(bytes).write(index, 13);
        Index read = new IndexReader(new ByteArrayInputStream(bytes.toByteArray())).read();

        // nest information is not stored in persistent format version 13 and older,
        // so every class appears to be its own nest host
        for (String name : new String[] { HOST, A, B, C, UNRELATED }) {
            ClassInfo clazz = read.getClassByName(name);
            assertEquals(DotName.createSimple(name), clazz.nestHost());
            assertEquals(Collections.emptySet(), clazz.nestMembers());
        }
    }

    private static Index buildIndex() throws IOException {
        Indexer indexer = new Indexer();
        indexer.index(NestMatesTest.class.getResourceAsStream("/test/NestMatesExample.class"));
        indexer.index(NestMatesTest.class.getResourceAsStream("/test/NestMatesExample$A.class"));
        indexer.index(NestMatesTest.class.getResourceAsStream("/test/NestMatesExample$A$B.class"));
        indexer.index(NestMatesTest.class.getResourceAsStream("/test/NestMatesExample$C.class"));
        indexer.index(NestMatesTest.class.getResourceAsStream("/test/expr/Value.class"));
        return indexer.complete();
    }

    private void doTest(Index index) {
        ClassInfo host = index.getClassByName(HOST);
        assertEquals(DotName.createSimple(HOST), host.nestHost());
        assertEquals(setOf(A, B, C), host.nestMembers());

        for (String member : new String[] { A, B, C }) {
            ClassInfo clazz = index.getClassByName(member);
            assertEquals(DotName.createSimple(HOST), clazz.nestHost());
            assertEquals(Collections.emptySet(), clazz.nestMembers());
        }

        ClassInfo unrelated = index.getClassByName(UNRELATED);
        assertEquals(DotName.createSimple(UNRELATED), unrelated.nestHost());
        assertEquals(Collections.emptySet(), unrelated.nestMembers());
    }

    private static Set<DotName> setOf(String... strings) {
        Set<DotName> result = new HashSet<>();
        for (String string : strings) {
            result.add(DotName.createSimple(string));
        }
        return result;
    }
}
