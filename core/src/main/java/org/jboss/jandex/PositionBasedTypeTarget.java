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
 * A common parent for type targets which provide a position. Refer to the
 * subclass for information on the meaning of the position value.
 *
 * @see org.jboss.jandex.ClassExtendsTypeTarget
 * @see org.jboss.jandex.MethodParameterTypeTarget
 * @see org.jboss.jandex.TypeParameterTypeTarget
 * @see org.jboss.jandex.TypeParameterBoundTypeTarget
 * @see org.jboss.jandex.ThrowsTypeTarget
 * @since 2.0
 * @author Jason T. Greene
 */
public abstract class PositionBasedTypeTarget extends TypeTarget {
    private final short position; // throws is a u2, everything else is u1

    PositionBasedTypeTarget(AnnotationTarget enclosingTarget, int position) {
        super(enclosingTarget);
        this.position = (short) position;
    }

    PositionBasedTypeTarget(AnnotationTarget enclosingTarget, Type target, int position) {
        super(enclosingTarget, target);
        this.position = (short) position;
    }

    /**
     * Returns a subclass specific position where the type is located.
     *
     * @return the position
     */
    public final int position() {
        return position & 0xFFFF;
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

        PositionBasedTypeTarget other = (PositionBasedTypeTarget) o;
        v = Short.compare(position, other.position);
        if (v != 0) {
            return v;
        }

        assert this.equals(o) : "PositionBasedTypeTarget.compareTo method not consistent with equals";
        return 0;
    }
}
