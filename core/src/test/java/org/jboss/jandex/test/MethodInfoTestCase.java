package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;

public class MethodInfoTestCase {
    // note that a lot of the example classes have a default constructor

    static class Example1 {
        // compiles to the `ConstantValue` attribute, no static initializer
        static final String str = "";
    }

    static class Example2 {
        // not a constant, compiles to a static initializer
        static String str = "";
    }

    static class Example3 {
        static final String str;

        static {
            str = "";
        }
    }

    static class Example4 {
        static final String str;

        static {
            str = "";
        }

        static final int i = 0;
    }

    // the 2 static initializers are merged into 1
    static class Example5 {
        static final String str;

        static {
            str = "";
        }

        static final int i;

        static {
            i = 0;
        }
    }

    static class Example6 {
        static final String str = "";

        final int i = 0;
    }

    static class Example7 {
        static final String str;

        static {
            str = "";
        }

        final int i;

        Example7() {
            i = 0;
        }
    }

    static class Example8 {
        static final String str = "";

        final int i;

        {
            i = 0;
        }
    }

    // the instance initializer is folded into the constructor
    static class Example9 {
        static final String str;

        static {
            str = "";
        }

        final int i;

        Example9() {
        }

        {
            i = 0;
        }
    }

    // the instance initializers are folded into the default constructor
    static class Example10 {
        final String str;

        {
            str = "";
        }

        final int i;

        {
            i = 0;
        }
    }

    static class Example11 {
        Example11() {
        }

        Example11(int i) {
        }
    }

    static class Example12 {
        static final String str;

        static {
            str = "";
        }

        Example12() {
        }

        Example12(int i) {
        }

        Example12(String str) {
        }
    }

    @Test
    public void testIsStaticInitializerAndConstructor() throws IOException {
        verify(Index.singleClass(Example1.class), 0, 1);
        verify(Index.singleClass(Example2.class), 1, 1);
        verify(Index.singleClass(Example3.class), 1, 1);
        verify(Index.singleClass(Example4.class), 1, 1);
        verify(Index.singleClass(Example5.class), 1, 1);
        verify(Index.singleClass(Example6.class), 0, 1);
        verify(Index.singleClass(Example7.class), 1, 1);
        verify(Index.singleClass(Example8.class), 0, 1);
        verify(Index.singleClass(Example9.class), 1, 1);
        verify(Index.singleClass(Example10.class), 0, 1);
        verify(Index.singleClass(Example11.class), 0, 2);
        verify(Index.singleClass(Example12.class), 1, 3);
    }

    private void verify(ClassInfo clazz, int expectedStaticInitializers, int expectedConstructors) {
        int staticInitializers = 0;
        int constructors = 0;
        for (MethodInfo method : clazz.methods()) {
            if (method.isStaticInitializer()) {
                staticInitializers++;
            }
            if (method.isConstructor()) {
                constructors++;
            }
        }

        assertEquals(expectedStaticInitializers, staticInitializers);
        assertEquals(expectedConstructors, constructors);
    }
}
