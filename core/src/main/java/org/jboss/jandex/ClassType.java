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

/**
 * Represents a class type. Class types also include erasures of parameterized types.
 * <p>
 * Note that an inner class type enclosed in a parameterized type or in a type
 * annotated with a type annotation is represented as {@link ParameterizedType},
 * where the enclosing type is represented as the parameterized type's owner.
 *
 * @author Jason T. Greene
 */
public final class ClassType extends Type {

    public static final ClassType OBJECT_TYPE = new ClassType(DotName.OBJECT_NAME);
    public static final ClassType STRING_TYPE = new ClassType(DotName.STRING_NAME);
    public static final ClassType CLASS_TYPE = new ClassType(DotName.CLASS_NAME);

    public static final ClassType BYTE_CLASS = new ClassType(DotName.BYTE_CLASS_NAME);
    public static final ClassType CHARACTER_CLASS = new ClassType(DotName.CHARACTER_CLASS_NAME);
    public static final ClassType DOUBLE_CLASS = new ClassType(DotName.DOUBLE_CLASS_NAME);
    public static final ClassType FLOAT_CLASS = new ClassType(DotName.FLOAT_CLASS_NAME);
    public static final ClassType INTEGER_CLASS = new ClassType(DotName.INTEGER_CLASS_NAME);
    public static final ClassType LONG_CLASS = new ClassType(DotName.LONG_CLASS_NAME);
    public static final ClassType SHORT_CLASS = new ClassType(DotName.SHORT_CLASS_NAME);
    public static final ClassType BOOLEAN_CLASS = new ClassType(DotName.BOOLEAN_CLASS_NAME);
    public static final ClassType VOID_CLASS = new ClassType(DotName.VOID_CLASS_NAME);

    /**
     * Create an instance of a class type with given {@code name}.
     * <p>
     * Note that an inner class type enclosed in a parameterized type or in a type
     * annotated with a type annotation is represented as {@link ParameterizedType},
     * where the enclosing type is represented as the parameterized type's owner.
     *
     * @param name the binary name of this class type
     * @return the class type
     * @since 3.0.4
     */
    public static ClassType create(DotName name) {
        return new ClassType(name);
    }

    /**
     * Create an instance of a class type with given {@code name}.
     * <p>
     * Note that an inner class type enclosed in a parameterized type or in a type
     * annotated with a type annotation is represented as {@link ParameterizedType},
     * where the enclosing type is represented as the parameterized type's owner.
     *
     * @param name the binary name of this class type
     * @return the class type
     * @since 3.1.0
     */
    public static ClassType create(String name) {
        return create(DotName.createSimple(name));
    }

    /**
     * Create an instance of a class type for given {@code clazz}.
     * <p>
     * Note that an inner class type enclosed in a parameterized type or in a type
     * annotated with a type annotation is represented as {@link ParameterizedType},
     * where the enclosing type is represented as the parameterized type's owner.
     *
     * @param clazz the class
     * @return the class type
     * @since 3.1.0
     */
    public static ClassType create(Class<?> clazz) {
        return create(DotName.createSimple(clazz.getName()));
    }

    /**
     * Create a builder of a class type with the given {@code name}.
     *
     * @param name binary name of the class
     * @return the builder
     * @since 3.1.0
     */
    public static Builder builder(DotName name) {
        return new Builder(name);
    }

    /**
     * Create a builder of a class type for the given class.
     *
     * @param clazz the class
     * @return the builder
     * @since 3.1.0
     */
    public static Builder builder(Class<?> clazz) {
        return builder(DotName.createSimple(clazz.getName()));
    }

    ClassType(DotName name) {
        this(name, null);
    }

    ClassType(DotName name, AnnotationInstance[] annotations) {
        super(name, annotations);
    }

    @Override
    public Kind kind() {
        return Kind.CLASS;
    }

    @Override
    public ClassType asClassType() {
        return this;
    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new ClassType(name(), newAnnotations);
    }

    ParameterizedType toParameterizedType() {
        return new ParameterizedType(name(), null, null, annotationArray());
    }

    /**
     * Convenient builder for {@link ClassType}.
     *
     * @since 3.1.0
     */
    public static final class Builder extends Type.Builder<Builder> {

        Builder(DotName name) {
            super(name);
        }

        /**
         * Returns the built class type.
         *
         * @return the built class type
         */
        public ClassType build() {
            return new ClassType(name, annotationsArray());
        }

    }
}
