package org.jboss.jandex.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Release builds (as well as downstream rebuilds) of Jandex are supposed to be built using JDK 17,
 * so that the {@code test-data} module doesn't have to download a second JDK during the build process,
 * but we want to make sure those builds still produce Java 8 bytecode.
 */
public class BytecodeVersionTest {
    @Test
    public void verifyJava8() throws IOException {
        try (InputStream in = BytecodeVersionTest.class.getResourceAsStream("/org/jboss/jandex/Main.class")) {
            if (in == null) {
                fail("Could not find org.jboss.jandex.Main");
            }

            DataInputStream data = new DataInputStream(new BufferedInputStream(in));
            verifyMagic(data);
            verifyVersion(data);
        }
    }

    private void verifyMagic(DataInputStream stream) throws IOException {
        int magic = stream.readInt();
        if (magic != 0xCA_FE_BA_BE) {
            fail("Invalid magic value: " + Integer.toHexString(magic));
        }
    }

    private void verifyVersion(DataInputStream stream) throws IOException {
        int minor = stream.readUnsignedShort();
        int major = stream.readUnsignedShort();

        if (major != 52) { // Java 8
            fail("Unexpected class file format version: " + major + "." + minor + ", Jandex must be Java 8 bytecode");
        }
    }
}
