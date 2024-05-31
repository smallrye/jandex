package org.jboss.jandex;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class Compare {
    public static <T> Comparator<T[]> array(Comparator<T> c) {
        return new Comparator<T[]>() {
            @Override
            public int compare(T[] o1, T[] o2) {
                if (o1 == o2) {
                    return 0;
                }

                int v = nullable(o1, o2);
                if (v != 0) {
                    return v;
                }

                v = o1.length - o2.length;
                if (v != 0) {
                    return v;
                }
                for (int i = 0; i < o1.length; i++) {
                    v = c.compare(o1[i], o2[i]);
                    if (v != 0) {
                        return v;
                    }
                }

                return 0;
            }
        };
    }

    public static <T> Comparator<Collection<T>> list(Comparator<T> c) {
        return new Comparator<Collection<T>>() {
            @Override
            public int compare(Collection<T> o1, Collection<T> o2) {
                if (o1 == o2) {
                    return 0;
                }

                int v = nullable(o1, o2);
                if (v != 0) {
                    return v;
                }

                Iterator<T> i1 = o1.iterator();
                Iterator<T> i2 = o2.iterator();
                while (i1.hasNext() && i2.hasNext()) {
                    v = c.compare(i1.next(), i2.next());
                    if (v != 0) {
                        return v;
                    }
                }

                return 0;
            }
        };
    }

    public static int bytes(byte[] o1, byte[] o2) {
        if (o1 == o2) {
            return 0;
        }

        int v = nullable(o1, o2);
        if (v != 0) {
            return v;
        }
        v = o1.length - o2.length;
        if (v != 0) {
            return v;
        }
        for (int i = 0; i < o1.length; i++) {
            if (o1[i] != o2[i]) {
                return o1[i] - o2[i];
            }
        }
        return 0;
    }

    public static int bytes_bytes(byte[][] o1, byte[][] o2) {
        if (o1 == o2) {
            return 0;
        }
        int v = nullable(o1, o2);
        if (v != 0) {
            return v;
        }
        v = o1.length - o2.length;
        if (v != 0) {
            return v;
        }
        for (int i = 0; i < o1.length; i++) {
            v = bytes(o1[i], o2[i]);
            if (v != 0) {
                return v;
            }
        }
        return 0;
    }

    static int nullable(Object o1, Object o2) {
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return 0;
    }

}
