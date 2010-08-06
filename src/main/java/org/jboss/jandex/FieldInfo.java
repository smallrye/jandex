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

import java.lang.reflect.Modifier;

/**
 * Represents a field that was annotated.
 *
 * <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 *
 */
public final class FieldInfo implements AnnotationTarget {
    private final String name;
    private final Type type;
    private final short flags;
    private final ClassInfo clazz;

    FieldInfo(ClassInfo clazz, String name, Type type, short flags) {
        this.clazz = clazz;
        this.name = name;
        this.type = type;
        this.flags = flags;
    }


    /**
     * Returns the local name of the field
     *
     * @return the local name of the field
     */
    public final String name() {
        return name;
    }

    /**
     * Returns the class which declared the field
     *
     * @return the declaring class
     */
    public final ClassInfo declaringClass() {
        return clazz;
    }

    /**
     * Returns the Java Type of this field.
     *
     * @return the type
     */
    public final Type type() {
        return type;
    }

    /**
     * Returns the access fields of this field. {@link Modifier} can be used on this value.
     *
     * @return the access flags of this field
     */
    public final short flags() {
        return flags;
    }

    public String toString() {
        return type + " " + clazz.name() + "." + name;
    }
}
