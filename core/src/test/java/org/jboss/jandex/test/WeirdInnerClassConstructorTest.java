package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

// a Java compiler will never generate classes like this, but e.g. Groovy compiler may
// (seems legal from the JVMS perspective, but not exactly sure...)
public class WeirdInnerClassConstructorTest {
    @Test
    public void innerClass() throws IOException {
        String innerName = "Inner";
        String name = WeirdInnerClassConstructorTest.class.getName().replace('.', '/') + "$" + innerName;
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, name, null, Type.getInternalName(Object.class), null);
        writer.visitInnerClass(name, Type.getInternalName(WeirdInnerClassConstructorTest.class), innerName, Opcodes.ACC_PUBLIC);
        MethodVisitor ctor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE),
                null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE), false);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitEnd();
        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        ClassInfo clazz = Index.singleClass(bytes);

        assertEquals(1, clazz.constructors().size());
        assertEquals(0, clazz.constructors().get(0).parametersCount());
        assertTrue(clazz.constructors().get(0).parameterTypes().isEmpty());
        assertEquals(0, clazz.constructors().get(0).descriptorParametersCount());
        assertTrue(clazz.constructors().get(0).descriptorParameterTypes().isEmpty());
        assertTrue(clazz.hasNoArgsConstructor());
    }

    @Test
    public void localClass() throws IOException, NoSuchMethodException {
        String localName = "Local";
        String name = WeirdInnerClassConstructorTest.class.getName().replace('.', '/') + "$" + localName;
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, name, null, Type.getInternalName(Object.class), null);
        writer.visitInnerClass(name, null, localName, Opcodes.ACC_PUBLIC);
        writer.visitOuterClass(Type.getInternalName(WeirdInnerClassConstructorTest.class), "localClass",
                Type.getMethodDescriptor(Type.VOID_TYPE));
        // to pretend that this local class is declared in non-static context
        FieldVisitor field = writer.visitField(Opcodes.ACC_SYNTHETIC, "this$0",
                Type.getDescriptor(WeirdInnerClassConstructorTest.class), null, null);
        field.visitEnd();
        MethodVisitor ctor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE),
                null, null);
        ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE), false);
        ctor.visitInsn(Opcodes.RETURN);
        ctor.visitEnd();
        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        ClassInfo clazz = Index.singleClass(bytes);

        assertEquals(1, clazz.constructors().size());
        assertEquals(0, clazz.constructors().get(0).parametersCount());
        assertTrue(clazz.constructors().get(0).parameterTypes().isEmpty());
        assertEquals(0, clazz.constructors().get(0).descriptorParametersCount());
        assertTrue(clazz.constructors().get(0).descriptorParameterTypes().isEmpty());
        assertTrue(clazz.hasNoArgsConstructor());
    }
}
