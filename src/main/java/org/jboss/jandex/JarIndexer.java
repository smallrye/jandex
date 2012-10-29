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
 * @author Ales Justin
 */
public class JarIndexer {

    /**
     * Get index file.
     *
     * It is a new jar file, if the newJar flag is true.
     * Otherwise it's a standalone index file.
     *
     * @param jarFile The file to index
     * @param newJar If the new jar should be created
     * @return location of the index
     */
    public static File getIndexFile(File jarFile, boolean newJar) {
        final String name = jarFile.getName();
        final int p = name.lastIndexOf(".");
        if (p < 0)
            throw new IllegalArgumentException("File has no extension / ext: " + jarFile);

        // this method is here so we keep the naming details here, as impl detail
        final String ext = name.substring(p);
        if (newJar)
            return new File(jarFile.getAbsolutePath().replace(ext, "-jandex" + ext));
        else
            return new File(jarFile.getAbsolutePath().replace(ext, "-" + ext.substring(1)) + ".idx");
    }
    
    /**
     * Indexes a jar file and saves the result. If the modify flag is set, index is saved to META-INF/jandex.idx.
     * Otherwise an external file is created with a similar name to the original file, 
     * concatinating <code>.idx</code> suffix.
     *
     * @param jarFile The file to index
     * @param indexer The indexer to use
     * @param modify If the original jar should be modified
     * @param newJar If the new jar should be created
     * @param verbose If we should print what we are doing to standard out
     * @return indexing result
     * @throws IOException for any I/o error
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Result createJarIndex(File jarFile, Indexer indexer, boolean modify, boolean newJar, boolean verbose) throws IOException {
        File tmpCopy = null;
        ZipOutputStream zo = null;
        OutputStream out;
        File outputFile = null;

        JarFile jar = new JarFile(jarFile);

        if (modify) {
            tmpCopy = File.createTempFile(jarFile.getName().substring(0, jarFile.getName().lastIndexOf('.')) + "00", "jmp");
            out = zo = new ZipOutputStream(new FileOutputStream(tmpCopy));
        } else if (newJar) {
            outputFile = getIndexFile(jarFile, newJar);
            out = zo = new ZipOutputStream(new FileOutputStream(outputFile));
        } else {
            outputFile = getIndexFile(jarFile, newJar);
            out = new FileOutputStream(outputFile);
        }

        try {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (modify) {
                    JarEntry clone = (JarEntry) entry.clone();
                    // Compression level and format can vary across implementations
                    if (clone.getMethod() != ZipEntry.STORED)
                        clone.setCompressedSize(-1);
                    zo.putNextEntry(clone);
                    final InputStream stream = jar.getInputStream(entry);
                    try {
                        copy(stream, zo);
                    } finally {
                        safeClose(stream);
                    }
                }

                if (entry.getName().endsWith(".class")) {
                    try {
                        final InputStream stream = jar.getInputStream(entry);
                        ClassInfo info;
                        try {
                            info = indexer.index(stream);
                        } finally {
                            safeClose(stream);
                        }
                        if (verbose && info != null)
                            printIndexEntryInfo(info);
                    } catch (Exception e) {
                        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                        System.err.println("ERROR: Could not index " + entry.getName() + ": " + message); if (verbose)
                        e.printStackTrace(System.err);
                    }
                }
            }

            if (modify || newJar) {
                zo.putNextEntry(new ZipEntry("META-INF/jandex.idx"));
            }

            IndexWriter writer = new IndexWriter(out);
            Index index = indexer.complete();
            int bytes = writer.write(index);

            out.flush();

            if (modify) {
                jarFile.delete();
                if (!tmpCopy.renameTo(jarFile)) {
                    FileInputStream fis = new FileInputStream(tmpCopy);
                    FileOutputStream fos = new FileOutputStream(new File(jarFile.getAbsolutePath()));
                    try {
                        byte[] b = new byte[1024];
                        for (int count=0; (count = fis.read(b, 0, 1024)) >= 1024;  ) {
                            fos.write(b, 0, count);
                        }
                    } finally {
                        fis.close();
                        fos.close();
                    }
                    tmpCopy.delete();
                }
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
