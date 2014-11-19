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
 * Represents a type annotation target which occurs within a method parameter type. This class conveys
 * the enclosing method and the zero-based position of the parameter. Since type targets can appear at
 * any depth of the type tree at this location, the corresponding type reference is also included.
 *
 * <p>
 * Consider the following example involving a type target using the "Bar" annotation:
 *
 * <pre class="brush:java; gutter: false;">
 * public void foo(List&lt;@Bar T&gt; { ... }
 * </pre>
 *
 * <p>This example would be represented as a <code>MethodParameterTypeTarget</code> with an enclosing target
 * of foo's <code>MethodInfo</code>, and <code>position()</code> would return 0. The "Bar" annotation
 * would appear on the type variable T.
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public class MethodParameterTypeTarget extends PositionBasedTypeTarget {

    MethodParameterTypeTarget(MethodInfo enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    MethodParameterTypeTarget(AnnotationTarget enclosingTarget, Type target, int position) {
        super(enclosingTarget, target, position);
    }

    @Override
    public final Usage usage() {
        return Usage.METHOD_PARAMETER;
    }

    @Override
    public MethodInfo enclosingTarget() {
        return (MethodInfo) super.enclosingTarget();
    }

    @Override
    public MethodParameterTypeTarget asMethodParameterType() {
        return this;
    }
}
