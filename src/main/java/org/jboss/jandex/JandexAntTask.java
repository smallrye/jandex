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