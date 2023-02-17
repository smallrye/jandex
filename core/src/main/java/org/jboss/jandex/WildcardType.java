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
 * Represents a generic wildcard. A generic wildcard can have either an upper (extends)
 * or a lower (super) bound. A wildcard declared without a bound ("?") has a default extends bound
 * of "java.lang.Object".
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public class WildcardType extends Type {

    /**
     * A wildcard without a bound, an equivalent of {@code ?}.
     *
     * @since 3.1.0
     */
    public static final WildcardType UNBOUNDED = new WildcardType(null, true);

    /**
     * Creates a new wildcard type.
     *
     * @param bound the bound (lower or upper)
     * @param isExtends true if the bound is an upper (extends) bound, false if lower (super)
     * @return the new instance
     *
     * @since 2.1
     * @deprecated use {@link #createUpperBound(Type)} or {@link #createLowerBound(Type)} instead
     */
    @Deprecated
    public static WildcardType create(Type bound, boolean isExtends) {
        return new WildcardType(bound, isExtends);
    }

    /**
     * Create a new wildcard type with an upper bound.
     *
     * @param upperBound the upper bound
     * @return the new instance
     * @since 3.1.0
     */
    public static WildcardType createUpperBound(Type upperBound) {
        return new WildcardType(upperBound, true);
    }

    /**
     * Create a new wildcard type with an upper bound.
     *
     * @param upperBound the upper bound
     * @return the new instance
     * @since 3.1.0
     */
    public static WildcardType createUpperBound(Class<?> upperBound) {
        return createUpperBound(ClassType.create(upperBound));
    }

    /**
     * Create a new wildcard type with a lower bound.
     *
     * @param lowerBound the lower bound
     * @return the new instance
     * @since 3.1.0
     */
    public static WildcardType createLowerBound(Type lowerBound) {
        return new WildcardType(lowerBound, false);
    }

    /**
     * Create a new wildcard type with a lower bound.
     *
     * @param lowerBound the lower bound
     * @return the new instance
     * @since 3.1.0
     */
    public static WildcardType createLowerBound(Class<?> lowerBound) {
        return createLowerBound(ClassType.create(lowerBound));
    }

    /**
     * Create a builder of a wildcard type.
     * 
     * @return the builder
     * @since 3.1.0
     */
    public static Builder builder() {
        return new Builder();
    }

    private final boolean isExtends;
    private final Type bound;
    private int hash;

    WildcardType(Type bound, boolean isExtends) {
        this(bound, isExtends, null);
    }

    WildcardType(Type bound, boolean isExtends, AnnotationInstance[] annotations) {
        // can't get the name here, because the bound may be a not-yet-patched type variable reference
        // (hence we also need to override the name() method, see below)
        super(DotName.OBJECT_NAME, annotations);
        this.bound = isExtends && bound == null ? ClassType.OBJECT_TYPE : bound;
        this.isExtends = isExtends;

    }

    @Override
    public DotName name() {
        if (isExtends && bound != null) {
            return bound.name();
        }
        return DotName.OBJECT_NAME;
    }

    /**
     * Returns the upper bound of this wildcard (e.g. {@code SomeType} for {@code ? extends SomeType}).
     * <p>
     * Returns {@code java.lang.Object} if this wildcard declares a lower bound
     * ({@code ? super SomeType}).
     *
     * @return the upper bound, or {@code Object} if this wildcard has a lower bound
     */
    public Type extendsBound() {
        return isExtends ? bound : ClassType.OBJECT_TYPE;
    }

    /**
     * Returns the lower bound of this wildcard (e.g. {@code SomeType} for {@code ? super SomeType}).
     * <p>
     * Returns {@code null} if this wildcard declares an upper bound
     * ({@code ? extends SomeType}).
     *
     * @return the lower bound, or {@code null} if this wildcard has an uper bound
     */
    public Type superBound() {
        return isExtends ? null : bound;
    }

    Type bound() {
        return bound;
    }

    boolean isExtends() {
        return isExtends;
    }

    boolean hasImplicitObjectBound() {
        return isExtends && bound == ClassType.OBJECT_TYPE;
    }

    @Override
    public Kind kind() {
        return Kind.WILDCARD_TYPE;
    }

    @Override
    public WildcardType asWildcardType() {
        return this;
    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new WildcardType(bound, isExtends, newAnnotations);
    }

    Type copyType(Type bound) {
        return new WildcardType(bound, isExtends, annotationArray());
    }

    @Override
    String toString(boolean simple) {
        StringBuilder builder = new StringBuilder();
        appendAnnotations(builder);
        builder.append('?');

        if (isExtends && bound != ClassType.OBJECT_TYPE) {
            builder.append(" extends ").append(bound.toString(true));
        }

        if (!isExtends && bound != null) {
            builder.append(" super ").append(bound.toString(true));
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

        WildcardType other = (WildcardType) o;
        return isExtends == other.isExtends && bound.equals(other.bound);
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash != 0) {
            return hash;
        }

        hash = super.hashCode();
        hash = 31 * hash + (isExtends ? 1 : 0);
        hash = 31 * hash + bound.hashCode();
        return this.hash = hash;
    }

    @Override
    public boolean internEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (!super.internEquals(o)) {
            return false;
        }

        WildcardType other = (WildcardType) o;
        return isExtends == other.isExtends && bound.internEquals(other.bound);
    }

    @Override
    public int internHashCode() {
        int hash = super.internHashCode();
        hash = 31 * hash + (isExtends ? 1 : 0);
        hash = 31 * hash + bound.internHashCode();
        return hash;
    }

    /**
     * Convenient builder for {@link WildcardType}.
     * <p>
     * Note that only one bound may be set. If the {@code setUpperBound()} and
     * {@code setLowerBound()} methods are called multiple times, only the last
     * call is taken into account; the previously set bounds are ignored.
     *
     * @since 3.1.0
     */
    public static final class Builder extends Type.Builder<Builder> {

        private boolean isExtends = true;
        private Type bound;

        Builder() {
            super(DotName.OBJECT_NAME);
        }

        /**
         * Sets the upper bound.
         *
         * @param upperBound the class whose type is set as the upper bound, must not be {@code null}
         * @return this builder
         */
        public Builder setUpperBound(Class<?> upperBound) {
            return setUpperBound(ClassType.create(upperBound));
        }

        /**
         * Sets the upper bound.
         *
         * @param upperBound the upper bound, must not be {@code null}
         * @return this builder
         */
        public Builder setUpperBound(Type upperBound) {
            this.bound = upperBound;
            this.isExtends = true;
            return this;
        }

        /**
         * Sets the lower bound.
         *
         * @param lowerBound the class whose type is set as the lower bound, must not be {@code null}
         * @return this builder
         */
        public Builder setLowerBound(Class<?> lowerBound) {
            return setLowerBound(ClassType.create(lowerBound));
        }

        /**
         * Sets the lower bound.
         *
         * @param lowerBound the lower bound, must not be {@code null}
         * @return this builder
         */
        public Builder setLowerBound(Type lowerBound) {
            this.bound = lowerBound;
            this.isExtends = false;
            return this;
        }

        /**
         * Returns the built wildcard type.
         *
         * @return the built wildcard type
         */
        public WildcardType build() {
            return new WildcardType(bound, isExtends, annotationsArray());
        }

    }
}
