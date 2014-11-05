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
 * Specifies "void" in a method signature.
 *
 * @since 2.0
 * @author Jason T. Greene
 */
public class VoidType extends Type {
    static final VoidType VOID = new VoidType(null);

    private VoidType(AnnotationInstance[] annotations) {
        super(new DotName(null, "void", true, false), annotations);
    }

    @Override
    public Kind kind() {
        return Kind.VOID;
    }

    @Override
    public VoidType asVoidType() {
        return this;
    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new VoidType(newAnnotations);
    }
}
