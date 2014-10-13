package org.jboss.jandex;

/**
* @author Jason T. Greene
*/
public class ClassExtendsTypeTarget extends PositionBasedTypeTarget {
    private int superTypeIndex;

    ClassExtendsTypeTarget(ClassInfo enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    @Override
    public final Kind kind() {
        return Kind.CLASS_EXTENDS;
    }

    @Override
    public ClassInfo enclosingTarget() {
        return (ClassInfo) super.enclosingTarget();
    }
}
