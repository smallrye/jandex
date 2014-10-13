package org.jboss.jandex;

/**
 * @author Jason T. Greene
 */
public class MethodParameterTypeTarget extends PositionBasedTypeTarget {

    MethodParameterTypeTarget(MethodInfo enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    @Override
    public final Kind kind() {
        return Kind.METHOD_PARAMETER;
    }

    @Override
    public MethodInfo enclosingTarget() {
        return (MethodInfo) super.enclosingTarget();
    }
}
