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
