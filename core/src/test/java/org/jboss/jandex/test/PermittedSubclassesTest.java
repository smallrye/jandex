package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class PermittedSubclassesTest {
    @Test
    public void test() throws IOException {
        Indexer indexer = new Indexer();
        indexer.index(PermittedSubclassesTest.class.getResourceAsStream("/test/expr/Add.class"));
        indexer.index(PermittedSubclassesTest.class.getResourceAsStream("/test/expr/Arith.class"));
        indexer.index(PermittedSubclassesTest.class.getResourceAsStream("/test/expr/BestValue.class"));
        indexer.index(PermittedSubclassesTest.class.getResourceAsStream("/test/expr/Expr.class"));
        indexer.index(PermittedSubclassesTest.class.getResourceAsStream("/test/expr/Mul.class"));
        indexer.index(PermittedSubclassesTest.class.getResourceAsStream("/test/expr/Value.class"));
        Index index = indexer.complete();

        doTest(index);
        doTest(IndexingUtil.roundtrip(index));
    }

    private void doTest(Index index) {
        ClassInfo expr = index.getClassByName("test.expr.Expr");
        assertTrue(expr.isSealed());
        assertFalse(expr.isFinal());
        assertEquals(setOf("test.expr.Value", "test.expr.Arith"), expr.permittedSubclasses());

        ClassInfo value = index.getClassByName("test.expr.Value");
        assertFalse(value.isSealed());
        assertFalse(value.isFinal());
        assertTrue(value.interfaceNames().contains(DotName.createSimple("test.expr.Expr")));
        assertEquals(Collections.emptySet(), value.permittedSubclasses());

        ClassInfo bestValue = index.getClassByName("test.expr.BestValue");
        assertFalse(bestValue.isSealed());
        assertFalse(bestValue.isFinal());
        assertEquals(bestValue.superName(), DotName.createSimple("test.expr.Value"));
        assertEquals(Collections.emptySet(), bestValue.permittedSubclasses());

        ClassInfo arith = index.getClassByName("test.expr.Arith");
        assertTrue(arith.isSealed());
        assertFalse(arith.isFinal());
        assertTrue(arith.isAbstract());
        assertTrue(value.interfaceNames().contains(DotName.createSimple("test.expr.Expr")));
        assertEquals(setOf("test.expr.Add", "test.expr.Mul"), arith.permittedSubclasses());

        ClassInfo add = index.getClassByName("test.expr.Add");
        assertFalse(add.isSealed());
        assertTrue(add.isFinal());
        assertEquals(DotName.createSimple("test.expr.Arith"), add.superName());
        assertEquals(Collections.emptySet(), value.permittedSubclasses());

        ClassInfo mul = index.getClassByName("test.expr.Mul");
        assertFalse(mul.isSealed());
        assertTrue(mul.isFinal());
        assertEquals(DotName.createSimple("test.expr.Arith"), mul.superName());
        assertEquals(Collections.emptySet(), value.permittedSubclasses());
    }

    private static Set<DotName> setOf(String... strings) {
        Set<DotName> result = new HashSet<>();
        for (String string : strings) {
            result.add(DotName.createSimple(string));
        }
        return result;
    }
}
