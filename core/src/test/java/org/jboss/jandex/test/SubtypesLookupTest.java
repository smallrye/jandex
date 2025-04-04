package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

public class SubtypesLookupTest {
    interface Foo {
    }

    interface Bar extends Foo {
    }

    interface Baz extends Bar {
    }

    interface Quux {
    }

    static class A implements Foo {
    }

    static class B extends A implements Quux {
    }

    static class C extends B implements Bar {
    }

    static class D extends B implements Baz {
    }

    static class E implements Bar {
    }

    static class F implements Baz, Quux {
    }

    static class Z {
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(Foo.class, Bar.class, Baz.class, A.class, B.class, C.class, D.class, E.class, F.class, Z.class);
        test(index);

        test(IndexingUtil.roundtrip(index));
    }

    private void test(Index index) {
        check(index.getKnownDirectSubclasses(Foo.class)); // empty
        check(index.getKnownDirectSubinterfaces(Foo.class), "Bar");
        check(index.getKnownDirectImplementations(Foo.class), "A");
        check(index.getKnownDirectImplementors(Foo.class), "A", "Bar"); // Bar is intentional
        check(index.getAllKnownSubclasses(Foo.class)); // empty
        check(index.getAllKnownSubinterfaces(Foo.class), "Bar", "Baz");
        check(index.getAllKnownImplementations(Foo.class), "A", "B", "C", "D", "E", "F");
        check(index.getAllKnownImplementors(Foo.class), "A", "B", "C", "D", "E", "F");

        check(index.getKnownDirectSubclasses(Bar.class)); // empty
        check(index.getKnownDirectSubinterfaces(Bar.class), "Baz");
        check(index.getKnownDirectImplementations(Bar.class), "C", "E");
        check(index.getKnownDirectImplementors(Bar.class), "C", "E", "Baz"); // Baz is intentional
        check(index.getAllKnownSubclasses(Bar.class)); // empty
        check(index.getAllKnownSubinterfaces(Bar.class), "Baz");
        check(index.getAllKnownImplementations(Bar.class), "C", "D", "E", "F");
        check(index.getAllKnownImplementors(Bar.class), "C", "D", "E", "F");

        check(index.getKnownDirectSubclasses(Baz.class)); // empty
        check(index.getKnownDirectSubinterfaces(Baz.class)); // empty
        check(index.getKnownDirectImplementations(Baz.class), "D", "F");
        check(index.getKnownDirectImplementors(Baz.class), "D", "F");
        check(index.getAllKnownSubclasses(Baz.class)); // empty
        check(index.getAllKnownSubinterfaces(Baz.class)); // empty
        check(index.getAllKnownImplementations(Baz.class), "D", "F");
        check(index.getAllKnownImplementors(Baz.class), "D", "F");

        check(index.getKnownDirectSubclasses(Quux.class)); // empty
        check(index.getKnownDirectSubinterfaces(Quux.class)); // empty
        check(index.getKnownDirectImplementations(Quux.class), "B", "F");
        check(index.getKnownDirectImplementors(Quux.class), "B", "F");
        check(index.getAllKnownSubclasses(Quux.class)); // empty
        check(index.getAllKnownSubinterfaces(Quux.class)); // empty
        check(index.getAllKnownImplementations(Quux.class), "B", "C", "D", "F");
        check(index.getAllKnownImplementors(Quux.class), "B", "C", "D", "F");

        check(index.getKnownDirectSubclasses(A.class), "B");
        check(index.getKnownDirectSubinterfaces(A.class)); // empty
        check(index.getKnownDirectImplementations(A.class)); // empty
        check(index.getKnownDirectImplementors(A.class)); // empty
        check(index.getAllKnownSubclasses(A.class), "B", "C", "D");
        check(index.getAllKnownSubinterfaces(A.class)); // empty
        check(index.getAllKnownImplementations(A.class)); // empty
        check(index.getAllKnownImplementors(A.class)); // empty

        check(index.getKnownDirectSubclasses(B.class), "C", "D");
        check(index.getKnownDirectSubinterfaces(B.class)); // empty
        check(index.getKnownDirectImplementations(B.class)); // empty
        check(index.getKnownDirectImplementors(B.class)); // empty
        check(index.getAllKnownSubclasses(B.class), "C", "D");
        check(index.getAllKnownSubinterfaces(B.class)); // empty
        check(index.getAllKnownImplementations(B.class)); // empty
        check(index.getAllKnownImplementors(B.class)); // empty

        for (Class<?> clazz : Arrays.asList(C.class, D.class, E.class, F.class, Z.class)) {
            check(index.getKnownDirectSubclasses(clazz)); // empty
            check(index.getKnownDirectSubinterfaces(clazz)); // empty
            check(index.getKnownDirectImplementations(clazz)); // empty
            check(index.getKnownDirectImplementors(clazz)); // empty
            check(index.getAllKnownSubclasses(clazz)); // empty
            check(index.getAllKnownSubinterfaces(clazz)); // empty
            check(index.getAllKnownImplementations(clazz)); // empty
            check(index.getAllKnownImplementors(clazz)); // empty
        }
    }

    private void check(Collection<ClassInfo> lookedUpTypes, String... expectedTypes) {
        Set<String> names = new HashSet<>();
        for (ClassInfo lookedUpType : lookedUpTypes) {
            names.add(lookedUpType.name().local());
        }
        assertEquals(new HashSet<>(Arrays.asList(expectedTypes)), names);
    }
}
