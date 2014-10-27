package org.jboss.jandex;

/**
* @author Jason T. Greene
*/
public class TypeParameterBoundTypeTarget extends TypeParameterTypeTarget {
    private int boundPosition;

    TypeParameterBoundTypeTarget(AnnotationTarget enclosingTarget, int position, int boundPosition) {
        super(enclosingTarget, position);
        this.boundPosition = boundPosition;
    }

    TypeParameterBoundTypeTarget(AnnotationTarget enclosingTarget, Type target, int position, int boundPosition) {
        super(enclosingTarget, target, position);
        this.boundPosition = boundPosition;
    }

    public final int boundPosition() {
        return boundPosition;
    }

    @Override
    public final Usage usage() {
        return Usage.TYPE_PARAMETER_BOUND;
    }

    @Override
    public TypeParameterBoundTypeTarget asTypeParameterBound() {
        return this;
    }
}
