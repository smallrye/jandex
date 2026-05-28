package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class Utf8ConstantEncodingTest {
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
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "Test", null,
                Type.getInternalName(Object.class), null);
        AnnotationVisitor ann = writer.visitAnnotation(Type.getDescriptor(MyAnnotation.class), true);
        ann.visit("value", LONG_STRING);
        ann.visitEnd();
        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        Indexer indexer = new Indexer();
        indexer.indexClass(MyAnnotation.class);
        indexer.index(new ByteArrayInputStream(bytes));
        Index index = indexer.complete();

        verifyAnnotationValue(index);
        verifyAnnotationValue(IndexingUtil.roundtrip(index));
    }

    private void verifyAnnotationValue(Index index) {
        ClassInfo clazz = index.getClassByName("Test");
        String annotationValue = clazz.declaredAnnotation(MyAnnotation.DOT_NAME).value().asString();
        assertEquals(LONG_STRING, annotationValue);
    }
}
