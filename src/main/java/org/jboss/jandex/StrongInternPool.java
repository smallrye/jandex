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

package org.jboss.jandex;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A strong intern pool. The pool acts as a set where the first stored entry can be retrieved.
 * This can be used to conserve memory by eliminating duplicate objects (those that are equal
 * but have different identity). It however holds strong references to every item in the pool,
 * so it must be cleared to allow for GC.
 *
 * Note: It is very important to use a smaller load factor than you normally
 * would for HashSet, since the implementation is open-addressed with linear
 * probing. With a 50% load-factor a get is expected to return in only 2 probes.
 * However, a 90% load-factor is expected to return in around 50 probes.
 *
 * @author Jason T. Greene
 */
class StrongInternPool<E> implements Cloneable, Serializable {
    /**
     * Marks null keys.
     */
    private static final Object NULL = new Object();

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = 10929568968762L;

    /**
     * Same default as HashMap, must be a power of 2
     */
    private static final int DEFAULT_CAPACITY = 8;

    /**
     * MAX_INT - 1
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 67%, just like IdentityHashMap
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.67f;

    /**
     * The open-addressed table
     */
    private transient Object[] table;

    /**
     * The current number of key-value pairs
     */
    private transient int size;

    /**
     * The next resize
     */
    private transient int threshold;

    /**
     * The user defined load factor which defines when to resize
     */
    private final float loadFactor;

    /**
     * Counter used to detect changes made outside of an iterator
     */
    private transient int modCount;

    /**
     * Cache for an index
     */
    private transient Index index;

    public StrongInternPool(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Can not have a negative size table!");

        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;

        if (!(loadFactor > 0F && loadFactor <= 1F))
            throw new IllegalArgumentException("Load factor must be greater than 0 and less than or equal to 1");

        this.loadFactor = loadFactor;
        init(initialCapacity, loadFactor);
    }


    private void init(int initialCapacity, float loadFactor) {
        int c = 1;
        for (; c < initialCapacity; c <<= 1);
        threshold = (int) (c * loadFactor);

        // Include the load factor when sizing the table for the first time
        if (initialCapacity > threshold && c < MAXIMUM_CAPACITY) {
            c <<= 1;
            threshold = (int) (c * loadFactor);
        }

        this.table = new Object[c];
    }

