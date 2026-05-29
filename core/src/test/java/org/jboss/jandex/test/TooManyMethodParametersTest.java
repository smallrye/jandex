package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class TooManyMethodParametersTest {
    // the Kotlin compiler happily generates methods with more than 255 parameters,
    // even if the JVM specification prohibits them

    @Test
    public void test() throws IOException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "Test", null,
                Type.getInternalName(Object.class), null);
        Type[] params = new Type[300];
        for (int i = 0; i < 300; i++) {
            params[i] = Type.getType(String.class);
        }
        MethodVisitor hugeMethod = writer.visitMethod(Opcodes.ACC_PUBLIC, "hugeMethod",
                Type.getMethodDescriptor(Type.VOID_TYPE, params), null, null);
        for (int i = 0; i < 300; i++) {
            hugeMethod.visitParameter("p" + i, 0);
            AnnotationVisitor ann = hugeMethod.visitParameterAnnotation(i, Type.getDescriptor(MyAnnotation.class), true);
            ann.visit("value", "" + i);
            ann.visitEnd();
        }
        hugeMethod.visitCode();
        hugeMethod.visitInsn(Opcodes.RETURN);
        hugeMethod.visitEnd();
        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        Indexer indexer = new Indexer();
        indexer.index(new ByteArrayInputStream(bytes));
        Index index = indexer.complete();

        ClassInfo clazz = index.getClassByName("Test");
        assertNotNull(clazz);
        MethodInfo method = clazz.firstMethod("hugeMethod");
        assertNotNull(method);

        for (short i = 0; i < 300; i++) {
            MethodParameterInfo param = MethodParameterInfo.create(method, i);
            List<AnnotationInstance> paramAnnotations = new ArrayList<>();
            for (AnnotationInstance annotation : method.annotations()) {
                if (annotation.target().equals(param)) {
                    paramAnnotations.add(annotation);
                }
            }
            assertEquals(1, paramAnnotations.size());
            assertEquals("MyAnnotation", paramAnnotations.get(0).name().withoutPackagePrefix());
            assertEquals("" + i, paramAnnotations.get(0).value().asString());
        }
    }
}
