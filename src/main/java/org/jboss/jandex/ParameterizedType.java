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

/**
 * @author Jason T. Greene
 */
public class ParameterizedType extends Type {
    private final Type[] parameters;
    private final Type owner;
    private int hash;

    ParameterizedType(DotName name, Type[] parameters, Type owner) {
        super(name);
        this.parameters = parameters == null ? EMPTY_ARRAY : parameters;
        this.owner = owner;
    }

    public Type[] parameters() {
        return parameters;
    }

    public Type owner() {
        return owner;
    }

    @Override
    public Kind kind() {
        return Kind.PARAMETERIZED_TYPE;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (owner != null) {
            builder.append(owner);
            builder.append('$').append(name().local());
        } else {
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
        return hash;
    }
}
