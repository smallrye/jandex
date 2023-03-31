package org.jboss.jandex;

// there's also a Java 11+ variant of this class, which must be kept in sync
class BitTricks {
    static boolean isAsciiOnly(byte[] chars, int off, int len) {
        int longRounds = len >>> 3;
        for (int i = 0; i < longRounds; i++) {
            long batch = ((long) chars[off]) << 56
                    | ((long) chars[off + 1]) << 48
                    | ((long) chars[off + 2]) << 40
                    | ((long) chars[off + 3]) << 32
                    | chars[off + 4] << 24
                    | chars[off + 5] << 16
                    | chars[off + 6] << 8
                    | chars[off + 7];
            // check that all 8 bytes are <= 127 (ASCII) in one go
            if ((batch & 0x80_80_80_80_80_80_80_80L) != 0) {
                return false;
            }
            off += Long.BYTES;
        }

        int byteRounds = len & 7;
        if (byteRounds > 0) {
            for (int i = 0; i < byteRounds; i++) {
                if ((chars[off + i] & 0x80) != 0) {
                    return false;
                }
            }
        }

        return true;
    }
}
