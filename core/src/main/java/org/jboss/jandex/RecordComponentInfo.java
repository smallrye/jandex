/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021 Red Hat, Inc., and individual contributors
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
import java.util.function.Function;

/**
 * Represents an individual Java record component that was annotated.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 */
public final class RecordComponentInfo implements Declaration, Descriptor, GenericSignature {
    private ClassInfo clazz;
    private RecordComponentInternal internal;

    RecordComponentInfo() {
    }

    RecordComponentInfo(ClassInfo clazz, RecordComponentInternal internal) {
        this.clazz = clazz;
        this.internal = internal;
    }

    RecordComponentInfo(ClassInfo clazz, byte[] name, Type type) {
        this(clazz, new RecordComponentInternal(name, type));
    }

    /**
     * Constructs a new mock record component info
     *
     * @param clazz the (record) class declaring this record component
     * @param name the name of this record component
     * @param type the type of this record component
     * @return the new mock record component info
     */
    public static RecordComponentInfo create(ClassInfo clazz, String name, Type type) {
        if (clazz == null)
            throw new IllegalArgumentException("Clazz can't be null");

        if (name == null)
            throw new IllegalArgumentException("Name can't be null");

        return new RecordComponentInfo(clazz, Utils.toUTF8(name), type);
    }

    /**
     * Returns the (record) class declaring this record component.
     *
     * @return the (record) class declaring this record component
     */
    public final ClassInfo declaringClass() {
        return clazz;
    }

    /**
     * Returns the component field corresponding to this record component.
     *
     * @return the component field
     */
    public final FieldInfo field() {
        return clazz.field(internal.name());
    }

    /**
     * Returns the accessor method corresponding to this record component.
     *
     * @return the accessor method
     */
    public final MethodInfo accessor() {
        return clazz.method(internal.name());
    }

    /**
     * Returns the name of this record component.
     *
     * @return the name of this record component
     */
    public final String name() {
        return internal.name();
    }

    /**
     * Returns the type of this record component.
     *
     * @return the type of this record component
     */
    public Type type() {
        return internal.type();
    }

    /**
     * Returns whether an annotation instance with given name is declared on this record component or any type
     * within its signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @see #annotation(DotName)
     */
    public final boolean hasAnnotation(DotName name) {
        return internal.hasAnnotation(name);
    }

    /**
     * Returns the annotation instance with given name declared on this record component or any type within its signature.
     * The {@code target()} method of the returned annotation instance may be used to determine the exact location
     * of the annotation instance.
     * <p>
     * In case an annotation with given name occurs more than once, the result of this method is not deterministic.
     * For such situations, {@link #annotations(DotName)} is preferable.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @see #annotations(DotName)
     */
    public final AnnotationInstance annotation(DotName name) {
        return internal.annotation(name);
    }

