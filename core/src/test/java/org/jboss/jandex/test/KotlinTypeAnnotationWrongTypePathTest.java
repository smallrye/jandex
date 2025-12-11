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
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.StubMethod;
import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.TypePath;
import net.bytebuddy.jar.asm.TypeReference;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.OpenedClassReader;

public class KotlinTypeAnnotationWrongTypePathTest {
    private static final String TEST_CLASS = "org.jboss.jandex.test.TestClass";

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
        // List<List<@MyAnnotation("foobar") String>>
        TypeDescription.Generic annotatedString = TypeDescription.Generic.Builder.of(String.class)
                .annotate(AnnotationDescription.Builder.ofType(MyAnnotation.class)
                        .define("value", "foobar").build())
                .build();
        TypeDescription.Generic listOfAnnotatedString = TypeDescription.Generic.Builder.parameterizedType(
                TypeDescription.Generic.Builder.of(List.class).build().asErasure(), annotatedString).build();
        TypeDescription.Generic listOfListOfAnnotatedString = TypeDescription.Generic.Builder.parameterizedType(
                TypeDescription.Generic.Builder.of(List.class).build().asErasure(), listOfAnnotatedString).build();

        byte[] bytes = new ByteBuddy()
                .subclass(Object.class)
                .name(TEST_CLASS)
                .defineMethod("foo", void.class)
                .withParameter(listOfListOfAnnotatedString, "bar")
                .intercept(StubMethod.INSTANCE)
                .visit(new AsmVisitorWrapper.AbstractBase() {
                    @Override
                    public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor,
                            Implementation.Context implementationContext, TypePool typePool,
                            FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods,
                            int writerFlags, int readerFlags) {
                        return new ClassVisitor(OpenedClassReader.ASM_API, classVisitor) {
                            @Override
                            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                    String[] exceptions) {
                                if ("foo".equals(name)) {
                                    // List<? extends List<String>>
                                    signature = "(Ljava/util/List<+Ljava/util/List<Ljava/lang/String;>;>;)V";
                                }
                                return super.visitMethod(access, name, descriptor, signature, exceptions);
                            }
                        };
                    }
                })
                .make()
                .getBytes();

        Indexer indexer = new Indexer();
        indexer.index(new ByteArrayInputStream(bytes));
        Index index = indexer.complete();

        ClassInfo clazz = index.getClassByName(TEST_CLASS);
        assertNotNull(clazz);
        MethodInfo method = clazz.firstMethod("foo");
        assertNotNull(method);
        assertEquals(1, method.parametersCount());
        Type type = method.parameterType(0);
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
        // List<String>
        TypeDescription.Generic listOfString = TypeDescription.Generic.Builder.parameterizedType(
                TypeDescription.ForLoadedType.of(List.class), TypeDescription.ForLoadedType.of(String.class)).build();

        byte[] bytes = new ByteBuddy()
                .subclass(Object.class)
                .name(TEST_CLASS)
                .defineMethod("foo", listOfString)
                .intercept(StubMethod.INSTANCE)
                .visit(new AsmVisitorWrapper.AbstractBase() {
                    @Override
                    public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor,
                            Implementation.Context implementationContext, TypePool typePool,
                            FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods,
                            int writerFlags, int readerFlags) {
                        return new ClassVisitor(OpenedClassReader.ASM_API, classVisitor) {
                            @Override
                            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                    String[] exceptions) {
                                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                                if ("foo".equals(name)) {
                                    AnnotationVisitor av = mv.visitTypeAnnotation(
                                            TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(),
                                            TypePath.fromString("0;"),
                                            "Lorg/jboss/jandex/test/MyAnnotation;", true);
                                    av.visit("value", "000");
                                    av.visitEnd();

                                    av = mv.visitTypeAnnotation(
                                            TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(),
                                            TypePath.fromString("1;"),
                                            "Lorg/jboss/jandex/test/MyAnnotation;", true);
                                    av.visit("value", "111");
                                    av.visitEnd();
                                }
                                return mv;
                            }
                        };
                    }
                })
                .make()
                .getBytes();

        Indexer indexer = new Indexer();
        indexer.index(new ByteArrayInputStream(bytes));
        Index index = indexer.complete();

        ClassInfo clazz = index.getClassByName(TEST_CLASS);
        assertNotNull(clazz);
        MethodInfo method = clazz.firstMethod("foo");
        assertNotNull(method);
        Type type = method.returnType();
        assertNotNull(type);
        assertEquals("java.util.List<java.lang.@MyAnnotation(\"000\") String>", type.toString());
    }
}
