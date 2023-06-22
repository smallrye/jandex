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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a Java type usage that is specified on methods, fields, classes,
 * annotations, or other types. A type can be any class based type (interface, class, annotation),
 * any primitive, any array, any generic type declaration, or void.
 * <p>
 * A type usage may have annotations associated with its declaration. A type is equal to
 * another type if, and only if, it represents the same exact definition including the annotations
 * specific to its usage.
 * <p>
 * To reduce memory overhead, type instances are often shared between their enclosing classes.
 *
 * @author Jason T. Greene
 */
public abstract class Type implements Descriptor {
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
         * A Java primitive (boolean, byte, short, char, int, long, float, double)
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

        /** A generic wildcard type */
        WILDCARD_TYPE,

        /** A generic parameterized type */
        PARAMETERIZED_TYPE,

        /** A reference to a resolved type variable occuring in the bound of a recursive type parameter */
        TYPE_VARIABLE_REFERENCE,

        ;

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
                case 8:
                    return TYPE_VARIABLE_REFERENCE;
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
     * Creates a type of the specified kind and {@code name} in the {@link Class#getName()}
     * format. Specifically:
     * <ul>
     * <li>if {@code kind} is {@code VOID}, the {@code name} is ignored;</li>
     * <li>if {@code kind} is {@code PRIMITIVE}, the name must be the corresponding Java
     * keyword ({@code boolean}, {@code byte}, {@code short}, {@code int}, {@code long},
     * {@code float}, {@code double}, {@code char});
     * <li>if {@code kind} is {@code CLASS}, the {@code name} must be a binary name
     * of the class;</li>
     * <li>if {@code kind} is {@code ARRAY}, the {@code name} must consists of one or
     * more {@code [} characters corresponding to the number of dimensions of the array type,
     * followed by the element type as a single-character code for primitive types
     * or {@code Lbinary.name.of.TheClass;} for class types (for example, {@code [I}
     * for {@code int[]} or {@code [[Ljava.lang.String;} for {@code String[][]});</li>
     * <li>all other kinds cause an exception.</li>
     * </ul>
     *
     * @param name the name of type to use or parse; must not be {@code null}
     * @param kind the kind of type to create; must not be {@code null}
     * @return the type
     * @throws java.lang.IllegalArgumentException if the {@code kind} is not supported
     *
     */
    public static Type create(DotName name, Kind kind) {
        if (name == null) {
            throw new IllegalArgumentException("name can not be null!");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind can not be null!");
        }

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
                        while (string.charAt(++end) != ';')
                            ;

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
     * Creates an instance of specified type with given type {@code annotations}.
     * To create the type instance, this method delegates to {@link #create(DotName, Kind)}.
     *
     * @param name the name of type to use or parse; must not be {@code null}
     * @param kind the kind of type to create; must not be {@code null}
     * @param annotations the type annotations that should be present on the type instance; may be {@code null}
     * @return the annotated type
     * @throws java.lang.IllegalArgumentException if the {@code kind} is not supported
     */
    public static Type createWithAnnotations(DotName name, Kind kind, AnnotationInstance[] annotations) {
        Type type = create(name, kind);
        return annotations == null ? type : type.copyType(annotations);
    }

    /**
     * Returns the name of this type (or its erasure in case of generic types) as a {@link DotName},
     * using the {@link Class#getName()} format. Specifically:
     * <ul>
     * <li>for primitive types and the void pseudo-type, the corresponding Java keyword
     * is returned ({@code void}, {@code boolean}, {@code byte}, {@code short}, {@code int},
     * {@code long}, {@code float}, {@code double}, {@code char});
     * <li>for class types, the binary name of the class is returned;</li>
     * <li>for array types, a string is returned that consists of one or more {@code [}
     * characters corresponding to the number of dimensions of the array type,
     * followed by the element type as a single-character code for primitive types
     * or {@code Lbinary.name.of.TheClass;} for class types (for example, {@code [I}
     * for {@code int[]} or {@code [[Ljava.lang.String;} for {@code String[][]});</li>
     * <li>for parameterized types, the binary name of the generic class is returned
     * (for example, {@code java.util.List} for {@code List<String>});</li>
     * <li>for type variables, the name of the first bound of the type variable is returned,
     * or {@code java.lang.Object} for type variables that have no bound;</li>
     * <li>for wildcard types, the name of the upper bound is returned,
     * or {@code java.lang.Object} if the wildcard type does not have an upper bound
     * (for example, {@code java.lang.Number} for {@code ? extends Number}).</li>
     * </ul>
     *
     * @return the name of this type (or its erasure in case of generic types)
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
     * Casts this type to a {@link org.jboss.jandex.ClassType} and returns it if the kind is {@link Kind#CLASS}.
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
     * {@link Kind#PARAMETERIZED_TYPE}. Throws an exception otherwise.
     *
     * @return a {@code ParameterizedType}
     * @throws java.lang.IllegalArgumentException if not a parameterized type
     * @since 2.0
     */
    public ParameterizedType asParameterizedType() {
        throw new IllegalArgumentException("Not a parameterized type!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.TypeVariable} and returns it if the kind is
     * {@link Kind#TYPE_VARIABLE}. Throws an exception otherwise.
     *
     * @return a {@code TypeVariable}
     * @throws java.lang.IllegalArgumentException if not a type variable
     * @since 2.0
     */
    public TypeVariable asTypeVariable() {
        throw new IllegalArgumentException("Not a type variable!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.TypeVariableReference} and returns it if the kind is
     * {@link Kind#TYPE_VARIABLE_REFERENCE}. Throws an exception otherwise.
     *
     * @return a {@code TypeVariableReference}
     * @throws java.lang.IllegalArgumentException if not a type variable
     * @since 2.0
     */
    public TypeVariableReference asTypeVariableReference() {
        throw new IllegalArgumentException("Not a type variable reference!");
    }

    /**
     * Casts this type to an {@link org.jboss.jandex.ArrayType} and returns it if the kind is
     * {@link Kind#ARRAY}. Throws an exception otherwise.
     *
     * @return an {@code ArrayType}
     * @throws java.lang.IllegalArgumentException if not an array type
     * @since 2.0
     */
    public ArrayType asArrayType() {
        throw new IllegalArgumentException("Not an array type!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.WildcardType} and returns it if the kind is
     * {@link Kind#WILDCARD_TYPE}. Throws an exception otherwise.
     *
     * @return a {@code WildcardType}
     * @throws java.lang.IllegalArgumentException if not a wildcard type
     * @since 2.0
     */
    public WildcardType asWildcardType() {
        throw new IllegalArgumentException("Not a wildcard type!");
    }

    /**
     * Casts this type to an {@link org.jboss.jandex.UnresolvedTypeVariable} and returns it if the kind is
     * {@link Kind#UNRESOLVED_TYPE_VARIABLE}. Throws an exception otherwise.
     *
     * @return an {@code UnresolvedTypeVariable}
     * @throws java.lang.IllegalArgumentException if not an unresolved type
     * @since 2.0
     */
    public UnresolvedTypeVariable asUnresolvedTypeVariable() {
        throw new IllegalArgumentException("Not an unresolved type variable!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.PrimitiveType} and returns it if the kind is
     * {@link Kind#PRIMITIVE}. Throws an exception otherwise.
     *
     * @return a {@code PrimitiveType}
     * @throws java.lang.IllegalArgumentException if not a primitive type
     * @since 2.0
     */
    public PrimitiveType asPrimitiveType() {
        throw new IllegalArgumentException("Not a primitive type!");
    }

    /**
     * Casts this type to a {@link org.jboss.jandex.VoidType} and returns it if the kind is
     * {@link Kind#VOID}. Throws an exception otherwise.
     *
     * @return a {@code VoidType}
     * @throws java.lang.IllegalArgumentException if not a void type
     * @since 2.0
     */
    public VoidType asVoidType() {
        throw new IllegalArgumentException("Not a void type!");
    }

    AnnotationInstance[] annotationArray() {
        return annotations;
    }

    /**
     * Returns whether an annotation instance with given name is declared on this type usage.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @see #annotation(DotName)
     */
    public final boolean hasAnnotation(DotName name) {
        return annotation(name) != null;
    }

    /**
     * Returns the annotation instance with given name declared on this type usage.
     * <p>
     * To allow for {@code Type} object reuse, the annotation instances returned by this method
     * have a {@code null} annotation target.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     */
    public final AnnotationInstance annotation(DotName name) {
        return AnnotationInstance.binarySearch(annotations, name);
    }

    /**
     * Returns the annotation instances with given name declared on this type usage.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     * <p>
     * To allow for {@code Type} object reuse, the annotation instances returned by this method
     * have a {@code null} annotation target.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotations()
     */
    public final List<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
        if (index == null) {
            throw new IllegalArgumentException("Index must not be null");
        }
        List<AnnotationInstance> instances = new ArrayList<>();
        AnnotationInstance declaredInstance = annotation(name);
        if (declaredInstance != null) {
            instances.add(declaredInstance);
        }
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.declaredAnnotation(Index.REPEATABLE);
        if (repeatable != null) {
            Type containingType = repeatable.value().asClass();
            AnnotationInstance container = annotation(containingType.name());
            if (container != null) {
                for (AnnotationInstance nestedInstance : container.value().asNestedArray()) {
                    instances.add(AnnotationInstance.create(nestedInstance, container.target()));
                }
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances declared on this type usage.
     * <p>
     * To allow for {@code Type} object reuse, the annotation instances returned by this method
     * have a {@code null} annotation target.
     *
     * @return immutable list of annotation instances, never {@code null}
     * @since 2.0
     */
    public List<AnnotationInstance> annotations() {
        return new ImmutableArrayList<>(annotations);
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
     * Returns a string representation for this type. It is similar, yet not identical
     * to a Java source code representation.
     *
     * @return the string representation.
     */
    public String toString() {
        return toString(false);
    }

    String toString(boolean simple) {
        StringBuilder builder = new StringBuilder();
        String packagePrefix = name.packagePrefix();
        if (packagePrefix != null) {
            builder.append(packagePrefix).append('.');
        }
        appendAnnotations(builder);
        builder.append(name.withoutPackagePrefix());

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
     * Returns the bytecode descriptor of this type (or its erasure in case of generic types).
     * Specifically:
     * <ul>
     * <li>for primitive types and the void pseudo-type, the single-character descriptor
     * is returned;</li>
     * <li>for class types, the {@code Lbinary/name/of/TheClass;} string is returned;</li>
     * <li>for array types, a string is returned that consists of one or more {@code [}
     * characters corresponding to the number of dimensions of the array type, followed by
     * the descriptor of the element type (for example, {@code [I} for {@code int[]} or
     * {@code [[Ljava/lang/String;} for {@code String[][]});</li>
     * <li>for parameterized types, the descriptor of the generic class is returned
     * (for example, {@code Ljava/util/List;} for {@code List<String>});</li>
     * <li>for type variables, the descriptor of the first bound of the type variable
     * is returned, or the descriptor of the {@code java.lang.Object} class for type
     * variables that have no bound;</li>
     * <li>for wildcard types, the descriptor of the upper bound is returned,
     * or the descriptor of the {@code java.lang.Object} class if the wildcard type
     * does not have an upper bound (for example, {@code Ljava/lang/Number;} for
     * {@code ? extends Number}).</li>
     * </ul>
     * Descriptors of type variables are substituted for descriptors of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, or if it returns the type variable itself, no substitution happens
     * and the type variable descriptor is used unmodified.
     * <p>
     * Note that the return value does not come directly from bytecode. Jandex does not store the descriptor
     * strings. Instead, the return value is reconstructed from the Jandex object model.
     *
     * @return the bytecode descriptor of this type (or its erasure in case of generic types)
     */
    public String descriptor(Function<String, Type> typeVariableSubstitution) {
        StringBuilder result = new StringBuilder();
        DescriptorReconstruction.typeDescriptor(this, typeVariableSubstitution, result);
        return result.toString();
    }

    /**
     * Compares this {@code Type} with another type. A type is equal to another type
     * if it is of the same kind, and all of their fields are equal. This includes
     * annotations, which must be equal as well.
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

    boolean internEquals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Type type = (Type) o;

        return name.equals(type.name) && Arrays.equals(annotations, type.annotations);
    }

    int internHashCode() {
        int result = name.hashCode();
        result = 31 * result + Arrays.hashCode(annotations);
        return result;
    }

    /**
     * Base class for type builders.
     *
     * @param <THIS> self type
     * @since 3.1.0
     */
    static abstract class Builder<THIS extends Builder<THIS>> {

        protected final DotName name;
        protected final List<AnnotationInstance> annotations;

        protected Builder(DotName name) {
            this.name = Objects.requireNonNull(name);
            this.annotations = new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        protected THIS self() {
            return (THIS) this;
        }

        /**
         * @return the annotations array or {@code null} if no annotation was specified
         */
        protected AnnotationInstance[] annotationsArray() {
            return annotations.isEmpty() ? null : annotations.toArray(AnnotationInstance.EMPTY_ARRAY);
        }

        /**
         * Adds an annotation to the type being created by this builder.
         * Note that it becomes a <em>type annotation</em>.
         *
         * @param annotation the annotation instance; can be created using {@code AnnotationInstance.builder()}
         * @return this builder
         * @see Type#annotations()
         */
        public THIS addAnnotation(AnnotationInstance annotation) {
            annotations.add(Objects.requireNonNull(annotation));
            return self();
        }

    }
}