    private static boolean eq(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }

        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals((Object[])o1, (Object[])o2);
        }

        if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals((byte[])o1, (byte[])o2);
        }

        return o1 != null && o1.equals(o2);
    }

    public StrongInternPool(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public StrongInternPool() {
        this(DEFAULT_CAPACITY);
    }

    // The normal bit spreader...
    private static int hash(Object o) {
        int h = o instanceof Object[] ? Arrays.hashCode((Object[])o) : o instanceof byte[] ? Arrays.hashCode((byte[])o) : o.hashCode();
        return ((h << 1 ) - (h << 8));
    }

    @SuppressWarnings("unchecked")
    private static final <K> K maskNull(K key) {
        return key == null ? (K) NULL : key;
    }

    private static final <K> K unmaskNull(K key) {
        return key == NULL ? null : key;
    }

    private int nextIndex(int index, int length) {
        index = (index >= length - 1) ? 0 : index + 1;
        return index;
    }

    private static final int index(int hashCode, int length) {
        return hashCode & (length - 1);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object entry) {
        entry = maskNull(entry);

        int hash = hash(entry);
        int length = table.length;
        int index = index(hash, length);

        for (int start = index;;) {
            Object e = table[index];
            if (e == null)
                return false;

            if (eq(entry,e))
                return true;

            index = nextIndex(index, length);
            if (index == start) // Full table
                return false;
        }
    }

    private int offset(Object entry) {
        entry = maskNull(entry);

        int hash = hash(entry);
        int length = table.length;
        int index = index(hash, length);

        for (int start = index;;) {
            Object e = table[index];
            if (e == null)
                return -1;

            if (eq(entry,e))
                return index;

            index = nextIndex(index, length);
            if (index == start) // Full table
                return -1;
        }
    }

    /***
     * Internalizes the specified object by always returning the first ever stored.
     * Equivalent objects (via .equals) but with different identity (aka duplicates)
     * can be eliminated with this method.
     *
     * @param entry the object to internalize
     * @return the one true unique (and equivalent) object
     */
    @SuppressWarnings("unchecked")
    public E intern(E entry) {
        entry = maskNull(entry);

        Object[] table = this.table;
        int hash = hash(entry);
        int length = table.length;
        int index = index(hash, length);

        for (int start = index;;) {
            Object e = table[index];
            if (e == null)
                break;

            if (eq(entry, e))
                return (E)unmaskNull(e);

            index = nextIndex(index, length);
            if (index == start)
                throw new IllegalStateException("Table is full!");
        }

        modCount++;
        table[index] = entry;
        if (++size >= threshold)
            resize(length);

        return unmaskNull(entry);
    }

    private void resize(int from) {
        int newLength = from << 1;

        // Can't get any bigger
        if (newLength > MAXIMUM_CAPACITY || newLength <= from)
            return;

        Object[] newTable = new Object[newLength];
        Object[] old = table;

        for (Object e : old) {
            if (e == null)
                continue;

            int index = index(hash(e), newLength);
            while (newTable[index] != null)
                index = nextIndex(index, newLength);

            newTable[index] = e;
        }

        threshold = (int) (loadFactor * newLength);
        table = newTable;
    }

    public boolean remove(Object o) {
        o = maskNull(o);

        Object[] table = this.table;
        int length = table.length;
        int hash = hash(o);
        int start = index(hash, length);

        for (int index = start;;) {
            Object e = table[index];
            if (e == null)
                return false;

            if (eq(e, o)) {
                table[index] = null;
                relocate(index);
                modCount++;
                size--;
                return true;
            }

            index = nextIndex(index, length);
            if (index == start)
                return false;
        }
    }

    private void relocate(int start) {
        Object[] table = this.table;
        int length = table.length;
        int current = nextIndex(start, length);

        for (;;) {
            Object e = table[current];
            if (e == null)
                return;

            // A Doug Lea variant of Knuth's Section 6.4 Algorithm R.
            // This provides a non-recursive method of relocating
            // entries to their optimal positions once a gap is created.
            int prefer = index(hash(e), length);
            if ((current < prefer && (prefer <= start || start <= current)) || (prefer <= start && start <= current)) {
                table[start] = e;
                table[current] = null;
                start = current;
            }

            current = nextIndex(current, length);
        }
    }

    public void clear() {
        modCount++;
        Object[] table = this.table;
        for (int i = 0; i < table.length; i++)
            table[i] = null;

        size = 0;
    }

    @SuppressWarnings("unchecked")
    public StrongInternPool<E> clone() {
        try {
            StrongInternPool<E> clone = (StrongInternPool<E>) super.clone();
            clone.table = table.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            // should never happen
            throw new IllegalStateException(e);
        }
    }

    /**
     * Advanced method that returns the internal table. The resulting
     * array will contain nulls at random places that must be skipped. In
     * addition, it will not operate correctly if a null was inserted into the
     * set. Use at your own risk....
     *
     * @return an array containing elements in this set along with randomly
     *         placed nulls,
     */
    public Object[] toInternalArray() {
        return table;
    }

    public void printDebugStats() {
        int optimal = 0;
        int total = 0;
        int totalSkew = 0;
        int maxSkew = 0;
        for (int i = 0; i < table.length; i++) {
            Object e = table[i];
            if (e != null) {

                total++;
                int target = index(hash(e), table.length);
                if (i == target)
                    optimal++;
                else {
                    int skew = Math.abs(i - target);
                    if (skew > maxSkew)
                        maxSkew = skew;
                    totalSkew += skew;
                }

            }
        }

        System.out.println(" Size:            " + size);
        System.out.println(" Real Size:       " + total);
        System.out.println(" Optimal:         " + optimal + " (" + (float) optimal * 100 / total + "%)");
        System.out.println(" Average Distnce: " + ((float) totalSkew / (total - optimal)));
        System.out.println(" Max Distance:    " + maxSkew);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        int size = s.readInt();

        init(size, loadFactor);

        for (int i = 0; i < size; i++) {
            putForCreate((E) s.readObject());
        }

        this.size = size;
    }

    private void putForCreate(E entry) {
        entry = maskNull(entry);

        Object[] table = this.table;
        int hash = hash(entry);
        int length = table.length;
        int index = index(hash, length);

        Object e = table[index];
        while (e != null) {
            index = nextIndex(index, length);
            e = table[index];
        }

        table[index] = entry;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(size);

        for (Object e : table) {
            if (e != null) {
                s.writeObject(unmaskNull(e));
            }
        }
    }

    public Iterator<E> iterator() {
        return new IdentityHashSetIterator();
    }

    public Index index() {
        if (index == null || index.modCount != modCount) {
            index = new Index();
        }

        return index;
    }

    public String toString() {
        Iterator<E> i = iterator();
        if (! i.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = i.next();
            sb.append(e);
            if (! i.hasNext())
                return sb.append(']').toString();
            sb.append(", ");
        }
    }

    public class Index {
        private int[] offsets;
        private int modCount;

        Index() {
            offsets = new int[table.length];
            for (int i = 0, c = 1; i < offsets.length; i++) {
                if (table[i] != null)
                    offsets[i] = c++;
            }
            modCount = StrongInternPool.this.modCount;
        }

        public int positionOf(E e)
        {
            int offset = offset(e);
            return offset < 0 ? -1 : offsets[offset];
        }
    }

    private class IdentityHashSetIterator implements Iterator<E> {
        private int next = 0;
        private int expectedCount = modCount;
        private int current = -1;
        private boolean hasNext;
        Object table[] = StrongInternPool.this.table;

        public boolean hasNext() {
            if (hasNext == true)
                return true;

            Object table[] = this.table;
            for (int i = next; i < table.length; i++) {
                if (table[i] != null) {
                    next = i;
                    return hasNext = true;
                }
            }

            next = table.length;
            return false;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (modCount != expectedCount)
                throw new ConcurrentModificationException();

            if (!hasNext && !hasNext())
                throw new NoSuchElementException();

            current = next++;
            hasNext = false;

            return (E) unmaskNull(table[current]);
        }

        public void remove() {
            if (modCount != expectedCount)
                throw new ConcurrentModificationException();

            int current = this.current;
            int delete = current;

            if (current == -1)
                throw new IllegalStateException();

            // Invalidate current (prevents multiple remove)
            this.current = -1;

            // Start were we relocate
            next = delete;

            Object[] table = this.table;
            if (table != StrongInternPool.this.table) {
                StrongInternPool.this.remove(table[delete]);
                table[delete] = null;
                expectedCount = modCount;
                return;
            }

            int length = table.length;
            int i = delete;

            table[delete] = null;
            size--;

            for (;;) {
                i = nextIndex(i, length);
                Object e = table[i];
                if (e == null)
                    break;

                int prefer = index(hash(e), length);
                if ((i < prefer && (prefer <= delete || delete <= i)) || (prefer <= delete && delete <= i)) {
                    // Snapshot the unseen portion of the table if we have
                    // to relocate an entry that was already seen by this
                    // iterator
                    if (i < current && current <= delete && table == StrongInternPool.this.table) {
                        int remaining = length - current;
                        Object[] newTable = new Object[remaining];
                        System.arraycopy(table, current, newTable, 0, remaining);

                        // Replace iterator's table.
                        // Leave table local var pointing to the real table
                        this.table = newTable;
                        next = 0;
                    }

                    // Do the swap on the real table
                    table[delete] = e;
                    table[i] = null;
                    delete = i;
                }
            }
        }
    }
}
