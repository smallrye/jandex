package org.jboss.jandex.test;

import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.jboss.jandex.Result;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNull;

/**
 * Author: Ingo Weiss <ingo@redhat.com>
 */

public class CorruptedJarTest {
    @Test(timeout = 30000, expected = IllegalStateException.class)
    public void testIndexingCorruptedJar() throws IOException {
        File corruptedJar = new File(Thread.currentThread().getContextClassLoader()
                .getResource("corrupted.jar").getFile());
        Indexer indexer = new Indexer();
        Result result = JarIndexer.createJarIndex(corruptedJar, indexer, false, false, true);
        assertNull(result);
    }
}
