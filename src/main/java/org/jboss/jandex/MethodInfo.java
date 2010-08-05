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

import java.lang.reflect.Modifier;

/**
 * Represents a Java method that was annotated. 
 * 
 *  <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 * 
 * @author Jason T. Greene
 */
public final class MethodInfo implements AnnotationTarget {
    private final String name;
    private final Type[] args;
    private final Type returnType;
    private final short flags;
    private final ClassInfo clazz;

    MethodInfo(ClassInfo clazz, String name, Type[] args, Type returnType,  short flags) {
        this.clazz = clazz;
        this.name = name;
        this.args = args;
        this.returnType = returnType;
        this.flags = flags;
    }

    /**
     * Returns the name of this method
     * 
     * @return the name of the method
     */
    public final String name() {
        return name;
    }
    
    /**
     * Returns the class that declared this method
     * 
     * @return the declaring class
     */
    public final ClassInfo declaringClass() {
        return clazz;
    }

    /**
     * Returns an array containing parameter types in parameter order. 
     * 
     * @return all parameter types
     */
    public final Type[] args() {
        return args;
    }
    
    /**
     * Returns this method's return parameter type.
     * If this method has a void return, a special void type is returned.
     * 
     * @return the type of this method's return value
     */
    public final Type returnType() {
        return returnType;
    }


    /**
     * Returns the access fields of this method. {@link Modifier} can be used on this value.
     * 
     * @return the access flags of this method
     */
    public final short flags() {
        return flags;
    }  
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(returnType).append(' ').append(clazz.name()).append('.').append(name).append('(');
        for (int i = 0; i < args.length; i++) {
            builder.append(args[i]);
            if (i + 1 < args.length)
                builder.append(", ");
        }
        builder.append(')');
        
        return builder.toString();
    }
}
