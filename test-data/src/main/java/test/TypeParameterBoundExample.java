package test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TypeParameterBoundExample {
    // extends Runnable produces T::List signature, so
    // @Nullable targets index=1 (optional class bound always counts)
    public static class ListConsumer<T extends @Nullable List<?>>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }

    // extends Runnable produces T:ArrayList signature since ArrayList is class
    public static class ArrayListConsumer<T extends @Nullable ArrayList<?>>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }

    // extends Runnable produces T:ArrayList signature since ArrayList is class
    public static class SerializableListConsumer<T extends @Nullable List<?> & @Untainted Serializable>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }

    // extends Runnable produces T:ArrayList signature since ArrayList is class
    public static class SerializableListConsumerDoubleA<T extends @Nullable @Untainted List<?> & @Untainted Serializable>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }

    class Nest1<T> {
        class Nest2<X> {
            class Nest3<Y> {
                public void foo() {
                    new Consumer<@Nullable Object[]>() {
                        public void accept(@Nullable Object[] objects) {
                            new Nest1<String>.Nest2<Object[]>.Nest3<@Nullable Integer>() {

                            };
                            new Nest1<String>.Nest2<@Nullable Object[]>.Nest3<Integer>() {

                            };
                        }
                    };
                }
            }
        }
    }

    public class IteratorSupplier implements Supplier<Consumer<@Nullable Object[]>> {
        @Override
        public Consumer<@Nullable Object[]> get() {
            return new Consumer<@Nullable Object[]>() {
                @Override
                public void accept(@Nullable Object[] objects) {
                }
            };
        }
    }
}
