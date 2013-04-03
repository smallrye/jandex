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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An annotation instance represents a specific usage of an annotation on a
 * target. It contains a set of values, as well as a reference to the target
 * itself (e.g. class, field, method, etc).
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe
 * publication.
 *
 * @author Jason T. Greene
 *
 */
public final class AnnotationInstance {
    private static final AnnotationValue[] ANNOTATION_VALUES_TYPE = new AnnotationValue[0];

    private final DotName name;
    private final AnnotationTarget target;
    private final AnnotationValue[] values;

    AnnotationInstance(DotName name, AnnotationTarget target, AnnotationValue[] values) {
        this.name = name;
        this.target = target;
        this.values = values.length > 0 ? values : AnnotationValue.EMPTY_VALUE_ARRAY;
    }

    /**
     * Construct a new mock annotation instance. The passed values array will be defensively copied.
     *
     * @param name the name of the annotation instance
     * @param target the thing the annotation is declared on
     * @param values the values of this annotation instance
     * @return the new mock Annotation Instance
     */
    public static final AnnotationInstance create(DotName name, AnnotationTarget target, AnnotationValue[] values) {
        if (name == null)
            throw new IllegalArgumentException("Name can't be null");

        if (values == null)
            throw new IllegalArgumentException("Values can't be null");

        values = values.clone();

        // Sort entries so they can be binary searched
        Arrays.sort(values, new Comparator<AnnotationValue>() {
            public int compare(AnnotationValue o1, AnnotationValue o2) {
                return o1.name().compareTo(o2.name());
            }
        });

        return new AnnotationInstance(name, target, values);
    }

    /**
     * Construct a new mock annotation instance. The passed values list will be defensively copied.
     *
     * @param name the name of the annotation instance
     * @param target the thing the annotation is declared on
     * @param values the values of this annotation instance
     * @return the new mock Annotation Instance
     */
    public static final AnnotationInstance create(DotName name, AnnotationTarget target, List<AnnotationValue> values) {
        if (name == null)
            throw new IllegalArgumentException("Name can't be null");

        if (values == null)
            throw new IllegalArgumentException("Values can't be null");

        return create(name, target, values.toArray(ANNOTATION_VALUES_TYPE));
    }

    /**
     * The name of this annotation in DotName form.
     *
     * @return the name of this annotation
     */
    public DotName name() {
        return name;
    }

    /**
     * The Java element that this annotation was declared on. This can be
     * a class, a field, a method, or a method parameter. In addition it may
     * be null if this instance is a nested annotation, in which case there is
     * no target.
     *
     * @return the target this annotation instance refers to
     */
    public AnnotationTarget target() {
        return target;
    }

    /**
     * Returns a value that corresponds with the specified parameter name.
     * If the parameter was not specified by this instance then null is
     * returned. Note that this also applies to a defaulted parameter,
     * which is not recorded in the target class.
     *
     * @param name the parameter name
     * @return the value of the specified parameter, or null if not provided
     */
    public AnnotationValue value(final String name) {
        int result = Arrays.binarySearch(values, name, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                return ((AnnotationValue)o1).name().compareTo(name);
            }
        });
        return result >= 0 ? values[result] : null;
    }

    /**
     * Returns the value that is associated with the special default "value"
     * parameter.
     *
     * @return the "value" value
     */
    public AnnotationValue value() {
        return value("value");
    }

    /**
     * Returns a list of all parameter values on this annotation instance.
     * While random access is allowed, the ordering algorithm
     * of the list should not be relied upon. Although it will
     * be consistent for the life of this instance.
     *
     * @return the parameter values of this annotation
     */
    public List<AnnotationValue> values() {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("@").append(name).append("(");
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if (i < values.length - 1)
                builder.append(",");
        }
        builder.append(')');
        if (target != null)
            builder.append(" on ").append(target);

        return builder.toString();
    }
}
