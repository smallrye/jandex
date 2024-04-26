package org.jboss.jandex;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Annotation overlay allows overriding annotation information from an index. This is useful when
 * Jandex is used as a language model and annotations are directly used as framework metadata.
 * Transforming metadata is a frequent requirement in such situations, but core Jandex is immutable.
 * This interface is layered on top of core Jandex and provides the necessary indirection between
 * the user and the core Jandex that is required to apply the transformations.
 *
 * @since 3.2.0
 */
public interface AnnotationOverlay {
    /**
     * Returns a new builder for an annotation overlay for given {@code index} and a given collection
     * of {@code transformations}.
     *
     * <p>
     * <strong>Thread safety</strong>
     *
     * <p>
     * The object returned by the builder is immutable and can be shared between threads without safe publication.
     *
     * @param index the Jandex index, must not be {@code null}
     * @param annotationTransformations the collection of annotation transformations
     * @return the annotation overlay builder, never {@code null}
     */
    static Builder builder(IndexView index, Collection<AnnotationTransformation> annotationTransformations) {
        Objects.requireNonNull(index);
        if (annotationTransformations == null) {
            annotationTransformations = Collections.emptyList();
        }
        return new Builder(index, annotationTransformations);
    }

    /**
     * Returns the index whose annotation information is being overlaid.
     *
     * @return the index underlying this annotation overlay, never {@code null}
     */
    IndexView index();

    /**
     * Returns whether an annotation instance with given {@code name} is declared on given {@code declaration}.
     * <p>
     * Like {@link AnnotationTarget#hasDeclaredAnnotation(DotName)}, and unlike {@link AnnotationTarget#hasAnnotation(DotName)},
     * this method ignores annotations declared on nested annotation targets. This doesn't hold in case of methods
     * in the {@linkplain Builder#compatibleMode() compatible mode}, where method parameters are considered
     * part of methods.
     *
     * @param declaration the declaration to inspect, must not be {@code null}
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     */
    boolean hasAnnotation(Declaration declaration, DotName name);

