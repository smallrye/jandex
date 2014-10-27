package org.jboss.jandex;

/**
 * @author Jason T. Greene
 */
public class MethodParameterTypeTarget extends PositionBasedTypeTarget {

    MethodParameterTypeTarget(MethodInfo enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    MethodParameterTypeTarget(AnnotationTarget enclosingTarget, Type target, int position) {
        super(enclosingTarget, target, position);
    }

    @Override
    public final Usage usage() {
        return Usage.METHOD_PARAMETER;
    }

    @Override
    public MethodInfo enclosingTarget() {
        return (MethodInfo) super.enclosingTarget();
    }

    @Override
    public MethodParameterTypeTarget asMethodParameter() {
        return this;
    }
}
