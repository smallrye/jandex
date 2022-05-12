package org.jboss.jandex.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jboss.jandex.ClassSummary;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

/**
 * Generate a Jandex index inside a given JAR.
 */
@Mojo(name = "jandex-jar", threadSafe = true)
public class JandexJarGoal extends AbstractMojo {
    /**
     * The JAR that should be indexed and inside which the index should be stored.
     */
    @Parameter(required = true)
    private File jar;

    /**
     * Path to the index inside the JAR. Defaults to <code>META-INF/jandex.idx</code>.
     */
    @Parameter(defaultValue = "META-INF/jandex.idx")
    private String indexName;

    /**
     * Names or glob patterns of files in the JAR that should be indexed.
     */
    @Parameter
    private List<String> includes;

    /**
     * Names or glob patterns of files in the JAR that should <em>not</em> be indexed.
     * Excludes have priority over includes.
     */
    @Parameter
    private List<String> excludes;

    @Parameter(defaultValue = "true")
    private boolean useDefaultExcludes;

    /**
     * Print verbose output (debug output without needing to enable -X for the whole build).
     */
    @Parameter(defaultValue = "false")
    private boolean verbose;

    /**
     * Skip execution if set.
     */
    @Parameter(property = "jandex.skip", defaultValue = "false")
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Jandex execution skipped");
            return;
        }

        if (!jar.isFile()) {
            getLog().warn("Skipping, expected JAR does not exist or is not a file: " + jar);
            return;
        }

        Index index = indexJar();

        getLog().info("Saving Jandex index into JAR: " + jar);
        Path tmp = createTempFile("jandextmp");
        try (ZipFile zip = new ZipFile(jar);
                ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(tmp.toFile().toPath()))) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || entry.getName().equals(indexName)) {
                    continue;
                }

                ZipEntry newEntry = new ZipEntry(entry);
                // Compression level and format can vary across implementations
                if (newEntry.getMethod() != ZipEntry.STORED) {
                    newEntry.setCompressedSize(-1);
                }
                out.putNextEntry(newEntry);
                try (InputStream in = zip.getInputStream(entry)) {
                    copy(in, out);
                }
            }

            out.putNextEntry(new ZipEntry(indexName));
            new IndexWriter(out).write(index);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException e1) {
                e.addSuppressed(e1);
            }
            throw new MojoExecutionException(e.getMessage(), e);
        }

        Path originalJar = jar.toPath();
        Path backupJar = createTempFile("jandexbackup");

        try {
            Files.move(originalJar, backupJar, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tmp, originalJar);
            Files.delete(backupJar);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Index indexJar() throws MojoExecutionException {
        ArchiveScanner scanner = new ArchiveScanner(jar);
        scanner.setFilenameComparator(String::compareTo);
        if (useDefaultExcludes) {
            scanner.addDefaultExcludes();
        }
        if (includes != null) {
            scanner.setIncludes(includes.toArray(new String[0]));
        }
        if (excludes != null) {
            scanner.setExcludes(excludes.toArray(new String[0]));
        }
        scanner.scan();
        String[] filesInJar = scanner.getIncludedFiles();

        Indexer indexer = new Indexer();
        try (ZipFile zip = new ZipFile(jar)) {
            for (String file : filesInJar) {
                if (file.endsWith(".class")) {
                    try (InputStream in = zip.getInputStream(zip.getEntry(file))) {
                        ClassSummary info = indexer.indexWithSummary(in);
                        if (isVerbose() && info != null) {
                            getLog().info("Indexed " + info.name() + " (" + info.annotationsCount() + " annotations)");
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        return indexer.complete();
    }

    private Path createTempFile(String suffix) throws MojoExecutionException {
        try {
            return Files.createTempFile(jar.toPath().getParent(), jar.getName(), suffix);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.flush();
    }

    private boolean isVerbose() {
        return verbose || getLog().isDebugEnabled();
    }
}
