package org.jboss.jandex.test;

import java.util.List;

// commonly encountered synthetic parameters (other synthetic declarations, such as fields
// or methods, are not a concern)
//
// these declarations are artifacts of compiler implementation and are not mandated
// by the Java Language Specification (unlike implicit declarations)
public class SyntheticDeclarationsExample {
    public SyntheticDeclarationsExample prepare() {
        staticMethod("static");
        method("non-static");
        privateInnerClass = PrivateInnerClass.class;
        return this;
    }

    static Class<?> localClassInStaticContext;
    Class<?> localClassNotInStaticContext;
    Class<?> privateInnerClass;

    private static void staticMethod(String str) {
        class LocalClassInStaticContext {
            // additional parameter at the end (captured variable) is synthesised
            LocalClassInStaticContext(int num) {
                String capture = str + num;
            }

            // additional parameter at the end (captured variable) is synthesised
            LocalClassInStaticContext(Object ignored, @MyAnnotation("static local class") Integer num) {
                String capture = str + num;
            }

            // additional parameter at the end (captured variable) is synthesised
            // the type parameter just enforces emitting the generic signature
            <T> LocalClassInStaticContext(@MyAnnotation("static local class <T>") List<T> list) {
                String capture = str + list;
            }
        }

        localClassInStaticContext = LocalClassInStaticContext.class;
    }

    private void method(String str) {
        class LocalClassNotInStaticContext {
            // additional parameters at the beginning (instance of the enclosing class)
            // and at the end (captured variable) are synthesised
            LocalClassNotInStaticContext(int num) {
                String capture = str + num;
            }

            // additional parameters at the beginning (instance of the enclosing class)
            // and at the end (captured variable) are synthesised
            LocalClassNotInStaticContext(Object ignored, @MyAnnotation("local class") Integer num) {
                String capture = str + num;
            }

            // additional parameters at the beginning (instance of the enclosing class)
            // and at the end (captured variable) are synthesised
            // the type parameter just enforces emitting the generic signature
            <T> LocalClassNotInStaticContext(@MyAnnotation("local class <T>") List<T> list) {
                String capture = str + list;
            }
        }

        localClassNotInStaticContext = LocalClassNotInStaticContext.class;
    }

    private class PrivateInnerClass {
        // additional parameter at the beginning (instance of the enclosing class) is synthesised
        private PrivateInnerClass(@MyAnnotation("private inner class") int i) {
        }
    }

    public enum EnumWithConstructor {
        FOO("foo", 1),
        BAR("bar", 2),
        ;

        // additional parameters at the beginning are synthesised
        EnumWithConstructor(@MyAnnotation("enum: str") String str, @MyAnnotation("enum: num") int num) {
        }
    }
}
