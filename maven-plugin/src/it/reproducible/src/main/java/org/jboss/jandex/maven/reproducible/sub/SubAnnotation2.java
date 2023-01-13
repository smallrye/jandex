/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.jandex.maven.reproducible.sub;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

public class SubAnnotation2 {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FieldAnnotation {

    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ParameterAnnotation {

    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {
        String name();
        int[] ints();
        String other() default "something";
        String override() default "override-me";

        long longValue();
        Class<?> klass();
        NestedAnnotation nested();
        ElementType[] enums();
        NestedAnnotation[] nestedArray();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation1 {}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation2 {}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation3 {}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation4 {}

    public @interface NestedAnnotation {
        float value();
    }

    @TestAnnotation(name = "Test", override = "somethingelse", ints = { 1, 2, 3, 4, 5 }, klass = Void.class, nested = @NestedAnnotation(1.34f), nestedArray = {
            @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) }, enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
    public class DummyClass implements Serializable {
        void doSomething(int x, long y, Long foo){}
        void doSomething(int x, long y){}

        @FieldAnnotation
        private int x;

        @MethodAnnotation1
        @MethodAnnotation2
        @MethodAnnotation4
        void doSomething(int x, long y, String foo){}

        public class Nested {
            public Nested(int noAnnotation) {}
            public Nested(@ParameterAnnotation byte annotated) {}
        }
    }

    public enum Enum {
        A(1), B(2);

        private Enum(int noAnnotation) {}
        private Enum(@ParameterAnnotation byte annotated) {}
    }

    @TestAnnotation(name = "Test", ints = { 1, 2, 3, 4, 5 }, klass = Void.class, nested = @NestedAnnotation(1.34f), nestedArray = {
        @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) }, enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
    public static class NestedA implements Serializable {
    }

    @TestAnnotation(name = "Test", ints = { 1, 2, 3, 4, 5 }, klass = Void.class, nested = @NestedAnnotation(1.34f), nestedArray = {
        @NestedAnnotation(3.14f), @NestedAnnotation(2.27f) }, enums = { ElementType.TYPE, ElementType.PACKAGE }, longValue = 10)
    public static class NestedB implements Serializable {

        NestedB(Integer foo) {
        }
    }

    public static class NestedC implements Serializable {
    }

    public class NestedD implements Serializable {
    }

    public static class NoEnclosureAnonTest {
        static Class<?> anonymousStaticClass;
        Class<?> anonymousInnerClass;

        static {
            anonymousStaticClass = new Object() {}.getClass();
        }
        {
            anonymousInnerClass = new Object() {}.getClass();
        }
    }

    public static class ApiClass {
        public static void superApi() {}
    }

    public static class ApiUser {
        public void f() {
            ApiClass.superApi();
        }
    }
}