    /**
     * Returns whether an annotation instance of given {@code clazz} is declared on given {@code declaration}.
     * <p>
     * Like {@link AnnotationTarget#hasDeclaredAnnotation(Class)}, and unlike {@link AnnotationTarget#hasAnnotation(Class)},
     * this method ignores annotations declared on nested annotation targets. This doesn't hold in case of methods
     * in the {@linkplain Builder#compatibleMode() compatible mode}, where method parameters are considered
     * part of methods.
     *
     * @param declaration the declaration to inspect, must not be {@code null}
     * @param clazz the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @see #hasAnnotation(Declaration, DotName)
     */
    default boolean hasAnnotation(Declaration declaration, Class<? extends Annotation> clazz) {
        return hasAnnotation(declaration, DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns whether any annotation instance with one of given {@code names} is declared on given {@code declaration}.
     * <p>
     * This method ignores annotations declared on nested annotation targets. This doesn't hold in case of methods
     * in the {@linkplain Builder#compatibleMode() compatible mode}, where method parameters are considered
     * part of methods.
     *
     * @param declaration the declaration to inspect, must not be {@code null}
     * @param names names of the annotation types to look for, must not be {@code null}
     * @return {@code true} if any of the annotations is present, {@code false} otherwise
     */
    boolean hasAnyAnnotation(Declaration declaration, Set<DotName> names);

    /**
     * Returns whether any annotation instance of one of given {@code classes} is declared on given {@code declaration}.
     * <p>
     * This method ignores annotations declared on nested annotation targets. This doesn't hold in case of methods
     * in the {@linkplain Builder#compatibleMode() compatible mode}, where method parameters are considered
     * part of methods.
     *
     * @param declaration the declaration to inspect, must not be {@code null}
     * @param classes annotation types to look for, must not be {@code null}
     * @return {@code true} if any of the annotations is present, {@code false} otherwise
     */
    default boolean hasAnyAnnotation(Declaration declaration, Class<? extends Annotation>... classes) {
        Set<DotName> names = new HashSet<>(classes.length);
        for (Class<? extends Annotation> clazz : classes) {
            names.add(DotName.createSimple(clazz.getName()));
        }
        return hasAnyAnnotation(declaration, names);
    }

    /**
     * Returns the annotation instance with given {@code name} declared on given {@code declaration}.
     * <p>
     * Like {@link AnnotationTarget#annotation(DotName)}, and unlike {@link AnnotationTarget#annotation(DotName)},
     * this method doesn't return annotations declared on nested annotation targets. This doesn't hold in case of methods
     * in the {@linkplain Builder#compatibleMode() compatible mode}, where method parameters are considered
     * part of methods.
     *
     * @param declaration the declaration to inspect, must not be {@code null}
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     */
    AnnotationInstance annotation(Declaration declaration, DotName name);

    /**
     * Returns the annotation instance of given {@code clazz} declared on given {@code declaration}.
     * <p>
     * Like {@link AnnotationTarget#annotation(Class)}, and unlike {@link AnnotationTarget#annotation(Class)},
     * this method doesn't return annotations declared on nested annotation targets. This doesn't hold in case of methods
     * in the {@linkplain Builder#compatibleMode() compatible mode}, where method parameters are considered
     * part of methods.
     *
     * @param declaration the declaration to inspect, must not be {@code null}
     * @param clazz the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @see #annotation(Declaration, DotName)
     */
    default AnnotationInstance annotation(Declaration declaration, Class<? extends Annotation> clazz) {
        return annotation(declaration, DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns the annotation instances with given {@code name} declared on given {@code declaration}.
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance.
     * <p>
     * The annotation class must be present in the index underlying this annotation overlay.
     * <p>
     * Like {@link AnnotationTarget#declaredAnnotationsWithRepeatable(DotName, IndexView)}, and unlike
     * {@link AnnotationTarget#annotationsWithRepeatable(DotName, IndexView)}, this method doesn't return
     * annotations declared on nested annotation targets. This doesn't hold in case of methods
     * in the {@linkplain Builder#compatibleMode() compatible mode}, where method parameters are considered
     * part of methods.
     *
     * @param declaration the declaration to inspect, must not be {@code null}
     * @param name name of the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     */
    Collection<AnnotationInstance> annotationsWithRepeatable(Declaration declaration, DotName name);

    /**
     * Returns the annotation instances of given type ({@code clazz}) declared on given {@code declaration}.
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance.
     * <p>
     * The annotation class must be present in the index underlying this annotation overlay.
     * <p>
     * Like {@link AnnotationTarget#declaredAnnotationsWithRepeatable(Class, IndexView)}, and unlike
     * {@link AnnotationTarget#annotationsWithRepeatable(Class, IndexView)}, this method doesn't return
     * annotations declared on nested annotation targets. This doesn't hold in case of methods
     * in the {@linkplain Builder#compatibleMode() compatible mode}, where method parameters are considered
     * part of methods.
     *
     * @param declaration the declaration to inspect, must not be {@code null}
     * @param clazz the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @see #annotationsWithRepeatable(Declaration, DotName)
     */
    default Collection<AnnotationInstance> annotationsWithRepeatable(Declaration declaration,
            Class<? extends Annotation> clazz) {
        return annotationsWithRepeatable(declaration, DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns the annotation instances declared on given {@code declaration}.
     * <p>
     * Like {@link AnnotationTarget#declaredAnnotations()}, and unlike {@link AnnotationTarget#annotations()},
     * this method doesn't return annotations declared on nested annotation targets. This doesn't hold in case of methods
     * in the {@linkplain Builder#compatibleMode() compatible mode}, where method parameters are considered
     * part of methods.
     *
     * @param declaration the declaration to inspect, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     */
    Collection<AnnotationInstance> annotations(Declaration declaration);

    /**
     * The builder for an annotation overlay.
     */
    final class Builder {
        private final IndexView index;
        private final Collection<AnnotationTransformation> annotationTransformations;

        private boolean compatibleMode;
        private boolean runtimeAnnotationsOnly;
        private boolean inheritedAnnotations;

        Builder(IndexView index, Collection<AnnotationTransformation> annotationTransformations) {
            this.index = index;
            this.annotationTransformations = annotationTransformations;
        }

        /**
         * When called, the built annotation overlay shall treat method parameters as part of methods.
         * This means that annotations on method parameters are returned when asking for annotations
         * of a method, asking for annotations on method parameters results in an exception, and
         * annotation transformations for method parameters are ignored.
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
         * {@linkplain AnnotationTransformation annotation transformations} are not inspected
         * and are always returned.
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
         * Builds and returns an annotation overlay based on the configuration of this builder.
         *
         * @return the annotation overlay, never {@code null}
         */
        public AnnotationOverlay build() {
            return new AnnotationOverlayImpl(index, compatibleMode, runtimeAnnotationsOnly, inheritedAnnotations,
                    annotationTransformations);
        }
    }
}
