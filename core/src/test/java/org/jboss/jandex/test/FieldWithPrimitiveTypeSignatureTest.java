package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.PrimitiveType;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class FieldWithPrimitiveTypeSignatureTest {
    // the Oracle JDBC driver in version 23.3.0.23.09 contains a class with a field
    // whose generic signature encodes a primitive type, which is invalid per JVMS

    @Test
    public void test() throws IOException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "Test", null,
                Type.getInternalName(Object.class), null);
        writer.visitField(Opcodes.ACC_PUBLIC, "foo", Type.BYTE_TYPE.getDescriptor(), "Z", null);
        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        ClassInfo clazz = Index.singleClass(bytes);

        assertEquals(1, clazz.fields().size());
        FieldInfo field = clazz.field("foo");
        assertNotNull(field);
        assertEquals(PrimitiveType.BOOLEAN, field.type());
    }
}
