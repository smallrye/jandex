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
 * Represents a type variable that could not be resolved during indexing. This type will only occur
 * as a result of a bug, or a non-compliant Java class file. It is provided in order to prevent
 * failure.
 *
 * @author Jason T. Greene
 */
public final class UnresolvedTypeVariable extends Type {
    private final String name;
    private int hash;

    UnresolvedTypeVariable(String name) {
        this (name, null);
    }

    UnresolvedTypeVariable(String name, AnnotationInstance[] annotations) {
        super(DotName.OBJECT_NAME, annotations);
        this.name = name;
    }

    /**
     * The identifier of this unresolved type variable as it appears in Java source code.
     *
     * <p> The following class has a type parameter, with an identifier of "T":
     * <pre class="brush:java; gutter: false;">
     *     class Foo&lt;T extends Number&gt; {}
     * </pre>
     *
     * @return the identifier of this type variable
     */
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
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new UnresolvedTypeVariable(name, newAnnotations);
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
