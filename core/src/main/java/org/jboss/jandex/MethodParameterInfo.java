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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an individual Java method parameter that was annotated.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 */
public final class MethodParameterInfo implements AnnotationTarget {
    private final MethodInfo method;
    private final short position;

    MethodParameterInfo(MethodInfo method, short position) {
        this.method = method;
        this.position = position;
    }

    /**
     * Constructs a new mock method parameter info
     *
     * @param method the method containing this parameter.
     * @param parameter the zero based index of this parameter
     * @return the new mock parameter info
     */
    public static MethodParameterInfo create(MethodInfo method, short parameter) {
        return new MethodParameterInfo(method, parameter);
    }

    /**
     * Returns the method this parameter belongs to.
     *
     * @return the declaring Java method
     */
    public final MethodInfo method() {
        return method;
    }

    /**
     * Returns the 0 based position of this parameter.
     *
     * @return the position of this parameter
     */
    public final short position() {
        return position;
    }

    /**
     * Returns the name of this parameter, or {@code null} if not known.
     * 
     * @return the name of this parameter, or {@code null} if not known
     */
    public final String name() {
        return method.parameterName(position);
    }

    /**
     * Returns the type of this parameter.
     *
     * @return the type of this parameter
     * @since 3.0
     */
    public final Type type() {
        return method.parameterType(position);
    }

    // assumes `annotation` is on `this.method`
    private boolean targetsThis(AnnotationInstance annotation) {
        return annotation.target().kind() == Kind.METHOD_PARAMETER
                && annotation.target().asMethodParameter().position == position;
    }

    // assumes `annotation` is on `this.method`
    private boolean targetsThisOrNested(AnnotationInstance annotation) {
        return targetsThis(annotation)
                || annotation.target().kind() == Kind.TYPE
                        && annotation.target().asType().usage() == TypeTarget.Usage.METHOD_PARAMETER
                        && annotation.target().asType().asMethodParameterType().position() == position;
    }

    /**
     * Returns whether an annotation instance with given name is declared on this method parameter or any type
     * within its signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #annotation(DotName)
     */
    @Override
    public boolean hasAnnotation(DotName name) {
        return annotation(name) != null;
    }

    /**
     * Returns the annotation instance with given name declared on this method parameter or any type within
     * its signature. The {@code target()} method of the returned annotation instance may be used to determine
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
    @Override
    public AnnotationInstance annotation(DotName name) {
        for (AnnotationInstance instance : method.methodInternal().annotationArray()) {
            if (targetsThisOrNested(instance) && instance.name().equals(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Returns the annotation instances with given name declared on this method parameter or any type within
     * its signature. The {@code target()} method of the returned annotation instances may be used to determine
     * the exact location of the respective annotation instance.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     * @see #annotations()
     */
    @Override
    public List<AnnotationInstance> annotations(DotName name) {
        List<AnnotationInstance> instances = new ArrayList<>();
        for (AnnotationInstance instance : method.methodInternal().annotationArray()) {
            if (targetsThisOrNested(instance) && instance.name().equals(name)) {
                instances.add(instance);
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances with given name declared on this method parameter or any type within
     * its signature. The {@code target()} method of the returned annotation instances may be used to determine
     * the exact location of the respective annotation instance.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotations(DotName)
     * @see #annotations()
     */
    @Override
    public List<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
        if (index == null) {
            throw new IllegalArgumentException("Index must not be null");
        }
        List<AnnotationInstance> instances = new ArrayList<>(annotations(name));
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.declaredAnnotation(Index.REPEATABLE);
        if (repeatable != null) {
            Type containingType = repeatable.value().asClass();
            for (AnnotationInstance container : annotations(containingType.name())) {
                for (AnnotationInstance nestedInstance : container.value().asNestedArray()) {
                    instances.add(AnnotationInstance.create(nestedInstance, container.target()));
                }
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances declared on this method parameter or any type within its signature.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     *
     * @return immutable list of annotation instances, never {@code null}
     * @since 3.0
     */
    @Override
    public List<AnnotationInstance> annotations() {
        List<AnnotationInstance> instances = new ArrayList<>();
        for (AnnotationInstance instance : method.methodInternal().annotationArray()) {
            if (targetsThisOrNested(instance)) {
                instances.add(instance);
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns whether an annotation instance with given name is declared on this method parameter.
     * <p>
     * Unlike {@link #hasAnnotation(DotName)}, this method ignores annotations declared on types within
     * the methor parameter signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #hasAnnotation(DotName)
     */
    @Override
    public boolean hasDeclaredAnnotation(DotName name) {
        return declaredAnnotation(name) != null;
    }

    /**
     * Returns the annotation instance with given name declared on this method parameter.
     * <p>
     * Unlike {@link #annotation(DotName)}, this method doesn't return annotations declared on types within
     * the methor parameter signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotation(DotName)
     */
    @Override
    public AnnotationInstance declaredAnnotation(DotName name) {
        for (AnnotationInstance instance : method.methodInternal().annotationArray()) {
            if (targetsThis(instance) && instance.name().equals(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Returns the annotation instances with given name declared on this method parameter.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     * <p>
     * Unlike {@link #annotationsWithRepeatable(DotName, IndexView)}, this method doesn't return annotations
     * declared on types within the methor parameter signature.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     */
    @Override
    public List<AnnotationInstance> declaredAnnotationsWithRepeatable(DotName name, IndexView index) {
        if (index == null) {
            throw new IllegalArgumentException("Index must not be null");
        }
        List<AnnotationInstance> instances = new ArrayList<>();
        AnnotationInstance declaredInstance = declaredAnnotation(name);
        if (declaredInstance != null) {
            instances.add(declaredInstance);
        }
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.declaredAnnotation(Index.REPEATABLE);
        if (repeatable != null) {
            Type containingType = repeatable.value().asClass();
            AnnotationInstance container = declaredAnnotation(containingType.name());
            if (container != null) {
                for (AnnotationInstance nestedInstance : container.value().asNestedArray()) {
                    instances.add(AnnotationInstance.create(nestedInstance, container.target()));
                }
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances declared on this method parameter.
     * <p>
     * Unlike {@link #annotations()}, this method doesn't return annotations declared on types within
     * the methor parameter signature.
     *
     * @return immutable list of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotations()
     */
    @Override
    public List<AnnotationInstance> declaredAnnotations() {
        List<AnnotationInstance> instances = new ArrayList<>();
        for (AnnotationInstance instance : method.methodInternal().annotationArray()) {
            if (targetsThis(instance)) {
                instances.add(instance);
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns a string representation describing this method parameter
     *
     * @return a string representation of this parameter
     */
    public String toString() {
        return method + " #" + position;
    }

    @Override
    public final ClassInfo asClass() {
        throw new IllegalArgumentException("Not a class");
    }

    @Override
    public final FieldInfo asField() {
        throw new IllegalArgumentException("Not a field");
    }

    @Override
    public final MethodInfo asMethod() {
        throw new IllegalArgumentException("Not a method");
    }

    @Override
    public final MethodParameterInfo asMethodParameter() {
        return this;
    }

    @Override
    public final TypeTarget asType() {
        throw new IllegalArgumentException("Not a type");
    }

    @Override
    public final RecordComponentInfo asRecordComponent() {
        throw new IllegalArgumentException("Not a record component");
    }

    @Override
    public Kind kind() {
        return Kind.METHOD_PARAMETER;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + method.hashCode();
        result = 31 * result + (int) position;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MethodParameterInfo other = (MethodParameterInfo) o;
        return method.equals(other.method) && position == other.position;
    }

}
