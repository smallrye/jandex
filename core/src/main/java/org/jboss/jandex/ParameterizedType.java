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
 * Represents a parameterized type. The {@code name()} corresponds to the raw type, and the
 * {@code arguments()} list corresponds to the type arguments applied to the generic class
 * in order to instantiate this parameterized type.
 * <p>
 * For example, the parameterized type {@code Map<String, Integer>} would have a name of
 * {@code java.util.Map} and two {@code ClassType} arguments: {@code java.lang.String} and
 * {@code java.lang.Integer}.
 * <p>
 * Additionally, a parameterized type is used to represent an inner type whose enclosing type
 * is either parameterized or has type annotations. In this case, the {@code owner()} method
 * returns the type of the enclosing class. Such inner type may itself be parameterized.
 * <p>
 * For example, assume the following declarations:
 * 
 * <pre class="brush:java">
 * class A&lt;T&gt; {
 *     class B {
 *     }
 * }
 *
 * class C {
 *     class D {
 *     }
 * }
 * </pre>
 *
 * Then, the type {@code A<String>.B} is reprezented as a parameterized type, even though
 * {@code B} is not parameterized, because the enclosing type is parameterized. The owner
 * of this type is the parameterized type {@code A<String>}.
 * <p>
 * Similarly, the type {@code @TypeAnn C.D} is reprezented as a parameterized type, even
 * though neither {@code C} nor {@code D} are parameterized, because the enclosing type
 * has a type annotation. The owner of this type is the class type {@code @TypeAnn C}.
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public class ParameterizedType extends Type {

    /**
     * Create an instance of a parameterized type with given {@code name}, which denotes
     * a generic class, and given type {@code arguments}.
     * <p>
     * An {@code owner} may be supplied when the new instance is supposed to represent
     * an inner type, in case the enclosing type is either parameterized or annotated
     * with a type annotation.
     *
     * @param name the name of this parameterized type
     * @param arguments an array of type arguments applied to a generic class to form this type
     * @param owner the enclosing type if annotated or parameterized, otherwise {@code null}
     * @return the parameterized type
     * @since 2.1
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
     * Returns the list of type arguments used to instantiate this parameterized type.
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
     * Returns the owner (enclosing) type of this parameterized type, if the owner is parameterized
     * or has type annotations. In the latter case, the owner may be a {@code ClassType}. Returns
     * {@code null} otherwise.
     * <p>
     * Note that parameterized inner classes whose enclosing types are not parameterized or type-annotated
     * have no owner and hence this method returns {@code null} in such case.
     * <p>
     * This example shows the case where a parameterized type is used to represent a non-parameterized
     * class {@code X}:
     *
     * <pre class="brush:java">
     * Y&lt;String&gt;.X
     * </pre>
     *
     * This example will return a parameterized type for {@code Y} when {@code X}'s {@code owner()} method
     * is called.
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

    @Override
    ParameterizedType copyType(AnnotationInstance[] newAnnotations) {
        return new ParameterizedType(name(), arguments, owner, newAnnotations);
    }

    ParameterizedType copyType(Type[] arguments) {
        return new ParameterizedType(name(), arguments, owner, annotationArray());
    }

    ParameterizedType copyType(int argumentIndex, Type argument) {
        if (argumentIndex > this.arguments.length) {
            throw new IllegalArgumentException("Type argument index outside of bounds");
        }

        Type[] arguments = this.arguments.clone();
        arguments[argumentIndex] = argument;
        return new ParameterizedType(name(), arguments, owner, annotationArray());
    }

    ParameterizedType copyType(Type owner) {
        return new ParameterizedType(name(), arguments, owner, annotationArray());
    }

    @Override
    String toString(boolean simple) {
        StringBuilder builder = new StringBuilder();

        if (owner != null) {
            builder.append(owner);
            builder.append('.');
            appendAnnotations(builder);
            builder.append(name().local());
        } else {
            String packagePrefix = name().packagePrefix();
            if (packagePrefix != null) {
                builder.append(packagePrefix).append('.');
            }
            appendAnnotations(builder);
            builder.append(name().withoutPackagePrefix());
        }

        if (arguments.length > 0) {
            builder.append('<');
            builder.append(arguments[0].toString(true));
            for (int i = 1; i < arguments.length; i++) {
                builder.append(", ").append(arguments[i].toString(true));
            }
            builder.append('>');
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

        ParameterizedType other = (ParameterizedType) o;

        return (owner == other.owner || (owner != null && owner.equals(other.owner)))
                && Arrays.equals(arguments, other.arguments);
    }

    @Override
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

    @Override
    public boolean internEquals(Object o) {
        if (this == o) {
            return true;
        }

        if (!super.internEquals(o)) {
            return false;
        }

        ParameterizedType other = (ParameterizedType) o;

        return (owner == other.owner || (owner != null && owner.internEquals(other.owner)))
                && Interned.arrayEquals(arguments, other.arguments);
    }

    @Override
    public int internHashCode() {
        int hash = super.internHashCode();
        hash = 31 * hash + Interned.arrayHashCode(arguments);
        hash = 31 * hash + (owner != null ? owner.internHashCode() : 0);
        return hash;
    }
}
