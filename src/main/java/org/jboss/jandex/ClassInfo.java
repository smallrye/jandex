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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a class entry in an index. A ClassInfo is only a partial view of a
 * Java class, it is not intended as a complete replacement for Java reflection.
 * Only the methods and fields which are references by an annotation are stored.
 *
 * <p>Global information including the parent class, implemented interfaces, and
 * access flags are also provided since this information is often necessary.
 *
 * <p>Note that a parent class and interface may exist outside of the scope of the
 * index (e.g. classes in a different jar) so the references are stored as names
 * instead of direct references. It is expected that multiple indexes may need
 * to be queried to assemble a full hierarchy in a complex multi-jar environment
 * (e.g. an application server).
 *
 * <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 *
 */
public final class ClassInfo implements AnnotationTarget {
    private final DotName name;
    private short flags;
    private final DotName superName;
    private final DotName[] interfaces;
    private final Map<DotName, List<AnnotationInstance>> annotations;

    ClassInfo(DotName name, DotName superName, short flags, DotName[] interfaces, Map<DotName, List<AnnotationInstance>> annotations) {
        this.name = name;;
        this.superName = superName;
        this.flags = flags;
        this.interfaces = interfaces;
        this.annotations = Collections.unmodifiableMap(annotations);
    }

    public String toString() {
        return name.toString();
    }

    public final DotName name() {
        return name;
    }

    public final short flags() {
        return flags;
    }

    public final DotName superName() {
        return superName;
    }

    public final DotName[] interfaces() {
        return interfaces;
    }

    public final Map<DotName, List<AnnotationInstance>> annotations() {
        return annotations;
    }
}
