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
 * Represents a type annotation target which occurs directly on a field type, a method return type, or a method receiver
 * type. This class conveys the enclosing field or method, and if a method, also differentiates between the receiver and
 * return value. Since type targets can appear at any depth of the type tree at this location, the
 * corresponding type reference is also included.
 *
 * <p>
 * Consider the following example involving a type target using the "Bar" annotation:
 *
 * <pre class="brush:java; gutter: false;">
 * public List&lt;@Bar T&gt; foo { return foo; }
 * </pre>
 *
 * This example would be represented as an <code>EmptyTypeTarget</code> with an enclosing target of Foo's
 * <code>MethodInfo</code>, and <code>isReceiver</code> would return false.
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
     * Returns whether the type occurs within a method receiver (the "this" reference the method receives).
     * It will return false if the type occurs within a method return, or a field.
     *
     * @return true if occurs within a method receiver, otherwise false
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
}
