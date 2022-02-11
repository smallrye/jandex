package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.Callable;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaConstant;

public class ConstantDynamicTestCase {

    @Test
    public void testConstantDynamicSupport() throws Exception {
        // Creating dynamic constants using Byte Buddy
        // Inspired from https://www.javacodegeeks.com/2018/08/hands-on-java-constantdynamic.html
        final byte[] dynamicConstantsClass = new ByteBuddy()
                .with(ClassFileVersion.JAVA_V11)
                .subclass(Callable.class)
                .method(ElementMatchers.named("call"))
                .intercept(FixedValue.value(JavaConstant.Dynamic.ofInvocation(Object.class.getConstructor())))
                .make()
                .getBytes();
        ClassInfo classInfo = IndexingUtil.indexSingle(dynamicConstantsClass);
        assertNotNull(classInfo);
    }
}
