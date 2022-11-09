package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.test.util.IndexingUtil;
import org.junit.jupiter.api.Test;

// see also ImplicitDeclarationsExample and ImplicitDeclarationsExampleSupplement
public class ImplicitDeclarationsTest {
    private final ImplicitDeclarationsExample obj = new ImplicitDeclarationsExample();
    private final Index index = buildIndex();

    //
    // implicit declarations that can't be marked mandated (fields, methods, constructors, annotations)
    //

    // default constructor of a normal class
    @Test
    public void defaultConstructorOfANormalClass() {
        List<MethodInfo> constructors = index.getClassByName(ImplicitDeclarationsExample.class).constructors();
        assertEquals(1, constructors.size());
        MethodInfo constructor = constructors.get(0);
        assertEquals(0, constructor.parametersCount());
        assertEquals(0, constructor.descriptorParametersCount());
    }

    // default constructor of an enum class
    @Test
    public void defaultConstructorOfAnEnumClass() {
        List<MethodInfo> constructors = index.getClassByName(ImplicitDeclarationsExample.SimpleEnum.class).constructors();
        assertEquals(1, constructors.size());
        MethodInfo constructor = constructors.get(0);
        assertEquals(0, constructor.parametersCount());
        assertTrue(constructor.parameters().isEmpty());
        assertTrue(constructor.parameterTypes().isEmpty());
        assertEquals(2, constructor.descriptorParametersCount()); // both javac and ecj emit 2 synthetic parameters
    }

    // canonical constructor of a record class
    @Test
    public void canonicalConstructorOfARecordClass() {
        List<MethodInfo> constructors = index.getClassByName("test.ImplicitDeclarationsExampleSupplement$SimpleRecord")
                .constructors();
        assertEquals(1, constructors.size());
        MethodInfo constructor = constructors.get(0);
        assertEquals(2, constructor.parametersCount());
        assertEquals("java.lang.String", constructor.parameterType(0).name().toString());
        assertEquals("str", constructor.parameterName(0));
        assertEquals("int", constructor.parameterType(1).name().toString());
        assertEquals("num", constructor.parameterName(1));
        assertEquals(2, constructor.descriptorParametersCount());
        assertEquals("java.lang.String", constructor.descriptorParameterTypes().get(0).name().toString());
        assertEquals("int", constructor.descriptorParameterTypes().get(1).name().toString());

        DotName ann = DotName.createSimple("test.MyAnnotation");
        assertTrue(constructor.parameters().get(0).hasAnnotation(ann));
        assertTrue(constructor.parameters().get(0).type().hasAnnotation(ann));
        assertEquals("record: str", constructor.parameters().get(0).annotation(ann).value().asString());
        assertEquals("record: str", constructor.parameters().get(0).type().annotation(ann).value().asString());
        assertEquals(2, constructor.parameters().get(0).annotations().size());
        assertTrue(constructor.parameters().get(1).hasAnnotation(ann));
        assertTrue(constructor.parameters().get(1).type().hasAnnotation(ann));
        assertEquals("record: num", constructor.parameters().get(1).annotation(ann).value().asString());
        assertEquals("record: num", constructor.parameters().get(1).type().annotation(ann).value().asString());
        assertEquals(2, constructor.parameters().get(1).annotations().size());
    }

    // anonymous constructor of an anonymous class that inherits from a nested class
    @Test
    public void anonymousConstructorOfAnAnonymousClassThatInheritsFromANestedClass() {
        ClassInfo clazz = index.getClassByName(ImplicitDeclarationsExample.staticMethod());
        assertEquals(ImplicitDeclarationsExample.NestedClass.class.getName(), clazz.superName().toString());

        List<MethodInfo> constructors = clazz.constructors();
        assertEquals(1, constructors.size());
        MethodInfo constructor = constructors.get(0);
        assertEquals(1, constructor.parametersCount());
        assertEquals("int", constructor.parameterType(0).name().toString());
        assertEquals(1, constructor.descriptorParametersCount());
        assertEquals("int", constructor.descriptorParameterTypes().get(0).name().toString());
    }

    // anonymous constructor of an anonymous class that inherits from an inner class
    @Test
    public void anonymousConstructorOfAnAnonymousClassThatInheritsFromAnInnerClass() {
        ClassInfo clazz = index.getClassByName(obj.method());
        assertEquals(ImplicitDeclarationsExample.NonPrivateInnerClass.class.getName(), clazz.superName().toString());

        List<MethodInfo> constructors = clazz.constructors();
        assertEquals(1, constructors.size());
        MethodInfo constructor = constructors.get(0);
        assertNotNull(constructor);
    }

