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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a resolved type parameter or type argument. The {@code name()} of this type variable
 * corresponds to the raw type name. For type variables, the raw type name is the first upper bound. The
 * {@code identifier()} is the name of the type variable as present in the source code.
 * <p>
 * For example:
 *
 * <pre class="brush:java">
 * T extends Number
 * </pre>
 *
 * In this case, the identifier is {@code T}, while the name is {@code java.lang.Number}.
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public final class TypeVariable extends Type {

    /**
     * Create an instance of a type variable with the given {@code identifier}.
     * The type variable has a single bound of {@code java.lang.Object}.
     *
     * @param identifier identifier of the type variable
     * @return the type variable
     * @see #identifier()
     * @since 3.1.0
     */
    public static TypeVariable create(String identifier) {
        return new TypeVariable(identifier, new Type[] { ClassType.OBJECT_TYPE });
    }

    /**
     * Create a builder of a type variable.
     *
     * @return the builder
     * @since 3.1.0
     */
    public static Builder builder(String identifier) {
        return new Builder(identifier);
    }

    // The lower 31 bits represents the hash code
    private static final int HASH_MASK = Integer.MAX_VALUE;
    // The high bit represents the implicit object bound flag
    private static final int IMPLICIT_MASK = Integer.MIN_VALUE;

    private final String identifier;
    private final Type[] bounds;

    // MSB is stolen to represent an implicit object bound (signature with ::Interface)
    private int hash;

    TypeVariable(String name) {
        this(name, EMPTY_ARRAY);
    }

    TypeVariable(String name, Type[] bounds) {
        this(name, bounds, null);
    }

    TypeVariable(String name, Type[] bounds, AnnotationInstance[] annotations) {
        this(name, bounds, annotations, false);
    }

    TypeVariable(String name, Type[] bounds, AnnotationInstance[] annotations, boolean implicitObjectBound) {
        // can't get the name here, because the bound may be a not-yet-patched type variable reference
        // (hence we also need to override the name() method, see below)
        super(DotName.OBJECT_NAME, annotations);
        this.identifier = name;
        this.bounds = bounds;
        this.hash = implicitObjectBound ? Integer.MIN_VALUE : 0;
    }

    @Override
    public DotName name() {
        if (bounds.length > 0) {
            return bounds[0].name();
        }
        return DotName.OBJECT_NAME;
    }

    /**
     * The identifier of this type variable as it appears in Java source code.
     *
     * <p>
     * The following class has a type parameter with an identifier of {@code T}:
     *
     * <pre class="brush:java">
     * class Foo&lt;T extends Number&gt; {
     * }
     * </pre>
     *
     * @return the identifier of this type variable
     */
    public String identifier() {
        return identifier;
    }

    public List<Type> bounds() {
        return new ImmutableArrayList<>(bounds);
    }

    Type[] boundArray() {
        return bounds;
    }

    boolean hasImplicitObjectBound() {
        return (hash & IMPLICIT_MASK) != 0;
    }

    @Override
    public Kind kind() {
        return Kind.TYPE_VARIABLE;
    }

    @Override
    public TypeVariable asTypeVariable() {
        return this;
    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new TypeVariable(identifier, bounds, newAnnotations, hasImplicitObjectBound());
    }

    @Override
    Type withoutAnnotations() {
        Type[] newBounds = new Type[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            newBounds[i] = bounds[i].withoutAnnotations();
        }
        return new TypeVariable(identifier, newBounds, null, hasImplicitObjectBound());
    }

    TypeVariable copyType(int boundIndex, Type bound) {
        if (boundIndex > bounds.length) {
            throw new IllegalArgumentException("Bound index outside of bounds");
        }

        Type[] bounds = this.bounds.clone();
        bounds[boundIndex] = bound;
        return new TypeVariable(identifier, bounds, annotationArray(), hasImplicitObjectBound());
    }

    @Override
    String toString(boolean simple) {
        StringBuilder builder = new StringBuilder();
        appendAnnotations(builder);
        builder.append(identifier);

        if (!simple && bounds.length > 0 && !(bounds.length == 1 && ClassType.OBJECT_TYPE.equals(bounds[0]))) {
            builder.append(" extends ").append(bounds[0].toString(true));

            for (int i = 1; i < bounds.length; i++) {
                builder.append(" & ").append(bounds[i].toString(true));
            }
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!super.equals(o)) {
            return false;
        }

        TypeVariable that = (TypeVariable) o;

        return identifier.equals(that.identifier) && Arrays.equals(bounds, that.bounds)
                && hasImplicitObjectBound() == that.hasImplicitObjectBound();
    }

    @Override
    public int hashCode() {
        int hash = this.hash & HASH_MASK;
        if (hash != 0) {
            return hash;
        }

        hash = super.hashCode();
        hash = 31 * hash + identifier.hashCode();
        hash = 31 * hash + Arrays.hashCode(bounds);
        hash &= HASH_MASK;
        this.hash |= hash;
        return hash;
    }

    @Override
    boolean internEquals(Object o) {
        if (this == o) {
            return true;
        }

        if (!super.internEquals(o)) {
            return false;
        }

        TypeVariable that = (TypeVariable) o;

        return identifier.equals(that.identifier) && TypeInterning.arrayEquals(bounds, that.bounds)
                && hasImplicitObjectBound() == that.hasImplicitObjectBound();
    }

    @Override
    int internHashCode() {
        int hash = super.internHashCode();
        hash = 31 * hash + identifier.hashCode();
        hash = 31 * hash + TypeInterning.arrayHashCode(bounds);
        return hash;
    }

    /**
     * Convenient builder for {@link TypeVariable}. If no bound is added, the built
     * type variable will have a single bound of {@code java.lang.Object}.
     *
     * @since 3.1.0
     */
    public static final class Builder extends Type.Builder<Builder> {

        private final String identifier;
        private final List<Type> bounds;

        Builder(String identifier) {
            super(DotName.OBJECT_NAME);
            this.identifier = identifier;
            this.bounds = new ArrayList<>();
        }

        /**
         * Adds a bound.
         *
         * @param bound the bound, must not be {@code null}
         * @return this builder
         */
        public Builder addBound(Type bound) {
            bounds.add(Objects.requireNonNull(bound));
            return this;
        }

        /**
         * Adds a bound.
         *
         * @param clazz the class whose type is added as a bound, must not be {@code null}
         * @return this builder
         */
        public Builder addBound(Class<?> clazz) {
            return addBound(ClassType.create(clazz));
        }

        /**
         * Returns the built type variable.
         *
         * @return the built type variable
         */
        public TypeVariable build() {
            return new TypeVariable(identifier,
                    bounds.isEmpty() ? new Type[] { ClassType.OBJECT_TYPE } : bounds.toArray(EMPTY_ARRAY),
                    annotationsArray());
        }

    }
}
