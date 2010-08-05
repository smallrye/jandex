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
    
