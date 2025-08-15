package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodSignatureKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MethodSignatureKeyTest {
    private Index index;

    @BeforeEach
    public void setUp() throws IOException {
        index = Index.of(SomeClass.class, SuperClass.class, SuperSuperClass.class, TheRoot.class);
    }

    private Set<MethodSignatureKey> allMethodsOf(Class<?>... classes) {
        return Arrays.stream(classes)
                .map(index::getClassByName)
                .map(ClassInfo::methods)
                .flatMap(List::stream)
                .map(MethodInfo::signatureKey)
                .collect(Collectors.toSet());
    }

    @Test
    public void shouldFindDirectOverriding() {
        Set<MethodSignatureKey> methods = allMethodsOf(SomeClass.class);

        MethodInfo parentMethod = index.getClassByName(SuperClass.class).firstMethod("fromSuperClass");

        assertTrue(methods.contains(parentMethod.signatureKey()));
    }

    @Test
    public void shouldFindGenericOverriding() {
        Set<MethodSignatureKey> methods = allMethodsOf(SomeClass.class, SuperClass.class);

        MethodInfo genericMethod = index.getClassByName(SuperSuperClass.class).firstMethod("generic");

        assertTrue(methods.contains(genericMethod.signatureKey()));
    }

    @Test
    public void shouldNotFindNonOverriddenFromSuperClass() {
        Set<MethodSignatureKey> methods = allMethodsOf(SomeClass.class);

        MethodInfo parentMethod = index.getClassByName(SuperClass.class).firstMethod("notOverriddenFromSuperClass");

        assertFalse(methods.contains(parentMethod.signatureKey()));
    }

    @Test
    public void shouldNotFindNonGenericNonOverriddenFromSuperSuperClass() {
        Set<MethodSignatureKey> methods = allMethodsOf(SomeClass.class, SuperClass.class);

        MethodInfo parentMethod = index.getClassByName(SuperSuperClass.class).firstMethod("notOverriddenNonGeneric");

        assertFalse(methods.contains(parentMethod.signatureKey()));
    }

    @Test
    public void shouldNotFindGenericNonOverriddenFromSuperSuperClass() {
        Set<MethodSignatureKey> methods = allMethodsOf(SomeClass.class, SuperClass.class);

        MethodInfo parentMethod = index.getClassByName(SuperSuperClass.class).firstMethod("notOverriddenGeneric");

        assertFalse(methods.contains(parentMethod.signatureKey()));
    }

    @Test
    public void shouldNotFindAlmostMatchingGeneric() {
        Set<MethodSignatureKey> methods = allMethodsOf(SomeClass.class, SuperClass.class);

        MethodInfo parentMethod = index.getClassByName(SuperSuperClass.class).firstMethod("almostMatchingGeneric");

        assertFalse(methods.contains(parentMethod.signatureKey()));
    }

    @Test
    public void shouldFindOverriddenInTheMiddleOfHierarchy() {
        Set<MethodSignatureKey> methods = allMethodsOf(SomeClass.class, SuperClass.class, SuperSuperClass.class);

        MethodInfo parentMethod = index.getClassByName(TheRoot.class).firstMethod("generic");

        assertTrue(methods.contains(parentMethod.signatureKey()));
    }

    public static class SomeClass extends SuperClass<Boolean> {
        @Override
        void generic(Integer param) {
        }

        @Override
        void nonGeneric(String param) {
        }

        @Override
        void fromSuperClass(int param) {
        }
    }

    public static class SuperClass<V> extends SuperSuperClass<Integer, V> {
        void fromSuperClass(int param) {
        }

        void notOverriddenFromSuperClass(int param) {
        }

        void almostMatchingGeneric(V param) {
        }
    }

    public static class SuperSuperClass<V, U> extends TheRoot<String, U, V> {
        void generic(V arg) {
        }

        void almostMatchingGeneric(Integer arg) {
        }

        void nonGeneric(String param) {
        }

        void notOverriddenGeneric(V arg) {
        }

        void notOverriddenNonGeneric(String param) {
        }
    }

    public static class TheRoot<U, V, X> {
        void generic(X param) {
        }
    }
}
