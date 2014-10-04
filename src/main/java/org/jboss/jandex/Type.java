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
 * Represents a Java type declaration that is specified on methods or fields. A
 * type can be any class based type (interface, class, annotation), any
 * primitive, any array, or void.
 *
 * @author Jason T. Greene
 */
public abstract class Type implements AnnotationTarget {
    public static final Type[] EMPTY_ARRAY = new Type[0];
    private static final AnnotationInstance[] EMPTY_ANNOTATIONS = new AnnotationInstance[0];
    private final DotName name;
    private final AnnotationInstance[] annotations;

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
        VOID,

        TYPE_VARIABLE,

        UNRESOLVED_TYPE_VARIABLE,

        WILDCARD_TYPE,

        PARAMETERIZED_TYPE;
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

    @Deprecated
    public static Type create(DotName name, Kind kind) {
        if (name == null)
            throw new IllegalArgumentException("name can not be null!");

        if (kind == null)
            throw new IllegalArgumentException("kind can not be null!");

        String string = name.toString();

        switch (kind) {
            case ARRAY:
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

                        type = new ClassType(DotName.createSimple(string.substring(start + 1, end).replace('.', '/')));
                        break;
                    default:
                        throw new IllegalArgumentException("Component type not supported: " + c);
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
    public abstract Kind kind();

    public ClassType asClassType() {
        throw new IllegalArgumentException("Not a class type!");
    }

    public ParameterizedType asParameterizedType() {
        throw new IllegalArgumentException("Not a parameterized type!");
    }

    public TypeVariable asTypeVariable() {
        throw new IllegalArgumentException("Not a type variable!");
    }

    public ArrayType asArrayType() {
        throw new IllegalArgumentException("Not an array type!");
    }

    public WildcardType asWildcardType() {
        throw new IllegalArgumentException("Not a wildcard type!");
    }

    public UnresolvedTypeVariable asUnresolvedTypeVariable() {
        throw new IllegalArgumentException("Not an unresolved type variable!");
    }

    public PrimitiveType asPrimitiveType() {
        throw new IllegalArgumentException("Not a primitive type!");
    }

    public VoidType asVoidType() {
        throw new IllegalArgumentException("Not a void type!");
    }

    public String toString() {
        return toString(false);
    }

    public String toString(boolean simple) {
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

    public List<AnnotationInstance> annotations() {
        return Collections.unmodifiableList(Arrays.asList(annotations));
    }

    AnnotationInstance[] annotationArray() {
        return annotations;
    }

    Type addAnnotation(AnnotationInstance annotation) {
        AnnotationInstance[] newAnnotations = Arrays.copyOf(annotations, annotations.length + 1);
        newAnnotations[newAnnotations.length - 1] = annotation;
        Type type = copyType(newAnnotations);
        annotation.setTarget(type);
        return type;
    }

    abstract Type copyType(AnnotationInstance[] newAnnotations);

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(annotations);
        return result;
    }
}
