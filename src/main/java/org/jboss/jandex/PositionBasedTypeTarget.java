package org.jboss.jandex;

/**
* @author Jason T. Greene
*/
public abstract class PositionBasedTypeTarget extends TypeTarget {
    private final int position;

    PositionBasedTypeTarget(AnnotationTarget enclosingTarget, int position) {
        super(enclosingTarget);
        this.position = position;
    }

    PositionBasedTypeTarget(AnnotationTarget enclosingTarget, Type target, int position) {
        super(enclosingTarget, target);
        this.position = position;
    }

    public final int position() {
        return position;
    }

}
