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
        TYPE}

    /**
     * Returns the kind of object this target represents.
     *
     * @return the target kind.
     * @since 2.0
     */
    Kind kind();

    /**
     * Casts and returns this target as a <code>ClassInfo</code> if it is of kind <code>CLASS</code>
     *
     * @return this instance cast to a class
     * @since 2.0
     */
    ClassInfo asClass();

    /**
     * Casts and returns this target as a <code>FieldInfo</code> if it is of kind <code>FIELD</code>
     *
     * @return this instance cast to a field
     * @since 2.0
     */
    FieldInfo asField();

    /**
     * Casts and returns this target as a <code>MethodInfo</code> if it is of kind <code>METHOD</code>
     *
     * @return this instance cast to a method
     * @since 2.0
     */
    MethodInfo asMethod();

    /**
     * Casts and returns this target as a <code>MethodParameterInfo</code> if it is of kind <code>METHOD_PARAMETER</code>
     *
     * @return this instance cast to a method parameter
     * @since 2.0
     */
    MethodParameterInfo asMethodParameter();

    /**
      * Casts and returns this target as a <code>TypeTarget</code> if it is of kind <code>TYPE</code>
      *
      * @return this instance cast to a type target
      * @since 2.0
      */
    TypeTarget asType();

}
