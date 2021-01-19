package test;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TypeParameterBoundExample {
    // extends Runnable produces T::List signature, so
    // @Nullable targets index=1 (optional class bound always counts)
    public static class ListConsumer<T extends List<?>>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }

    // extends Runnable produces T:ArrayList signature since ArrayList is class
    public static class ArrayListConsumer<T extends ArrayList<?>>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }

    // extends Runnable produces T:ArrayList signature since ArrayList is class
    public static class SerializableListConsumer<T extends @Nullable List<?> & Serializable>
            implements Consumer<T> {
        @Override
        public void accept(T t) {
        }
    }
}
