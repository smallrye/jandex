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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a Java method that was annotated.
 *
 *  <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 */
public final class MethodInfo implements AnnotationTarget {
    static final int SYNTHETIC = 0x1000;
    static final int BRIDGE    = 0x0040;

    private final String name;
    private Type[] parameters;
    private Type returnType;
    private Type[] exceptions;
    private Type receiverType;
    private Type[] typeParameters;
    private AnnotationInstance[] annotations;

    private final short flags;
    private final ClassInfo clazz;

    static final NameAndParameterComparator NAME_AND_PARAMETER_COMPARATOR = new NameAndParameterComparator();

    static class NameAndParameterComparator implements Comparator<MethodInfo> {
        public int compare(MethodInfo instance, MethodInfo instance2) {
            int x = instance.name().compareTo(instance2.name());
            if (x != 0) {
                return x;
            }

            x = instance.parameters.length - instance2.parameters.length;
            if (x != 0) {
                return x;
            }

            for (int i = 0; i < instance.parameters.length; i++) {
                Type t1 = instance.parameters[i];
                Type t2 = instance2.parameters[i];

                x = t1.name().compareTo(t2.name());
                if (x != 0) {
                    return x;
                }
            }

            // Prefer non-synthetic methods when matching
            return (instance.flags & (SYNTHETIC|BRIDGE)) - (instance2.flags & (SYNTHETIC|BRIDGE));
        }
    }


    MethodInfo(ClassInfo clazz, String name, List<Type> parameters, Type returnType,  short flags) {
        this.clazz = clazz;
        this.name = name;
        this.parameters = parameters.size() == 0 ? Type.EMPTY_ARRAY : parameters.toArray(new Type[parameters.size()]);
        this.returnType = returnType;
        this.flags = flags;
        this.annotations = AnnotationInstance.EMPTY_ARRAY;
        this.exceptions = Type.EMPTY_ARRAY;
        this.typeParameters = Type.EMPTY_ARRAY;
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
     public static MethodInfo create(ClassInfo clazz, String name, Type[] args, Type returnType, short flags) {
         if (clazz == null)
             throw new IllegalArgumentException("Clazz can't be null");

         if (name == null)
             throw new IllegalArgumentException("Name can't be null");

         if (args == null)
            throw new IllegalArgumentException("Values can't be null");

         if (returnType == null)
            throw new IllegalArgumentException("returnType can't be null");

         return new MethodInfo(clazz, name, Arrays.asList(args), returnType, flags);
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
    @Deprecated
    public final Type[] args() {
        return parameters.clone();
    }

    /**
     * Returns a list containing parameter types in parameter order.
     *
     * @return all parameter types
     */
    public final List<Type> parameters() {
        return Collections.unmodifiableList(Arrays.asList(parameters));
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

    public final Type receiverType() {
        return receiverType != null ? receiverType : new ClassType(clazz.name());
    }

    public final List<Type> exceptions() {
        return Collections.unmodifiableList(Arrays.asList(exceptions));
    }

    public final List<Type> typeParameters() {
        return Collections.unmodifiableList(Arrays.asList(typeParameters));
    }

    public final List<AnnotationInstance> annotations() {
        return Collections.unmodifiableList(Arrays.asList(annotations));
    }

    public final AnnotationInstance annotation(DotName name) {
        AnnotationInstance key = new AnnotationInstance(name, null, null);
        int i = Arrays.binarySearch(annotations, key, AnnotationInstance.NAME_COMPARATOR);
        return i >= 0 ? annotations[i] : null;
    }

    public final boolean hasAnnotation(DotName name) {
        return annotation(name) != null;
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
        for (int i = 0; i < parameters.length; i++) {
            builder.append(parameters[i]);
            if (i + 1 < parameters.length)
                builder.append(", ");
        }
        builder.append(')');

        if (exceptions.length > 0) {
            builder.append(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                builder.append(exceptions[i]);
                if (i < exceptions.length - 1) {
                    builder.append(", ");
                }
            }
        }

        return builder.toString();
    }

    void setTypeParameters(List<Type> typeParameters) {
        if (typeParameters.size() > 0) {
            this.typeParameters = typeParameters.toArray(new Type[typeParameters.size()]);
        }
    }

    void setParameters(List<Type> parameters, NameTable names) {
        this.parameters = parameters.size() == 0 ? Type.EMPTY_ARRAY : names.intern(parameters.toArray(new Type[parameters.size()]));
    }

    void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    void setExceptions(List<Type> exceptions, NameTable names) {
        this.exceptions = exceptions.size() == 0 ? Type.EMPTY_ARRAY : names.intern(exceptions.toArray(new Type[exceptions.size()]));
    }

    void setReceiverType(Type receiverType) {
        this.receiverType = receiverType;
    }

    void setAnnotations(List<AnnotationInstance> annotations) {
        if (annotations.size() > 0) {
            this.annotations = annotations.toArray(new AnnotationInstance[annotations.size()]);
            Arrays.sort(this.annotations, AnnotationInstance.NAME_COMPARATOR);
        }
    }
}