    // the `values` method of an enum class
    @Test
    public void theValuesMethodOfAnEnumClass() {
        List<MethodInfo> methods = index.getClassByName(ImplicitDeclarationsExample.SimpleEnum.class)
                .methods()
                .stream()
                .filter(it -> "values".equals(it.name()))
                .collect(Collectors.toList());
        assertEquals(1, methods.size());
        MethodInfo method = methods.get(0);
        assertEquals(0, method.parametersCount());
        assertEquals(0, method.descriptorParametersCount());
        assertEquals(ImplicitDeclarationsExample.SimpleEnum.class.getName() + "[]", method.returnType().toString());
    }

    // the `valueOf` method of an enum class
    @Test
    public void theValueOfMethodOfAnEnumClass() {
        List<MethodInfo> methods = index.getClassByName(ImplicitDeclarationsExample.SimpleEnum.class)
                .methods()
                .stream()
                .filter(it -> "valueOf".equals(it.name()))
                .collect(Collectors.toList());
        assertEquals(1, methods.size());
        MethodInfo method = methods.get(0);
        assertEquals(1, method.parametersCount());
        assertEquals(String.class.getName(), method.parameterType(0).name().toString());
        assertEquals(1, method.descriptorParametersCount());
        assertEquals(String.class.getName(), method.descriptorParameterTypes().get(0).name().toString());
        assertEquals(ImplicitDeclarationsExample.SimpleEnum.class.getName(), method.returnType().name().toString());
    }

