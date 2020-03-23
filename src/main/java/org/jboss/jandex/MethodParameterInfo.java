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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an individual Java method parameter that was annotated.
 *
 * <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 */
public final class MethodParameterInfo implements AnnotationTarget {
    private final MethodInfo method;
    private final short position;

    MethodParameterInfo(MethodInfo method, short position) {
        this.method = method;
        this.position = position;
    }

    /**
     * Constructs a new mock method parameter info
     *
     * @param method the method containing this parameter.
     * @param position the zero based index of this parameter
     * @return the new mock parameter info
     */
    public static MethodParameterInfo create(MethodInfo method, short position) {
        return new MethodParameterInfo(method, position);
    }

    /**
     * Returns the method this parameter belongs to.
     *
     * @return the declaring Java method
     */
    public final MethodInfo method() {
        return method;
    }

    /**
     * Returns the 0 based position of this parameter.
     *
     * @return the position of this parameter
     */
    public final short position() {
        return position;
    }

    /**
     * Returns the name of this parameter.
     * 
     * @return the name of this parameter.
     */
    public final String name() {
        return method.parameterName(position);
    }

    /**
     * Returns the {@link AnnotationInstance}s declared on this parameter.
     *
     * @return the annotation instances declared on this parameter, or an empty list if none
     */
    public final List<AnnotationInstance> annotations() {

        final List<AnnotationInstance> paramAnnotations = new ArrayList<AnnotationInstance>();
        final List<AnnotationInstance> methodAnnotations = method.annotations();

        for (AnnotationInstance annotation : methodAnnotations) {
            final AnnotationTarget target = annotation.target();
            if (target.kind() != AnnotationTarget.Kind.METHOD_PARAMETER) {
                continue;
            }
            if (target.asMethodParameter().position() != position) {
                continue;
            }
            paramAnnotations.add(annotation);
        }

        if (paramAnnotations.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(paramAnnotations);
    }

    /**
     * Retrieves an {@link AnnotationInstance} representing annotation
     * declared on this parameter. If an annotation by that name is not
     * present, <code>null</code> will be returned instead.
     *
     * @param name the name of the annotation to locate
     * @return the annotation if found, otherwise, <code>null</code>
     */
    public final AnnotationInstance annotation(DotName name) {

        final AnnotationInstance annotation = method.annotation(name);

        if (annotation == null) {
            return null;
        }

        final AnnotationTarget target = annotation.target();

        if (target.kind() != AnnotationTarget.Kind.METHOD_PARAMETER) {
            return null;
        }
        if (target.asMethodParameter().position() != position) {
            return null;
        }

        return annotation;
    }

    /**
     * Check whether or not the {@link AnnotationInstance} with the given
     * name occurs on this parameter.
     *
     * @see #annotations()
     * @see #annotation(DotName)
     * @param name the name of the annotation to look for
     * @return true if the annotation is present, false otherwise
     */
    public final boolean hasAnnotation(DotName name) {
        return annotation(name) != null;
    }

    /**
     * Returns a string representation describing this method parameter
     *
     * @return a string representation of this parameter
     */
    public String toString() {
        return method + " #" + position;
    }

    @Override
    public final ClassInfo asClass() {
        throw new IllegalArgumentException("Not a class");
    }

    @Override
    public final FieldInfo asField() {
        throw new IllegalArgumentException("Not a field");
    }

    @Override
    public final MethodInfo asMethod() {
        throw new IllegalArgumentException("Not a method");
    }

    @Override
    public final MethodParameterInfo asMethodParameter() {
        return this;
    }

    @Override
    public final TypeTarget asType() {
        throw new IllegalArgumentException("Not a type");
    }

    @Override
    public Kind kind() {
        return Kind.METHOD_PARAMETER;
    }
}
