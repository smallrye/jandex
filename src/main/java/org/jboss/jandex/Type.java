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

/**
 * Represents a Java type declaration that is specified on methods or fields. A
 * type can be any class based type (interface, class, annotation), any
 * primitive, any array, or void.
 *
 * @author Jason T. Greene
 */
public final class Type {
    private final DotName name;
    private final Kind kind;

    /**
     * Represents a "kind" of Type.
     *
     * @author Jason T. Greene
     *
     */
    public enum Kind {
        /** A Java class, interface, or annotation */
        CLASS,

        /** A Java array */
        ARRAY,

        /**
         * A Java primitive (boolean, byte, short, char, int, long, float,
         * double)
         */
        PRIMITIVE,

        /** Used to designate a Java method that returns nothing */
        VOID;

        /**
         * This method exists since the brainiacs that designed java thought
         * that not only should enums be complex objects instead of simple
         * integral types like every other sane language, they also should have
         * the sole mechanism to reverse an ordinal (values() method) perform an
         * array copy.
         */
        public static Kind fromOrdinal(int ordinal) {

            switch (ordinal) {
                case 0:
                    return CLASS;
                case 1:
                    return ARRAY;
                case 2:
                    return PRIMITIVE;
                default:
                case 3:
                    return VOID;
            }
        }
    };

    Type(DotName name, Kind kind) {
        this.name = name;
        this.kind = kind;
    }

    public static final Type create(DotName name, Kind kind) {
        if (name == null)
            throw new IllegalArgumentException("name can not be null!");

        if (kind == null)
            throw new IllegalArgumentException("kind can not be null!");

        return new Type(name, kind);
    }

    /**
     * Returns the name of this type. Primitives and void are returned as the
     * Java reserved word (void, boolean, byte, short, char, int, long, float,
     * double). Arrays are returned using the internal JVM array syntax (see JVM
     * specification). Classes are returned as a normal DotName.
     *
     * @return the name of this type
     */
    public DotName name() {
        return name;
    }

    /**
     * Returns the kind of Type this is.
     *
     * @return the kind
     */
    public Kind kind() {
        return kind;
    }

    public String toString() {
        return name.toString();
    }
}
