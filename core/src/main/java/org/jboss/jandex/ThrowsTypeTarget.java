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
 * Represents a target of type annotation which occurs in the {@code throws} clause of an enclosing method.
 * This class conveys the enclosing method and the zero-based position of the throwable type.
 * The corresponding type reference is also included.
 *
 * <p>
 * Consider the following example involving a type target using the {@code Bar} annotation:
 *
 * <pre class="brush:java">
 * public void foo(List&lt;T&gt; l) throws @Bar FooException { ... }
 * </pre>
 *
 * <p>
 * This example would be represented as a {@code ThrowsTypeTarget} with an enclosing target
 * of the {@code MethodInfo} of {@code foo}, and {@code position()} would be 0.
 *
 * @author Jason T. Greene
 * @since 2.0
 */
public class ThrowsTypeTarget extends PositionBasedTypeTarget {
    ThrowsTypeTarget(MethodInfo enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    ThrowsTypeTarget(AnnotationTarget enclosingTarget, Type target, int position) {
        super(enclosingTarget, target, position);
    }

    @Override
    public Usage usage() {
        return Usage.THROWS;
    }

    @Override
    public MethodInfo enclosingTarget() {
        return (MethodInfo) super.enclosingTarget();
    }

    @Override
    public ThrowsTypeTarget asThrows() {
        return this;
    }
}
