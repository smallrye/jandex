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
 * Represents a type that is the target of a type annotation. Type annotations can
 * occur at any nesting level on any type declaration. For this reason, an enclosing
 * target is provided, as well as other usage specific information to determine the
 * starting point for locating the type.
 *
 * <p>It is expected that callers will traverse the full tree from the specified
 * starting point, since this context is important in interpreting the meaning
 * of the type annotation</p>
 *
 * @see org.jboss.jandex.EmptyTypeTarget
 * @see org.jboss.jandex.ClassExtendsTypeTarget
 * @see org.jboss.jandex.TypeParameterTypeTarget
 * @see org.jboss.jandex.TypeParameterBoundTypeTarget
 * @see org.jboss.jandex.MethodParameterTypeTarget
 * @see org.jboss.jandex.ThrowsTypeTarget
 *
* @author Jason T. Greene
*/
public abstract class TypeTarget implements AnnotationTarget {
    private final AnnotationTarget enclosingTarget;
    private Type target;

    public enum Usage {EMPTY, CLASS_EXTENDS, METHOD_PARAMETER, TYPE_PARAMETER, TYPE_PARAMETER_BOUND, THROWS}

    TypeTarget(AnnotationTarget enclosingTarget, Type target) {
        this.enclosingTarget = enclosingTarget;
        this.target = target;
    }

    TypeTarget(AnnotationTarget enclosingTarget) {
        this(enclosingTarget, null);
    }

    void setTarget(Type target) {
        this.target = target;
    }

    @Override
    public final Kind kind() {
        return Kind.TYPE;
    }

    /**
     * Returns the enclosing target that contains the type referred to by the {@link #target()} method.
     *
     * @return the enclosing target
     */
    public AnnotationTarget enclosingTarget() {
        return enclosingTarget;
    }

    /**
     * Returns the type which contains the respective annotation.
     *
     * @return the type containing the respective annotation
     */
    public Type target() {
        return target;
    }

    /**
     * Returns the kind of usage of this type target. This allows a caller to use a switch statement
     * as opposed to <code>getClass()</code> comparisons.
     *
     * @return the kind of usage of this type target
     */
    public abstract Usage usage();

    public EmptyTypeTarget asEmpty() {
        throw new IllegalArgumentException("Not an empty type target");
    }

    /**
     * Casts and returns this type target as a <code>ClassExtendsTypeTarget</code>. If this type target
     * is not a <code>ClassExtendsTypeTarget</code>, then an exception will be thrown.
     *
     * @return an instance of <code>ClassExtendsTypeTarget</code>
     * @throws java.lang.IllegalArgumentException if this is not a <code>ClassExtendsTypeTarget</code>
     */
    public ClassExtendsTypeTarget asClassExtends() {
        throw new IllegalArgumentException("Not a class extends type target");
    }

    /**
     * Casts and returns this type target as a <code>MethodParameterTypeTarget</code>. If this type target
     * is not a <code>MethodParameterTypeTarget</code>, then an exception will be thrown.
     *
     * @return an instance of <code>MethodParameterTypeTarget</code>
     * @throws java.lang.IllegalArgumentException if this is not a <code>MethodParameterTypeTarget</code>
     */
    public MethodParameterTypeTarget asMethodParameter() {
        throw new IllegalArgumentException("Not a method parameter type target");
    }

    /**
     * Casts and returns this type target as a <code>TypeParameterTypeTarget</code>. If this type target
     * is not a <code>TypeParameterTypeTarget</code>, then an exception will be thrown.
     *
     * @return an instance of <code>TypeParameterTypeTarget</code>
     * @throws java.lang.IllegalArgumentException if this is not a <code>TypeParameterTypeTarget</code>
     */
    public TypeParameterTypeTarget asTypeParameter() {
        throw new IllegalArgumentException("Not a type parameter target");
    }

    /**
     * Casts and returns this type target as a <code>TypeParameterBoundTypeTarget</code>. If this type target
     * is not a <code>TypeParameterBoundTypeTarget</code>, then an exception will be thrown.
     *
     * @return an instance of <code>TypeParameterBoundTypeTarget</code>
     * @throws java.lang.IllegalArgumentException if this is not a <code>TypeParameterBoundTypeTarget</code>
     */
    public TypeParameterBoundTypeTarget asTypeParameterBound() {
        throw new IllegalArgumentException("Not a type parameter bound target");
    }

    /**
      * Casts and returns this type target as a <code>ThrowsTypeTarget</code>. If this type target
      * is not a <code>ThrowsTypeTarget</code>, then an exception will be thrown.
      *
      * @return an instance of <code>ThrowsTypeTarget</code>
      * @throws java.lang.IllegalArgumentException if this is not a <code>TypeParameterBoundTypeTarget</code>
      */
    public ThrowsTypeTarget asThrows() {
        throw new IllegalArgumentException("Not a throws type target");
    }
}
