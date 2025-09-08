package org.jboss.jandex.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.SerializedIndexScanner;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import test.BridgeMethods;

public class ReproducibleSerializedIndexTest {
    static List<String> testDataClassNames;
    static Map<String, byte[]> classData;
    static byte[] referenceSerializedIndex;

    @BeforeAll
    public static void setup() throws Exception {
        Class<?> c = BridgeMethods.class;
        URL referenceClassResourceUrl = c.getClassLoader().getResource(c.getName().replace('.', '/') + ".class");
        Assertions.assertNotNull(referenceClassResourceUrl, "jandex-test-data is missing class " + c.getName());
        URI classUri = referenceClassResourceUrl.toURI();
        classData = new HashMap<>();

        switch (classUri.getScheme()) {
            case "jar": {
                URI jarUri = URI
                        .create(classUri.getSchemeSpecificPart().substring(0, classUri.getSchemeSpecificPart().indexOf('!')));
                try (FileInputStream fileInputStream = new FileInputStream(Paths.get(jarUri).toFile());
                        JarInputStream jarFile = new JarInputStream(new BufferedInputStream(fileInputStream))) {
                    JarEntry entry;
                    while ((entry = jarFile.getNextJarEntry()) != null) {
                        if (!entry.getName().endsWith(".class")) {
                            continue;
                        }
                        String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int rd;
                        byte[] buffer = new byte[1024];
                        while ((rd = jarFile.read(buffer)) != -1) {
                            baos.write(buffer, 0, rd);
                        }
                        byte[] bytecode = baos.toByteArray();
                        classData.put(className, bytecode);
                    }
                }
                testDataClassNames = classData.keySet().stream().sorted().collect(Collectors.toList());
                break;
            }
            case "file": {
                URI testDataClassesUri = classUri.resolve("..");
                Path testDataClassesDir = Paths.get(testDataClassesUri);

                try (Stream<Path> classesContent = Files.walk(testDataClassesDir)) {
                    testDataClassNames = Collections.unmodifiableList(classesContent.filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".class"))
                            .map(path -> {
                                String relativePath = testDataClassesDir.relativize(path).toString();
                                String className = relativePath.replace('/', '.').substring(0, relativePath.length() - 6);
                                try {
                                    classData.put(className, Files.readAllBytes(path));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return className;
                            })
                            .sorted()
                            .collect(Collectors.toList()));
                }

                break;
            }
            default:
                throw new RuntimeException("Unsupported URI scheme: " + classUri);
        }

        referenceSerializedIndex = serializedIndex(testDataClassNames);

        dumpIndexScan("Reference index scan:", referenceSerializedIndex);
    }

    @RepeatedTest(5)
    public void sameClassesOrder() throws Exception {
        byte[] actual = serializedIndex(testDataClassNames);

        dumpIndexScan("Actual index scan:", actual);

        Assertions.assertArrayEquals(referenceSerializedIndex, actual);
    }

    @RepeatedTest(5)
    public void reversedClassesOrder() throws Exception {
        List<String> classes = new ArrayList<>(testDataClassNames);
        Collections.reverse(classes);

        byte[] actual = serializedIndex(classes);

        dumpIndexScan("Actual index scan:", actual);

        Assertions.assertArrayEquals(referenceSerializedIndex, actual);
    }

    @RepeatedTest(25)
    public void randomClassesOrder() throws Exception {
        List<String> classes = new ArrayList<>(testDataClassNames);
        Collections.shuffle(classes);

        byte[] actual = serializedIndex(classes);

        dumpIndexScan("Actual index scan:", actual);

        Assertions.assertArrayEquals(referenceSerializedIndex, actual);
    }

    static byte[] serializedIndex(List<String> classNames) throws IOException {
        return IndexingUtil.serializeIndex(buildIndex(classNames));
    }

    static Index buildIndex(List<String> classNames) throws IOException {
        Indexer indexer = new Indexer();
        for (String className : classNames) {
            indexer.index(new ByteArrayInputStream(classData.get(className)));
        }
        return indexer.complete();
    }

    static void dumpIndexScan(String kind, byte[] actual) throws IOException {
        if (Boolean.getBoolean("intellij.debug.agent")) {
            System.out.println(kind);
            new SerializedIndexScanner(new ByteArrayInputStream(actual)).scan(System.out::println);
        }
    }
}
