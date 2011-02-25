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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ant Task that indexes jars, and saves the resulting index
 * @author Stuart Douglas
 */
public class JandexAntTask  extends Task {

    private final List<FileSet> filesets = new ArrayList<FileSet>();

    private boolean modify = false;

    private boolean newJar = false;

    private boolean verbose = false;

    private boolean run = true;

    @Override
    public void execute() throws BuildException {
        if(!run) {
            return;
        }
        if(modify && newJar) {
            throw new BuildException("Specifying both modify and newJar does not make sense.");
        }
        Indexer indexer = new Indexer();
        for(FileSet fileset : filesets) {
            String[] files = fileset.getDirectoryScanner(getProject()).getIncludedFiles();
            for(String file : files) {
                if(file.endsWith(".jar")) {
                    try {
                        JarIndexer.createJarIndex(new File(fileset.getDir().getAbsolutePath() + "/" +file), indexer, modify, newJar,verbose);
                    } catch (IOException e) {
                       throw new BuildException(e);
                    }
                }
            }
        }

    }

    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }

    public boolean isModify() {
        return modify;
    }

    public void setModify(boolean modify) {
        this.modify = modify;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public boolean isNewJar() {
        return newJar;
    }

    public void setNewJar(boolean newJar) {
        this.newJar = newJar;
    }
}