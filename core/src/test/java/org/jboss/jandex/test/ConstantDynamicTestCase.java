package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.Callable;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ConstantDynamicTestCase {
    @Test
    public void testConstantDynamicSupport() throws Exception {
        Type OBJ_TYPE = Type.getType(Object.class);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "Test", null,
                OBJ_TYPE.getInternalName(), new String[] { Type.getInternalName(Callable.class) });
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC, "call", Type.getMethodDescriptor(OBJ_TYPE), null,
                new String[] { Type.getInternalName(Exception.class) });
        method.visitCode();
        method.visitLdcInsn(new ConstantDynamic(
                "_",
                OBJ_TYPE.getDescriptor(),
                new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/ConstantBootstraps", "invoke",
                        Type.getMethodDescriptor(
                                OBJ_TYPE,
                                Type.getObjectType("java/lang/invoke/MethodHandles$Lookup"),
                                Type.getType(String.class),
                                Type.getType(Class.class),
                                Type.getObjectType("java/lang/invoke/MethodHandle"),
                                Type.getType(Object[].class)),
                        false),
                new Handle(Opcodes.H_NEWINVOKESPECIAL, OBJ_TYPE.getInternalName(), "<init>",
                        Type.getMethodDescriptor(Type.VOID_TYPE),
                        false)));
        method.visitInsn(Opcodes.ARETURN);
        method.visitEnd();
        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        ClassInfo classInfo = Index.singleClass(bytes);
        assertNotNull(classInfo);
    }
}
