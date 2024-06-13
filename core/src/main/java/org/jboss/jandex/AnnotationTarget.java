/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.jandex;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Represents an object that can be a target of an annotation.
 *
 * @see ClassInfo
 * @see FieldInfo
 * @see MethodInfo
 * @see MethodParameterInfo
 *
 * @author Jason T. Greene
 *
 */
public interface AnnotationTarget {

    /**
     * Specifies the kind of object a target represents.
     */
    public enum Kind {
        /**
         * An object of type {@link org.jboss.jandex.ClassInfo}
         */
        CLASS,

        /**
         * An object of type {@link org.jboss.jandex.FieldInfo}
         */
        FIELD,

        /**
         * An object of type {@link org.jboss.jandex.MethodInfo}
         */
        METHOD,

        /**
         * An object of type {@link org.jboss.jandex.MethodParameterInfo}
         */
        METHOD_PARAMETER,

        /**
         * An object of type {@link org.jboss.jandex.TypeTarget}
         */
        TYPE,

        /**
         * An object of type {@link org.jboss.jandex.RecordComponentInfo}
         *
         * @since 2.4
         */
        RECORD_COMPONENT
    }

    /**
     * Returns the kind of object this target represents.
     *
     * @return the target kind.
     * @since 2.0
     */
    Kind kind();

    /**
     * Casts and returns this target as a {@code ClassInfo} if it is of kind {@code CLASS}
     *
     * @return this instance cast to a class
     * @since 2.0
     */
    ClassInfo asClass();

    /**
     * Casts and returns this target as a {@code FieldInfo} if it is of kind {@code FIELD}
     *
     * @return this instance cast to a field
     * @since 2.0
     */
    FieldInfo asField();

    /**
     * Casts and returns this target as a {@code MethodInfo} if it is of kind {@code METHOD}
     *
     * @return this instance cast to a method
     * @since 2.0
     */
    MethodInfo asMethod();

    /**
     * Casts and returns this target as a {@code MethodParameterInfo} if it is of kind {@code METHOD_PARAMETER}
     *
     * @return this instance cast to a method parameter
     * @since 2.0
     */
    MethodParameterInfo asMethodParameter();

    /**
     * Casts and returns this target as a {@code TypeTarget} if it is of kind {@code TYPE}
     *
     * @return this instance cast to a type target
     * @since 2.0
     */
    TypeTarget asType();

    /**
     * Casts and returns this target as a {@code RecordComponentInfo} if it is of kind {@code RECORD_COMPONENT}
     *
     * @return this instance cast to a record component
     * @since 2.4
     */
    RecordComponentInfo asRecordComponent();

    // ---

    /**
     * Returns whether an annotation instance with given name is declared on this annotation target or any of its
     * nested annotation targets.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #annotation(DotName)
     */
    boolean hasAnnotation(DotName name);

    /**
     * Returns whether an annotation instance with given name is declared on this annotation target or any of its
     * nested annotation targets.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #annotation(DotName)
     */
    default boolean hasAnnotation(String name) {
        return hasAnnotation(DotName.createSimple(name));
    }

