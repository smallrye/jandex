package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

public class KotlinTypeAnnotationWrongTypePathTest {
    // the tests below emulate Kotlin bugs in emitting wrong type annotation paths

    // for a method declaration `fun foo(bar: List<List<@Valid String>>) {}`,
    // the Kotlin compiler emits the following signature:
    // `(Ljava/util/List<+Ljava/util/List<Ljava/lang/String;>;>;)V`
    // which essentially corresponds to the following Java method declaration:
    // `public final void foo(List<? extends List<@Valid String>> bar) {}`
    //
    // the Kotlin compiler emits the following type annotation path:
    // `location=[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]`, which is incorrect,
    // because it corresponds to a Java method declaration of:
    // `public final void foo(List<List<@Valid String>> bar) {}`
    //
    // when the first Java declaration above is compiled with a Java compiler,
    // it emits the following type annotation path:
    // `location=[TYPE_ARGUMENT(0), WILDCARD, TYPE_ARGUMENT(0)]`
    @Test
    public void test1() throws IOException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "Test", null,
                Type.getInternalName(Object.class), null);
        MethodVisitor foo = writer.visitMethod(0, "foo", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(List.class)),
                "(Ljava/util/List<+Ljava/util/List<Ljava/lang/String;>;>;)V", null);
        foo.visitParameter("bar", 0);
        org.objectweb.asm.AnnotationVisitor ann = foo.visitTypeAnnotation(
                TypeReference.newFormalParameterReference(0).getValue(), TypePath.fromString("0;0;"),
                Type.getDescriptor(MyAnnotation.class), true);
        ann.visit("value", "foobar");
        ann.visitEnd();
        foo.visitCode();
        foo.visitInsn(Opcodes.RETURN);
        foo.visitEnd();
        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        Indexer indexer = new Indexer();
        indexer.index(new ByteArrayInputStream(bytes));
        Index index = indexer.complete();

        ClassInfo clazz = index.getClassByName("Test");
        assertNotNull(clazz);
        MethodInfo method = clazz.firstMethod("foo");
        assertNotNull(method);
        assertEquals(1, method.parametersCount());
        org.jboss.jandex.Type type = method.parameterType(0);
        assertNotNull(type);

        assertEquals("java.util.List<? extends java.util.List<java.lang.String>>", type.toString());
    }

    // at least with Kotlin 2.2.21, compiling the following code with `-Xemit-jvm-type-annotations`:
    //
    // ```
    // typealias Consumer<E> = (E) -> Unit
    // class Foo<E> {
    //     fun Consumer<E>.bar() = ::baz
    //     fun baz(a: Int, b: Int) {
    //     }
    // }
    // ```
    //
    // leads to the following bytecode for the `bar` method:
    //
    // public final kotlin.reflect.KFunction<kotlin.Unit> bar(kotlin.jvm.functions.Function1<? super E, kotlin.Unit>);
    //     ...
    //     Signature: (Lkotlin/jvm/functions/Function1<-TE;Lkotlin/Unit;>;)Lkotlin/reflect/KFunction<Lkotlin/Unit;>;
    //     RuntimeVisibleTypeAnnotations:
    //       0: #16(#17=s#18): METHOD_RETURN, location=[TYPE_ARGUMENT(0)]
    //         kotlin.ParameterName(name="a")
    //       1: #16(#17=s#19): METHOD_RETURN, location=[TYPE_ARGUMENT(1)]
    //         kotlin.ParameterName(name="b")
    //
    // the return type is parameterized with 1 type argument, but the type annotations
    // expect that there are 2 type arguments
    @Test
    public void test2() throws IOException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        writer.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "Test", null,
                Type.getInternalName(Object.class), null);
        MethodVisitor foo = writer.visitMethod(0, "foo", Type.getMethodDescriptor(Type.getType(List.class)),
                "()Ljava/util/List<Ljava/lang/String;>;", null);
        org.objectweb.asm.AnnotationVisitor ann = foo.visitTypeAnnotation(
                TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(), TypePath.fromString("0;"),
                Type.getDescriptor(MyAnnotation.class), true);
        ann.visit("value", "000");
        ann.visitEnd();
        ann = foo.visitTypeAnnotation(TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(),
                TypePath.fromString("1;"), Type.getDescriptor(MyAnnotation.class), true);
        ann.visit("value", "111");
        ann.visitEnd();
        foo.visitCode();
        foo.visitInsn(Opcodes.ACONST_NULL);
        foo.visitInsn(Opcodes.ARETURN);
        foo.visitEnd();
        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        Indexer indexer = new Indexer();
        indexer.index(new ByteArrayInputStream(bytes));
        Index index = indexer.complete();

        ClassInfo clazz = index.getClassByName("Test");
        assertNotNull(clazz);
        MethodInfo method = clazz.firstMethod("foo");
        assertNotNull(method);
        org.jboss.jandex.Type type = method.returnType();
        assertNotNull(type);
        assertEquals("java.util.List<java.lang.@MyAnnotation(\"000\") String>", type.toString());
    }
}
