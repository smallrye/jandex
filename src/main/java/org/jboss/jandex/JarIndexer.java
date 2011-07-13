package org.jboss.jandex;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * Class which contains utility methods to create an index for a jar file
 *
 * @author Stuart Douglas
 * @author Jason T. Greene
 *
 */
public class JarIndexer {

    /**
     * Indexes a jar file and saves the result. If the modify flag is try it is saved META-INF/jandex.idx.
     * Otherwies an external file is created with a similar name to the original file, however the
     * <code>.jar</code> extension is replaced with <code>-jar.idx</code>
     *
     * @param jarFile The file to index
     * @param indexer The indexer to use
     * @param modify If the original jar should be modified
     * @param verbose If we should print what we are doing to standard out
     */
    public static Result createJarIndex(File jarFile, Indexer indexer, boolean modify, boolean newJar, boolean verbose) throws IOException {
        File tmpCopy = null;
        ZipOutputStream zo = null;
        OutputStream out = null;
        File outputFile = null;

        JarFile jar = new JarFile(jarFile);

        if (modify) {
            tmpCopy = File.createTempFile(jarFile.getName().substring(0, jarFile.getName().lastIndexOf('.')) + "00", "jmp");
            out = zo = new ZipOutputStream(new FileOutputStream(tmpCopy));
        } else if (newJar) {
            outputFile = new File(jarFile.getAbsolutePath().replace(".jar", "-jandex.jar"));
            out = zo = new ZipOutputStream(new FileOutputStream(outputFile));
        } else
        {
            outputFile = new File(jarFile.getAbsolutePath().replace(".jar", "-jar") + ".idx");
            out = new FileOutputStream(outputFile);
        }

        try {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (modify) {
                    zo.putNextEntry(entry);
                    copy(jar.getInputStream(entry), zo);
                }

                if (entry.getName().endsWith(".class")) {
                    ClassInfo info = indexer.index(jar.getInputStream(entry));
                    if (verbose && info != null)
                        printIndexEntryInfo(info);
                }
            }

            if (modify || newJar) {
                zo.putNextEntry(new ZipEntry("META-INF/jandex.idx"));
            }

            IndexWriter writer = new IndexWriter(out);
            Index index = indexer.complete();
            int bytes = writer.write(index);

            out.flush();
            out.close();
            jar.close();

            if (modify) {
                jarFile.delete();
                tmpCopy.renameTo(jarFile);
                tmpCopy = null;
            }
            return new Result(index, modify ? "META-INF/jandex.idx" : outputFile.getPath(),  bytes);
        } finally {
            safeClose(out);
            safeClose(jar);
            if (tmpCopy != null)
                tmpCopy.delete();
        }
    }

    private static void printIndexEntryInfo(ClassInfo info) {
        System.out.println("Indexed " + info.name() + " (" + info.annotations().size() + " annotations)");
    }
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.flush();
    }

    private static void safeClose(JarFile close) {
        try {
            close.close();
        } catch (Exception ignore) {
        }
    }

    private static void safeClose(Closeable close) {
        try {
            close.close();
        } catch (Exception ignore) {
        }
    }

    private JarIndexer() {
    }
}
