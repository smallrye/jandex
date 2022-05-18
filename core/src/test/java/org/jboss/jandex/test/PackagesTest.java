package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

public class PackagesTest {
    @Test
    public void getClassesInPackage() throws IOException {
        Index index = Index.of(PackagesTest.class, String.class, List.class, AtomicInteger.class);

        Collection<ClassInfo> classes = index.getClassesInPackage("java.lang");
        assertEquals(1, classes.size());
        assertEquals("java.lang.String", classes.iterator().next().name().toString());

        classes = index.getClassesInPackage("java.util");
        assertEquals(1, classes.size());
        assertEquals("java.util.List", classes.iterator().next().name().toString());

        classes = index.getClassesInPackage("java.util.concurrent");
        assertTrue(classes.isEmpty());

        classes = index.getClassesInPackage("java.util.concurrent.atomic");
        assertEquals(1, classes.size());
        assertEquals("java.util.concurrent.atomic.AtomicInteger", classes.iterator().next().name().toString());

        classes = index.getClassesInPackage("org.jboss.jandex.test");
        assertEquals(1, classes.size());
        assertEquals("org.jboss.jandex.test.PackagesTest", classes.iterator().next().name().toString());

        classes = index.getClassesInPackage((DotName) null);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void getSubpackages() throws IOException {
        Index index = Index.of(PackagesTest.class, String.class, List.class, AtomicInteger.class);

        Collection<DotName> packages = index.getSubpackages("java");
        assertEquals(2, packages.size());
        assertTrue(packages.contains(DotName.createSimple("java.lang")));
        assertTrue(packages.contains(DotName.createSimple("java.util")));

        packages = index.getSubpackages("java.util");
        assertEquals(1, packages.size());
        assertTrue(packages.contains(DotName.createSimple("java.util.concurrent")));

        packages = index.getSubpackages("java.util.concurrent");
        assertEquals(1, packages.size());
        assertTrue(packages.contains(DotName.createSimple("java.util.concurrent.atomic")));

        packages = index.getSubpackages("java.util.concurrent.atomic");
        assertEquals(0, packages.size());

        packages = index.getSubpackages("org");
        assertEquals(1, packages.size());
        assertTrue(packages.contains(DotName.createSimple("org.jboss")));

        packages = index.getSubpackages("org.jboss");
        assertEquals(1, packages.size());
        assertTrue(packages.contains(DotName.createSimple("org.jboss.jandex")));

        packages = index.getSubpackages("org.jboss.jandex");
        assertEquals(1, packages.size());
        assertTrue(packages.contains(DotName.createSimple("org.jboss.jandex.test")));

        packages = index.getSubpackages("org.jboss.jandex.test");
        assertEquals(0, packages.size());

        packages = index.getSubpackages((DotName) null);
        assertTrue(packages.isEmpty());
    }
}
