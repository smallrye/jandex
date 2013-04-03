/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
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
      * Construct a new mock Method instance.
      *
      * @param clazz the class declaring the field
      * @param name the name of the field
      * @param args a read only array containing the types of each parameter in parameter order
     *  @param returnType the return value type
      * @param flags the method attributes
      * @return a mock field
      */
     public static final MethodInfo create(ClassInfo clazz, String name, Type[] args, Type returnType, short flags) {
         if (clazz == null)
             throw new IllegalArgumentException("Clazz can't be null");

         if (name == null)
             throw new IllegalArgumentException("Name can't be null");

         if (args == null)
            throw new IllegalArgumentException("Values can't be null");

         if (returnType == null)
            throw new IllegalArgumentException("returnType can't be null");

         return new MethodInfo(clazz, name, args, returnType, flags);
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
