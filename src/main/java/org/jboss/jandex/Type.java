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
import java.util.List;

/**
 * Represents a Java type declaration usage that is specified on methods, fields, classes,
 * annotations, or other types. A type can be any class based type (interface, class, annotation),
 * any primitive, any array, any generic type declaration, or void.
 *
 * <p>A type usage may have annotations associated with its declaration. A type is equal to
 * another type if, and only if, it represents the same exact definition including the annotations
 * specific to its usage.
 *
 * <p>To reduce memory overhead, type instances are often shared between their enclosing classes.
 *
 * @author Jason T. Greene
 */
public abstract class Type {
    public static final Type[] EMPTY_ARRAY = new Type[0];
    private static final AnnotationInstance[] EMPTY_ANNOTATIONS = new AnnotationInstance[0];
    private final DotName name;
    private final AnnotationInstance[] annotations;

    /**
     * Represents a "kind" of Type.
     *
     * @author Jason T. Greene
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
        VOID,

        /** A resolved generic type parameter or type argument */
        TYPE_VARIABLE,

        /**
         * An unresolved type parameter or argument. This is merely a placeholder
         * which occurs during an error condition or incomplete processing. In most
         * cases, it need not be dealt with.
         */
        UNRESOLVED_TYPE_VARIABLE,

        /** A generic wildcard type. */
        WILDCARD_TYPE,

