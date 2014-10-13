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

    public final int boundPosition() {
        return boundPosition;
    }

    @Override
    public final Kind kind() {
        return Kind.TYPE_PARAMETER_BOUND;
    }
}
