package org.jboss.jandex.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;

import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.OpenedClassReader;

public class ClassOrderWhenPropagatingTypeParameterBoundsTest {
    // a Java compiler will never generate inner classes like this (an inner class's name
    // always has its outer class's name as a prefix), but it's legal and some bytecode
    // obfuscators do this

    @Test
    public void test() throws IOException {
        byte[] a = new ByteBuddy().subclass(Object.class)
                .name("org.jboss.jandex.test.A")
                .visit(new AsmVisitorWrapper.AbstractBase() {
                    @Override
                    public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor,
                            Implementation.Context implementationContext, TypePool typePool,
                            FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags,
                            int readerFlags) {
                        return new ClassVisitor(OpenedClassReader.ASM_API, classVisitor) {
                            @Override
                            public void visitEnd() {
                                super.visitInnerClass("org/jboss/jandex/test/A", "org/jboss/jandex/test/C", "A",
                                        Modifier.STATIC);
                            }
                        };
                    }
                })
                .make()
                .getBytes();
        byte[] b = new ByteBuddy().subclass(Object.class)
                .name("org.jboss.jandex.test.B")
                .make()
                .getBytes();
        byte[] c = new ByteBuddy().subclass(Object.class)
                .name("org.jboss.jandex.test.C")
                .make()
                .getBytes();

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
