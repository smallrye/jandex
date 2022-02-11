package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.ModifierAdjustment;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.implementation.FixedValue;

public class ModifiersTestCase {

    @Test
    public void testClassIsAnnotation() throws IOException {
        assertTrue(BasicTestCase.getClassInfo(BasicTestCase.TestAnnotation.class).isAnnotation());
    }

    @Test
    public void testClassIsEnum() throws IOException {
        assertTrue(BasicTestCase.getClassInfo(FieldInfoTestCase.FieldInfoTestEnum.class).isEnum());
    }

    @Test
    public void testClassIsSynthetic() throws Exception {
        Indexer indexer = new Indexer();
        final byte[] syntheticClass = new ByteBuddy().subclass(Object.class)
                .visit(new ModifierAdjustment().withTypeModifiers(SyntheticState.SYNTHETIC)).make()
                .getBytes();
        ClassInfo classInfo = indexer.index(new ByteArrayInputStream(syntheticClass));
        assertTrue(classInfo.isSynthetic());
    }

    @Test
    public void testMethodIsSynthetic() throws Exception {
        Indexer indexer = new Indexer();
        final byte[] syntheticClass = new ByteBuddy().subclass(Object.class)
                .visit(new ModifierAdjustment().withTypeModifiers(SyntheticState.SYNTHETIC))
                .defineMethod("ping", String.class, SyntheticState.SYNTHETIC).intercept(FixedValue.value("Hello World!")).make()
                .getBytes();
        ClassInfo classInfo = indexer.index(new ByteArrayInputStream(syntheticClass));
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
    public void testFieldIsSynthetic() throws Exception {
        Indexer indexer = new Indexer();
        final byte[] syntheticClass = new ByteBuddy().subclass(Object.class)
                .visit(new ModifierAdjustment().withTypeModifiers(SyntheticState.SYNTHETIC))
                .defineField("ping", String.class, SyntheticState.SYNTHETIC).make().getBytes();
        ClassInfo classInfo = indexer.index(new ByteArrayInputStream(syntheticClass));
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

}
