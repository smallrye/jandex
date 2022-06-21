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
    private static final Type OBJECT = new ClassType(DotName.OBJECT_NAME);

    private final boolean isExtends;
    private final Type bound;
    private int hash;

    /**
     * Create a new mock instance of WildcardType.
     *
     * @param bound the bound (lower or upper)
     * @param isExtends true if lower, false if upper (super)
     * @return thew new mock instance
     *
     * @since 2.1
     */
    public static WildcardType create(Type bound, boolean isExtends) {
        return new WildcardType(bound, isExtends);
    }

    WildcardType(Type bound, boolean isExtends) {
        this(bound, isExtends, null);
    }

    WildcardType(Type bound, boolean isExtends, AnnotationInstance[] annotations) {
        // can't get the name here, because the bound may be a not-yet-patched type variable reference
        // (hence we also need to override the name() method, see below)
        super(DotName.OBJECT_NAME, annotations);
        this.bound = isExtends && bound == null ? OBJECT : bound;
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
        return isExtends ? bound : OBJECT;
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
        return isExtends && bound == OBJECT;
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
    String toString(boolean simple) {
        StringBuilder builder = new StringBuilder();
        appendAnnotations(builder);
        builder.append('?');

        if (isExtends && bound != OBJECT) {
            builder.append(" extends ").append(bound.toString(true));
        }

        if (!isExtends && bound != null) {
            builder.append(" super ").append(bound.toString(true));
        }

        return builder.toString();
    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new WildcardType(bound, isExtends, newAnnotations);
    }

    Type copyType(Type bound) {
        return new WildcardType(bound, isExtends, annotationArray());
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
}