    /**
     * Returns whether an annotation instance of given type is declared on this annotation target or any of its
     * nested annotation targets.
     *
     * @param clazz the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #annotation(DotName)
     */
    default boolean hasAnnotation(Class<? extends Annotation> clazz) {
        return hasAnnotation(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns the annotation instance with given name declared on this annotation target or any of its nested
     * annotation targets. The {@code target()} method of the returned annotation instance may be used to determine
     * the exact location of the annotation instance.
     * <p>
     * In case an annotation with given name occurs more than once, the result of this method is not deterministic.
     * For such situations, {@link #annotations(DotName)} is preferable.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotations(DotName)
     */
    AnnotationInstance annotation(DotName name);

    /**
     * Returns the annotation instance with given name declared on this annotation target or any of its nested
     * annotation targets. The {@code target()} method of the returned annotation instance may be used to determine
     * the exact location of the annotation instance.
     * <p>
     * In case an annotation with given name occurs more than once, the result of this method is not deterministic.
     * For such situations, {@link #annotations(DotName)} is preferable.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotations(DotName)
     */
    default AnnotationInstance annotation(String name) {
        return annotation(DotName.createSimple(name));
    }

    /**
     * Returns the annotation instance of given type declared on this annotation target or any of its nested
     * annotation targets. The {@code target()} method of the returned annotation instance may be used to determine
     * the exact location of the annotation instance.
     * <p>
     * In case an annotation with given name occurs more than once, the result of this method is not deterministic.
     * For such situations, {@link #annotations(DotName)} is preferable.
     *
     * @param clazz the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotations(DotName)
     */
    default AnnotationInstance annotation(Class<? extends Annotation> clazz) {
        return annotation(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns the annotation instances with given name declared on this annotation target and nested annotation targets.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     * @see #annotations()
     */
    Collection<AnnotationInstance> annotations(DotName name);

    /**
     * Returns the annotation instances with given name declared on this annotation target and nested annotation targets.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     * @see #annotations()
     */
    default Collection<AnnotationInstance> annotations(String name) {
        return annotations(DotName.createSimple(name));
    }

    /**
     * Returns the annotation instances of given type declared on this annotation target and nested annotation targets.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     *
     * @param clazz the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     * @see #annotations()
     */
    default Collection<AnnotationInstance> annotations(Class<? extends Annotation> clazz) {
        return annotations(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns the annotation instances with given name declared on this annotation target and nested annotation targets.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotations(DotName)
     * @see #annotations()
     */
    Collection<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index);

    /**
     * Returns the annotation instances with given name declared on this annotation target and nested annotation targets.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotations(DotName)
     * @see #annotations()
     */
    default Collection<AnnotationInstance> annotationsWithRepeatable(String name, IndexView index) {
        return annotationsWithRepeatable(DotName.createSimple(name), index);
    }

    /**
     * Returns the annotation instances of given type declared on this annotation target and nested annotation targets.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     *
     * @param clazz the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotations(DotName)
     * @see #annotations()
     */
    default Collection<AnnotationInstance> annotationsWithRepeatable(Class<? extends Annotation> clazz, IndexView index) {
        return annotationsWithRepeatable(DotName.createSimple(clazz.getName()), index);
    }

    /**
     * Returns the annotation instances declared on this annotation target and nested annotation targets.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     *
     * @return immutable collection of annotation instances, never {@code null}
     * @since 3.0
     */
    Collection<AnnotationInstance> annotations();

    /**
     * Returns whether an annotation instance with given name is declared on this annotation target.
     * <p>
     * Unlike {@link #hasAnnotation(DotName)}, this method ignores annotations declared on nested annotation targets.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #hasAnnotation(DotName)
     */
    boolean hasDeclaredAnnotation(DotName name);

    /**
     * Returns whether an annotation instance with given name is declared on this annotation target.
     * <p>
     * Unlike {@link #hasAnnotation(DotName)}, this method ignores annotations declared on nested annotation targets.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #hasAnnotation(DotName)
     */
    default boolean hasDeclaredAnnotation(String name) {
        return hasDeclaredAnnotation(DotName.createSimple(name));
    }

    /**
     * Returns whether an annotation instance of given type is declared on this annotation target.
     * <p>
     * Unlike {@link #hasAnnotation(DotName)}, this method ignores annotations declared on nested annotation targets.
     *
     * @param clazz the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #hasAnnotation(DotName)
     */
    default boolean hasDeclaredAnnotation(Class<? extends Annotation> clazz) {
        return hasDeclaredAnnotation(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns the annotation instance with given name declared on this annotation target.
     * <p>
     * Unlike {@link #annotation(DotName)}, this method doesn't return annotations declared on nested annotation targets.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotation(DotName)
     */
    AnnotationInstance declaredAnnotation(DotName name);

    /**
     * Returns the annotation instance with given name declared on this annotation target.
     * <p>
     * Unlike {@link #annotation(DotName)}, this method doesn't return annotations declared on nested annotation targets.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotation(DotName)
     */
    default AnnotationInstance declaredAnnotation(String name) {
        return declaredAnnotation(DotName.createSimple(name));
    }

    /**
     * Returns the annotation instance of given type declared on this annotation target.
     * <p>
     * Unlike {@link #annotation(DotName)}, this method doesn't return annotations declared on nested annotation targets.
     *
     * @param clazz the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotation(DotName)
     */
    default AnnotationInstance declaredAnnotation(Class<? extends Annotation> clazz) {
        return declaredAnnotation(DotName.createSimple(clazz.getName()));
    }

    /**
     * Returns the annotation instances with given name declared on this annotation target.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     * <p>
     * Unlike {@link #annotationsWithRepeatable(DotName, IndexView)}, this method doesn't return annotations
     * declared on nested annotation targets.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     */
    Collection<AnnotationInstance> declaredAnnotationsWithRepeatable(DotName name, IndexView index);

    /**
     * Returns the annotation instances with given name declared on this annotation target.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     * <p>
     * Unlike {@link #annotationsWithRepeatable(DotName, IndexView)}, this method doesn't return annotations
     * declared on nested annotation targets.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     */
    default Collection<AnnotationInstance> declaredAnnotationsWithRepeatable(String name, IndexView index) {
        return declaredAnnotationsWithRepeatable(DotName.createSimple(name), index);
    }

    /**
     * Returns the annotation instances of given type declared on this annotation target.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     * <p>
     * Unlike {@link #annotationsWithRepeatable(DotName, IndexView)}, this method doesn't return annotations
     * declared on nested annotation targets.
     *
     * @param clazz the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable collection of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     */
    default Collection<AnnotationInstance> declaredAnnotationsWithRepeatable(Class<? extends Annotation> clazz,
            IndexView index) {
        return declaredAnnotationsWithRepeatable(DotName.createSimple(clazz.getName()), index);
    }

    /**
     * Returns the annotation instances declared on this annotation target.
     * <p>
     * Unlike {@link #annotations()}, this method doesn't return annotations declared on nested annotation targets.
     *
     * @return immutable collection of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotations()
     */
    Collection<AnnotationInstance> declaredAnnotations();
}
