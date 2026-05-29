package org.jboss.jandex.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ClassOrderWhenPropagatingTypeParameterBoundsTest {
    // a Java compiler will never generate inner classes like this (an inner class's name
    // always has its outer class's name as a prefix), but it's legal and some bytecode
    // obfuscators do this

    @Test
    public void test() throws IOException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "org/jboss/jandex/test/A", null,
                Type.getInternalName(Object.class), null);
        writer.visitInnerClass("org/jboss/jandex/test/A", "org/jboss/jandex/test/C", "A", Opcodes.ACC_STATIC);
        writer.visitEnd();
        byte[] a = writer.toByteArray();

        writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "org/jboss/jandex/test/B", null,
                Type.getInternalName(Object.class), null);
        writer.visitEnd();
        byte[] b = writer.toByteArray();

        writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "org/jboss/jandex/test/C", null,
                Type.getInternalName(Object.class), null);
        writer.visitEnd();
        byte[] c = writer.toByteArray();

        Indexer indexer = new Indexer();
        indexer.index(new ByteArrayInputStream(a));
        indexer.index(new ByteArrayInputStream(b));
        indexer.index(new ByteArrayInputStream(c));

        // this is not guaranteed to fail when the `Comparator` used in `Indexer.propagateTypeParameterBounds()`
        // is incorrect (because the sorting algorithm doesn't have to fail when its `Comparator` is incorrect,
        // especially with such a small list of classes to sort), but inserting a call to `TotalOrderChecker.check()`
        // there is enough to catch problems
        indexer.complete();
    }
}
