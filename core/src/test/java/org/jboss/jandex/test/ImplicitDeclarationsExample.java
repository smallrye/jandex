package org.jboss.jandex.test;

import java.util.List;

// implicit declarations, including implicitly declared (aka mandated) parameters (JLS 13.1)
//
// these declarations, albeit not visible in the source code, are required to be present
// by the Java Language Specification (unlike synthetic declarations, which are artifacts
// of compiler implementation)
public class ImplicitDeclarationsExample {
    // implicitly declares a default constructor

    // implicitly declares a container annotation
    @MyRepeatableAnnotation("foo")
    @MyRepeatableAnnotation("bar")
    public static class NestedClass {
        NestedClass(int i) {
        }
    }

    public class NonPrivateInnerClass {
        // implicitly declares an additional first parameter (instance of the enclosing class)
        NonPrivateInnerClass(@MyAnnotation("non-private inner class") int i) {
        }

        // also implicitly declares an additional first parameter (instance of the enclosing class)
        // the type parameter just enforces emitting the generic signature
        <T> NonPrivateInnerClass(@MyAnnotation("non-private inner class <T>") List<T> list) {
        }
    }

    public static Class<?> staticMethod() {
        return new NestedClass(1) {
            // implicitly declares an anonymous constructor
        }.getClass();
    }

    public Class<?> method() {
        return new NonPrivateInnerClass(1) {
            // implicitly declares an anonymous constructor
            // that constructor implicitly declares an additional first parameter (instance of the enclosing class)
        }.getClass();
    }

    public enum SimpleEnum {
        @MyAnnotation("enum: foo")
        FOO,
        @MyAnnotation("enum: bar")
        BAR,

        // implicitly declares a default constructor, enum constant fields, and the `values` and `valueOf` methods

        // the `valueOf` method also implicitly declares a parameter `name`
    }

    public interface SimpleInterface {
        // implicitly declares methods from java.lang.Object
        // note that we DON'T want those to be present in Jandex, just like they are not present in bytecode

        void someMethod();
    }
}
