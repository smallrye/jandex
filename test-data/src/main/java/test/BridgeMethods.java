package test;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BridgeMethods {
    public class NestedConsumer implements Consumer<BridgeMethods.NestedConsumer> {
        @Override
        public void accept(BridgeMethods.@Nullable NestedConsumer nestedConsumer) {
        }
    }

    public static class ArrayWithNullableElementsConsumer
            implements Consumer<@Nullable Object[]> {
        @Override
        public void accept(@Nullable Object[] objects) {
        }
    }

    public static class NullableArrayConsumer
            implements Consumer<Object @Nullable []> {
        @Override
        public void accept(Object @Nullable [] objects) {
        }
    }

    public static class NullableArrayWithNullableElementsConsumer
            implements Consumer<@Nullable Object @Nullable []> {
        @Override
        public void accept(@Nullable Object @Nullable [] objects) {
        }
    }

    public static class ArrayWithNullableElementsSupplier
            implements Supplier<@Nullable Object[]> {
        @Override
        public @Nullable Object[] get() {
            return new Object[0];
        }
    }

    public static class NullableArraySupplier
            implements Supplier<Object @Nullable []> {
        @Override
        public Object @Nullable [] get() {
            return null;
        }
    }

    public static class Superclass {
        public <@Untainted E extends @Nullable Object> Collection<E> typeVariable() {
            return null;
        }

        public Collection<@Untainted ? extends @Nullable Object> wildcard() {
            return null;
        }
    }

    public static class Subclass extends Superclass {
        @Override
        public <@Untainted E extends @Nullable Object> Set<E> typeVariable() {
            return null;
        }

        @Override
        public Set<@Untainted ? extends @Nullable Object> wildcard() {
            return null;
        }
    }
}
