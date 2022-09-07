package org.jboss.jandex;

/**
 * Jandex types that need to implement special equality/hash code for the purpose of interning
 * should implement this interface. Note that if the equality/hash code is structural, it is
 * likely that types which use {@code Interned} types should themselves also be {@code Interned}
 * (if they are subject to the interning process, of course).
 * <p>
 * The main reason why this interface exists is the {@code Type} hierarchy. Types need a different
 * equality and hash code for intering because of type variable references. To maintain structural
 * equality/hash code for external users (that is, the common {@code equals()} and {@code hashCode()}
 * methods), type variable references equality/hash code must only consider the type variable name
 * (and annotations). This is not suitable for the interning purpose, because two type variable
 * references may have the same name and annotations, yet point to different type variables
 * (e.g. because those type variables have different annotations).
 * <p>
 * We could possibly implement a "deep" structural equality and hash code for type variable references
 * that would take into account the type variable the reference points to, but that still wouldn't be
 * enough. When interning, types must be interned "from the inside", which means that when a type variable
 * reference is being interned, the "target" type variable is not interned yet (possibly even not yet set)
 * and can change later. This is one of the reasons why type variable references are mutable, and that
 * in turn requires that their equality and hash code for interning purposes are based on identity.
 * <p>
 * Note that type variable references are only mutated during indexing. After an {@code Index} is complete,
 * type variable references must be considered "frozen".
 */
interface Interned {
    boolean internEquals(Object o);

    int internHashCode();

    // ---

    static boolean arrayEquals(Interned[] a, Interned[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            if (!a[i].internEquals(b[i])) {
                return false;
            }
        }

        return true;
    }

    static int arrayHashCode(Interned[] array) {
        if (array == null) {
            return 0;
        }

        int result = 1;

        for (int i = 0; i < array.length; i++) {
            Interned item = array[i];
            result = 31 * result + (item == null ? 0 : item.internHashCode());
        }

        return result;
    }

}
