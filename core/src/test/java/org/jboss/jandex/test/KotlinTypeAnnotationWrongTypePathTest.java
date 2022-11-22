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
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.OpenedClassReader;

public class KotlinTypeAnnotationWrongTypePathTest {
    private static final String TEST_CLASS = "org.jboss.jandex.test.TestClass";

    // emulates a Kotlin bug in emitting a wrong type annotation path
    //
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
    public void test() throws IOException {
        // List<List<String>>
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
}
