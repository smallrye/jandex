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
import java.util.List;

/**
 * The result from a jar indexing operation.
 *
 * @author Jason T. Greene
 * @author Stuart Douglas
 * @author Ales Justin
 */
public final class Result {
    private final Index index;
    private final int annotations;
    private final int instances;
    private final int classes;
    private final int bytes;
    private final String name;
    private final File outputFile;

    Result(Index index, String name, int bytes, File outputFile) {
        this.index = index;
        annotations = index.annotations.size();
        instances = countInstances(index);
        classes = index.classes.size();
        this.bytes = bytes;
        this.name = name;
        this.outputFile = outputFile;
    }

    private int countInstances(Index index) {
        int c = 0;
        for (List<AnnotationInstance> list : index.annotations.values())
            c += list.size();

        return c;
    }

    public Index getIndex() {
        return index;
    }

    public int getAnnotations() {
        return annotations;
    }

    public int getBytes() {
        return bytes;
    }

    public int getClasses() {
        return classes;
    }

    public int getInstances() {
        return instances;
    }

    public String getName() {
        return name;
    }

    public File getOutputFile() {
        return outputFile;
    }
}
