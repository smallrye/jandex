package org.jboss.jandex;

/**
 * @author Jason T. Greene
 */
public class ThrowsTypeTarget extends PositionBasedTypeTarget {
    ThrowsTypeTarget(MethodInfo enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    ThrowsTypeTarget(AnnotationTarget enclosingTarget, Type target, int position) {
        super(enclosingTarget, target, position);
    }

    @Override
    public Usage usage() {
        return Usage.THROWS;
    }

    @Override
    public MethodInfo enclosingTarget() {
        return (MethodInfo) super.enclosingTarget();
    }

    @Override
    public ThrowsTypeTarget asThrows() {
        return this;
    }
}
