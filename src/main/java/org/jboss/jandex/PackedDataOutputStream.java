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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that can pack integers into as few bytes as possible.
 * {@link PackedDataOutputStream}
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is not thread-safe can <b>not<b> be shared between threads.
 *
 * @author Jason T. Greene
 */
class PackedDataOutputStream extends DataOutputStream {

    static final int MAX_1BYTE = 0x7F;
    static final int MAX_2BYTE = 0x3FFF;
    static final int MAX_3BYTE = 0x1FFFFF;
    static final int MAX_4BYTE = 0x0FFFFFFF;

    public PackedDataOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Writes an unsigned integer in as few bytes as possible. Every byte uses
     * the first bit as a control bit to signal when there are additional bytes
     * to be read on the receiving end. The remaining seven bits are data.
     * Depending on the size of the number one to five bytes may be written. The
     * bytes are written in network-order (big endian)
     *
     * <p>
     * Note that a signed integer can still be used, but due to two's
     * compliment, all negative values will be written as five bytes
     *
     * @param i the integer to pack and write
     * @throws IOException if any i/o error occurs
     */
    public void writePackedU32(int i) throws IOException {
        if ((i & ~MAX_1BYTE) == 0) {
            writeByte(i);
        } else if ((i & ~MAX_2BYTE) == 0) {
            writeByte(((i >>> 07) & MAX_1BYTE) | 0x80);
            writeByte((i & MAX_1BYTE));
        } else if ((i & ~MAX_3BYTE) == 0) {
            writeByte(((i >>> 14) & MAX_1BYTE) | 0x80);
            writeByte(((i >>> 07) & MAX_1BYTE) | 0x80);
            writeByte((i & MAX_1BYTE));
        } else if ((i & ~MAX_4BYTE) == 0) {
            writeByte(((i >>> 21) & MAX_1BYTE) | 0x80);
            writeByte(((i >>> 14) & MAX_1BYTE) | 0x80);
            writeByte(((i >>> 07) & MAX_1BYTE) | 0x80);
            writeByte((i & MAX_1BYTE));
        } else {
            writeByte(((i >>> 28) & MAX_1BYTE) | 0x80);
            writeByte(((i >>> 21) & MAX_1BYTE) | 0x80);
            writeByte(((i >>> 14) & MAX_1BYTE) | 0x80);
            writeByte(((i >>> 07) & MAX_1BYTE) | 0x80);
            writeByte((i & MAX_1BYTE));
        }
    }
}

