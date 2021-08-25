package org.jboss.jandex.test;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Callable;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Indexer;
import org.junit.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaConstant;

public class ConstantDynamicTestCase {

    @Test
    public void testConstantDynamicSupport() throws Exception {
        Indexer indexer = new Indexer();
        // Creating dynamic constants using Byte Buddy
        // Inspired from https://www.javacodegeeks.com/2018/08/hands-on-java-constantdynamic.html
        final byte[] dynamicConstantsClass = new ByteBuddy()
                .with(ClassFileVersion.JAVA_V11)
                .subclass(Callable.class)
                .method(ElementMatchers.named("call"))
                .intercept(FixedValue.value(JavaConstant.Dynamic.ofInvocation(Object.class.getConstructor())))
                .make()
                .getBytes();
        ClassInfo classInfo = indexer.index(new ByteArrayInputStream(dynamicConstantsClass));
        assertNotNull(classInfo);
    }
}
