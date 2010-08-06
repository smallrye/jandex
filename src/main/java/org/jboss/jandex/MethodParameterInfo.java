/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jandex;

/**
 * Represents an individual Java method parameter that was annotated.
 *
 * <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 */
public final class MethodParameterInfo implements AnnotationTarget {
    private final MethodInfo method;
    private final short parameter;

    MethodParameterInfo(MethodInfo method,short parameter)
    {
        this.method = method;
        this.parameter = parameter;
    }

    /**
     * Returns the method this parameter belongs to.
     *
     * @return the declaring Java method
     */
    public final MethodInfo method() {
        return method;
    }

    /**
     * Returns the 0 based position of this parameter.
     *
     * @return the position of this parameter
     */
    public final short position() {
        return parameter;
    }

    public String toString() {
        return method + " #" + parameter;
    }
}
