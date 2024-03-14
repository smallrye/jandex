package org.jboss.jandex.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class IOUtil {
    private static final int BUFFER_SIZE = 8192;

    /**
     * Copies the remaining of what's to read from given input stream into the given output stream.
     * The given input stream is not closed, that is left to the caller.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(in);
        Objects.requireNonNull(out);

        byte[] buffer = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
    }

    /**
     * Finds all occurences of given {@code search} in given {@code array} and replaces them
     * with given {@code replacement}. The {@code search} and {@code replacement} arrays
     * must have the same length.
     */
    public static void searchAndReplace(byte[] array, byte[] search, byte[] replacement) {
        Objects.requireNonNull(array);
        Objects.requireNonNull(search);
        Objects.requireNonNull(replacement);

        if (search.length != replacement.length) {
            throw new IllegalArgumentException("Search and replacement must have the same length");
        }
        if (array.length < search.length) {
            throw new IllegalArgumentException("Array must be at least as long as search");
        }
        if (search.length == 0) {
            return;
        }

        outer: for (int i = 0; i < array.length - search.length + 1; i++) {
            for (int j = 0; j < search.length; j++) {
                if (array[i + j] != search[j]) {
                    continue outer;
                }
            }
            System.arraycopy(replacement, 0, array, i, replacement.length);
        }
    }
}
