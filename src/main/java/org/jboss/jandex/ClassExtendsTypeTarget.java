package org.jboss.jandex;

/**
* @author Jason T. Greene
*/
public class ClassExtendsTypeTarget extends PositionBasedTypeTarget {
    ClassExtendsTypeTarget(ClassInfo enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    public ClassExtendsTypeTarget(AnnotationTarget enclosingTarget, Type target, int position) {
        super(enclosingTarget, target, position);
    }

    @Override
    public final Kind kind() {
        return Kind.CLASS_EXTENDS;
    }

    @Override
    public ClassInfo enclosingTarget() {
        return (ClassInfo) super.enclosingTarget();
    }

    @Override
    public ClassExtendsTypeTarget asClassExtends() {
        return this;
    }
}