    /**
     * Returns the annotation instances with given name declared on this record component or any type within its signature.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     * @see #annotations()
     */
    @Override
    public final List<AnnotationInstance> annotations(DotName name) {
        List<AnnotationInstance> instances = new ArrayList<>();
        for (AnnotationInstance instance : internal.annotationArray()) {
            if (instance.name().equals(name)) {
                instances.add(instance);
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances with given name declared on this record component or any type within its signature.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
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
     * @see #annotations(DotName)
     * @see #annotations()
     */
    public final List<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
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
     * Returns the annotation instances declared on this record component or any type within its signature.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     *
     * @return immutable list of annotation instances, never {@code null}
     */
    public final List<AnnotationInstance> annotations() {
        return internal.annotations();
    }

    /**
     * Returns whether an annotation instance with given name is declared on this record component.
     * <p>
     * Unlike {@link #hasAnnotation(DotName)}, this method ignores annotations declared on
     * types within the record component signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #hasAnnotation(DotName)
     */
    @Override
    public final boolean hasDeclaredAnnotation(DotName name) {
        return declaredAnnotation(name) != null;
    }

    /**
     * Returns the annotation instance with given name declared on this record component.
     * <p>
     * Unlike {@link #annotation(DotName)}, this method doesn't return annotations declared on types
     * within the record component signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotation(DotName)
     */
    @Override
    public final AnnotationInstance declaredAnnotation(DotName name) {
        for (AnnotationInstance instance : internal.annotationArray()) {
            if (instance.target().kind() == Kind.RECORD_COMPONENT && instance.name().equals(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Returns the annotation instances with given name declared on this record component.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     * <p>
     * Unlike {@link #annotationsWithRepeatable(DotName, IndexView)}, this method doesn't return annotations
     * declared on types within the record component signature.
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
    public final List<AnnotationInstance> declaredAnnotationsWithRepeatable(DotName name, IndexView index) {
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
     * Returns the annotation instances declared on this record component.
     * <p>
     * Unlike {@link #annotations()}, this method doesn't return annotations declared on types
     * within the record component signature.
     *
     * @return immutable list of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotations()
     */
    @Override
    public final List<AnnotationInstance> declaredAnnotations() {
        List<AnnotationInstance> instances = new ArrayList<>();
        for (AnnotationInstance instance : internal.annotationArray()) {
            if (instance.target().kind() == Kind.RECORD_COMPONENT) {
                instances.add(instance);
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns whether this record component must have a generic signature. That is, whether the Java compiler
     * when compiling this record component had to emit the {@code Signature} bytecode attribute.
     *
     * @return whether this record component must have a generic signature
     */
    @Override
    public boolean requiresGenericSignature() {
        return GenericSignatureReconstruction.requiresGenericSignature(this);
    }

    /**
     * Returns a generic signature of this record component, possibly without any generic-related information.
     * That is, produces a correct generic signature even if this record component does not use any type variables.
     * <p>
     * Signatures of type variables are substituted for signatures of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, no substitution happens and the type variable signature is used
     * unmodified.
     * <p>
     * Note that the return value does not come directly from bytecode. Jandex does not store the signature
     * strings. Instead, the return value is reconstructed from the Jandex object model.
     *
     * @param typeVariableSubstitution a substitution function from type variable identifiers to types
     * @return a generic signature of this record component with type variables substituted, never {@code null}
     */
    @Override
    public String genericSignature(Function<String, Type> typeVariableSubstitution) {
        return GenericSignatureReconstruction.reconstructGenericSignature(this, typeVariableSubstitution);
    }

    /**
     * Returns a bytecode descriptor of this record component.
     * <p>
     * Descriptors of type variables are substituted for descriptors of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, or if it returns the type variable itself, no substitution happens
     * and the type variable descriptor is used unmodified.
     * <p>
     * Note that the return value does not come directly from bytecode. Jandex does not store the descriptor
     * strings. Instead, the return value is reconstructed from the Jandex object model.
     *
     * @return the bytecode descriptor of this record component
     */
    @Override
    public String descriptor(Function<String, Type> typeVariableSubstitution) {
        return DescriptorReconstruction.recordComponentDescriptor(this, typeVariableSubstitution);
    }

    /**
     * Returns a string representation describing this record component. It is similar although not
     * necessarily identical to a Java source code declaration of this record component.
     *
     * @return a string representation of this record component
     */
    public String toString() {
        return internal.toString(clazz);
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
        throw new IllegalArgumentException("Not a method parameter");
    }

    @Override
    public final TypeTarget asType() {
        throw new IllegalArgumentException("Not a type");
    }

    @Override
    public final RecordComponentInfo asRecordComponent() {
        return this;
    }

    @Override
    public Kind kind() {
        return Kind.RECORD_COMPONENT;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + clazz.hashCode();
        result = 31 * result + internal.hashCode();
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
        RecordComponentInfo other = (RecordComponentInfo) o;
        return clazz.equals(other.clazz) && internal.equals(other.internal);
    }

    void setType(Type type) {
        internal.setType(type);
    }

    void setAnnotations(List<AnnotationInstance> annotations) {
        internal.setAnnotations(annotations);
    }

    RecordComponentInternal recordComponentInternal() {
        return internal;
    }

    void setRecordComponentInternal(RecordComponentInternal internal) {
        this.internal = internal;
    }

    void setClassInfo(ClassInfo clazz) {
        this.clazz = clazz;
    }
}
