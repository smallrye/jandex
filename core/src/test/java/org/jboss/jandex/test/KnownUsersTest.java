package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class KnownUsersTest {
    static class SuperClass<T> {
    }

    interface ImplementedInterface1<T> {
    }

    interface ImplementedInterface2<T> {
    }

    static class TestClass<T extends Number> extends SuperClass<CharSequence>
            implements ImplementedInterface1<T>, ImplementedInterface2<RuntimeException> {
        int i;

        Map<String, List<Integer>> m;

        TestClass(StringBuilder str) {
            m = new HashMap<>();
            m.put("foo", new ArrayList<>());
        }

        <U extends T, V extends Collection<?>, W extends Exception> U bar(Set<? extends Long> s, Queue<? super Double> q)
                throws IllegalArgumentException, IllegalStateException, W {
            // `toString()` to force a class reference to `File` into the constant pool
            Paths.get("").toFile().toString();
            return null;
        }

        static class NestedClass {
        }

        class InnerClass {
        }
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(SuperClass.class, ImplementedInterface1.class, ImplementedInterface2.class, TestClass.class);
        doTest(index);
        doTest(IndexingUtil.roundtrip(index));
    }

    private void doTest(Index index) {
        // from class signature
        assertKnownUsers(index, SuperClass.class);
        assertKnownUsers(index, ImplementedInterface1.class);
        assertKnownUsers(index, ImplementedInterface2.class);
        assertKnownUsers(index, Number.class);
        assertKnownUsers(index, CharSequence.class);
        assertKnownUsers(index, RuntimeException.class);
        // from field types
        assertKnownUsers(index, String.class);
        assertKnownUsers(index, Integer.class);
        // from method signatures
        assertKnownUsers(index, StringBuilder.class);
        assertKnownUsers(index, Collection.class);
        assertKnownUsers(index, Exception.class);
        assertKnownUsers(index, Set.class);
        assertKnownUsers(index, Long.class);
        assertKnownUsers(index, Queue.class);
        assertKnownUsers(index, Double.class);
        assertKnownUsers(index, IllegalArgumentException.class);
        assertKnownUsers(index, IllegalStateException.class);
        // from method bodies (class references in the constant pool)
        assertKnownUsers(index, HashMap.class);
        assertKnownUsers(index, ArrayList.class);
        assertKnownUsers(index, Paths.class);
        assertKnownUsers(index, Path.class);
        assertKnownUsers(index, File.class);
        // member classes (class references in the constant pool)
        assertKnownUsers(index, TestClass.NestedClass.class);
        assertKnownUsers(index, TestClass.InnerClass.class);
    }

    private void assertKnownUsers(Index index, Class<?> clazz) {
        Collection<ClassInfo> knownUsers = index.getKnownUsers(clazz);

        assertNotNull(knownUsers);
        assertFalse(knownUsers.isEmpty());
        for (ClassInfo knownUser : knownUsers) {
            if (TestClass.class.getName().equals(knownUser.name().toString())) {
                return;
            }
        }
        fail("Expected " + TestClass.class.getName() + " to be a known user of " + clazz.getName());
    }
}
