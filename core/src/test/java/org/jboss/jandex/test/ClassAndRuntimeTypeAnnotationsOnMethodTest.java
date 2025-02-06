package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

public class ClassAndRuntimeTypeAnnotationsOnMethodTest {
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE_USE)
    @interface ClassAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    @interface RuntimeAnnotation {
    }

    static class TestMethod {
        static void method(@RuntimeAnnotation int foo, @ClassAnnotation String bar) {
        }
    }

    @Test
    public void test() throws IOException {
        Index index = Index.of(ClassAnnotation.class, RuntimeAnnotation.class, TestMethod.class);
        MethodInfo method = index.getClassByName(TestMethod.class).firstMethod("method");
        assertNotNull(method);
        assertEquals(2, method.parametersCount());

        Type param0 = method.parameterType(0);
        assertEquals(Type.Kind.PRIMITIVE, param0.kind());
        assertEquals(PrimitiveType.Primitive.INT, param0.asPrimitiveType().primitive());
        assertEquals(1, param0.annotations().size());
        assertEquals(RuntimeAnnotation.class.getName(), param0.annotations().get(0).name().toString());

        Type param1 = method.parameterType(1);
        assertEquals(Type.Kind.CLASS, param1.kind());
        assertEquals(DotName.STRING_NAME, param1.asClassType().name());
        assertEquals(1, param1.annotations().size());
        assertEquals(ClassAnnotation.class.getName(), param1.annotations().get(0).name().toString());
    }
}