    // public static fields of an enum class
    @Test
    public void publicStaticFieldsOfAnEnumClass() {
        ClassInfo clazz = index.getClassByName(ImplicitDeclarationsExample.SimpleEnum.class);

        {
            FieldInfo foo = clazz.field("FOO");
            assertNotNull(foo);
            assertTrue(foo.isEnumConstant());
            assertTrue(Modifier.isPublic(foo.flags()));
            assertTrue(Modifier.isStatic(foo.flags()));
            assertEquals(ImplicitDeclarationsExample.SimpleEnum.class.getName(), foo.type().name().toString());

            assertTrue(foo.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("enum: foo", foo.annotation(MyAnnotation.DOT_NAME).value().asString());
            if (!CompiledWith.ecj()) {
                // javac DOESN'T put the annotation on the _type_ of the implicitly declared field
                assertEquals(1, foo.annotations().size());
                assertTrue(foo.type().annotations().isEmpty());
            } else {
                // ecj DOES put the annotation on the _type_ of the implicitly declared field,
                // contrary to the `@Target` declaration
                assertEquals(2, foo.annotations().size());
                assertFalse(foo.type().annotations().isEmpty());
                assertTrue(foo.type().hasAnnotation(MyAnnotation.DOT_NAME));
                assertEquals("enum: foo", foo.type().annotation(MyAnnotation.DOT_NAME).value().asString());
            }
        }

        {
            FieldInfo bar = clazz.field("BAR");
            assertNotNull(bar);
            assertTrue(bar.isEnumConstant());
            assertTrue(Modifier.isPublic(bar.flags()));
            assertTrue(Modifier.isStatic(bar.flags()));
            assertEquals(ImplicitDeclarationsExample.SimpleEnum.class.getName(), bar.type().name().toString());

            assertTrue(bar.hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("enum: bar", bar.annotation(MyAnnotation.DOT_NAME).value().asString());
            if (!CompiledWith.ecj()) {
                // javac DOESN'T put the annotation on the _type_ of the implicitly declared field
                assertEquals(1, bar.annotations().size());
                assertTrue(bar.type().annotations().isEmpty());
            } else {
                // ecj DOES put the annotation on the _type_ of the implicitly declared field,
                // contrary to the `@Target` declaration
                assertEquals(2, bar.annotations().size());
                assertFalse(bar.type().annotations().isEmpty());
                assertTrue(bar.type().hasAnnotation(MyAnnotation.DOT_NAME));
                assertEquals("enum: bar", bar.type().annotation(MyAnnotation.DOT_NAME).value().asString());
            }
        }
    }

    // component fields of a record class
    @Test
    public void componentFieldsOfARecordClass() {
        ClassInfo clazz = index.getClassByName("test.ImplicitDeclarationsExampleSupplement$SimpleRecord");

        DotName ann = DotName.createSimple("test.MyAnnotation");

        {
            FieldInfo str = clazz.field("str");
            assertNotNull(str);
            assertTrue(Modifier.isPrivate(str.flags()));
            assertFalse(Modifier.isStatic(str.flags()));
            assertEquals("java.lang.String", str.type().name().toString());

            assertTrue(str.hasAnnotation(ann));
            assertTrue(str.type().hasAnnotation(ann));
            assertEquals("record: str", str.annotation(ann).value().asString());
            assertEquals("record: str", str.type().annotation(ann).value().asString());
            assertEquals(2, str.annotations().size());
        }

        {
            FieldInfo num = clazz.field("num");
            assertNotNull(num);
            assertTrue(Modifier.isPrivate(num.flags()));
            assertFalse(Modifier.isStatic(num.flags()));
            assertEquals("int", num.type().name().toString());

            assertTrue(num.hasAnnotation(ann));
            assertTrue(num.type().hasAnnotation(ann));
            assertEquals("record: num", num.annotation(ann).value().asString());
            assertEquals("record: num", num.type().annotation(ann).value().asString());
            assertEquals(2, num.annotations().size());
        }
    }

    // accessor methods of a record class
    @Test
    public void accessorMethodsOfARecordClass() {
        ClassInfo clazz = index.getClassByName("test.ImplicitDeclarationsExampleSupplement$SimpleRecord");

        DotName ann = DotName.createSimple("test.MyAnnotation");

        {
            MethodInfo str = clazz.firstMethod("str");
            assertNotNull(str);
            assertTrue(Modifier.isPublic(str.flags()));
            assertFalse(Modifier.isStatic(str.flags()));
            assertEquals("java.lang.String", str.returnType().name().toString());
            assertEquals(0, str.parametersCount());
            assertEquals(0, str.descriptorParametersCount());

            assertTrue(str.hasAnnotation(ann));
            assertTrue(str.returnType().hasAnnotation(ann));
            assertEquals("record: str", str.annotation(ann).value().asString());
            assertEquals("record: str", str.returnType().annotation(ann).value().asString());
            assertEquals(2, str.annotations().size());
        }

        {
            MethodInfo num = clazz.firstMethod("num");
            assertNotNull(num);
            assertTrue(Modifier.isPublic(num.flags()));
            assertFalse(Modifier.isStatic(num.flags()));
            assertEquals("int", num.returnType().name().toString());
            assertEquals(0, num.parametersCount());
            assertEquals(0, num.descriptorParametersCount());

            assertTrue(num.hasAnnotation(ann));
            assertTrue(num.returnType().hasAnnotation(ann));
            assertEquals("record: num", num.annotation(ann).value().asString());
            assertEquals("record: num", num.returnType().annotation(ann).value().asString());
            assertEquals(2, num.annotations().size());
        }
    }

    // java.lang.Object methods on interface (which we DON'T expect to see, as they are not present in bytecode)
    @Test
    public void javaLangObjectMethodsOnInterface() {
        ClassInfo clazz = index.getClassByName(ImplicitDeclarationsExample.SimpleInterface.class);

        assertNotNull(clazz.firstMethod("someMethod"));

        assertNull(clazz.firstMethod("equals"));
        assertNull(clazz.firstMethod("hashCode"));
        assertNull(clazz.firstMethod("toString"));

        assertNull(clazz.firstMethod("clone"));
        assertNull(clazz.firstMethod("finalize"));
        assertNull(clazz.firstMethod("getClass"));

        assertNull(clazz.firstMethod("notify"));
        assertNull(clazz.firstMethod("notifyAll"));
        assertNull(clazz.firstMethod("wait"));
    }

    // container annotation
    @Test
    public void containerAnnotation() {
        ClassInfo clazz = index.getClassByName(ImplicitDeclarationsExample.NestedClass.class);
        assertFalse(clazz.annotations().isEmpty());

        assertTrue(clazz.hasAnnotation(MyRepeatableAnnotation.List.DOT_NAME));
        assertNotNull(clazz.annotation(MyRepeatableAnnotation.List.DOT_NAME));
        assertNotNull(clazz.annotations(MyRepeatableAnnotation.List.DOT_NAME));
        assertFalse(clazz.annotations(MyRepeatableAnnotation.List.DOT_NAME).isEmpty());
        assertFalse(clazz.annotationsWithRepeatable(MyRepeatableAnnotation.List.DOT_NAME, index).isEmpty());
        assertEquals(1, clazz.annotationsWithRepeatable(MyRepeatableAnnotation.List.DOT_NAME, index).size());

        assertFalse(clazz.hasAnnotation(MyRepeatableAnnotation.DOT_NAME));
        assertNull(clazz.annotation(MyRepeatableAnnotation.DOT_NAME));
        assertNotNull(clazz.annotations(MyRepeatableAnnotation.DOT_NAME));
        assertTrue(clazz.annotations(MyRepeatableAnnotation.DOT_NAME).isEmpty());
        assertFalse(clazz.annotationsWithRepeatable(MyRepeatableAnnotation.DOT_NAME, index).isEmpty());
        assertEquals(2, clazz.annotationsWithRepeatable(MyRepeatableAnnotation.DOT_NAME, index).size());
    }

    //
    // implicit declarations that can be marked mandated (parameters)
    //

    // first parameter of a constructor of a non-private inner class
    @Test
    public void firstParameterOfAConstructorOfANonPrivateInnerClass() {
        List<MethodInfo> constructors = index.getClassByName(ImplicitDeclarationsExample.NonPrivateInnerClass.class)
                .constructors();
        assertEquals(2, constructors.size());

        {
            MethodInfo constructor = constructors.stream()
                    .filter(it -> it.parameterType(0).kind() == Type.Kind.PRIMITIVE)
                    .findAny()
                    .orElse(null);
            assertNotNull(constructor);

            assertEquals(1, constructor.parametersCount());
            assertEquals("int", constructor.parameterType(0).name().toString());
            assertEquals("i", constructor.parameterName(0));
            assertEquals(2, constructor.descriptorParametersCount());
            assertEquals(ImplicitDeclarationsExample.class.getName(),
                    constructor.descriptorParameterTypes().get(0).name().toString());
            assertEquals("int", constructor.descriptorParameterTypes().get(1).name().toString());

            assertFalse(constructor.parameters().get(0).annotations().isEmpty());
            assertTrue(constructor.parameters().get(0).hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("non-private inner class",
                    constructor.parameters().get(0).annotation(MyAnnotation.DOT_NAME).value().asString());
            assertTrue(constructor.parameters().get(0).type().hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("non-private inner class",
                    constructor.parameters().get(0).type().annotation(MyAnnotation.DOT_NAME).value().asString());
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
            assertEquals(ImplicitDeclarationsExample.class.getName(),
                    constructor.descriptorParameterTypes().get(0).name().toString());
            assertEquals(List.class.getName(), constructor.descriptorParameterTypes().get(1).name().toString());

            assertFalse(constructor.parameters().get(0).annotations().isEmpty());
            assertTrue(constructor.parameters().get(0).hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("non-private inner class <T>",
                    constructor.parameters().get(0).annotation(MyAnnotation.DOT_NAME).value().asString());
            assertTrue(constructor.parameters().get(0).type().hasAnnotation(MyAnnotation.DOT_NAME));
            assertEquals("non-private inner class <T>",
                    constructor.parameters().get(0).type().annotation(MyAnnotation.DOT_NAME).value().asString());
        }
    }

    @Test
    public void firstParameterOfAnAnonymousConstructorOfAnAnonymousClassThatInheritsFromAnInnerClass() {
        List<MethodInfo> constructors = index.getClassByName(obj.method()).constructors();
        assertEquals(1, constructors.size());
        MethodInfo constructor = constructors.get(0);
        assertNotNull(constructor);

        // javac emits 2 parameters, the enclosing instance and then the explicitly declared parameter
        //
        // ecj emits 3 parameters, 2x the enclosing instance and then the explicitly declared parameter
        //
        // technically, both are probably allowed by the JLS, because the superclass is an inner class
        // and so its constructor accept the enclosing instance and the explicitly declared parameter,
        // and an anonymous constructor is specified to also accept the enclosing instance
        //
        // Jandex only strips the 1st enclosing instance
        assertEquals(CompiledWith.ecj() ? 2 : 1, constructor.parametersCount());
        assertEquals("int", constructor.parameterType(CompiledWith.ecj() ? 1 : 0).name().toString());
        // intentionally _not_ testing parameter names, those are not defined in any way
        assertEquals(CompiledWith.ecj() ? 3 : 2, constructor.descriptorParametersCount());
        assertEquals(ImplicitDeclarationsExample.class.getName(),
                constructor.descriptorParameterTypes().get(0).name().toString());
        assertEquals("int", constructor.descriptorParameterTypes().get(CompiledWith.ecj() ? 2 : 1).name().toString());
    }

    // parameter `name` of the `valueOf` method which is implicitly declared in an enum
    @Test
    public void parameterNameOfTheValueofMethodImplicitlyDeclaredOnAnEnum() {
        MethodInfo method = index.getClassByName(ImplicitDeclarationsExample.SimpleEnum.class)
                .firstMethod("valueOf");
        assertEquals(1, method.parametersCount());
        assertEquals(String.class.getName(), method.parameterType(0).name().toString());
        if (method.parameterName(0) != null) {
            // ecj DOESN'T emit the parameter name, not even to the local variable table
            // plus we also ignore emitted parameter names for mandated/synthetic parameters
            assertEquals("name", method.parameterName(0));
        }
        assertEquals(1, method.descriptorParametersCount());
        assertEquals(String.class.getName(), method.descriptorParameterTypes().get(0).name().toString());
    }

    // parameters of a compact constructor of a record
    @Test
    public void parametersOfACompactConstructorOfARecord() {
        List<MethodInfo> constructors = index
                .getClassByName("test.ImplicitDeclarationsExampleSupplement$RecordWithCompactConstructor")
                .constructors();
        assertEquals(1, constructors.size());
        MethodInfo constructor = constructors.get(0);
        assertEquals(2, constructor.parametersCount());
        assertEquals("java.lang.String", constructor.parameterType(0).name().toString());
        assertEquals("str", constructor.parameterName(0));
        assertEquals("int", constructor.parameterType(1).name().toString());
        assertEquals("num", constructor.parameterName(1));
        assertEquals(2, constructor.descriptorParametersCount());
        assertEquals("java.lang.String", constructor.descriptorParameterTypes().get(0).name().toString());
        assertEquals("int", constructor.descriptorParameterTypes().get(1).name().toString());

        DotName ann = DotName.createSimple("test.MyAnnotation");
        assertTrue(constructor.parameters().get(0).hasAnnotation(ann));
        assertTrue(constructor.parameters().get(0).type().hasAnnotation(ann));
        assertEquals("record: str", constructor.parameters().get(0).annotation(ann).value().asString());
        assertEquals("record: str", constructor.parameters().get(0).type().annotation(ann).value().asString());
        assertEquals(2, constructor.parameters().get(0).annotations().size());
        assertTrue(constructor.parameters().get(1).hasAnnotation(ann));
        assertTrue(constructor.parameters().get(1).type().hasAnnotation(ann));
        assertEquals("record: num", constructor.parameters().get(1).annotation(ann).value().asString());
        assertEquals("record: num", constructor.parameters().get(1).type().annotation(ann).value().asString());
        assertEquals(2, constructor.parameters().get(1).annotations().size());
    }

    private Index buildIndex() {
        try {
            ClassLoader cl = ImplicitDeclarationsTest.class.getClassLoader();

            Indexer indexer = new Indexer();
            indexer.indexClass(ImplicitDeclarationsExample.class);
            indexer.indexClass(ImplicitDeclarationsExample.NestedClass.class);
            indexer.indexClass(ImplicitDeclarationsExample.NonPrivateInnerClass.class);
            indexer.indexClass(ImplicitDeclarationsExample.SimpleEnum.class);
            indexer.indexClass(ImplicitDeclarationsExample.SimpleInterface.class);
            indexer.indexClass(ImplicitDeclarationsExample.staticMethod());
            indexer.indexClass(obj.method());
            indexer.indexClass(MyAnnotation.class);
            indexer.indexClass(MyRepeatableAnnotation.class);
            indexer.indexClass(MyRepeatableAnnotation.List.class);

            indexer.index(
                    cl.getResourceAsStream("test/ImplicitDeclarationsExampleSupplement$RecordWithCompactConstructor.class"));
            indexer.index(cl.getResourceAsStream("test/ImplicitDeclarationsExampleSupplement$SimpleRecord.class"));

            Index index = indexer.complete();
            return IndexingUtil.roundtrip(index);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