        /** A generic parameterized type */
        PARAMETERIZED_TYPE;

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
                case 4:
                    return TYPE_VARIABLE;
                case 5:
                    return UNRESOLVED_TYPE_VARIABLE;
                case 6:
                    return WILDCARD_TYPE;
                case 7:
                    return PARAMETERIZED_TYPE;
            }
        }
    }

    Type(DotName name, AnnotationInstance[] annotations) {
        this.name = name;
        annotations = annotations == null ? EMPTY_ANNOTATIONS : annotations;

        if (annotations.length > 1) {
            Arrays.sort(annotations, AnnotationInstance.NAME_COMPARATOR);
        }

        this.annotations = annotations;
    }

    /**
     * Creates a type instance of the specified kind. Types of kind <code>CLASS</code>,
     * directly use the specified name. Types of kind <code>ARRAY</code> parse the name
     * in the Java reflection format (Java descriptor format changing / to '.',
     * e.g. "[[[[Ljava.lang.String;"). Types of kind PRIMITIVE parsed using the
     * primitive descriptor format (e.g. "I" for int).
     * Types of kind VOID ignore the specified name, and return a void type. All
     * other types will throw an exception.
     *
     * @param name the name to use or parse
     * @param kind the kind of type to create
     * @return the type
     * @throws java.lang.IllegalArgumentException if the kind is no supported
     *
     */
    public static Type create(DotName name, Kind kind) {
        if (name == null)
            throw new IllegalArgumentException("name can not be null!");

        if (kind == null)
            throw new IllegalArgumentException("kind can not be null!");


        switch (kind) {
            case ARRAY:
                String string = name.toString();
                int start = string.lastIndexOf('[');
                if (start < 0) {
                    throw new IllegalArgumentException("Not a valid array name");
                }
                int depth = ++start;

                Type type = PrimitiveType.decode(string.charAt(start));
                if (type != null) {
                    return new ArrayType(type, depth);
                }

                char c = string.charAt(start);
                switch (c) {
                    case 'V':
                        type = VoidType.VOID;
                        break;
                    case 'L':
                        int end = start;
                        while (string.charAt(++end) != ';') ;

                        type = new ClassType(DotName.createSimple(string.substring(start + 1, end)));
                        break;
                    default:
                        type = PrimitiveType.decode(string.charAt(start));
                        if (type == null) {
                            throw new IllegalArgumentException("Component type not supported: " + c);
                        }
                }

                return new ArrayType(type, depth);
            case CLASS:
                return new ClassType(name);
            case PRIMITIVE:
                return PrimitiveType.decode(name.toString());
            case VOID:
                return VoidType.VOID;
            default:
                throw new IllegalArgumentException("Kind not supported: " + kind);
        }
    }

    /**
     * Returns the raw name of this type. Primitives and void are returned as the
     * Java reserved word (void, boolean, byte, short, char, int, long, float,
     * double). Arrays are returned using the Java reflection array syntax
     * (e.g. "[[[Ljava.lang.String;") Classes are returned as a normal <code>DotName</code>.
     *
     * <p>Generic values are returned as the underlying raw value. For example,
     * a wildcard such as <code>? extends Number</code>, has a raw type of
     * <code>Number</code>
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
    public abstract Kind kind();

    /**
     * Casts this type to a {@link org.jboss.jandex.ClassType} and returns it if the kind is {@link Kind#CLASS}
     * Throws an exception otherwise.
     *
     * @return a <code>ClassType</code>
     * @throws java.lang.IllegalArgumentException if not a class
     * @since 2.0
     */
    public ClassType asClassType() {
        throw new IllegalArgumentException("Not a class type!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.ParameterizedType} and returns it if the kind is
     * {@link Kind#PARAMETERIZED_TYPE}
     * Throws an exception otherwise.
     *
     * @return a <code>ClassType</code>
     * @throws java.lang.IllegalArgumentException if not a parameterized type
     * @since 2.0
     */
    public ParameterizedType asParameterizedType() {
        throw new IllegalArgumentException("Not a parameterized type!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.ParameterizedType} and returns it if the kind is
     * {@link Kind#TYPE_VARIABLE}
     * Throws an exception otherwise.
     *
     * @return a <code>ClassType</code>
     * @throws java.lang.IllegalArgumentException if not a type variable
     * @since 2.0
     */
    public TypeVariable asTypeVariable() {
        throw new IllegalArgumentException("Not a type variable!");
    }

    /**
     * Casts this type to an {@link org.jboss.jandex.ArrayType} and returns it if the kind is
     * {@link Kind#ARRAY}
     * Throws an exception otherwise.
     *
     * @return a <code>ClassType</code>
     * @throws java.lang.IllegalArgumentException if not an array type
     * @since 2.0
     */
    public ArrayType asArrayType() {
        throw new IllegalArgumentException("Not an array type!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.WildcardType} and returns it if the kind is
     * {@link Kind#WILDCARD_TYPE}
     * Throws an exception otherwise.
     *
     * @return a <code>ClassType</code>
     * @throws java.lang.IllegalArgumentException if not a wildcard type
     * @since 2.0
     */
    public WildcardType asWildcardType() {
        throw new IllegalArgumentException("Not a wildcard type!");
    }

    /**
     * Casts this type to an {@link org.jboss.jandex.UnresolvedTypeVariable} and returns it if the kind is
     * {@link Kind#UNRESOLVED_TYPE_VARIABLE}
     * Throws an exception otherwise.
     *
     * @return a <code>ClassType</code>
     * @throws java.lang.IllegalArgumentException if not an unresolved type
     * @since 2.0
     */
    public UnresolvedTypeVariable asUnresolvedTypeVariable() {
        throw new IllegalArgumentException("Not an unresolved type variable!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.PrimitiveType} and returns it if the kind is
     * {@link Kind#PRIMITIVE}
     * Throws an exception otherwise.
     *
     * @return a <code>ClassType</code>
     * @throws java.lang.IllegalArgumentException if not a primitive type
     * @since 2.0
     */
    public PrimitiveType asPrimitiveType() {
        throw new IllegalArgumentException("Not a primitive type!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.VoidType} and returns it if the kind is
     * {@link Kind#VOID}
     * Throws an exception otherwise.
     *
     * @return a <code>ClassType</code>
     * @throws java.lang.IllegalArgumentException if not a void type
     * @since 2.0
     */
    public VoidType asVoidType() {
        throw new IllegalArgumentException("Not a void type!");
    }


    /**
     * Returns a string representation for this type. It is similar, yet not equivalent
     * to a Java source code representation.
     *
     * @return the string representation.
     */
    public String toString() {
        return toString(false);
    }

    String toString(boolean simple) {
        StringBuilder builder = new StringBuilder();
        appendAnnotations(builder);
        builder.append(name);

        return builder.toString();
    }

    void appendAnnotations(StringBuilder builder) {
        AnnotationInstance[] annotations = this.annotations;
        if (annotations.length > 0) {
            for (AnnotationInstance instance : annotations) {
                builder.append(instance.toString(true)).append(' ');
            }
        }
    }

    /**
     * Compares this Type with another type, and returns true if they are equivalent.
     * A type is equivalent to another type if it is the same kind, and all of its
     * fields are equal. This includes annotations, which must be equal as well.
     *
     * @param o the type to compare to
     * @return true if equal
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Type type = (Type) o;

        return name.equals(type.name) && Arrays.equals(annotations, type.annotations);
    }

    /**
     * Returns the list of annotations declared on this type's usage. In order to allow for
     * type reuse, the annotation instances returned by this method will have a null annotation target
     * value. However, this information is not useful, because if it is accessed from this method,
     * the target is this type.
     *
     * @return a list of annotation instances declared on the usage this type represents
     * @since 2.0
     */
    public List<AnnotationInstance> annotations() {
        return Collections.unmodifiableList(Arrays.asList(annotations));
    }

    AnnotationInstance[] annotationArray() {
        return annotations;
    }

    public final AnnotationInstance annotation(DotName name) {
        AnnotationInstance key = new AnnotationInstance(name, null, null);
        int i = Arrays.binarySearch(annotations, key, AnnotationInstance.NAME_COMPARATOR);
        return i >= 0 ? annotations[i] : null;
    }

    public final boolean hasAnnotation(DotName name) {
        return annotation(name) != null;
    }

    Type addAnnotation(AnnotationInstance annotation) {
        AnnotationTarget target = annotation.target();
        if (target != null) {
            throw new IllegalArgumentException("Invalid target type");
        }

        AnnotationInstance[] newAnnotations = Arrays.copyOf(annotations, annotations.length + 1);
        newAnnotations[newAnnotations.length - 1] = annotation;
        return copyType(newAnnotations);
    }

    abstract Type copyType(AnnotationInstance[] newAnnotations);

    /**
     * Computes a hash code representing this type.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(annotations);
        return result;
    }
}
