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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Writes a Jandex index file to a stream. The write process is somewhat more
 * expensive to allow for fast reads and a compact size. For more information on
 * the index content, see the documentation on {@link Indexer}.
 *
 * <p>
 * The IndexWriter operates on standard output streams, and also provides
 * suitable buffering.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * IndexWriter is not thread-safe and can not be shared between concurrent
 * threads.
 *
 * @see Indexer
 * @see Index
 * @author Jason T. Greene
 *
 */
public final class IndexWriter {

    private final OutputStream out;

    /**
     * Constructs an IndexWriter using the specified stream
     *
     * @param out a stream to write an index to
     */
    public IndexWriter(OutputStream out) {
        this.out = out;
    }

    /**
     * Writes the specified index to the associated output stream. This may be called multiple times in order
     * to write multiple indexes. The default version of index file is used.
     *
     * @param index the index to write to the stream
     * @return the number of bytes written to the stream
     * @throws IOException if the underlying stream fails
     */
    public int write(Index index) throws IOException {
        return write(index, IndexWriterV2.MAX_VERSION);
    }

    /**
     * Writes the specified index to the associated output stream. This may be called multiple times in order
     * to write multiple indexes.
     *
     * @param index the index to write to the stream
     * @param version the index file version
     * @return the number of bytes written to the stream
     * @throws IOException if any i/o error occurs
     */
    @Deprecated
    public int write(Index index, byte version) throws IOException {
        return this.write(index, version & 0xFF);
    }

    /**
     * Writes the specified index to the associated output stream. This may be called multiple times in order
     * to write multiple indexes.
     *
     * @param index the index to write to the stream
     * @param version the index file version
     * @return the number of bytes written to the stream
     * @throws IOException if any i/o error occurs
     */
    public int write(Index index, int version) throws IOException {

        IndexWriterImpl writer = getWriter(version);
        if (writer == null) {
            throw new UnsupportedVersion("Version: " + version);
        }

        return writer.write(index, version);
    }

    private IndexWriterImpl getWriter(int version) {
        if (version >= IndexWriterV1.MIN_VERSION && version <= IndexWriterV1.MAX_VERSION) {
            return new IndexWriterV1(out);
        }

        if (version >= IndexWriterV2.MIN_VERSION && version <= IndexWriterV2.MAX_VERSION) {
            return new IndexWriterV2(out);
        }

        return null;
    }
}
