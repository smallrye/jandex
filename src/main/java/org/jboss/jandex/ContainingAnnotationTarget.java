package org.jboss.jandex;

import java.util.List;

/**
 * Holds methods common {@link org.jboss.jandex.FieldInfo} and {@link org.jboss.jandex.MethodInfo}.
 *
 */
public interface ContainingAnnotationTarget extends AnnotationTarget {

    List<AnnotationInstance> annotations();

    AnnotationInstance annotation(DotName name);

    boolean hasAnnotation(DotName name);
}
