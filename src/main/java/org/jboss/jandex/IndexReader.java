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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a Jandex index file and returns the saved index. See {@link Indexer}
 * for a thorough description of how the Index data is produced.
 *
 * <p>
 * An IndexReader loads the stream passed to it's constructor and applies the
 * appropriate buffering. The Jandex index format is designed for efficient
 * reading and low final memory storage.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * IndexReader is not thread-safe and can not be shared between concurrent
 * threads. The resulting index, however, is.
 *
 * @author Jason T. Greene
 */
public final class IndexReader {
    private static final int MAGIC = 0xBABE1F15;
    private InputStream input;


    /**
     * Constructs a new IndedReader using the passed stream. The stream is not
     * read from until the read method is called.
     *
     * @param input a stream which points to a jandex index file
     */
    public IndexReader(InputStream input) {
        this.input = input;
    }

    /**
     * Read the index at the associated stream of this reader. This method can be called multiple
     * times if the stream contains multiple index files.
     *
     * @return the Index contained in the stream
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the stream does not point to Jandex index data
     * @throws UnsupportedVersion if the index data is tagged with a version not known to this reader
     */
    public Index read() throws IOException {
        PackedDataInputStream stream = new PackedDataInputStream(new BufferedInputStream(input));
        if (stream.readInt() != MAGIC) {
            stream.close();
            throw new IllegalArgumentException("Not a jandex index");
        }
        byte version = stream.readByte();

        IndexReaderImpl reader = getReader(stream, version);
        if (reader == null) {
            stream.close();
            throw new UnsupportedVersion("Version: " + version);
        }

        return reader.read(version);
    }

    private IndexReaderImpl getReader(PackedDataInputStream stream, int version) {
        if (version >= IndexReaderV1.MIN_VERSION && version <= IndexReaderV1.MAX_VERSION) {
            return new IndexReaderV1(stream);
        } else if (version >= IndexReaderV2.MIN_VERSION && version <= IndexReaderV2.MAX_VERSION) {
            return new IndexReaderV2(stream);
        }

        return null;
    }
}
