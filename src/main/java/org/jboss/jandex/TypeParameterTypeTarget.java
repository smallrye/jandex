package org.jboss.jandex;

/**
 * @author Jason T. Greene
 */
public class TypeParameterTypeTarget extends PositionBasedTypeTarget {

    TypeParameterTypeTarget(AnnotationTarget enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    @Override
    public Kind kind() {
        return Kind.TYPE_PARAMETER;
    }
}
