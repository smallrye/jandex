/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    private static File getIndexFile(File jarFile, boolean newJar) {
        final String name = jarFile.getName();
        final int p = name.lastIndexOf(".");
        if (p < 0)
            throw new IllegalArgumentException("File has no extension / ext: " + jarFile);

        // this method is here so we keep the naming details here, as impl detail
        final String ext = name.substring(p);
        String pattern = "\\" + ext + "$";

        if (newJar)
            return new File(jarFile.getAbsolutePath().replaceAll(pattern, "-jandex" + ext));
        else
            return new File(jarFile.getAbsolutePath().replaceAll(pattern, "-" + ext.substring(1)) + ".idx");
    }
    
    /**
     * Indexes a jar file and saves the result. If the modify flag is set, index is saved to META-INF/jandex.idx.
     * Otherwise an external file is created with a similar name to the original file, 
     * concatenating <code>.idx</code> suffix.
     *
     * @param jarFile The file to index
     * @param indexer The indexer to use
     * @param modify If the original jar should be modified
     * @param newJar If the new jar should be created
     * @param verbose If we should print what we are doing to standard out
     * @return indexing result
     * @throws IOException for any I/o error
     */
    public static Result createJarIndex(File jarFile, Indexer indexer, boolean modify, boolean newJar, boolean verbose) throws IOException {
        return createJarIndex(jarFile, indexer, modify, newJar, verbose, System.out, System.err);
    }

    /**
     * Indexes a jar file and saves the result. If the modify flag is set, index is saved to META-INF/jandex.idx.
     * Otherwise an external file is created with a similar name to the original file,
     * concatenating <code>.idx</code> suffix.
     *
     * @param jarFile The file to index
     * @param indexer The indexer to use
     * @param outputFile The index file to write to
     * @param modify If the original jar should be modified
     * @param newJar If the new jar should be created
     * @param verbose If we should print what we are doing to standard out
     * @return indexing result
     * @throws IOException for any I/o error
     */
    public static Result createJarIndex(File jarFile, Indexer indexer, File outputFile, boolean modify, boolean newJar, boolean verbose) throws IOException {
        return createJarIndex(jarFile, indexer, outputFile, modify, newJar, verbose, System.out, System.err);
    }

    /**
     * Indexes a jar file and saves the result. If the modify flag is set, index is saved to META-INF/jandex.idx.
     * Otherwise an external file is created with a similar name to the original file,
     * concatenating <code>.idx</code> suffix.
     *
     * @param jarFile The file to index
     * @param indexer The indexer to use
     * @param modify If the original jar should be modified
     * @param newJar If the new jar should be created
     * @param verbose If we should print what we are doing to the specified info stream
     * @param infoStream A print stream which will record verbose info, may be null
     * @param errStream A print stream to print errors, must not be null
     *
     * @return indexing result
     * @throws IOException for any I/o error
     */
    public static Result createJarIndex(File jarFile, Indexer indexer, boolean modify, boolean newJar, boolean verbose, PrintStream infoStream, PrintStream errStream) throws IOException {
        return createJarIndex(jarFile, indexer, null, modify, newJar, verbose, infoStream, errStream);
    }

    /**
     * Indexes a jar file and saves the result. If the modify flag is set, index is saved to META-INF/jandex.idx.
     * Otherwise an external file is created with a similar name to the original file,
     * concatenating <code>.idx</code> suffix.
     *
     * @param jarFile The file to index
     * @param indexer The indexer to use
     * @param outputFile The index file to write to
     * @param modify If the original jar should be modified
     * @param newJar If the new jar should be created
     * @param verbose If we should print what we are doing to the specified info stream
     * @param infoStream A print stream which will record verbose info, may be null               1
     * @param errStream A print stream to print errors, must not be null
     *
     * @return indexing result
     * @throws IOException for any I/o error
     */
    public static Result createJarIndex(File jarFile, Indexer indexer, File outputFile, boolean modify, boolean newJar, boolean verbose, PrintStream infoStream, PrintStream errStream) throws IOException {
        File tmpCopy = null;
        ZipOutputStream zo = null;
        OutputStream out;

        JarFile jar = new JarFile(jarFile);

        if (modify) {
            tmpCopy = File.createTempFile(jarFile.getName().substring(0, jarFile.getName().lastIndexOf('.')) + "00", "jmp");
            out = zo = new ZipOutputStream(new FileOutputStream(tmpCopy));
            outputFile = jarFile;
        } else if (newJar) {
            outputFile = getIndexFile(jarFile, newJar);
            out = zo = new ZipOutputStream(new FileOutputStream(outputFile));
        } else {
            if (outputFile == null) {
                outputFile = getIndexFile(jarFile, newJar);
            }
            out = new FileOutputStream(outputFile);
        }

        try {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (modify) {
                    if (!"META-INF/jandex.idx".equals(entry.getName())) {
                        JarEntry clone = (JarEntry) entry.clone();
                        // Compression level and format can vary across implementations
                        if (clone.getMethod() != ZipEntry.STORED)
                            clone.setCompressedSize(-1);
                        zo.putNextEntry(clone);
                        final InputStream stream = jar.getInputStream(entry);
                        try {
                            copy(stream, zo);
                        }
                        finally {
                            safeClose(stream);
                        }
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
                        if (verbose && info != null && infoStream != null)
                            printIndexEntryInfo(info, infoStream);
                    } catch (Exception e) {
                        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                        errStream.println("ERROR: Could not index " + entry.getName() + ": " + message); if (verbose)
                        e.printStackTrace(errStream);
                    }
                }
            }

            if (modify || newJar) {
                zo.putNextEntry(new ZipEntry("META-INF/jandex.idx"));
            }

            IndexWriter writer = new IndexWriter(out);
            Index index = indexer.complete();
            int bytes = writer.write(index);

            out.close();

            if (modify) {
                jarFile.delete();
                if (!tmpCopy.renameTo(jarFile)) {
                    copy(jarFile, tmpCopy);
                    tmpCopy.delete();
                }
                tmpCopy = null;
            }
            return new Result(index, modify ? "META-INF/jandex.idx" : outputFile.getPath(), bytes, outputFile);
        } finally {
            safeClose(out);
            safeClose(jar);
            if (tmpCopy != null)
                tmpCopy.delete();
        }
    }

    private static void copy(File dest, File source) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(new File(dest.getAbsolutePath()));
        try {
            byte[] b = new byte[8196];
            for (int count=0; (count = fis.read(b, 0, 8196)) >= 0;  ) {
                fos.write(b, 0, count);
            }
        } finally {
            fis.close();
            fos.close();
        }
    }

    private static void printIndexEntryInfo(ClassInfo info, PrintStream infoStream) {
        infoStream.println("Indexed " + info.name() + " (" + info.annotations().size() + " annotations)");
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
