package org.jboss.jandex.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.jboss.jandex.ClassSummary;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

/**
 * Generate a Jandex index for classes compiled as part of the current project.
 */
@Mojo(name = "jandex", defaultPhase = LifecyclePhase.PROCESS_CLASSES, threadSafe = true)
public class JandexGoal extends AbstractMojo {

    /**
     * By default, process the classes compiled for the project. If you need to process other sets of classes, such as
     * test classes, see the <code>fileSets</code> parameter.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File classesDir;

    /**
     * Process the classes found in these file sets, after considering the specified includes and excludes, if any.
     * The format is:
     *
     * <pre>
     * <code>
     * &lt;fileSets&gt;
     *   &lt;fileSet&gt;
     *     &lt;directory&gt;path-or-expression&lt;/directory&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;some/thing/*.good&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;some/thing/*.bad&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/fileSet&gt;
     * &lt;/fileSets&gt;
     * </code>
     * </pre>
     *
     * NOTE: Standard globbing expressions are supported in includes/excludes.
     */
    @Parameter
    private List<FileSet> fileSets;

    /**
     * If true, and if a file set rooted in the <code>target/classes</code> directory is not defined explicitly,
     * an implied file set rooted in the <code>target/classes</code> directory will be used.
     */
    @Parameter(defaultValue = "true")
    private boolean processDefaultFileSet;

    /**
     * Print verbose output (debug output without needing to enable -X for the whole build)
     */
    @Parameter(defaultValue = "false")
    private boolean verbose;

    /**
     * The directory in which the index file will be created.
     * Defaults to <code>${project.build.outputDirectory}/META-INF</code>.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}/META-INF")
    private File indexDir;

    /**
     * The name of the index file. Defaults to <code>jandex.idx</code>.
     */
    @Parameter(defaultValue = "jandex.idx")
    private String indexName;

    /**
     * Skip execution if set.
     */
    @Parameter(property = "jandex.skip", defaultValue = "false")
    private boolean skip = true;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Jandex execution skipped.");
            return;
        }

        if (fileSets == null) {
            fileSets = new ArrayList<>();
        }

        if (processDefaultFileSet) {
            boolean explicitlyConfigured = false;
            for (FileSet fileset : fileSets) {
                if (fileset.getDirectory().equals(classesDir)) {
                    explicitlyConfigured = true;
                    break;
                }
            }

            if (!explicitlyConfigured) {
                FileSet fs = new FileSet();
                fs.setDirectory(classesDir);
                fs.setIncludes(Collections.singletonList("**/*.class"));
                fileSets.add(fs);
            }
        }

        Indexer indexer = new Indexer();
        for (FileSet fileset : fileSets) {
            File dir = fileset.getDirectory();
            if (!dir.exists()) {
                getLog().info("[SKIP] Cannot process fileset in directory: " + fileset.getDirectory()
                        + ". Directory does not exist!");
                continue;
            }

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(dir);
            // order files to get reproducible result
            scanner.setFilenameComparator(new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareTo(s2);
                }
            });

            if (fileset.isUseDefaultExcludes()) {
                scanner.addDefaultExcludes();
            }

            List<String> includes = fileset.getIncludes();
            if (includes != null) {
                scanner.setIncludes(includes.toArray(new String[0]));
            }

            List<String> excludes = fileset.getExcludes();
            if (excludes != null) {
                scanner.setExcludes(excludes.toArray(new String[0]));
            }

            scanner.scan();
            String[] files = scanner.getIncludedFiles();

            for (String file : files) {
                if (file.endsWith(".class")) {
                    try (InputStream fis = Files.newInputStream(new File(dir, file).toPath())) {
                        ClassSummary info = indexer.indexWithSummary(fis);
                        if (isVerbose() && info != null) {
                            getLog().info("Indexed " + info.name() + " (" + info.annotationsCount()
                                    + " annotations)");
                        }
                    } catch (Exception e) {
                        throw new MojoExecutionException(e.getMessage(), e);
                    }
                }
            }
        }
        Index index = indexer.complete();

        File indexFile = new File(indexDir, indexName);
        getLog().info("Saving Jandex index: " + indexFile);
        try {
            Files.createDirectories(indexDir.toPath());
            try (OutputStream out = Files.newOutputStream(indexFile.toPath())) {
                IndexWriter writer = new IndexWriter(out);
                writer.write(index);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not save index " + indexFile, e);
        }
    }

    private boolean isVerbose() {
        return verbose || getLog().isDebugEnabled();
    }
}
