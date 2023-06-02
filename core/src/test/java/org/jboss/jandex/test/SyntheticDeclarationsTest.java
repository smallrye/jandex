package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.Test;

// see also SyntheticDeclarationsExample
public class SyntheticDeclarationsTest {
    private final SyntheticDeclarationsExample obj = new SyntheticDeclarationsExample().prepare();
    private final Index index = buildIndex();

    @Test
    public void localClassInStaticContext() {
        List<MethodInfo> constructors = index.getClassByName(SyntheticDeclarationsExample.localClassInStaticContext)
                .constructors();
        assertEquals(3, constructors.size());

        {
            // this constructor doesn't require the signature attribute to be emitted, so ecj doesn't,
            // and hence the captured variable is present as a parameter

            MethodInfo constructor = constructors.stream()
                    .filter(it -> it.parameterType(0).kind() == Type.Kind.PRIMITIVE)
                    .findAny()
                    .orElse(null);
            assertNotNull(constructor);

            assertEquals(CompiledWith.ecj() ? 2 : 1, constructor.parametersCount());
            assertEquals("int", constructor.parameterType(0).name().toString());
            assertEquals("num", constructor.parameterName(0));
            assertEquals(2, constructor.descriptorParametersCount());
            assertEquals("int", constructor.descriptorParameterTypes().get(0).name().toString());
            assertEquals(String.class.getName(), constructor.descriptorParameterTypes().get(1).name().toString()); // captured

            assertTrue(constructor.parameters().get(0).annotations().isEmpty());
            assertFalse(constructor.parameters().get(0).hasAnnotation(MyAnnotation.DOT_NAME));
        }

        {
            // this constructor doesn't require the signature attribute to be emitted, so ecj doesn't,
            // and hence the captured variable is present as a parameter

            MethodInfo constructor = constructors.stream()
                    .filter(it -> it.parameterType(0).kind() == Type.Kind.CLASS)
                    .findAny()
                    .orElse(null);
            assertNotNull(constructor);

            assertEquals(CompiledWith.ecj() ? 3 : 2, constructor.parametersCount());
            assertEquals(Object.class.getName(), constructor.parameterType(0).name().toString());
            assertEquals(Integer.class.getName(), constructor.parameterType(1).name().toString());
            assertEquals("ignored", constructor.parameterName(0));
            assertEquals("num", constructor.parameterName(1));
            assertEquals(3, constructor.descriptorParametersCount());
            assertEquals(Object.class.getName(), constructor.descriptorParameterTypes().get(0).name().toString());
            assertEquals(Integer.class.getName(), constructor.descriptorParameterTypes().get(1).name().toString());
            assertEquals(String.class.getName(), constructor.descriptorParameterTypes().get(2).name().toString()); // captured

            assertTrue(constructor.parameters().get(0).annotations().isEmpty());
            assertFalse(constructor.parameters().get(1).annotations().isEmpty());
            assertTrue(constructor.parameters().get(1).hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("static local class",
                    constructor.parameters().get(1).annotation(MyAnnotation.DOT_NAME).value().asString());
            assertTrue(constructor.parameters().get(1).type().hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("static local class",
                    constructor.parameters().get(1).type().annotation(MyAnnotation.DOT_NAME).value().asString());
        }

        {
            MethodInfo constructor = constructors.stream()
                    .filter(it -> it.parameterType(0).kind() == Type.Kind.PARAMETERIZED_TYPE)
                    .findAny()
                    .orElse(null);
            assertNotNull(constructor);

            assertEquals(1, constructor.parametersCount());
            assertEquals(List.class.getName(), constructor.parameterType(0).name().toString());
            assertEquals("list", constructor.parameterName(0));
            assertEquals(2, constructor.descriptorParametersCount());
            assertEquals(List.class.getName(), constructor.descriptorParameterTypes().get(0).name().toString());
            assertEquals(String.class.getName(), constructor.descriptorParameterTypes().get(1).name().toString()); // captured

            assertFalse(constructor.parameters().get(0).annotations().isEmpty());
            assertTrue(constructor.parameters().get(0).hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("static local class <T>",
                    constructor.parameters().get(0).annotation(MyAnnotation.DOT_NAME).value().asString());
            assertTrue(constructor.parameters().get(0).type().hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("static local class <T>",
                    constructor.parameters().get(0).type().annotation(MyAnnotation.DOT_NAME).value().asString());
        }
    }

    // constructor of a local class not declared in static context has a synthetic first parameter
    // (the enclosing instance) and for each captured variable, one synthetic parameter at the end
    @Test
    public void localClassNotInStaticContext() {
        List<MethodInfo> constructors = index.getClassByName(obj.localClassNotInStaticContext)
                .constructors();
        assertEquals(3, constructors.size());

        {
            // this constructor doesn't require the signature attribute to be emitted, so ecj doesn't,
            // and hence the enclosing instance and captured variable are present as parameters

            MethodInfo constructor = constructors.stream()
                    .filter(it -> it.parameterType(0).kind() == Type.Kind.PRIMITIVE)
                    .findAny()
                    .orElse(null);
            assertNotNull(constructor);

            assertEquals(CompiledWith.ecj() ? 2 : 1, constructor.parametersCount());
            assertEquals("int", constructor.parameterType(0).name().toString());
            assertEquals("num", constructor.parameterName(0));
            assertEquals(3, constructor.descriptorParametersCount());
            assertEquals(SyntheticDeclarationsExample.class.getName(),
                    constructor.descriptorParameterTypes().get(0).name().toString()); // enclosing instance
            assertEquals("int", constructor.descriptorParameterTypes().get(1).name().toString());
            assertEquals(String.class.getName(), constructor.descriptorParameterTypes().get(2).name().toString()); // captured

            assertTrue(constructor.parameters().get(0).annotations().isEmpty());
            assertFalse(constructor.parameters().get(0).hasAnnotation(MyAnnotation.DOT_NAME));
        }

        {
            // this constructor doesn't require the signature attribute to be emitted, so ecj doesn't,
            // and hence the enclosing instance and captured variable are present as parameters

            MethodInfo constructor = constructors.stream()
                    .filter(it -> it.parameterType(0).kind() == Type.Kind.CLASS)
                    .findAny()
                    .orElse(null);
            assertNotNull(constructor);

            assertEquals(CompiledWith.ecj() ? 3 : 2, constructor.parametersCount());
            assertEquals(Object.class.getName(), constructor.parameterType(0).name().toString());
            assertEquals(Integer.class.getName(), constructor.parameterType(1).name().toString());
            assertEquals("ignored", constructor.parameterName(0));
            assertEquals("num", constructor.parameterName(1));
            assertEquals(4, constructor.descriptorParametersCount());
            assertEquals(SyntheticDeclarationsExample.class.getName(),
                    constructor.descriptorParameterTypes().get(0).name().toString()); // enclosing instance
            assertEquals(Object.class.getName(), constructor.descriptorParameterTypes().get(1).name().toString());
            assertEquals(Integer.class.getName(), constructor.descriptorParameterTypes().get(2).name().toString());
            assertEquals(String.class.getName(), constructor.descriptorParameterTypes().get(3).name().toString()); // captured

            assertTrue(constructor.parameters().get(0).annotations().isEmpty());
            assertFalse(constructor.parameters().get(1).annotations().isEmpty());
            assertTrue(constructor.parameters().get(1).hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("local class",
                    constructor.parameters().get(1).annotation(MyAnnotation.DOT_NAME).value().asString());
            assertTrue(constructor.parameters().get(1).type().hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("local class",
                    constructor.parameters().get(1).type().annotation(MyAnnotation.DOT_NAME).value().asString());
        }

        {
            MethodInfo constructor = constructors.stream()
                    .filter(it -> it.parameterType(0).kind() == Type.Kind.PARAMETERIZED_TYPE)
                    .findAny()
                    .orElse(null);
            assertNotNull(constructor);

            assertEquals(1, constructor.parametersCount());
            assertEquals("java.util.List", constructor.parameterType(0).name().toString());
            assertEquals("list", constructor.parameterName(0));
            assertEquals(3, constructor.descriptorParametersCount());
            assertEquals(SyntheticDeclarationsExample.class.getName(),
                    constructor.descriptorParameterTypes().get(0).name().toString()); // enclosing instance
            assertEquals(List.class.getName(), constructor.descriptorParameterTypes().get(1).name().toString());
            assertEquals(String.class.getName(), constructor.descriptorParameterTypes().get(2).name().toString()); // captured

            assertFalse(constructor.parameters().get(0).annotations().isEmpty());
            assertTrue(constructor.parameters().get(0).hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("local class <T>",
                    constructor.parameters().get(0).annotation(MyAnnotation.DOT_NAME).value().asString());
            assertTrue(constructor.parameters().get(0).type().hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("local class <T>",
                    constructor.parameters().get(0).type().annotation(MyAnnotation.DOT_NAME).value().asString());
        }
    }

    // first parameter of a constructor of a private inner class is synthetic
    @Test
    public void firstParameterOfAConstructorOfAPrivateInnerClass() {
        List<MethodInfo> constructors = index.getClassByName(obj.privateInnerClass).constructors();
        assertEquals(1, constructors.size());
        MethodInfo constructor = constructors.get(0);
        assertEquals(1, constructor.parametersCount());
        assertEquals("int", constructor.parameterType(0).name().toString());
        assertEquals("i", constructor.parameterName(0));
        assertEquals(2, constructor.descriptorParametersCount());
        assertEquals(SyntheticDeclarationsExample.class.getName(),
                constructor.descriptorParameterTypes().get(0).name().toString());
        assertEquals("int", constructor.descriptorParameterTypes().get(1).name().toString());

        assertTrue(constructor.parameters().get(0).hasAnnotation(MyAnnotation.DOT_NAME));
        assertEquals("private inner class",
                constructor.parameters().get(0).annotation(MyAnnotation.DOT_NAME).value().asString());
        assertEquals("private inner class",
                constructor.parameters().get(0).type().annotation(MyAnnotation.DOT_NAME).value().asString());
        assertEquals(2, constructor.parameters().get(0).annotations().size());
    }

    // first 2 parameters of an enum constructor are synthetic (we DON'T expect to see them)
    @Test
    public void defaultConstructorOfAnEnumClass() {
        List<MethodInfo> constructors = index.getClassByName(SyntheticDeclarationsExample.EnumWithConstructor.class)
                .constructors();
        assertEquals(1, constructors.size());
        MethodInfo constructor = constructors.get(0);
        assertEquals(2, constructor.parametersCount());
        assertEquals("str", constructor.parameterName(0));
        assertEquals("num", constructor.parameterName(1));

        DotName ann = DotName.createSimple(MyAnnotation.class.getName());
        assertTrue(constructor.parameters().get(0).hasAnnotation(ann));
        assertEquals("enum: str", constructor.parameters().get(0).annotation(ann).value().asString());
        assertEquals("enum: str", constructor.parameters().get(0).type().annotation(ann).value().asString());
        assertEquals(2, constructor.parameters().get(0).annotations().size());
        assertTrue(constructor.parameters().get(1).hasAnnotation(ann));
        assertEquals("enum: num", constructor.parameters().get(1).annotation(ann).value().asString());
        assertEquals("enum: num", constructor.parameters().get(1).type().annotation(ann).value().asString());
        assertEquals(2, constructor.parameters().get(1).annotations().size());
    }

    private Index buildIndex() {
        try {
            Index index = Index.of(SyntheticDeclarationsExample.class,
                    SyntheticDeclarationsExample.EnumWithConstructor.class,
                    SyntheticDeclarationsExample.localClassInStaticContext,
                    obj.localClassNotInStaticContext,
                    obj.privateInnerClass);
            return index;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
