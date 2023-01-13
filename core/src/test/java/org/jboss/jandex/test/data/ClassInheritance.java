package org.jboss.jandex.test.data;

import java.io.Serializable;

public class ClassInheritance {
    public static class PlainClass {
    }

    public static class ParamClass<T> {
    }

    public static class ParamBoundClass<T extends Number & Comparable<Integer>> {
    }

    public interface PlainInterface {
    }

    public interface ParamInterface<T> {
    }

    public interface ParamBoundInterface<T extends Comparable<Integer> & Serializable> {
    }

    // ---

    public static class PlainClassExtendsPlainClass
            extends PlainClass {
    }

    public static class PlainClassExtendsParamClass
            extends ParamClass<String> {
    }

    public static class PlainClassExtendsParamBoundClass
            extends ParamBoundClass<Integer> {
    }

    public static class ParamClassExtendsPlainClass<T>
            extends PlainClass {
    }

    public static class ParamClassExtendsParamClass1<T>
            extends ParamClass<String> {
    }

    public static class ParamClassExtendsParamClass2<T>
            extends ParamClass<T> {
    }

    public static class ParamClassExtendsParamBoundClass<T>
            extends ParamBoundClass<Integer> {
    }

    public static class ParamBoundClassExtendsPlainClass<T extends Integer & Comparable<Integer>>
            extends PlainClass {
    }

    public static class ParamBoundClassExtendsParamClass1<T extends Integer & Comparable<Integer>>
            extends ParamClass<String> {
    }

    public static class ParamBoundClassExtendsParamClass2<T extends Integer & Comparable<Integer>>
            extends ParamClass<T> {
    }

    public static class ParamBoundClassExtendsParamBoundClass1<T extends Integer & Comparable<Integer>>
            extends ParamBoundClass<Integer> {
    }

    public static class ParamBoundClassExtendsParamBoundClass2<T extends Integer & Comparable<Integer>>
            extends ParamBoundClass<T> {
    }

    // ---

    public static class PlainClassImplementsPlainInterface
            implements PlainInterface {
    }

    public static class PlainClassImplementsParamInterface
            implements ParamInterface<String> {
    }

    public static class PlainClassImplementsParamBoundInterface
            implements ParamBoundInterface<Integer> {
    }

    public static class ParamClassImplementsPlainInterface<T>
            implements PlainInterface {
    }

    public static class ParamClassImplementsParamInterface1<T>
            implements ParamInterface<String> {
    }

    public static class ParamClassImplementsParamInterface2<T>
            implements ParamInterface<T> {
    }

    public static class ParamClassImplementsParamBoundInterface<T>
            implements ParamBoundInterface<Integer> {
    }

    public static class ParamBoundClassImplementsPlainInterface<T extends Integer>
            implements PlainInterface {
    }

    public static class ParamBoundClassImplementsParamInterface1<T extends Integer>
            implements ParamInterface<String> {
    }

    public static class ParamBoundClassImplementsParamInterface2<T extends Integer>
            implements ParamInterface<T> {
    }

    public static class ParamBoundClassImplementsParamBoundInterface1<T extends Integer>
            implements ParamBoundInterface<Integer> {
    }

    public static class ParamBoundClassImplementsParamBoundInterface2<T extends Integer>
            implements ParamBoundInterface<T> {
    }

    // ---

    public static class PlainClassExtendsPlainClassImplementsPlainInterface
            extends PlainClass
            implements PlainInterface {
    }

    public static class PlainClassExtendsParamClassImplementsParamInterface
            extends ParamClass<String>
            implements ParamInterface<String> {
    }

    public static class PlainClassExtendsParamBoundClassImplementsParamBoundInterface
            extends ParamBoundClass<Integer>
            implements ParamBoundInterface<Integer> {
    }

    public static class ParamClassExtendsPlainClassImplementsPlainInterface<T>
            extends PlainClass
            implements PlainInterface {
    }

    public static class ParamClassExtendsParamClassImplementsParamInterface1<T>
            extends ParamClass<String>
            implements ParamInterface<String> {
    }

    public static class ParamClassExtendsParamClassImplementsParamInterface2<T>
            extends ParamClass<T>
            implements ParamInterface<T> {
    }

    public static class ParamClassExtendsParamBoundClassImplementsParamBoundInterface<T>
            extends ParamBoundClass<Integer>
            implements ParamBoundInterface<Integer> {
    }

    public static class ParamBoundClassExtendsPlainClassImplementsPlainInterface<T extends Integer & Comparable<Integer>>
            extends PlainClass
            implements PlainInterface {
    }

    public static class ParamBoundClassExtendsParamClassImplementsParamInterface1<T extends Integer & Comparable<Integer>>
            extends ParamClass<String>
            implements ParamInterface<String> {
    }

    public static class ParamBoundClassExtendsParamClassImplementsParamInterface2<T extends Integer & Comparable<Integer>>
            extends ParamClass<T>
            implements ParamInterface<T> {
    }

    public static class ParamBoundClassExtendsParamBoundClassImplementsParamBoundInterface1<T extends Integer & Comparable<Integer>>
            extends ParamBoundClass<Integer>
            implements ParamBoundInterface<Integer> {
    }

    public static class ParamBoundClassExtendsParamBoundClassImplementsParamBoundInterface2<T extends Integer & Comparable<Integer>>
            extends ParamBoundClass<T>
            implements ParamBoundInterface<T> {
    }

    // ---

    public static class OuterParam<T extends Serializable> {
        public interface NestedParam<U> {
        }

        public class InnerParam<U extends Number> {
            public class InnerInnerRaw {
                public class InnerInnerInnerParam<V> {
                    public class Test<X extends String, Y extends Integer>
                            extends OuterParam<X>.InnerParam<Y>.InnerInnerRaw.InnerInnerInnerParam<String>
                            implements NestedParam<V> {
                    }
                }
            }
        }
    }
}
