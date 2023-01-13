package org.jboss.jandex.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.io.CachingOutputStream;
import org.jboss.jandex.ClassSummary;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

/**
 * Generate a Jandex index for classes compiled as part of the current project.
 */
@Mojo(name = "jandex", defaultPhase = LifecyclePhase.PROCESS_CLASSES, threadSafe = true, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class JandexGoal extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

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
     * </pre>
     *
     * Instead of the <code>directory</code> element, a <code>dependency</code> element may be used.
     * In that case, if the project has a corresponding dependency, classes in its artifact are processed.
     * The <code>dependency</code> element must specify a <code>groupId</code> and an <code>artifactId</code>
     * and may specify a <code>classifier</code>:
     *
     * <pre>
     * &lt;fileSets&gt;
     *   &lt;fileSet&gt;
     *     &lt;dependency&gt;
     *       &lt;groupId&gt;com.example&lt;/groupId&gt;
     *       &lt;artifactId&gt;my-project&lt;/artifactId&gt;
     *       &lt;classifier&gt;tests&lt;/artifactId&gt;
     *     &lt;/dependency&gt;
     *     &lt;includes&gt;
     *       &lt;include&gt;some/thing/*.good&lt;/include&gt;
     *     &lt;/includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;some/thing/*.bad&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/fileSet&gt;
     * &lt;/fileSets&gt;
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
     * Print verbose output (debug output without needing to enable -X for the whole build).
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
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Jandex execution skipped");
            return;
        }

        if (fileSets == null) {
            fileSets = new ArrayList<>();
        }

        if (processDefaultFileSet) {
            boolean explicitlyConfigured = false;
            for (FileSet fileset : fileSets) {
                if (fileset.getDirectory() != null && fileset.getDirectory().equals(classesDir)) {
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
        for (FileSet fileSet : fileSets) {
            if (fileSet.getDirectory() == null && fileSet.getDependency() == null) {
                throw new MojoExecutionException("File set must specify either directory or dependency");
            }
            if (fileSet.getDirectory() != null && fileSet.getDependency() != null) {
                throw new MojoExecutionException("File set may not specify both directory and dependency");
            }

            if (fileSet.getDirectory() != null) {
                indexDirectory(indexer, fileSet);
            } else if (fileSet.getDependency() != null) {
                indexDependency(indexer, fileSet);
            }
        }
        Index index = indexer.complete();

        File indexFile = new File(indexDir, indexName);
        getLog().info("Saving Jandex index: " + indexFile);
        try {
            Files.createDirectories(indexDir.toPath());
            try (OutputStream out = new CachingOutputStream(indexFile)) {
                IndexWriter writer = new IndexWriter(out);
                writer.write(index);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not save index " + indexFile, e);
        }
    }

    private void indexDirectory(Indexer indexer, FileSet fileSet) throws MojoExecutionException {
        File dir = fileSet.getDirectory();
        if (!dir.exists()) {
            getLog().warn("Skipping file set, directory does not exist: " + fileSet.getDirectory());
            return;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(dir);
        String[] files = findFilesToIndex(fileSet, scanner);
        for (String file : files) {
            if (file.endsWith(".class")) {
                try (InputStream in = Files.newInputStream(new File(dir, file).toPath())) {
                    ClassSummary info = indexer.indexWithSummary(in);
                    if (isVerbose() && info != null) {
                        getLog().info("Indexed " + info.name() + " (" + info.annotationsCount() + " annotations)");
                    }
                } catch (Exception e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        }
    }

    private void indexDependency(Indexer indexer, FileSet fileSet) throws MojoExecutionException {
        Dependency dependency = fileSet.getDependency();
        if (dependency.getGroupId() == null) {
            throw new MojoExecutionException("Dependency in file set must specify groupId");
        }
        if (dependency.getArtifactId() == null) {
            throw new MojoExecutionException("Dependency in file set must specify artifactId");
        }

        Artifact artifact = null;
        for (Artifact candidate : mavenProject.getArtifacts()) {
            if (candidate.getGroupId().equals(dependency.getGroupId())
                    && candidate.getArtifactId().equals(dependency.getArtifactId())
                    && (dependency.getClassifier() == null || candidate.getClassifier().equals(dependency.getClassifier()))) {
                artifact = candidate;
                break;
            }
        }
        if (artifact == null) {
            getLog().warn("Skipping file set, artifact not found among this project dependencies: " + dependency);
            return;
        }

        File archive = artifact.getFile();
        if (archive == null) {
            getLog().warn("Skipping file set, artifact file does not exist for dependency: " + dependency);
            return;
        }

        ArchiveScanner scanner = new ArchiveScanner(archive);
        String[] files = findFilesToIndex(fileSet, scanner);
        try (ZipFile zip = new ZipFile(archive)) {
            for (String file : files) {
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
    }

    private String[] findFilesToIndex(FileSet fileSet, Scanner scanner) {
        // order files to get reproducible result
        scanner.setFilenameComparator(String::compareTo);

        if (fileSet.isUseDefaultExcludes()) {
            scanner.addDefaultExcludes();
        }

        List<String> includes = fileSet.getIncludes();
        if (includes != null) {
            scanner.setIncludes(includes.toArray(new String[0]));
        }

        List<String> excludes = fileSet.getExcludes();
        if (excludes != null) {
            scanner.setExcludes(excludes.toArray(new String[0]));
        }

        scanner.scan();
        return scanner.getIncludedFiles();
    }

    private boolean isVerbose() {
        return verbose || getLog().isDebugEnabled();
    }
}
