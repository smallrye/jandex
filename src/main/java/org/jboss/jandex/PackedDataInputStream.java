/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jandex;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that reads integers that were packed by
 * {@link PackedDataOutputStream}
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is not thread-safe can <b>not<b> be shared between threads.
 *
 * @author Jason T. Greene
 */
class PackedDataInputStream extends DataInputStream {

    static final int MAX_1BYTE = 0x7F;

    public PackedDataInputStream(InputStream in) {
        super(in);
    }

    /**
     * Reads a packed unsigned integer. Every byte uses the first bit as a control bit to
     * signal when there are additional bytes to be read. The remaining seven bits are data.
     * Depending on the size of the number one to five bytes may be read.
     *
     * @return the unpacked integer
     *
     * @throws IOException
     */
    public int readPackedU32() throws IOException {
        byte b;
        int i = 0;

        do {
            b = readByte();
            i = (i << 7) | (b & MAX_1BYTE);
        }  while ((b & 0x80) == 0x80);

        return i;
    }
}

