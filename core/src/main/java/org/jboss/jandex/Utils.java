/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Common utilities
 *
 * @author Jason T. Greene
 */
class Utils {
    static final byte[] INIT_METHOD_NAME = Utils.toUTF8("<init>");

    static byte[] toUTF8(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    static String fromUTF8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    static <T> List<T> emptyOrWrap(List<T> list) {
        return list.size() == 0 ? Collections.<T> emptyList() : Collections.unmodifiableList(list);
    }

    static <T> Collection<T> emptyOrWrap(Collection<T> list) {
        return list.size() == 0 ? Collections.<T> emptyList() : Collections.unmodifiableCollection(list);
    }

    static <K, V> Map<K, V> emptyOrWrap(Map<K, V> map) {
        return map.size() == 0 ? Collections.<K, V> emptyMap() : Collections.unmodifiableMap(map);
    }

    static <T> List<T> listOfCapacity(int capacity) {
        return capacity > 0 ? new ArrayList<T>(capacity) : Collections.<T> emptyList();
    }

    static final class ReusableBufferedDataInputStream extends DataInputStream {
        private ReusableBufferedInputStream reusableBuffered = null;

        ReusableBufferedDataInputStream() {
            super(null);
        }

        void setInputStream(InputStream in) {
            Objects.requireNonNull(in);
            // this is already buffered: let's use it directly
            if (in instanceof BufferedInputStream) {
                assert !(in instanceof ReusableBufferedInputStream);
                this.in = in;
            } else {
                if (this.in == null) {
                    if (reusableBuffered == null) {
                        reusableBuffered = new ReusableBufferedInputStream();
                    }
                    this.in = reusableBuffered;
                }
                reusableBuffered.setInputStream(in);
            }
        }

        @Override
        public void close() {
            if (in == reusableBuffered) {
                reusableBuffered.close();
            } else {
                in = null;
            }
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        @Deprecated
        public synchronized void mark(int readlimit) {
            throw new UnsupportedOperationException("mark/reset not supported");
        }

        @Override
        @Deprecated
        public synchronized void reset() {
            throw new UnsupportedOperationException("mark/reset not supported");
        }
    }

    private static final class ReusableBufferedInputStream extends BufferedInputStream {
        private ReusableBufferedInputStream() {
            super(null);
        }

        void setInputStream(InputStream in) {
            Objects.requireNonNull(in);
            if (pos != 0 && this.in != null) {
                throw new IllegalStateException("the stream cannot be reused");
            }
            this.in = in;
        }

        @Override
        public void close() {
            in = null;
            count = 0;
            pos = 0;
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        @Deprecated
        public synchronized void mark(int readlimit) {
            throw new UnsupportedOperationException("mark/reset not supported");
        }

        @Override
        @Deprecated
        public synchronized void reset() {
            throw new UnsupportedOperationException("mark/reset not supported");
        }
    }
}
