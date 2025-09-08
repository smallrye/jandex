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

import static org.jboss.jandex.IndexReader.MAGIC;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public final class SerializedIndexScanner {

    /**
     * The latest index version supported by this version of Jandex.
     */
    private PackedDataInputStream input;
    private int version = -1;
    private SerializedIndexScannerV2 scanner;

    /**
     * Constructs a new IndedReader using the passed stream. The stream is not
     * read from until the read method is called.
     *
     * @param input a stream which points to a jandex index file
     */
    public SerializedIndexScanner(InputStream input) {
        this.input = new PackedDataInputStream(new BufferedInputStream(input));
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
    public void scan(Consumer<String> messageConsumer) throws IOException {
        if (version == -1) {
            readVersion();
        }

        scanner.read(messageConsumer);
    }

    private void initScanner(int version) throws IOException {
        SerializedIndexScannerV2 reader;
        if (version >= IndexReaderV2.MIN_VERSION && version <= IndexReaderV2.MAX_VERSION) {
            reader = new SerializedIndexScannerV2(input, version);
        } else {
            input.close();
            throw new UnsupportedVersion("Can't read index version " + version
                    + "; this IndexReader only supports index versions "
                    + IndexReaderV2.MIN_VERSION + "-" + IndexReaderV2.MAX_VERSION);
        }

        this.scanner = reader;
    }

    private void readVersion() throws IOException {
        if (input.readInt() != MAGIC) {
            input.close();
            throw new IllegalArgumentException("Not a jandex index");
        }

        version = input.readUnsignedByte();
        initScanner(version);
    }
}
