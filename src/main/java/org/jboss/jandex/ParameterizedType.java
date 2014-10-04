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
 * @author Jason T. Greene
 */
public class ParameterizedType extends Type {
    private final Type[] parameters;
    private final Type owner;
    private int hash;

    ParameterizedType(DotName name, Type[] parameters, Type owner) {
        this(name, parameters, owner, null);
    }

    ParameterizedType(DotName name, Type[] parameters, Type owner, AnnotationInstance[] annotations) {
        super(name, annotations);
        this.parameters = parameters == null ? EMPTY_ARRAY : parameters;
        this.owner = owner;
    }

    public List<Type> parameters() {
        return Collections.unmodifiableList(Arrays.asList(parameters));
    }

    Type[] parameterArray() {
        return parameters;
    }

    public Type owner() {
        return owner;
    }

    @Override
    public Kind kind() {
        return Kind.PARAMETERIZED_TYPE;
    }

    @Override
    public ParameterizedType asParameterizedType() {
        return this;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (owner != null) {
            builder.append(owner);
            builder.append('.');
            appendAnnotations(builder);
            builder.append(name().local());
        } else {
            appendAnnotations(builder);
            builder.append(name());
        }

        if (parameters.length > 0) {
            builder.append('<');
            builder.append(parameters[0]);
            for (int i = 1; i < parameters.length; i++) {
                builder.append(", ").append(parameters[i]);
            }
            builder.append('>');
        }

        return builder.toString();
    }

    @Override
    ParameterizedType copyType(AnnotationInstance[] newAnnotations) {
        return new ParameterizedType(name(), parameters, owner, newAnnotations);
    }

    ParameterizedType copyType(Type[] parameters) {
        return new ParameterizedType(name(), parameters, owner, annotationArray());
    }

    ParameterizedType copyType(Type owner) {
        return new ParameterizedType(name(), parameters, owner, annotationArray());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!super.equals(o)) {
            return false;
        }

        ParameterizedType other = (ParameterizedType) o;

        return (owner == other.owner || (owner != null && owner.equals(other.owner)))
                && Arrays.equals(parameters, other.parameters);
    }

    public int hashCode() {
        int hash = this.hash;
        if (hash != 0) {
            return hash;
        }

        hash = super.hashCode();
        hash = 31 * hash + Arrays.hashCode(parameters);
        hash = 31 * hash + (owner != null ? owner.hashCode() : 0);
        return this.hash = hash;
    }
}
