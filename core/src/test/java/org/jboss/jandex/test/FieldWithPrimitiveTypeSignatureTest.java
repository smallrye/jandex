package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.PrimitiveType;
import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.FieldVisitor;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.OpenedClassReader;

public class FieldWithPrimitiveTypeSignatureTest {
    private static final String TEST_CLASS = "org.jboss.jandex.test.TestClass";

    // the Oracle JDBC driver in version 23.3.0.23.09 contains a class with a field
    // whose generic signature encodes a primitive type, which is invalid per JVMS

    @Test
    public void test() throws IOException {
        byte[] bytecode = new ByteBuddy()
                .subclass(Object.class)
                .name(TEST_CLASS)
                .defineField("foo", boolean.class)
                .visit(new AsmVisitorWrapper.AbstractBase() {
                    @Override
                    public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor,
                            Implementation.Context implementationContext, TypePool typePool,
                            FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods,
                            int writerFlags, int readerFlags) {
                        return new ClassVisitor(OpenedClassReader.ASM_API, classVisitor) {
                            @Override
                            public FieldVisitor visitField(int access, String name, String descriptor, String signature,
                                    Object value) {
                                if ("foo".equals(name)) {
                                    signature = "Z"; // boolean
                                }
                                return super.visitField(access, name, descriptor, signature, value);
                            }
                        };
                    }
                })
                .make()
                .getBytes();
        ClassInfo clazz = Index.singleClass(bytecode);

        assertEquals(1, clazz.fields().size());
        FieldInfo field = clazz.field("foo");
        assertNotNull(field);
        assertEquals(PrimitiveType.BOOLEAN, field.type());
    }
}
