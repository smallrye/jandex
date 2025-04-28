package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.ModifierAdjustment;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.implementation.FixedValue;

public class ModifiersTestCase {

    @Test
    public void testClassIsAnnotation() throws IOException {
        assertTrue(Index.singleClass(BasicTestCase.TestAnnotation.class).isAnnotation());
    }

    @Test
    public void testClassIsEnum() throws IOException {
        assertTrue(Index.singleClass(FieldInfoTestCase.FieldInfoTestEnum.class).isEnum());
    }

    @Test
    public void testClassIsAbstract() throws IOException {
        assertTrue(Index.singleClass(MyAbstractClass.class).isAbstract());
        assertTrue(Index.singleClass(MyInterface.class).isAbstract());
        assertFalse(Index.singleClass(DummyTopLevel.class).isAbstract());
    }

    @Test
    public void testClassIsSynthetic() throws Exception {
        final byte[] syntheticClass = new ByteBuddy().subclass(Object.class)
                .visit(new ModifierAdjustment().withTypeModifiers(SyntheticState.SYNTHETIC))
                .make()
                .getBytes();
        ClassInfo classInfo = Index.singleClass(syntheticClass);
        assertTrue(classInfo.isSynthetic());
    }

    @Test
    public void testMethodIsSynthetic() throws Exception {
        final byte[] syntheticClass = new ByteBuddy().subclass(Object.class)
                .visit(new ModifierAdjustment().withTypeModifiers(SyntheticState.SYNTHETIC))
                .defineMethod("ping", String.class, SyntheticState.SYNTHETIC).intercept(FixedValue.value("Hello World!"))
                .make()
                .getBytes();
        ClassInfo classInfo = Index.singleClass(syntheticClass);
        assertTrue(classInfo.isSynthetic());
        MethodInfo ping = null;
        for (MethodInfo m : classInfo.methods()) {
            if (m.name().equals("ping")) {
                ping = m;
            }
        }
        assertNotNull(ping);
        assertTrue(ping.isSynthetic());
    }

    @Test
    public void testMethodIsDefault() throws Exception {
        ClassInfo clazz = Index.singleClass(MyInterface.class);
        assertTrue(clazz.method("defaultVal").isDefault());
        assertFalse(clazz.method("val").isDefault());
        assertFalse(clazz.method("age").isDefault());

        clazz = Index.singleClass(DummyTopLevel.class);
        assertFalse(clazz.method("<init>").isDefault());
    }

    @Test
    public void testMethodIsAbstract() throws Exception {
        ClassInfo clazz = Index.singleClass(MyInterface.class);
        assertFalse(clazz.method("defaultVal").isAbstract());
        assertTrue(clazz.method("val").isAbstract());
        assertFalse(clazz.method("age").isAbstract());

        clazz = Index.singleClass(MyAbstractClass.class);
        assertTrue(clazz.method("hello").isAbstract());
        assertFalse(clazz.method("answer").isAbstract());

        clazz = Index.singleClass(DummyTopLevel.class);
        assertFalse(clazz.method("<init>").isAbstract());
    }

    @Test
    public void testMethodIsBridge() throws Exception {
        ClassInfo clazz = Index.singleClass(FooWithBridge.class);
        List<MethodInfo> acceptMethods = clazz.methods().stream().filter(m -> m.name().equals("accept"))
                .collect(Collectors.toList());
        assertEquals(2, acceptMethods.size());
        for (MethodInfo m : acceptMethods) {
            if (m.parameterType(0).name().equals(DotName.OBJECT_NAME)) {
                // accept(Object)
                assertTrue(m.isBridge());
                assertTrue(m.isSynthetic());
            } else {
                // accept(String)
                assertFalse(m.isBridge());
                assertFalse(m.isSynthetic());
            }
        }
    }

    @Test
    public void testFieldIsSynthetic() throws Exception {
        final byte[] syntheticClass = new ByteBuddy().subclass(Object.class)
                .visit(new ModifierAdjustment().withTypeModifiers(SyntheticState.SYNTHETIC))
                .defineField("ping", String.class, SyntheticState.SYNTHETIC)
                .make()
                .getBytes();
        ClassInfo classInfo = Index.singleClass(syntheticClass);
        assertTrue(classInfo.isSynthetic());
        FieldInfo ping = null;
        for (FieldInfo f : classInfo.fields()) {
            if (f.name().equals("ping")) {
                ping = f;
            }
        }
        assertNotNull(ping);
        assertTrue(ping.isSynthetic());
    }

    interface MyInterface {

        static int age() {
            return 1;
        }

        boolean val() throws Exception;

        default boolean defaultVal() {
            return true;
        }

    }

    abstract class MyAbstractClass {
        abstract void hello();

        int answer() {
            return 42;
        }
    }

    public static class FooWithBridge implements Consumer<String> {

        @Override
        public void accept(String val) {
        }

    }

}
