package org.jboss.jandex.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexWriter;

public class IndexingUtil {

    public static Index roundtrip(Index index) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new IndexWriter(bytes).write(index);
        return new IndexReader(new ByteArrayInputStream(bytes.toByteArray())).read();
    }

    // serializes and deserializes the index and asserts that the hash of the serialized index matches the expected hash
    // this aids in testing that the serialization is deterministic accross different versions of the library and
    // on different platforms
    public static Index roundtrip(Index index, String expectedHash) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new IndexWriter(bytes).write(index);
        String actual = sha256(bytes.toByteArray());
        assertEquals(expectedHash, actual);
        return new IndexReader(new ByteArrayInputStream(bytes.toByteArray())).read();
    }

    public static String sha256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input);
            return hex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
