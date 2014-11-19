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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a resolved type parameter or type argument. The <code>name()</code> of this type variable
 * corresponds to the raw type name. For type variables, the raw type name is the first upper bound. The
 * <code>identifier()</code> specifies the name of the type variable as specified in the source code.
 * <p>For example, consider the type variable:
 *
 * <pre class="brush:java">T extends Number</pre>
 *
 * The <code>identifier()</code> is "T", while the <code>name()</code> is "java.lang.Number".
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public final class TypeVariable extends Type {
    private final String name;
    private final Type[] bounds;
    private int hash;

    TypeVariable(String name) {
        this(name, EMPTY_ARRAY);

    }

    TypeVariable(String name, Type[] bounds) {
        this(name, bounds, null);
    }

    TypeVariable(String name, Type[] bounds, AnnotationInstance[] annotations) {
        super(bounds.length > 0 ? bounds[0].name() : DotName.OBJECT_NAME, annotations);
        this.name = name;
        this.bounds = bounds;
    }

    /**
     * The identifier of this type variable as it appears in Java source code.
     *
     * <p> The following class has a type parameter, with an identifier of "T":
     * <pre class="brush:java; gutter:false;">
     *     class Foo&lt;T extends Number&gt; {}
     * </pre>
     *
     * @return the identifier of this type variable
     */
    public String identifier() {
        return name;
    }

    public List<Type> bounds() {
        return Collections.unmodifiableList(Arrays.asList(bounds));
    }

    Type[] boundArray() {
        return bounds;
    }

    @Override
    public Kind kind() {
        return Kind.TYPE_VARIABLE;
    }

    @Override
    public TypeVariable asTypeVariable() {
        return this;
    }

    String toString(boolean simple) {
        StringBuilder builder = new StringBuilder();
        appendAnnotations(builder);
        builder.append(name);

        // FIXME - revist this logic
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

        return name.equals(that.name) && Arrays.equals(bounds, that.bounds);

    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new TypeVariable(name, bounds, newAnnotations);
    }

    TypeVariable copyType(int boundIndex, Type bound) {
        if (boundIndex > bounds.length) {
            throw new IllegalArgumentException("Bound index outside of bounds");
        }

        Type[] bounds = this.bounds.clone();
        bounds[boundIndex] = bound;
        return new TypeVariable(name, bounds, annotationArray());
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash != 0) {
            return hash;
        }

        hash = super.hashCode();
        hash = 31 * hash + name.hashCode();
        hash = 31 * hash + Arrays.hashCode(bounds);
        return this.hash = hash;
    }
}
