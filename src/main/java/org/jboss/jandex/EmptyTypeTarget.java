package org.jboss.jandex;

/**
* @author Jason T. Greene
*/
public class EmptyTypeTarget extends TypeTarget {
    private boolean receiver;

    EmptyTypeTarget(AnnotationTarget enclosingTarget, boolean receiver) {
        super(enclosingTarget);
        this.receiver = receiver;
    }

    EmptyTypeTarget(AnnotationTarget enclosingTarget, Type target, boolean receiver) {
        super(enclosingTarget, target);
        this.receiver = receiver;
    }

    public boolean isReceiver() {
        return receiver;
    }

    @Override
    public final Usage usage() {
        return Usage.EMPTY;
    }

    @Override
    public EmptyTypeTarget asEmpty() {
        return this;
    }
}
