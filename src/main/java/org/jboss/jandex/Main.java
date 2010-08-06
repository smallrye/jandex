/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jandex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {

    private boolean modify;
    private boolean verbose;
    private boolean dump;
    private File outputFile;
    private File source;
  

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        Main main = new Main();
        main.execute(args);
    }
    
    private static class Result {
        private int annotations;
        private int classes;
        private int bytes;
        private String name;
        
        private Result(Index index, String name, int bytes) {
            annotations = index.annotations.size();
            classes = index.classes.size();
            this.bytes = bytes;
            this.name = name;
        }
    }

    private void execute(String[] args) {
        try {
            parseOptions(args);
            
            if (dump) {
                dumpIndex(source);
                return;
            }
            
            long start = System.currentTimeMillis();
            Indexer indexer = new Indexer();
            Result result = (source.isDirectory()) ? indexDirectory(source, indexer) : indexJar(source, indexer);
            double time = (System.currentTimeMillis() - start) / 1000.00;
            System.out.printf("Wrote %s in %.4f seconds (%d classes, %d annotations, %d bytes)\n", result.name, time, result.classes, result.annotations, result.bytes);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof FileNotFoundException) {
                System.err.println(e.getMessage() == null ? e.getClass().getSimpleName() : "ERROR: " + e.getMessage());
            } else {
                e.printStackTrace(System.err);
            }
            
            System.out.println();
            printUsage();
        }
    }

    private void dumpIndex(File source) throws IOException {
        FileInputStream input = new FileInputStream(source);
        IndexReader reader = new IndexReader(input);
        
        long start = System.currentTimeMillis();
        Index index = reader.read();
        long end = System.currentTimeMillis() - start;
        index.printAnnotations();
        index.printSubclasses();
        
        System.out.printf("\nRead %s in %.04f seconds\n", source.getName(), end / 1000.0);
    }

    private Result indexDirectory(File source, Indexer indexer) throws FileNotFoundException, IOException {
        File outputFile = this.outputFile;
        scanFile(source, indexer);

        if (modify) {
            new File(source, "META-INF").mkdirs();
            outputFile = new File(source, "META-INF/jandex.idx");
        }
        if (outputFile == null) {
            outputFile = new File(source.getName().replace('.', '-') + ".idx");
        }

        FileOutputStream out = new FileOutputStream(outputFile);
        IndexWriter writer = new IndexWriter(out);
        
        try {
            Index index = indexer.complete();
            int bytes = writer.write(index);
            return new Result(index, outputFile.getPath(), bytes);
        } finally {
            out.flush();
            out.close();
        }
    }

    private Result indexJar(File source, Indexer indexer) throws IOException {
        boolean modify = this.modify;
        boolean verbose = this.verbose;
        File tmpCopy = null;
        ZipOutputStream zo = null;
        OutputStream out = null;

        JarFile jar = new JarFile(source);

        if (modify) {
            tmpCopy = File.createTempFile(source.getName().substring(0, source.getName().lastIndexOf('.')), "jmp");
            out = zo = new ZipOutputStream(new FileOutputStream(tmpCopy));
        } else { 
            if (outputFile == null) {
                outputFile = new File(source.getName().replace('.', '-') + ".idx");
            }
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

            if (modify) {
                zo.putNextEntry(new ZipEntry("META-INF/jandex.idx"));
            }

            IndexWriter writer = new IndexWriter(out);
            Index index = indexer.complete();
            int bytes = writer.write(index);
           

            if (modify) {
                source.delete();
                tmpCopy.renameTo(source);
                tmpCopy = null;
            }
            return new Result(index, modify ? "META-INF/jandex.idx" : outputFile.getPath(),  bytes);
        } finally {
            out.flush();
            out.close();
            if (tmpCopy != null)
                tmpCopy.delete();
        }
    }

    private void printIndexEntryInfo(ClassInfo info) {
        System.out.println("Indexed " + info.name() + " (" + info.annotations().size() + " annotations)");
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.flush();
    }

    private void scanFile(File source, Indexer indexer) throws FileNotFoundException, IOException {
        if (source.isDirectory()) {
            File[] children = source.listFiles();
            if (children == null)
                throw new FileNotFoundException("Source directory disappeared: " + source);

            for (File child : children)
                scanFile(child, indexer);

            return;
        }

        FileInputStream input = new FileInputStream(source);
        ClassInfo info = indexer.index(input);
        if (verbose)
            printIndexEntryInfo(info);
        
        return;
    }

    private static void printUsage() {
        System.out.println("Usage: jandex [-v] [-m] [-o file-name] <directory> | <jar>");
        System.out.println("        -or-");
        System.out.println("       jandex [-d] <index-file-name>");
        System.out.println("Options:");
        System.out.println("  -v  verbose output");
        System.out.println("  -m  modify directory or jar instead of creating an external index file");
        System.out.println("  -o  name the external index file file-name");
        System.out.println("  -d  dump the index file index-file-name");
        System.out.println("\nThe default behavior, with no options specified, is to autogenerate an external index file");
    }

    private void parseOptions(String[] args) {
        int optionCount = 0;
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.length() < 2 || arg.charAt(0) != '-') {
                if (source != null)
                    throw new IllegalArgumentException("Only one source location can be specified");

                source = new File(arg);
                if (!source.exists())
                    throw new IllegalArgumentException("Source directory/jar not found: " + source.getName());
                
                continue;
            }

           
            switch (arg.charAt(1)) {
                case 'm':
                    modify = true;
                    optionCount++;
                    break;
                case 'd':
                    dump = true;
                    optionCount++;
                    break;
                case 'v':
                    verbose = true;
                    optionCount++;
                    break;
                case 'o':
                    if (i >= args.length)
                        throw new IllegalArgumentException("-o reuires an output file name");

                    String name = args[++i];
                    if (name.length() < 1)
                        throw new IllegalArgumentException("-o reuires an output file name");

                    outputFile = new File(name);
                    optionCount++;
                    break;
                default:
                    throw new IllegalArgumentException("Option not understood: " + arg);
            }
        }
        
        if (source == null)
            throw new IllegalArgumentException("Source location not specified");
        
        if (outputFile != null && modify)
            throw new IllegalArgumentException("-o and -m are mutually exclusive");
        
        if (dump && optionCount != 1)
            throw new IllegalArgumentException("-d can not be specified with other options");
       
    }

}
