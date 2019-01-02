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
 * Represents an individual Java method parameter that was annotated.
 *
 * <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 */
public final class MethodParameterInfo implements AnnotationTarget {
    private final MethodInfo method;
    private final short parameter;

    MethodParameterInfo(MethodInfo method, short parameter)
    {
        this.method = method;
        this.parameter = parameter;
    }

    /**
     * Constructs a new mock method parameter info
     *
     * @param method the method containing this parameter.
     * @param parameter the zero based index of this parameter
     * @return the new mock parameter info
     */
    public static MethodParameterInfo create(MethodInfo method, short parameter)
    {
        return new MethodParameterInfo(method, parameter);
    }

    /**
     * Returns the method this parameter belongs to.
     *
     * @return the declaring Java method
     */
    public final MethodInfo method() {
        return method;
    }

    /**
     * Returns the 0 based position of this parameter.
     *
     * @return the position of this parameter
     */
    public final short position() {
        return parameter;
    }

    /**
     * Returns the name of this parameter.
     * 
     * @return the name of this parameter.
     */
    public final String name() {
        return method.parameterName(parameter);
    }

    /**
     * Returns the type of this parameter.
     * 
     * @return the type of this parameter.
     * @throws UnsupportedOperationException if this method's container class is a local class or an enum.
     */
    public Type type() {
        return method.realParameter(parameter);
    }

    /**
     * Returns a string representation describing this method parameter
     *
     * @return a string representation of this parameter
     */
    public String toString() {
        return method + " #" + parameter;
    }

    @Override
    public final ClassInfo asClass() {
        throw new IllegalArgumentException("Not a class");
    }

    @Override
    public final FieldInfo asField() {
        throw new IllegalArgumentException("Not a field");
    }

    @Override
    public final MethodInfo asMethod() {
        throw new IllegalArgumentException("Not a method");
    }

    @Override
    public final MethodParameterInfo asMethodParameter() {
        return this;
    }

    @Override
    public final TypeTarget asType() {
        throw new IllegalArgumentException("Not a type");
    }

    @Override
    public Kind kind() {
        return Kind.METHOD_PARAMETER;
    }
}
