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

    public boolean isReceiver() {
        return receiver;
    }

    @Override
    public final Kind kind() {
        return Kind.EMPTY;
    }
}
