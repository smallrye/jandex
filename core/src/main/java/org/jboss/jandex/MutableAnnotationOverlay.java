package org.jboss.jandex;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * An {@link AnnotationOverlay} that can be freely mutated. The {@link #freeze()} operation
 * returns a list of {@linkplain AnnotationTransformation annotation transformations} that
 * can later be used to create an equivalent immutable annotation overlay.
 *
 * @since 3.2.0
 */
public interface MutableAnnotationOverlay extends AnnotationOverlay {
    /**
     * Returns a new builder for a mutable annotation overlay for given {@code index}.
     *
     * <p>
     * <strong>Thread safety</strong>
     *
     * <p>
     * The object returned by the builder is <em>not</em> thread safe and should be confined to a single thread.
     * After calling {@link #freeze()}, the object becomes immutable and can be shared between threads.
     *
     * @param index the Jandex index, must not be {@code null}
     * @return the mutable annotation overlay builder, never {@code null}
     */
    static MutableAnnotationOverlay.Builder builder(IndexView index) {
        Objects.requireNonNull(index);
        return new Builder(index);
    }

    /**
     * Adds given annotation instance to given {@code declaration}. When asking this annotation
     * overlay about annotation information for given declaration, the results will include
     * given annotation instance.
     *
     * @param declaration the declaration to modify, must not be {@code null}
     * @param annotation the annotation instance to add to {@code declaration} for, must not be {@code null}
     */
    void addAnnotation(Declaration declaration, AnnotationInstance annotation);

    /**
     * Removes all annotations matching given {@code predicate} from given {@code declaration}.
     * When asking this annotation overlay about annotation information for given declaration,
     * the results will not include matching annotation instances.
     *
     * @param declaration the declaration to modify, must not be {@code null}
     * @param predicate the annotation predicate, must not be {@code null}
     */
    void removeAnnotations(Declaration declaration, Predicate<AnnotationInstance> predicate);

    /**
     * Freezes this mutable annotation overlay and returns the annotation transformations to create
     * an equivalent immutable annotation overlay. After freezing, the {@link #addAnnotation(Declaration, AnnotationInstance)}
     * and {@link #removeAnnotations(Declaration, Predicate)} methods will throw an exception.
     *
     * @return immutable list of annotation transformations equivalent to mutations performed on this annotation overlay,
     *         never {@code null}
     */
    List<AnnotationTransformation> freeze();

    /**
     * The builder for a mutable annotation overlay.
     */
    final class Builder {
        private final IndexView index;

        private boolean compatibleMode;
        private boolean runtimeAnnotationsOnly;
        private boolean inheritedAnnotations;

        Builder(IndexView index) {
            this.index = index;
        }

        /**
         * When called, the built annotation overlay shall treat method parameters as part of methods.
         * This means that annotations on method parameters are returned when asking for annotations
         * of a method, asking for annotations on method parameters results in an exception, and
         * annotation transformations for methods are produced when adding/removing annotations
         * to/from a method parameter.
         * <p>
         * This method is called {@code compatibleMode} because the built annotation overlay is
         * compatible with the previous implementation of the same concept in Quarkus.
         *
         * @return this builder
         */
        public Builder compatibleMode() {
            compatibleMode = true;
            return this;
        }

        /**
         * When called, the built annotation overlay shall only return runtime-retained annotations;
         * class-retained annotations are ignored. Note that this only applies to annotations present
         * in class files (and therefore in Jandex); annotations added to the overlay using
         * {@link #addAnnotation(Declaration, AnnotationInstance)} are not inspected and are always
         * returned.
         *
         * @return this builder
         */
        public Builder runtimeAnnotationsOnly() {
            runtimeAnnotationsOnly = true;
            return this;
        }

        /**
         * When called, the built annotation overlay shall return {@linkplain java.lang.annotation.Inherited inherited}
         * annotations per the Java rules.
         *
         * @return this builder
         */
        public Builder inheritedAnnotations() {
            inheritedAnnotations = true;
            return this;
        }

        /**
         * Builds and returns a mutable annotation overlay based on the configuration of this builder.
         *
         * @return the mutable annotation overlay, never {@code null}
         */
        public MutableAnnotationOverlay build() {
            return new MutableAnnotationOverlayImpl(index, compatibleMode, runtimeAnnotationsOnly, inheritedAnnotations);
        }
    }
}
