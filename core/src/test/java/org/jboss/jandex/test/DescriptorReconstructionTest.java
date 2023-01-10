package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.Descriptor;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DescriptorReconstructionTest {
    static class TestData<A, B extends Number, C extends B, D extends C, E extends Comparable<E>> {
        boolean booleanField;
        int intField;
        double doubleField;
        String stringField;
        List<String> listField;
        Map<String, List<String>> mapField;
        A typeVarWithoutBoundField;
        B typeVarWithBoundField;
        D typeVarWithTransitiveBoundField;
        E recursiveTypeVarField;

        void voidMethod(boolean booleanParam, List<String> listParam) {
        }

        String stringMethod(int intParam, Map<String, List<String>> mapParam, A typeVarParam) {
            return null;
        }

        B typeVarMethod(double doubleParam, C typeVarParam, List<? extends Number> wildcardParam) {
            return null;
        }

        <X> X genericMethodWithoutBound(X typeParamParam, D typeVarParam, List<? super Integer> wildcardParam) {
            return null;
        }

        <X extends Number> X genericMethodWithBound(X typeParamParam, List<? super C> wildcardParam) {
            return null;
        }
    }

    private static IndexView index;

    @BeforeAll
    public static void setUp() throws IOException {
        index = Index.of(TestData.class);
    }

    @Test
    public void test() {
        assertEquals("Lorg/jboss/jandex/test/DescriptorReconstructionTest$TestData;",
                index.getClassByName(TestData.class).descriptor());

        assertFieldDescriptor("booleanField", "Z");
        assertFieldDescriptor("intField", "I");
        assertFieldDescriptor("doubleField", "D");
        assertFieldDescriptor("stringField", "Ljava/lang/String;");
        assertFieldDescriptor("listField", "Ljava/util/List;");
        assertFieldDescriptor("mapField", "Ljava/util/Map;");
        assertFieldDescriptor("typeVarWithoutBoundField", "Ljava/lang/Object;");
        assertFieldDescriptor("typeVarWithBoundField", "Ljava/lang/Number;");
        assertFieldDescriptor("typeVarWithTransitiveBoundField", "Ljava/lang/Number;");
        assertFieldDescriptor("recursiveTypeVarField", "Ljava/lang/Comparable;");

        assertMethodDescriptor("voidMethod",
                "(ZLjava/util/List;)V");
        assertMethodDescriptor("stringMethod",
                "(ILjava/util/Map;Ljava/lang/Object;)Ljava/lang/String;");
        assertMethodDescriptor("typeVarMethod",
                "(DLjava/lang/Number;Ljava/util/List;)Ljava/lang/Number;");
        assertMethodDescriptor("genericMethodWithoutBound",
                "(Ljava/lang/Object;Ljava/lang/Number;Ljava/util/List;)Ljava/lang/Object;");
        assertMethodDescriptor("genericMethodWithBound",
                "(Ljava/lang/Number;Ljava/util/List;)Ljava/lang/Number;");
    }

    @Test
    public void withSubstitution() {
        assertFieldDescriptor("typeVarWithoutBoundField", "Ljava/lang/String;",
                id -> "A".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
        assertFieldDescriptor("typeVarWithBoundField", "Ljava/lang/String;",
                id -> "B".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
        assertFieldDescriptor("typeVarWithTransitiveBoundField", "Ljava/lang/Number;",
                id -> null);
        assertFieldDescriptor("recursiveTypeVarField", "Ljava/lang/Comparable;",
                id -> null);

        assertMethodDescriptor("typeVarMethod",
                "(DLjava/lang/String;Ljava/util/List;)Ljava/lang/String;",
                id -> "B".equals(id) || "C".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
        assertMethodDescriptor("genericMethodWithoutBound",
                "(Ljava/lang/String;Ljava/lang/Number;Ljava/util/List;)Ljava/lang/String;",
                id -> "X".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
        assertMethodDescriptor("genericMethodWithBound",
                "(Ljava/lang/String;Ljava/util/List;)Ljava/lang/String;",
                id -> "X".equals(id) ? ClassType.create(DotName.STRING_NAME) : null);
    }

    private static void assertFieldDescriptor(String name, String expectedDescriptor) {
        assertFieldDescriptor(name, expectedDescriptor, Descriptor.NO_SUBSTITUTION);
    }

    private static void assertFieldDescriptor(String name, String expectedDescriptor, Function<String, Type> subst) {
        ClassInfo clazz = index.getClassByName(TestData.class);
        FieldInfo field = clazz.field(name);
        assertEquals(expectedDescriptor, field.descriptor(subst));
    }

    private static void assertMethodDescriptor(String name, String expectedDescriptor) {
        assertMethodDescriptor(name, expectedDescriptor, Descriptor.NO_SUBSTITUTION);
    }

    private static void assertMethodDescriptor(String name, String expectedDescriptor, Function<String, Type> subst) {
        ClassInfo clazz = index.getClassByName(TestData.class);
        MethodInfo method = clazz.firstMethod(name);
        assertEquals(expectedDescriptor, method.descriptor(subst));
    }
}
