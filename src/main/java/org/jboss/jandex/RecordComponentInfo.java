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

/**
 * Represents an individual Java record component that was annotated.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 */
public final class RecordComponentInfo implements AnnotationTarget {
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
     * Returns the list of annotation instances declared on this record component. It may be empty, but never null.
     *
     * @return the list of annotations on this record component
     */
    public List<AnnotationInstance> annotations() {
        return internal.annotations();
    }

    /**
     * Retrieves an annotation instance declared on this field. If an annotation by that name is not present, null will be returned.
     *
     * @param name the name of the annotation to locate on this field
     * @return the annotation if found, otherwise, null
     */
    public final AnnotationInstance annotation(DotName name) {
        return internal.annotation(name);
    }

    /**
     * Retrieves annotation instances declared on this field, by the name of the annotation.
     *
     * If the specified annotation is repeatable (JLS 9.6), the result also contains all values from the container annotation instance.
     *
     * @param name the name of the annotation
     * @param index the index used to obtain the annotation class
     * @return the annotation instances declared on this field, or an empty list if none
     * @throws IllegalArgumentException If the index does not contain the annotation definition or if it does not represent an annotation type
     */
    public final List<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
        AnnotationInstance ret = annotation(name);
        if (ret != null) {
            // Annotation present - no need to try to find repeatable annotations
            return Collections.singletonList(ret);
        }
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.classAnnotation(Index.REPEATABLE);
        if (repeatable == null) {
            return Collections.emptyList();
        }
        Type containingType = repeatable.value().asClass();
        AnnotationInstance containing = annotation(containingType.name());
        if (containing == null) {
            return Collections.emptyList();
        }
        AnnotationInstance[] values = containing.value().asNestedArray();
        List<AnnotationInstance> instances = new ArrayList<AnnotationInstance>(values.length);
        for (AnnotationInstance nestedInstance : values) {
            instances.add(nestedInstance);
        }
        return instances;
    }

    /**
     * Returns whether or not the annotation instance with the given name occurs on this field
     *
     * @see #annotations()
     * @see #annotation(DotName)
     * @param name the name of the annotation to look for
     * @return true if the annotation is present, false otherwise
     */
    public final boolean hasAnnotation(DotName name) {
        return internal.hasAnnotation(name);
    }

    /**
     * Returns a string representation describing this record component.
     *
     * @return a string representation of this record component
     */
    public String toString() {
        return name();
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
    public RecordComponentInfo asRecordComponent() {
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
