/*******************************************************************************
 * Copyright (C) 2013 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.jboss.jandex.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

/**
 * Generate a Jandex index for classes compiled as part of the current project.
 * 
 * @goal jandex
 * @phase process-classes
 * @author jdcasey
 */
public class JandexGoal
    implements Mojo
{

    /**
     * By default, process the classes compiled for the project. If you need to process other sets of classes, such as
     * test classes, see the "fileSets" parameter.
     * 
     * @parameter default-value="${project.build.outputDirectory}"
     * @readonly
     */
    private File classesDir;

    /**
     * Process the classes found in these file-sets, after considering the specified includes and excludes, if any. The
     * format is: <br/>
     * 
     * <pre>
     * <code>
     * &lt;fileSets&gt;
     *   &lt;fileSet&gt;
     *     &lt;directory&gt;path-or-expression&lt;/directory&gt;
     *     &ltincludes&gt;
     *       &lt;include&gt;some/thing/*.good&lt;/include&gt;
     *     &lt;includes&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;some/thing/*.bad&lt;/exclude&gt;
     *     &lt;/excludes&lt;
     *   &lt;/fileSet&gt;
     * &lt;/fileSets&gt;
     * </code>
     * </pre>
     * 
     * <br>
     * <em>NOTE: Standard globbing expressions are supported in includes/excludes.</em>
     * 
     * @parameter
     */
    private List<FileSet> fileSets;

    /**
     * If true, construct an implied file-set using the target/classes directory, and process the classes there.
     * 
     * @parameter default-value="true"
     */
    private final boolean processDefaultFileSet = true;

    /**
     * Print verbose output (debug output without needing to enable -X for the whole build)
     * 
     * @parameter default-value="false"
     */
    private boolean verbose = false;

    /**
     * Skip execution if set.
     * 
     * @parameter default-value="false" expression="${jandex.skip}"
     */
    private boolean skip = true;

    private Log log;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "Jandex execution skipped." );
            return;
        }

        if ( processDefaultFileSet )
        {
            boolean found = false;
            if ( fileSets != null )
            {
                for ( final FileSet fileset : fileSets )
                {
                    if ( fileset.getDirectory()
                                .equals( classesDir ) )
                    {
                        found = true;
                    }
                }
            }

            if ( !found )
            {
                final FileSet fs = new FileSet();
                fs.setDirectory( classesDir );
                fs.setIncludes( Collections.singletonList( "**/*.class" ) );

                if ( fileSets == null )
                {
                    fileSets = new ArrayList<FileSet>();
                }

                fileSets.add( fs );
            }
        }

        final Indexer indexer = new Indexer();
        for ( final FileSet fileset : fileSets )
        {
            final File dir = fileset.getDirectory();
            if ( !dir.exists() )
            {
                getLog().error( "[SKIP] Cannot process fileset in directory: " + fileset.getDirectory()
                                    + ". Directory does not exist!" );
                continue;
            }

            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( dir );

            if ( fileset.isUseDefaultExcludes() )
            {
                scanner.addDefaultExcludes();
            }

            final List<String> includes = fileset.getIncludes();
            if ( includes != null )
            {
                scanner.setIncludes( includes.toArray( new String[] {} ) );
            }

            final List<String> excludes = fileset.getExcludes();
            if ( excludes != null )
            {
                scanner.setExcludes( excludes.toArray( new String[] {} ) );
            }

            scanner.scan();
            final String[] files = scanner.getIncludedFiles();

            for ( final String file : files )
            {
                if ( file.endsWith( ".class" ) )
                {
                    FileInputStream fis = null;
                    try
                    {
                        fis = new FileInputStream( new File( dir, file ) );

                        final ClassInfo info = indexer.index( fis );
                        if ( doVerbose() && info != null )
                        {
                            getLog().info( "Indexed " + info.name() + " (" + info.annotations()
                                                                                 .size() + " annotations)" );
                        }
                    }
                    catch ( final Exception e )
                    {
                        throw new MojoExecutionException( e.getMessage(), e );
                    }
                    finally
                    {
                        IOUtil.close( fis );
                    }
                }
            }

            final File idx = new File( dir, "META-INF/jandex.idx" );
            idx.getParentFile()
               .mkdirs();

            FileOutputStream indexOut = null;
            try
            {
                indexOut = new FileOutputStream( idx );
                final IndexWriter writer = new IndexWriter( indexOut );
                final Index index = indexer.complete();
                writer.write( index );
            }
            catch ( final IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
            finally
            {
                IOUtil.close( indexOut );
            }
        }

    }

    private boolean doVerbose()
    {
        return verbose || getLog().isDebugEnabled();
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    public void setVerbose( final boolean verbose )
    {
        this.verbose = verbose;
    }

    public boolean isSkip()
    {
        return skip;
    }

    public void setSkip( final boolean skip )
    {
        this.skip = skip;
    }

    public void setLog( final Log log )
    {
        this.log = log;
    }

    public Log getLog()
    {
        return log;
    }

}
