package org.jboss.jandex;

import java.util.Deque;

/**
* @author Jason T. Greene
*/
public abstract class TypeTarget implements AnnotationTarget {
    private final AnnotationTarget enclosingTarget;
    private Type target;

    public enum Kind {EMPTY, CLASS_EXTENDS, METHOD_PARAMETER, TYPE_PARAMETER, TYPE_PARAMETER_BOUND, THROWS}

    TypeTarget(AnnotationTarget enclosingTarget) {
        this.enclosingTarget = enclosingTarget;
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
}
