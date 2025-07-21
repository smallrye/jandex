package org.jboss.jandex.gizmo2;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * The {@code gizmo2} module needs to produce Java 17 bytecode.
 */
public class BytecodeVersionTest {
    @Test
    public void verifyJava17() throws IOException {
        try (InputStream in = BytecodeVersionTest.class.getResourceAsStream("/org/jboss/jandex/gizmo2/Jandex2Gizmo.class")) {
            if (in == null) {
                fail("Could not find org.jboss.jandex.gizmo2.Jandex2Gizmo");
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

        if (major != 61) { // Java 17
            fail("Unexpected class file format version: " + major + "." + minor + ", Jandex Gizmo2 must be Java 17 bytecode");
        }
    }
}
