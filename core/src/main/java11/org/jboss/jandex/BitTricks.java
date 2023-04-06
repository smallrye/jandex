package org.jboss.jandex;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

class BitTricks {
    // we only verify that all octets are <= 127, so the order in which we read them into a `long` doesn't matter
    private static final VarHandle LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.nativeOrder());

    static boolean isAsciiOnly(byte[] chars, int offset, int length) {
        int longRounds = length >>> 3;
        for (int i = 0; i < longRounds; i++) {
            long batch = (long) LONG.get(chars, offset);
            // check that all 8 bytes are <= 127 (ASCII) in one go
            if ((batch & 0x80_80_80_80_80_80_80_80L) != 0) {
                return false;
            }
            offset += Long.BYTES;
        }

        int byteRounds = length & 7;
        if (byteRounds > 0) {
            for (int i = 0; i < byteRounds; i++) {
                if ((chars[offset + i] & 0x80) != 0) {
                    return false;
                }
            }
        }

        return true;
    }
}
