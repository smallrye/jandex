package org.jboss.jandex.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.junit.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.StubMethod;

@Retention(RetentionPolicy.RUNTIME)
@interface MyAnn {
}

public class TooManyMethodParametersTest {
    private static final String TEST_CLASS = "org.jboss.jandex.test.TestClass";

    // the Kotlin compiler happily generates methods with more than 255 parameters,
    // even if the JVM specification prohibits them
    //
    // fortunately, ByteBuddy is happy to generate invalid methods too, so we can avoid
    // bringing in Kotlin just to reproduce the issue

    @Test
    public void test() throws IOException {
        DynamicType.Builder.MethodDefinition.ParameterDefinition<Object> builder = new ByteBuddy()
                .with(new NamingStrategy.AbstractBase() {
                    @Override
                    protected String name(TypeDescription superClass) {
                        return TEST_CLASS;
                    }
                })
                .subclass(Object.class)
                .defineMethod("hugeMethod", void.class)
                .withParameter(String.class, "p0")
                .annotateParameter(AnnotationDescription.Builder.ofType(MyAnn.class).build());
        for (int i = 1; i < 300; i++) {
            builder = builder.withParameter(String.class, "p" + i)
                    .annotateParameter(AnnotationDescription.Builder.ofType(MyAnn.class).build());
        }
        byte[] syntheticClass = builder
                .intercept(StubMethod.INSTANCE)
                .make()
                .getBytes();

        Indexer indexer = new Indexer();
        indexer.index(new ByteArrayInputStream(syntheticClass));
        Index index = indexer.complete();

        ClassInfo clazz = index.getClassByName(DotName.createSimple(TEST_CLASS));
        assertNotNull(clazz);
        MethodInfo method = clazz.firstMethod("hugeMethod");
        assertNotNull(method);

        for (short i = 0; i < 300; i++) {
            MethodParameterInfo param = MethodParameterInfo.create(method, i);
            List<AnnotationInstance> paramAnnotations = new ArrayList<AnnotationInstance>();
            for (AnnotationInstance annotation : method.annotations()) {
                if (annotation.target().equals(param)) {
                    paramAnnotations.add(annotation);
                }
            }
            assertEquals(1, paramAnnotations.size());
            assertEquals("MyAnn", paramAnnotations.get(0).name().withoutPackagePrefix());
        }
    }
}
