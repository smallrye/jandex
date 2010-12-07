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

import java.util.Arrays;
import java.util.Collections;
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
    private final DotName name;
    private final AnnotationTarget target;
    private final AnnotationValue[] values;

    AnnotationInstance(DotName name, AnnotationTarget target, AnnotationValue[] values) {
        this.name = name;
        this.target = target;
        this.values = values;
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
     *
     * @param name the parameter name
     * @return the value of the specified parameter
     */
    public AnnotationValue value(String name) {
        int result = Arrays.binarySearch(values, name);
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
