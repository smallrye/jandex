package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.annotation.AnnotationDescription;

public class Utf8ConstantEncodingTest {
    private static final String CLASS_NAME = "org.jboss.jandex.test.MyTestClass";

    private static final String LONG_STRING;

    static {
        // in UTF-8, the null character is encoded as 0x00, while in "modified UTF-8" per `DataInput`,
        // it is encoded as 0xC0 0x80 (this is not the only difference between the two encodings,
        // but is the easiest to test with)
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 25000; i++) {
            longString.append('\0');
        }
        LONG_STRING = longString.toString();
    }

    @Test
    public void test() throws IOException {
        byte[] clazz = new ByteBuddy()
                .with(ClassFileVersion.JAVA_V8)
                .subclass(Object.class)
                .name(CLASS_NAME)
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

        Index index2 = IndexingUtil.roundtrip(index, "3874a5ba036b183f9216ea910e4d0415d64b21659dfa230b442f333672f1a8a9");

        verifyAnnotationValue(index2);
    }

    private void verifyAnnotationValue(Index index) {
        ClassInfo clazz = index.getClassByName(DotName.createSimple(CLASS_NAME));
        String annotationValue = clazz.declaredAnnotation(MyAnnotation.DOT_NAME).value().asString();
        assertEquals(LONG_STRING, annotationValue);
    }
}
