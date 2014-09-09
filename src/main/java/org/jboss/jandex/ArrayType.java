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
public final class ArrayType extends Type {
    private final Type component;
    private final int dimensions;
    private int hash;

    ArrayType(Type component, int dimensions) {
        super(null);
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
        builder.append(component);
        for (int i = 0; i < dimensions; i++) {
            builder.append("[]");
        }
        return builder.toString();
    }

    @Override
    public Kind kind() {
        return Kind.ARRAY;
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

        return dimensions == arrayType.dimensions && component.equals(arrayType.component);
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash != 0) {
            return hash;
        }
        hash = component.hashCode();
        hash = 31 * hash + dimensions;
        return this.hash = hash;
    }
}
