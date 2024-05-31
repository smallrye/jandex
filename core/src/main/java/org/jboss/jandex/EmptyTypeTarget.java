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
 * Represents a target of type annotation which occurs directly on a field type, a method return type,
 * or a method receiver type. This class conveys the enclosing field or method, and if a method, also
 * differentiates between the receiver and return value. Since type targets can appear at any depth
 * of the type tree at this location, the corresponding type reference is also included.
 *
 * <p>
 * Consider the following example involving a type target using the {@code Bar} annotation:
 *
 * <pre class="brush:java">
 * public List&lt;@Bar T&gt; foo() { ... }
 * </pre>
 *
 * This example would be represented as an {@code EmptyTypeTarget} with an enclosing target
 * of the {@code MethodInfo} of {@code foo}, and {@code isReceiver()} would be {@code false}.
 *
 * @author Jason T. Greene
 */
public class EmptyTypeTarget extends TypeTarget {
    private boolean receiver;

    EmptyTypeTarget(AnnotationTarget enclosingTarget, boolean receiver) {
        super(enclosingTarget);
        this.receiver = receiver;
    }

    EmptyTypeTarget(AnnotationTarget enclosingTarget, Type target, boolean receiver) {
        super(enclosingTarget, target);
        this.receiver = receiver;
    }

    /**
     * Returns whether the annotated type occurs within a method receiver (the {@code this} reference
     * the method receives). It will return {@code false} if the type occurs in a method return type
     * or in a field type.
     *
     * @return {@code true} if the annotated type occurs within a method receiver type, otherwise {@code false}
     */
    public boolean isReceiver() {
        return receiver;
    }

    @Override
    public final Usage usage() {
        return Usage.EMPTY;
    }

    @Override
    public EmptyTypeTarget asEmpty() {
        return this;
    }

    @Override
    public int compareTo(AnnotationTarget o) {

        if (this == o) {
            return 0;
        }

        int v = super.compareTo(o);
        if (v != 0) {
            return v;
        }

        EmptyTypeTarget other = (EmptyTypeTarget) o;
        v = Boolean.compare(receiver, other.receiver);
        if (v != 0) {
            return v;
        }

        assert this.equals(o) : "EmptyTypeTarget.compareTo method not consistent with equals";
        return 0;
    }
}
