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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a class entry in an index. A ClassInfo is only a partial view of a
 * Java class, it is not intended as a complete replacement for Java reflection.
 * Only the methods and fields which are references by an annotation are stored.
 *
 * <p>Global information including the parent class, implemented methodParameters, and
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
    private final short flags;
    private final DotName superName;
    private final DotName[] interfaces;
    private final Map<DotName, List<AnnotationInstance>> annotations;

    // Not final to allow lazy initialization, immutable once published
    private boolean hasNoArgsConstructor;

    ClassInfo(DotName name, DotName superName, short flags, DotName[] interfaces, Map<DotName, List<AnnotationInstance>> annotations) {
        this(name, superName, flags, interfaces, annotations, false);
    }

    ClassInfo(DotName name, DotName superName, short flags, DotName[] interfaces, Map<DotName, List<AnnotationInstance>> annotations, boolean hasNoArgsConstructor) {
        this.name = name;
        this.superName = superName;
        this.flags = flags;
        this.interfaces = interfaces;
        this.annotations = Collections.unmodifiableMap(annotations);
        this.hasNoArgsConstructor = hasNoArgsConstructor;
    }

    /**
     * Constructs a "mock" ClassInfo using the passed values. All passed values MUST NOT BE MODIFIED AFTER THIS CALL.
     * Otherwise the resulting object would not conform to the contract outlined above.
     *
     * @param name the name of this class
     * @param superName the name of the parent class
     * @param flags the class attributes
     * @param interfaces the methodParameters this class implements
     * @param annotations the annotations on this class
     * @return a new mock class representation
     */
    public static ClassInfo create(DotName name, DotName superName, short flags, DotName[] interfaces, Map<DotName, List<AnnotationInstance>> annotations, boolean hasNoArgsConstructor) {
        return new ClassInfo(name, superName, flags, interfaces, annotations, hasNoArgsConstructor);
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

    /**
     * Returns a boolean indicating the presence of a no-arg constructor, if supported by the underlying index store.
     * This information is available in indexes produced by Jandex 1.2.0 and later.
     *
     * @return <code>true</code> in case of the Java class has a no-args constructor, <code>false</code>
     *         if it does not, or it is not known
     * @since 1.2.0
     */
    public final boolean hasNoArgsConstructor() {
        return hasNoArgsConstructor;
    }

    /** Lazily initialize hasNoArgsConstructor. Can only be called before publication */
    void setHasNoArgsConstructor(boolean hasNoArgsConstructor) {
        this.hasNoArgsConstructor = hasNoArgsConstructor;
    }
}
