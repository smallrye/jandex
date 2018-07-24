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
 * Represents a generic parameterized type. The <code>name()</code> corresponds to the raw type,
 * and the arguments list corresponds to a list of type arguments passed to the parameterized type.
 *
 * <p>Additionally, a parameterized type is used to represent an inner class whose enclosing class
 * is either parameterized or has type annotations. In this case, the <code>owner()</code> method
 * will specify the type for the enclosing class. It is also possible for such a type to be parameterized
 * itself.
 *
 * <p>For example, the follow declaration would have a name of "java.util.Map", and two
 * <code>ClassType</code> arguments, the first being "java.lang.String", the second "java.lang.Integer":
 *
 * <pre class="brush:java; gutter:false">
 *     java.util.Map&lt;String, Integer&gt;
 * </pre>
 *
 * <p>Another example shows the case where a parameterized type is used to represent a non-parameterized
 * class (Y), whose owner (X) is itself parameterized:
 * <pre class="brush:java; gutter:false">
 *     Y&lt;String&gt;.X
 * </pre>
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public class ParameterizedType extends Type {

    /**
     * Create a new mock instance.
     *
     * @param name
     * @param arguments
     * @param owner
     * @return the mock instance
     */
    public static ParameterizedType create(DotName name, Type[] arguments, Type owner) {
        return new ParameterizedType(name, arguments, owner);
    }

    private final Type[] arguments;
    private final Type owner;
    private int hash;

    ParameterizedType(DotName name, Type[] arguments, Type owner) {
        this(name, arguments, owner, null);
    }

    ParameterizedType(DotName name, Type[] arguments, Type owner, AnnotationInstance[] annotations) {
        super(name, annotations);
        this.arguments = arguments == null ? EMPTY_ARRAY : arguments;
        this.owner = owner;
    }

    /**
     * Returns the list of arguments passed to this Parameterized type.
     *
     * @return the list of type arguments, or empty if none
     */
    public List<Type> arguments() {
        return Collections.unmodifiableList(Arrays.asList(arguments));
    }

    Type[] argumentsArray() {
        return arguments;
    }

    /**
     * Returns the owner (enclosing) type of this parameterized type if the owner is parameterized,
     * or contains type annotations. The latter may be a <code>ClassType</code>. Otherwise null is
     * returned.
     *
     * <p>Note that this means that inner classes whose enclosing types are not parameterized or
     * annotated may return null when this method is called.</p>
     *
     * <p>The example below shows the case where a parameterized type is used to represent a non-parameterized
     * class (Y).
     * <pre class="brush:java; gutter:false;">
     *     Y&lt;String&gt;.X
     * </pre>
     *
     * <p>This example will return a parameterized type for "Y" when X's <code>owner()</code> method
     * is called.</p>
     *
     * @return the owner type if the owner is parameterized or annotated, otherwise null
     */
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

        if (arguments.length > 0) {
            builder.append('<');
            builder.append(arguments[0]);
            for (int i = 1; i < arguments.length; i++) {
                builder.append(", ").append(arguments[i]);
            }
            builder.append('>');
        }

        return builder.toString();
    }

    @Override
    ParameterizedType copyType(AnnotationInstance[] newAnnotations) {
        return new ParameterizedType(name(), arguments, owner, newAnnotations);
    }

    ParameterizedType copyType(Type[] parameters) {
        return new ParameterizedType(name(), parameters, owner, annotationArray());
    }

    ParameterizedType copyType(Type owner) {
        return new ParameterizedType(name(), arguments, owner, annotationArray());
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
                && Arrays.equals(arguments, other.arguments);
    }

    public int hashCode() {
        int hash = this.hash;
        if (hash != 0) {
            return hash;
        }

        hash = super.hashCode();
        hash = 31 * hash + Arrays.hashCode(arguments);
        hash = 31 * hash + (owner != null ? owner.hashCode() : 0);
        return this.hash = hash;
    }
}
