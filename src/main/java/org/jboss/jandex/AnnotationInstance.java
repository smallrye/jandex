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
 * Represents a specific usage of an annotation on a target
 *
 * @author Jason T. Greene
 *
 */
public final class AnnotationInstance {
    private DotName name;
    private AnnotationTarget target;
    private AnnotationValue[] values;

    AnnotationInstance(DotName name, AnnotationTarget target, AnnotationValue[] values) {
        this.name = name;
        this.target = target;
        this.values = values;
    }

    public DotName name() {
        return name;
    }

    public AnnotationTarget target() {
        return target;
    }

    public AnnotationValue value(String name) {
        int result = Arrays.binarySearch(values, name);
        return result >= 0 ? values[result] : null;
    }

    public AnnotationValue value() {
        return value("value");
    }

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
