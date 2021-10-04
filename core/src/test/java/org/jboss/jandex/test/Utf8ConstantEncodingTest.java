package org.jboss.jandex.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.junit.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;

public class Utf8ConstantEncodingTest {
    @interface MyAnnotation {
        String value();
    }

    private static final String CLASS_NAME = "org.jboss.jandex.test.MyTestClass";

    private static final String LONG_STRING;

    static {
        // in UTF-8, the null character is encoded as 0x00, while in "modified UTF-8" per `DataInput`,
        // it is encoded as 0xC0 0x80 (this is not the only difference between the two encodings,
        // but is the easiest to test with)
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 25_000; i++) {
            longString.append('\0');
        }
        LONG_STRING = longString.toString();
    }

    @Test
    public void test() throws IOException {
        byte[] clazz = new ByteBuddy()
                .with(ClassFileVersion.JAVA_V8)
                .with(new NamingStrategy.AbstractBase() {
                    @Override
                    protected String name(TypeDescription superClass) {
                        return CLASS_NAME;
                    }
                })
                .subclass(Object.class)
                .annotateType(AnnotationDescription.Builder.ofType(MyAnnotation.class)
                        .define("value", LONG_STRING)
                        .build())
                .make()
                .getBytes();

        Indexer indexer = new Indexer();
        indexer.indexClass(MyAnnotation.class);
        indexer.index(new ByteArrayInputStream(clazz));
        Index index = indexer.complete();

        verifyAnnotationValue(index);

        Index index2 = roundtrip(index);

        verifyAnnotationValue(index2);
    }

    private void verifyAnnotationValue(Index index) {
        ClassInfo clazz = index.getClassByName(DotName.createSimple(CLASS_NAME));
        String annotationValue = clazz.classAnnotation(DotName.createSimple(MyAnnotation.class.getName())).value().asString();
        assertEquals(LONG_STRING, annotationValue);
    }

    private Index roundtrip(Index index) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new IndexWriter(baos).write(index);
        return new IndexReader(new ByteArrayInputStream(baos.toByteArray())).read();
    }
}
