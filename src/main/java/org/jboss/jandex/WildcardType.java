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
 * @author Jason T. Greene
 */
public class WildcardType extends Type {
    private static Type OBJECT = new ClassType(DotName.OBJECT_NAME);

    private final boolean isExtends;
    private final Type bound;
    private int hash;


    WildcardType(Type bound, boolean isExtends) {
        this(bound, isExtends, null);
    }

    WildcardType(Type bound, boolean isExtends, AnnotationInstance[] annotations) {
        super(isExtends && bound != null ? bound.name() : DotName.OBJECT_NAME, annotations);
        this.bound = isExtends && bound == null ? OBJECT : bound;
        this.isExtends = isExtends;

    }

    public Type extendsBound() {
        return isExtends ? bound : OBJECT;
    }

    public Type superBound() {
        return isExtends ? null : bound;
    }

    Type bound() {
        return bound;
    }

    @Override
    public Kind kind() {
        return Kind.WILDCARD_TYPE;
    }

    @Override
    public WildcardType asWildcardType() {
        return this;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        appendAnnotations(builder);
        builder.append('?');

        if (isExtends && bound != OBJECT) {
            builder.append(" extends ").append(bound);
        }

        if (!isExtends && bound != null) {
            builder.append(" super ").append(bound);
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
