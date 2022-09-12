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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a field.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 *
 */
public final class FieldInfo implements AnnotationTarget {

    private ClassInfo clazz;
    private FieldInternal internal;

    FieldInfo() {
    }

    FieldInfo(ClassInfo clazz, FieldInternal internal) {
        this.clazz = clazz;
        this.internal = internal;
    }

    FieldInfo(ClassInfo clazz, byte[] name, Type type, short flags) {
        this(clazz, new FieldInternal(name, type, flags));
    }

    /**
     * Construct a new mock Field instance.
     *
     * @param clazz the class declaring the field
     * @param name the name of the field
     * @param type the Java field type
     * @param flags the field attributes
     * @return a mock field
     */
    public static FieldInfo create(ClassInfo clazz, String name, Type type, short flags) {
        if (clazz == null)
            throw new IllegalArgumentException("Clazz can't be null");

        if (name == null)
            throw new IllegalArgumentException("Name can't be null");

        return new FieldInfo(clazz, Utils.toUTF8(name), type, flags);
    }

    /**
     * Returns the local name of the field
     *
     * @return the local name of the field
     */
    public final String name() {
        return internal.name();
    }

    /**
     * Returns the class which declared the field
     *
     * @return the declaring class
     */
    public final ClassInfo declaringClass() {
        return clazz;
    }

    /**
     * Returns the <code>Type</code> declared on this field. This may be an array, a primitive, or a generic type definition.
     *
     * @return the type of this field
     */
    public final Type type() {
        return internal.type();
    }

    public final Kind kind() {
        return Kind.FIELD;
    }

    /**
     * Returns whether an annotation instance with given name is declared on this field or any type
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
     * Returns the annotation instance with given name declared on this field or any type within its signature.
     * The {@code target()} method of the returned annotation instance may be used to determine the exact location
     * of the annotation instance.
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     * 
     * <pre class="brush:java">
     * {@literal @}MyFieldAnnotation
     * public String foo;
     *
     * public List&lt;{@literal @}MyTypeAnnotation String&gt; bar;
     * </pre>
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
     * Returns the annotation instances with given name declared on this field or any type within its signature.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     * 
     * <pre class="brush:java">
     * {@literal @}MyFieldAnnotation
     * public String foo;
     *
     * public List&lt;{@literal @}MyTypeAnnotation String&gt; bar;
     * </pre>
     *
     * @param name name of the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
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
     * Returns the annotation instances with given name declared on this field or any type within its signature.
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
     * Returns the annotation instances declared on this field or any type within its signature.
     * The {@code target()} method of the returned annotation instances may be used to determine the exact location
     * of the respective annotation instance.
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     * 
     * <pre class="brush:java">
     * {@literal @}MyFieldAnnotation
     * public String foo;
     *
     * public List&lt;{@literal @}MyTypeAnnotation String&gt; bar;
     * </pre>
     *
     * @return collection of annotation instances, never {@code null}
     */
    public final List<AnnotationInstance> annotations() {
        return internal.annotations();
    }

    /**
     * Returns whether an annotation instance with given name is declared on this field.
     * <p>
     * Unlike {@link #hasAnnotation(DotName)}, this method ignores annotations declared on
     * types within the field signature.
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
     * Returns the annotation instance with given name declared on this field.
     * <p>
     * Unlike {@link #annotation(DotName)}, this method doesn't return annotations declared on types
     * within the field signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotation(DotName)
     */
    @Override
    public final AnnotationInstance declaredAnnotation(DotName name) {
        for (AnnotationInstance instance : internal.annotationArray()) {
            if (instance.target().kind() == Kind.FIELD && instance.name().equals(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Returns the annotation instances with given name declared on this field.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     * <p>
     * Unlike {@link #annotationsWithRepeatable(DotName, IndexView)}, this method doesn't return annotations
     * declared on types within the field signature.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return list of annotation instances, never {@code null}
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
     * Returns the annotation instances declared on this field.
     * <p>
     * Unlike {@link #annotations()}, this method doesn't return annotations declared on types
     * within the field signature.
     *
     * @return list of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotations()
     */
    @Override
    public final List<AnnotationInstance> declaredAnnotations() {
        List<AnnotationInstance> instances = new ArrayList<>();
        for (AnnotationInstance instance : internal.annotationArray()) {
            if (instance.target().kind() == Kind.FIELD) {
                instances.add(instance);
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns whether this field is declared as an element of an enum.
     *
     * @return true if the field is declared as an element of an enum, false otherwise
     *
     * @see java.lang.reflect.Field#isEnumConstant()
     */
    public final boolean isEnumConstant() {
        return internal.isEnumConstant();
    }

    /**
     * Returns the access fields of this field. {@link Modifier} can be used on this value.
     *
     * @return the access flags of this field
     */
    public final short flags() {
        return internal.flags();
    }

    /**
     * 
     * @return {@code true} if this field is a synthetic field
     */
    public final boolean isSynthetic() {
        return Modifiers.isSynthetic(internal.flags());
    }

    /**
     * Returns an ordinal of this enum constant, that is, the zero-based position in the enum declaration.
     * This is currently very inefficient (requires traversing fields of the declaring class),
     * but may be improved in the future.
     * <p>
     * If this field is not an enum constant, returns -1.
     * <p>
     * Note that for the result to actually be the ordinal value, the index must be produced
     * by at least Jandex 2.4. Previous Jandex versions do not store field positions. At most 256
     * fields may be present in the class; if there's more, outcome is undefined. This also assumes
     * that the bytecode order corresponds to declaration order, which is not guaranteed,
     * but practically always holds.
     *
     * @return ordinal of this enum constant, or -1 if this field is not an enum constant
     * @since 3.0.1
     */
    public int enumConstantOrdinal() {
        return clazz.enumConstantOrdinal(internal);
    }

    /**
     * Returns a string representation describing this field. It is similar although not necessarily equivalent
     * to a Java source code expression representing this field.
     *
     * @return a string representation of this field
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
        return this;
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
        throw new IllegalArgumentException("Not a record component");
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
        FieldInfo other = (FieldInfo) o;
        return clazz.equals(other.clazz) && internal.equals(other.internal);
    }

    void setType(Type type) {
        internal.setType(type);
    }

    void setAnnotations(List<AnnotationInstance> annotations) {
        internal.setAnnotations(annotations);
    }

    FieldInternal fieldInternal() {
        return internal;
    }

    void setFieldInternal(FieldInternal internal) {
        this.internal = internal;
    }

    void setClassInfo(ClassInfo clazz) {
        this.clazz = clazz;
    }
}
