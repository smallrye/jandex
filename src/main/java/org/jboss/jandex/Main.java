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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Responsible for launching the indexing tool on a java command line.
 *
 * @author Jason T. Greene
 */
public class Main {

    private boolean modify;
    private boolean verbose;
    private boolean dump;
    private boolean jarFile;
    private File outputFile;
    private File source;
    private Index index;

    private Main()  {

    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        Main main = new Main();
        main.execute(args);
    }

    private void execute(String[] args) {
        boolean printUsage = true;
        try {
            parseOptions(args);

            printUsage = false;
            if (dump) {
                dumpIndex(source);
                return;
            }

            long start = System.currentTimeMillis();
            index = getIndex(start);
            outputFile = null;
            source = null;
        } catch (Exception e) {
            if (!verbose && (e instanceof IllegalArgumentException || e instanceof FileNotFoundException)) {
                System.err.println(e.getMessage() == null ? e.getClass().getSimpleName() : "ERROR: " + e.getMessage());
            } else {
                e.printStackTrace(System.err);
            }

            if (printUsage) {
                System.out.println();
                printUsage();
            }
        }
    }

    private Index getIndex(long start) throws IOException {
        Indexer indexer = new Indexer();
        Result result = (source.isDirectory()) ? indexDirectory(source, indexer) : JarIndexer.createJarIndex(source, indexer, outputFile, modify, jarFile, verbose);

        double time = (System.currentTimeMillis() - start) / 1000.00;
        System.out.printf("Wrote %s in %.4f seconds (%d classes, %d annotations, %d instances, %d bytes)\n", result.getName(), time, result.getClasses(), result.getAnnotations(), result.getInstances(), result.getBytes());
        return result.getIndex();
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
            return new Result(index, outputFile.getPath(), bytes, outputFile);
        } finally {
            out.flush();
            out.close();
        }
    }

    private void printIndexEntryInfo(ClassInfo info) {
        System.out.println("Indexed " + info.name() + " (" + info.annotations().size() + " annotations)");
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

        if (! source.getName().endsWith(".class"))
            return;

        FileInputStream input = new FileInputStream(source);

        try {
            ClassInfo info = indexer.index(input);
            if (verbose && info != null)
                printIndexEntryInfo(info);
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            System.err.println("ERROR: Could not index " + source.getName() + ": " + message);
            if (verbose)
                e.printStackTrace(System.err);
        } finally {
            safeClose(input);
        }
    }

    private void safeClose(FileInputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (Throwable t) {
                // EAT
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage: jandex [-v] [-m] [-o file-name] <directory> | <jar>");
        System.out.println("        -or-");
        System.out.println("       jandex [-d] <index-file-name>");
        System.out.println("Options:");
        System.out.println("  -v  verbose output");
        System.out.println("  -m  modify directory or jar instead of creating an external index file");
        System.out.println("  -o  name the external index file file-name");
        System.out.println("  -j  export the index file to a jar file");
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
                case 'j':
                    jarFile = true;
                    optionCount++;
                    break;
                case 'o':
                    if (i >= args.length)
                        throw new IllegalArgumentException("-o requires an output file name");

                    String name = args[++i];
                    if (name.length() < 1)
                        throw new IllegalArgumentException("-o requires an output file name");

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
