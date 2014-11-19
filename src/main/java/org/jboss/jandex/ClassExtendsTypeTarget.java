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
 * Represents a type annotation target which occurs in the extends or implements clause of an enclosing class.
 * This class conveys the enclosing class definition, as well as a position to indicate the interface or superclass
 * this target applies to. Since type targets can appear at any depth of the type tree at this location, the
 * corresponding type reference is also included.
 *
 * <p>The special position 65535 is used to indicate the type usage is on the super type in the extends clause.
 * All other numbers denote the zero-based offset in the interface list of the implements clause.
 *
 * <p>
 * Consider the following example involving a type target using the "Bar" annotation:
 *
 * <pre class="brush:java; gutter: false;">
 * class Foo&lt;T&gt; implements List&lt;@Bar T&gt; {}
 * </pre>
 *
 * This example would return a position of 1 (marking the first interface), an enclosing target of the
 * <code>ClassInfo</code> representing "Foo", and a target type of the type variable "T".
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public class ClassExtendsTypeTarget extends PositionBasedTypeTarget {
    ClassExtendsTypeTarget(ClassInfo enclosingTarget, int position) {
        super(enclosingTarget, position);
    }

    public ClassExtendsTypeTarget(AnnotationTarget enclosingTarget, Type target, int position) {
        super(enclosingTarget, target, position);
    }

    @Override
    public final Usage usage() {
        return Usage.CLASS_EXTENDS;
    }

    @Override
    public ClassInfo enclosingTarget() {
        return (ClassInfo) super.enclosingTarget();
    }

    @Override
    public ClassExtendsTypeTarget asClassExtends() {
        return this;
    }
}
