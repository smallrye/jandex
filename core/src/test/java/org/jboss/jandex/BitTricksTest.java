package org.jboss.jandex;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BitTricks#isAsciiOnly(byte[], int, int)}, which returns {@code true} iff every
 * byte in the window {@code [off, off + len)} is ASCII (high bit clear, i.e. {@code <= 0x7F}).
 * <p>
 * This lives in package {@code org.jboss.jandex} because the class and the method are package-private.
 * <p>
 * There are two implementations of {@code BitTricks} that must be kept in sync: the plain byte loop
 * in {@code src/main/java} and the {@code VarHandle} variant in {@code src/main/java11} that reads
 * eight bytes at a time followed by a byte-by-byte tail. A normal {@code mvn test} exercises the
 * base (byte-loop) implementation only; the java11 variant is layered into the multi-release JAR at
 * package time, after the test phase, so it is not on the test classpath here. These tests therefore
 * verify the base implementation's contract, which the java11 variant must equally satisfy. Input
 * lengths are chosen around the eight-byte batch boundary (0, 1, 7, 8, 11, 16) so the same cases
 * also cover the java11 batch and tail paths if that variant is ever placed on the test classpath.
 */
public class BitTricksTest {
    // fills a byte array with a repeating ASCII byte (0x61..0x7A)
    private static byte[] ascii(int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) ('a' + (i % 26));
        }
        return result;
    }

    @Test
    public void emptyIsAscii() {
        assertTrue(BitTricks.isAsciiOnly(new byte[0], 0, 0));
    }

    @Test
    public void asciiBoundaryValuePasses() {
        // 0x7F is the highest ASCII byte; the high bit is clear
        assertTrue(BitTricks.isAsciiOnly(new byte[] { 0x00, 0x7F }, 0, 2));
    }

    @Test
    public void highBitByteIsNotAscii() {
        // 0x80 is the lowest byte with the high bit set
        assertFalse(BitTricks.isAsciiOnly(new byte[] { (byte) 0x80 }, 0, 1));
    }

    @Test
    public void allBitsSetByteIsNotAscii() {
        assertFalse(BitTricks.isAsciiOnly(new byte[] { (byte) 0xFF }, 0, 1));
    }

    @Test
    public void nonAsciiInShortInputIsNotAscii() {
        byte[] bytes = ascii(5);
        bytes[2] = (byte) 0x80;
        assertFalse(BitTricks.isAsciiOnly(bytes, 0, 5));
    }

    @Test
    public void bytesOutsideWindowAreIgnored() {
        // non-ASCII bytes before the offset and after offset+length must not affect the result
        byte[] bytes = ascii(6);
        bytes[0] = (byte) 0x80;
        bytes[5] = (byte) 0xFF;
        assertTrue(BitTricks.isAsciiOnly(bytes, 1, 4));
    }

    @Test
    public void nonZeroOffsetDetectsNonAscii() {
        byte[] bytes = ascii(6);
        bytes[3] = (byte) 0x80;
        assertFalse(BitTricks.isAsciiOnly(bytes, 1, 4));
    }

    // Lengths below are chosen relative to the java11 variant's eight-byte batch: length 7 is the
    // largest all-tail input, length 8 is exactly one batch with no tail, length 11 is one batch
    // plus a three-byte tail, and length 16 is two full batches.

    @Test
    public void sevenByteTailAllAscii() {
        // length 7 == largest input handled entirely by the byte-by-byte tail (zero batches)
        assertTrue(BitTricks.isAsciiOnly(ascii(7), 0, 7));
    }

    @Test
    public void singleBatchAllAscii() {
        // length 8 == exactly one eight-byte batch, no tail
        assertTrue(BitTricks.isAsciiOnly(ascii(8), 0, 8));
    }

    @Test
    public void batchPlusTailAllAscii() {
        // length 11 == one eight-byte batch plus a three-byte tail
        assertTrue(BitTricks.isAsciiOnly(ascii(11), 0, 11));
    }

    @Test
    public void twoBatchesAllAscii() {
        assertTrue(BitTricks.isAsciiOnly(ascii(16), 0, 16));
    }

    @Test
    public void asciiBoundaryValuesInsideBatchPass() {
        // a full batch of the boundary value 0x7F must still be reported as ASCII
        byte[] bytes = new byte[16];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0x7F;
        }
        assertTrue(BitTricks.isAsciiOnly(bytes, 0, 16));
    }

    @Test
    public void nonAsciiAtAnyBatchPositionIsDetected() {
        // poison each position in turn across two full batches: this exercises every byte lane of
        // the java11 batch read, including the batch seam (index 7 -> index 8) and the first batch
        for (int i = 0; i < 16; i++) {
            byte[] bytes = ascii(16);
            bytes[i] = (byte) 0x80;
            assertFalse(BitTricks.isAsciiOnly(bytes, 0, 16), "non-ASCII byte at index " + i + " not detected");
        }
    }

    @Test
    public void nonAsciiInTailAfterBatchIsDetected() {
        // length 11 == one batch plus a three-byte tail; the non-ASCII byte is in the tail
        byte[] bytes = ascii(11);
        bytes[9] = (byte) 0x80;
        assertFalse(BitTricks.isAsciiOnly(bytes, 0, 11));
    }

    @Test
    public void nonZeroOffsetWithBatchAndTail() {
        // offset 4 shifts the batch read to a non-eight-aligned start; non-ASCII sits in the tail
        byte[] bytes = ascii(20);
        bytes[18] = (byte) 0x80;
        assertFalse(BitTricks.isAsciiOnly(bytes, 4, 15));
    }
}
