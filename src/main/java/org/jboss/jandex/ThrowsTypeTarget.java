package org.jboss.jandex;

/**
 * @author Jason T. Greene
 */
public class ThrowsTypeTarget extends PositionBasedTypeTarget {
    ThrowsTypeTarget(MethodInfo enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    @Override
    public Kind kind() {
        return Kind.THROWS;
    }

    @Override
    public MethodInfo enclosingTarget() {
        return (MethodInfo) super.enclosingTarget();
    }
}
