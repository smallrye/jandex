package org.jboss.jandex;

import java.util.Deque;

/**
* @author Jason T. Greene
*/
public abstract class TypeTarget implements AnnotationTarget {
    private final AnnotationTarget enclosingTarget;
    private Type target;

    public enum Kind {EMPTY, CLASS_EXTENDS, METHOD_PARAMETER, TYPE_PARAMETER, TYPE_PARAMETER_BOUND, THROWS}

    TypeTarget(AnnotationTarget enclosingTarget, Type target) {
        this.enclosingTarget = enclosingTarget;
        this.target = target;
    }

    TypeTarget(AnnotationTarget enclosingTarget) {
        this(enclosingTarget, null);
    }

    void setTarget(Type target) {
        if (this.target != null) {
            throw new IllegalStateException("Attempt to reassign target");
        }

        this.target = target;
    }

    public AnnotationTarget enclosingTarget() {
        return enclosingTarget;
    }

    public Type target() {
        return target;
    }

    public abstract Kind kind();

    public EmptyTypeTarget asEmpty() {
        throw new IllegalArgumentException("Not an empty type target");
    }

    public ClassExtendsTypeTarget asClassExtends() {
        throw new IllegalArgumentException("Not a class extends type target");
    }

    public MethodParameterTypeTarget asMethodParameter() {
        throw new IllegalArgumentException("Not a method parameter type target");
    }

     public TypeParameterTypeTarget asTypeParameter() {
        throw new IllegalArgumentException("Not a type parameter target");
    }

    public TypeParameterBoundTypeTarget asTypeParameterBound() {
        throw new IllegalArgumentException("Not a type parameter bound target");
    }

    public ThrowsTypeTarget asThrows() {
        throw new IllegalArgumentException("Not a throws type target");
    }
}
