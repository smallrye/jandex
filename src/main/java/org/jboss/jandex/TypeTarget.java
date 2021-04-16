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
 * @since 2.0
 *
 * @author Jason T. Greene
*/
public abstract class TypeTarget implements AnnotationTarget {
    private final AnnotationTarget enclosingTarget;
    private Type target;

    /** Specifies a form of usage of a type annotation */
    public enum Usage {
        /** Indicates a type annotation occurs within a field, method receiver, or method return type */
        EMPTY,

        /** Indicates a type annotation occurs within class' extends or implements clause */
        CLASS_EXTENDS,

        /** Indicates a type annotation occurs within a method parameter */
        METHOD_PARAMETER,

        /** Indicates a type annotation occurs within a method or class type parameter */
        TYPE_PARAMETER,

        /** Indicates a type annotation occurs within the bound of a method or class type parameter */
        TYPE_PARAMETER_BOUND,

        /** Indicates a type annotation occurs within the throws clause of a method */
        THROWS
    }

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
     * Returns the type which contains the respective annotation. In some cases this may be null
     * (e.g. in the case of a bridge method, which erases type information so there is no
     * target to map to)
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
    public MethodParameterTypeTarget asMethodParameterType() {
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
        throw new IllegalArgumentException("Not a method parameter");
    }

    @Override
    public final TypeTarget asType() {
        return this;
    }

    @Override
    public RecordComponentInfo asRecordComponent() {
        throw new IllegalArgumentException("Not a record component");
    }

}
