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
