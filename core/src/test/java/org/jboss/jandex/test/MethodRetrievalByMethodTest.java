package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.Arrays;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;

public class MethodRetrievalByMethodTest {
    static class Grandparent {
        public void doSomething(String p1, int p2) {
        }

        public void doSomething(Object p) {
        }

        private Number hello() {
            return 1;
        }
    }

    static class Parent extends Grandparent {
        @Override
        public void doSomething(String p1, int p2) {
        }

        public void doSomething(int p1, String p2) {
        }

        private Integer hello() {
            return 2;
        }
    }

    static class Child extends Parent {
        @Override
        public void doSomething(String p1, int p2) {
        }

        private Long hello() {
            return 3L;
        }
    }

    @Test
    void test() throws Exception {
        Index index = Index.of(Grandparent.class, Parent.class, Child.class);

        ClassInfo childClazz = index.getClassByName(Child.class);

        MethodInfo doSomething = childClazz.firstMethod("doSomething");
        MethodInfo hello = childClazz.firstMethod("hello");

        for (Class<?> ancestor : Arrays.asList(Parent.class, Grandparent.class)) {
            ClassInfo ancestorClazz = index.getClassByName(ancestor);

            MethodInfo ancestorDoSomething = ancestorClazz.method(doSomething);
            assertNotNull(ancestorDoSomething);
            assertNotSame(doSomething, ancestorDoSomething);
            assertEquals("doSomething", ancestorDoSomething.name());
            assertEquals(String.class.getName(), ancestorDoSomething.parameterType(0).name().toString());
            assertEquals("int", ancestorDoSomething.parameterType(1).name().toString());

            MethodInfo ancestorHello = ancestorClazz.method(hello);
            assertNotNull(ancestorHello);
            assertNotSame(hello, ancestorHello);
            assertEquals("hello", ancestorHello.name());
            assertEquals(0, ancestorHello.parametersCount());
        }
    }
}
