package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

// asserts that the indexing process is reproducible (that is, always produces the same output)
// on the same JVM version with the same sequence of input files, using the test classes from
// packages `org.jboss.jandex.test` (the `core` module) and `test` (the `test-data` module)
//
// there's also a reproducibility integration test in the `maven-plugin` module, which uses
// a different set of inputs
public class ReproducibilityTest {
    static byte[] firstIndex;

    static byte[] index() throws IOException, URISyntaxException {
        Indexer indexer = new Indexer();
        ClassLoader cl = ReproducibilityTest.class.getClassLoader();
        for (String pkg : Arrays.asList("org/jboss/jandex/test/", "test/")) {
            URI uri = cl.getResources(pkg).nextElement().toURI();
            String scheme = uri.getScheme();
            if ("file".equals(scheme)) {
                try (Stream<Path> stream = Files.walk(Paths.get(uri))) {
                    List<Path> classes = stream
                            .filter(it -> Files.isRegularFile(it) && it.getFileName().toString().endsWith(".class"))
                            .sorted()
                            .collect(Collectors.toList());
                    for (Path path : classes) {
                        try (InputStream in = Files.newInputStream(path)) {
                            indexer.index(in);
                        }
                    }
                }
            }
            if ("jar".equals(scheme)) {
                // opaque URI of the form `jar:<file URI>!/pkg/`
                String part = uri.getSchemeSpecificPart();
                uri = new URI(part.substring(0, part.indexOf("!")));
                try (JarFile jar = new JarFile(new File(uri))) {
                    List<JarEntry> classes = jar.stream()
                            .filter(it -> it.getName().endsWith(".class"))
                            .sorted(Comparator.comparing(ZipEntry::getName))
                            .collect(Collectors.toList());
                    for (JarEntry entry : classes) {
                        try (InputStream in = jar.getInputStream(entry)) {
                            indexer.index(in);
                        }
                    }
                }
            }
        }
        Index index = indexer.complete();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new IndexWriter(out).write(index);
        return out.toByteArray();
    }

    @BeforeAll
    public static void setup() throws IOException, URISyntaxException {
        firstIndex = index();
    }

    @RepeatedTest(50)
    public void test() throws IOException, URISyntaxException {
        assertArrayEquals(firstIndex, index());
    }
}
