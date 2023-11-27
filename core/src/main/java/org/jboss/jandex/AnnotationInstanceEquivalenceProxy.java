package org.jboss.jandex;

import java.util.Objects;

/**
 * Holds an {@link AnnotationInstance} and implements equality and hash code as equivalence.
 * <p>
 * When using equivalence proxies, it is usually a mistake to obtain
 * the {@linkplain AnnotationInstance#target() target} of the delegate annotation instance.
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe
 * publication.
 *
 * @see AnnotationInstance#equivalentTo(AnnotationInstance)
 * @see AnnotationInstance#equivalenceHashCode()
 */
public final class AnnotationInstanceEquivalenceProxy {
    private final AnnotationInstance annotation;

    AnnotationInstanceEquivalenceProxy(AnnotationInstance annotation) {
        this.annotation = Objects.requireNonNull(annotation);
    }

    public AnnotationInstance get() {
        return annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return annotation.equivalentTo(((AnnotationInstanceEquivalenceProxy) o).annotation);
    }

    @Override
    public int hashCode() {
        return annotation.equivalenceHashCode();
    }
}
