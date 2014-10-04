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

import java.util.ArrayDeque;

/**
 * @author Jason T. Greene
 */
public final class ArrayType extends Type {
    private final Type component;
    private final int dimensions;
    private int hash;

    ArrayType(Type component, int dimensions) {
        this(component, dimensions, null);
    }

    ArrayType(Type component, int dimensions, AnnotationInstance[] annotations) {
        super(DotName.OBJECT_NAME, annotations);
        this.dimensions = dimensions;
        this.component = component;
    }

    public Type component() {
        return component;
    }

    @Override
    public DotName name() {
        StringBuilder builder = new StringBuilder();
        int dimensions = this.dimensions;
        while (dimensions-- > 0) {
            builder.append('[');
        }
        if (component instanceof PrimitiveType) {
            builder.append(((PrimitiveType)component).toCode());
        } else {
            // This relies on name() representing the erased type name
            // FIXME - Revisit whether we need /s or .s
            builder.append('L').append(component.name().toString().replace('.', '/')).append(';');
        }

        return DotName.createSimple(builder.toString());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        appendRootComponent(builder);
        appendArraySyntax(builder);

        return builder.toString();
    }

    private void appendRootComponent(StringBuilder builder) {
        if (component.kind() == Kind.ARRAY) {
            component.asArrayType().appendRootComponent(builder);
        } else {
            builder.append(component);
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
        if (component.kind() == Kind.ARRAY) {
            component.asArrayType().appendArraySyntax(builder);
        }
    }

    public int dimensions() {
        return dimensions;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (! (o instanceof ArrayType)) {
            return false;
        }
        ArrayType arrayType = (ArrayType) o;

        return super.equals(o) && dimensions == arrayType.dimensions && component.equals(arrayType.component);
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash != 0) {
            return hash;
        }

        hash = super.hashCode();
        hash = 31 * hash + component.hashCode();
        hash = 31 * hash + dimensions;
        return this.hash = hash;
    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new ArrayType(component, dimensions, newAnnotations);
    }

    Type copyType(Type component, int dimensions) {
        return new ArrayType(component, dimensions, annotationArray());
    }
}
