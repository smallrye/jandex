package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.SyntheticState;

// a Java compiler will never generate classes like this, but e.g. Groovy compiler may
// (seems legal from the JVMS perspective, but not exactly sure...)
public class WeirdInnerClassConstructorTest {
    @Test
    public void innerClass() throws IOException {
        byte[] innerClass = new ByteBuddy().subclass(Object.class)
                .name(WeirdInnerClassConstructorTest.class.getName() + "$Inner")
                .innerTypeOf(WeirdInnerClassConstructorTest.class).asMemberType()
                .make()
                .getBytes();
        ClassInfo clazz = Index.singleClass(innerClass);

        assertEquals(1, clazz.constructors().size());
        assertEquals(0, clazz.constructors().get(0).parametersCount());
        assertTrue(clazz.constructors().get(0).parameterTypes().isEmpty());
        assertEquals(0, clazz.constructors().get(0).descriptorParametersCount());
        assertTrue(clazz.constructors().get(0).descriptorParameterTypes().isEmpty());
        assertTrue(clazz.hasNoArgsConstructor());
    }

    @Test
    public void localClass() throws IOException, NoSuchMethodException {
        Method method = WeirdInnerClassConstructorTest.class.getDeclaredMethod("localClass");

        byte[] localClass = new ByteBuddy().subclass(Object.class)
                .name(WeirdInnerClassConstructorTest.class.getName() + "$Local")
                .innerTypeOf(method)
                // to pretend that this local class is declared in non-static context
                .defineField("this$0", WeirdInnerClassConstructorTest.class, SyntheticState.SYNTHETIC)
                .make()
                .getBytes();
        ClassInfo clazz = Index.singleClass(localClass);

        assertEquals(1, clazz.constructors().size());
        assertEquals(0, clazz.constructors().get(0).parametersCount());
        assertTrue(clazz.constructors().get(0).parameterTypes().isEmpty());
        assertEquals(0, clazz.constructors().get(0).descriptorParametersCount());
        assertTrue(clazz.constructors().get(0).descriptorParameterTypes().isEmpty());
        assertTrue(clazz.hasNoArgsConstructor());
    }
}
