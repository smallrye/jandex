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
public final class UnresolvedTypeVariable extends Type {
    private final String name;
    private int hash;

    UnresolvedTypeVariable(String name) {
        super(DotName.OBJECT_NAME);
        this.name = name;
    }

    public String identifier() {
        return name;
    }

    @Override
    public Kind kind() {
        return Kind.UNRESOLVED_TYPE_VARIABLE;
    }

    @Override
    public UnresolvedTypeVariable asUnresolvedTypeVariable() {
        return this;
    }

    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UnresolvedTypeVariable)) {
            return false;
        }

        UnresolvedTypeVariable other = (UnresolvedTypeVariable) o;

        return name.equals(other.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
