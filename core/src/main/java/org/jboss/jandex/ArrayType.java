/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
 * Represents a Java array type. Note that this representation of array types is different
 * from the Java language representation.
 * <p>
 * In the Java language, array types have a component type and an element type. An element type
 * is never an array type; it is the ultimate type obtained after all array dimensions are removed.
 * For example, an element type of {@code String[][]} is {@code String}. A component type is
 * the element type in case of one-dimensional arrays, and the same array type with one dimension
 * less in case of multidimensional arrays. For example, the component type of {@code String[][]}
 * is {@code String[]}, whose component type is {@code String}. The number of dimensions can only
 * be found by obtaining the component type repeatedly, until the element type is reached.
 * <p>
 * On the other hand, the Jandex representation is compressed. It consists of a constituent type
 * and a number of dimensions. In case the array type does not contain type annotations,
 * the constituent type is the element type. For example, the array type {@code String[][]} has
 * 2 dimensions and a constituent type of {@code String}. However, to faithfully represent type
 * annotations, array types may be nested; that is, the constituent type may be another array type.
 * For example, the array type of {@code String[] @Ann []} has 1 dimension and a constituent type
 * of {@code String @Ann []}. In turn, this array type has 1 dimension and a constituent type
 * of {@code String}.
 * <p>
 * The {@link #constituent()} and {@link #dimensions()} methods provide access to the Jandex
 * native representation. The {@link #elementType()} and {@link #componentType()} methods,
 * as well as {@link #deepDimensions()}, provide access to the Java language representation.
 * <p>
 * The {@link #component()} method is present for backwards compatibility and should not be used.
 * It is equivalent to the {@link #constituent()} method.
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public final class ArrayType extends Type {

    /**
     * Create a new array type instance with the specified number of dimensions
     * and the specified constituent type.
     *
     * @param constituent the constituent type
     * @param dimensions the number of dimensions of this array
     * @return the new array type instance
     * @since 2.1
     * @see #constituent()
     * @see #dimensions()
     */
    public static ArrayType create(Type constituent, int dimensions) {
        return new ArrayType(constituent, dimensions);
    }

    /**
     * Create a builder of an array type.
     *
     * @param constituent the constituent type
     * @param dimensions the number of dimensions of the array
     * @return the builder
     * @since 3.1.0
     * @see #constituent()
     * @see #dimensions()
     */
    public static Builder builder(Type constituent, int dimensions) {
        return new Builder(constituent, dimensions);
    }

    private final Type constituent;
    private final int dimensions;
    private int hash;

    ArrayType(Type constituent, int dimensions) {
        this(constituent, dimensions, null);
    }

    ArrayType(Type constituent, int dimensions, AnnotationInstance[] annotations) {
        super(DotName.OBJECT_NAME, annotations);
        this.dimensions = dimensions;
        this.constituent = constituent;
        if (dimensions < 1) {
            throw new IllegalArgumentException("Number of dimensions of an array type must be >= 1");
        }
    }

    /**
     * Equivalent to {@link #constituent()}.
     * <p>
     * This method provides access to the Jandex compressed representation of array types.
     * It is likely you want to use {@link #elementType()} or {@link #componentType()} instead.
     *
     * @deprecated use {@link #constituent()}
     */
    @Deprecated
    public Type component() {
        return constituent;
    }

    /**
     * Returns the constituent type of the array. Note that it may be another array type in case
     * type annotations are present on the array components. For example, {@code String[][]} has
     * 2 dimensions and a constituent type of {@code String}, while {@code String[] @Ann []} has
     * 1 dimension and a constituent type of {@code String @Ann []}. The {@code String @Ann []}
     * array type, in turn, has 1 dimension and a constituent type of {@code String}.
     * <p>
     * This method provides access to the Jandex compressed representation of array types.
     * It is likely you want to use {@link #elementType()} or {@link #componentType()} instead.
     *
     * @return the constituent type
     * @since 3.1.0
     */
    public Type constituent() {
        return constituent;
    }

    /**
     * Returns the element type of the array. For example, both {@code String[][]} and
     * {@code String @Ann []} have an element type of {@code String}.
     * <p>
     * This method never returns an {@code ArrayType}.
     *
     * @return the element type
     * @since 3.1.0
     */
    public Type elementType() {
        Type element = constituent;
        while (element.kind() == Kind.ARRAY) {
            element = element.asArrayType().constituent;
        }
        return element;
    }

    /**
     * Returns the component type of the array. For example, {@code String[][]} has a component
     * type of {@code String[]}, while {@code String [] @Ann []} has a component type
     * of {@code String @Ann []}.
     *
     * @return the component type
     * @since 3.1.0
     */
    public Type componentType() {
        if (dimensions == 1) {
            return constituent;
        } else {
            return new ArrayType(constituent, dimensions - 1);
        }
    }

    /**
     * The number of dimensions this array type has. Note that the constituent type may be an array
     * type, so the number of dimensions does not necessarily correspond to the number of times
     * an array dimension would have to be removed from this array type to reach its element type.
     * For example, this method would return 2 for an array type of {@code String[][]}, but it would
     * return 1 for an array type of {@code String[] @Ann []}, because the constituent type of that
     * array type is yet another array type, also with 1 dimension.
     * <p>
     * This method is different to {@link #deepDimensions()} in case this {@code ArrayType} has
     * another {@code ArrayType} as its {@linkplain #constituent() constituent} type.
     * <p>
     * This method provides access to the Jandex compressed representation of array types.
     * It is likely you want to use {@link #deepDimensions()} instead.
     *
     * @return the number of dimensions of this array type
     */
    public int dimensions() {
        return dimensions;
    }

    /**
     * The total number of dimensions this array type has, when traversed deep to the element type.
     * For example, both {@code String[][]} and {@code String[] @Ann []} have a "deep" number
     * of dimensions equal to 2.
     * <p>
     * This method is different to {@link #dimensions()} in case this {@code ArrayType} has
     * another {@code ArrayType} as its {@linkplain #constituent() constituent} type.
     *
     * @return the "deep" number of dimensions of this array type
     * @since 3.1.0
     */
    public int deepDimensions() {
        int result = dimensions;
        Type constituent = this.constituent;
        while (constituent.kind() == Kind.ARRAY) {
            result += constituent.asArrayType().dimensions;
            constituent = constituent.asArrayType().constituent;
        }
        return result;
    }

    @Override
    public DotName name() {
        StringBuilder builder = new StringBuilder();

        Type type = this;
        while (type.kind() == Kind.ARRAY) {
            int dimensions = type.asArrayType().dimensions;
            while (dimensions-- > 0) {
                builder.append('[');
            }
            type = type.asArrayType().constituent;
        }

        // here, `type` is an element type of the array, i.e., never array
        if (type.kind() == Kind.PRIMITIVE) {
            builder.append(type.asPrimitiveType().toCode());
        } else {
            // This relies on name() representing the erased type name
            // For historical 1.x reasons, we follow the Java reflection format
            // instead of the Java descriptor format.
            builder.append('L').append(type.name().toString()).append(';');
        }

        return DotName.createSimple(builder.toString());
    }

    @Override
    public Kind kind() {
        return Kind.ARRAY;
    }

    @Override
    public ArrayType asArrayType() {
        return this;
    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new ArrayType(constituent, dimensions, newAnnotations);
    }

    Type copyType(Type component, int dimensions) {
        return new ArrayType(component, dimensions, annotationArray());
    }

    @Override
    String toString(boolean simple) {
        StringBuilder builder = new StringBuilder();

        appendRootComponent(builder, true);
        appendArraySyntax(builder);

        return builder.toString();
    }

    private void appendRootComponent(StringBuilder builder, boolean simple) {
        if (constituent.kind() == Kind.ARRAY) {
            constituent.asArrayType().appendRootComponent(builder, simple);
        } else {
            builder.append(constituent.toString(simple));
        }
    }

    private void appendArraySyntax(StringBuilder builder) {
        if (annotationArray().length > 0) {
            builder.append(' ');
            appendAnnotations(builder);
        }
        for (int i = 0; i < dimensions; i++) {
            builder.append("[]");
        }
        if (constituent.kind() == Kind.ARRAY) {
            constituent.asArrayType().appendArraySyntax(builder);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ArrayType)) {
            return false;
        }
        ArrayType arrayType = (ArrayType) o;

        return super.equals(o) && dimensions == arrayType.dimensions && constituent.equals(arrayType.constituent);
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash != 0) {
            return hash;
        }

        hash = super.hashCode();
        hash = 31 * hash + constituent.hashCode();
        hash = 31 * hash + dimensions;
        return this.hash = hash;
    }

    @Override
    public int internCompareTo(Type o) {
        if (this == o) {
            return 0;
        }

        int v = super.internCompareTo(o);
        if (v != 0) {
            return v;
        }

        ArrayType other = (ArrayType) o;

        v = dimensions - other.dimensions;
        if (v != 0) {
            return v;
        }
        v = constituent.internCompareTo(other.constituent);
        if (v != 0) {
            return v;
        }

        assert this.internEquals(o) : "ArrayType::internCompare method not consistent with internEquals";
        return 0;
    }

    @Override
    boolean internEquals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ArrayType)) {
            return false;
        }
        ArrayType arrayType = (ArrayType) o;
        return super.internEquals(o) && dimensions == arrayType.dimensions && constituent.internEquals(arrayType.constituent);
    }

    @Override
    int internHashCode() {
        int hash = super.internHashCode();
        hash = 31 * hash + constituent.internHashCode();
        hash = 31 * hash + dimensions;
        return hash;
    }

    /**
     * Convenient builder for {@link ArrayType}.
     *
     * @since 3.1.0
     */
    public static final class Builder extends Type.Builder<Builder> {

        private final Type constituent;
        private final int dimensions;

        Builder(Type constituent, int dimensions) {
            super(DotName.OBJECT_NAME);
            this.constituent = constituent;
            this.dimensions = dimensions;
        }

        /**
         * Returns the built array type.
         *
         * @return the built array type
         */
        public ArrayType build() {
            return new ArrayType(constituent, dimensions, annotationsArray());
        }

    }
}
