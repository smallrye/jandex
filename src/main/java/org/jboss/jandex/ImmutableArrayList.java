package org.jboss.jandex;

import java.util.AbstractList;

final class ImmutableArrayList<T> extends AbstractList<T> {
    private final T[] array;

    ImmutableArrayList(T[] array) {
        this.array = array;
    }

    @Override
    public T get(int index) {
        return array[index];
    }

    @Override
    public int size() {
        return array.length;
    }
}
