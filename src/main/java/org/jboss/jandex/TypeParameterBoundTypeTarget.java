/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
 * Represents a type annotation target which occurs within a bound of type parameter type.
 * This class conveys the zero-based position of the parameter, the zeo-based position of the
 * bound, and finally, the enclosing method or class where it occurs. Since type targets can
 * appear at any depth of the type tree at this location, the corresponding
 * type reference is also included.
 *
 * <p>
 * Consider the following example involving a type target using the "Bar" annotation:
 *
 * <pre class="brush:java; gutter: false;">
 * public &lt;T extends Number &amp; @Bar Serializable&gt; void foo(List&lt;T&gt;) { ... }
 * </pre>
 *
 * <p>This example would be represented as a <code>TypeParameterBoundTypeTarget</code> with
 * an enclosing target of foo's <code>MethodInfo</code>, a <code>position()</code> value of "0"
 * and a <code>boundPosition()</code> value of "1". The "Bar" annotation would appear on the
 * type "Serializable" in the bound list of T, on the first type parameter.
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public class TypeParameterBoundTypeTarget extends TypeParameterTypeTarget {
    // bound pos is defined as a u1, reuse the extra bytes for the boolean
    private short boundPosition;
    private boolean adjusted;

    TypeParameterBoundTypeTarget(AnnotationTarget enclosingTarget, int position, int boundPosition) {
        super(enclosingTarget, position);
        this.boundPosition = (short)boundPosition;
    }

    TypeParameterBoundTypeTarget(AnnotationTarget enclosingTarget, Type target, int position, int boundPosition) {
        super(enclosingTarget, target, position);
        this.boundPosition = (short)boundPosition;
    }

    /**
     * Returns the index of the bound this type annotation is within.
     *
     * @return the index of the bound this type annotation is within
     */
    public final int boundPosition() {
        return boundPosition;
    }

    void adjustDown() {
       if (!adjusted) {
           boundPosition--;
           adjusted = true;
       }
    }

    @Override
    public final Usage usage() {
        return Usage.TYPE_PARAMETER_BOUND;
    }

    @Override
    public TypeParameterBoundTypeTarget asTypeParameterBound() {
        return this;
    }
}
