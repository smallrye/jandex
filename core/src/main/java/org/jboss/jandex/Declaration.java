package org.jboss.jandex;

/**
 * An {@link AnnotationTarget} that is also a declaration.
 *
 * @see ClassInfo
 * @see FieldInfo
 * @see MethodInfo
 * @see MethodParameterInfo
 * @see RecordComponentInfo
 * @since 3.1.0
 */
public interface Declaration extends AnnotationTarget {
    @Override
    default boolean isDeclaration() {
        return true;
    }

    @Override
    default Declaration asDeclaration() {
        return this;
    }
}
