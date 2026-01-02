package org.jboss.jandex;

// there's also a Java 11+ variant of this class, which must be kept in sync
class BitTricks {
    static boolean isAsciiOnly(byte[] chars, int off, int len) {
        for (int i = 0; i < len; i++) {
            if ((chars[off + i] & 0x80) != 0) {
                return false;
            }
        }

        return true;
    }
}
