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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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

    private MethodInternal methodInternal;
    private ClassInfo clazz;


    MethodInfo() {
    }

    MethodInfo(ClassInfo clazz, MethodInternal methodInternal) {
        this.methodInternal = methodInternal;
        this.clazz = clazz;
    }

    MethodInfo(ClassInfo clazz, byte[] name, Type[] parameters, Type returnType,  short flags) {
        this(clazz, new MethodInternal(name, parameters, returnType, flags));
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

         byte[] bytes;
         try {
             bytes = name.getBytes("UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new IllegalArgumentException(e);
         }
         return new MethodInfo(clazz, bytes, args, returnType, flags);
     }


    /**
     * Returns the name of this method
     *
     * @return the name of the method
     */
    public final String name() {
        return methodInternal.name();
    }

    public final Kind kind() {
        return Kind.METHOD;
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
        return methodInternal.copyParameters();
    }

    final Type[] copyParameters() {
        return methodInternal.copyParameters();
    }

    /**
     * Returns a list containing parameter types in parameter order.
     *
     * @return all parameter types
     */
    public final List<Type> parameters() {
        return methodInternal.parameters();
    }

    /**
     * Returns this method's return parameter type.
     * If this method has a void return, a special void type is returned.
     *
     * @return the type of this method's return value
     */
    public final Type returnType() {
        return methodInternal.returnType();
    }

    public final Type receiverType() {
        return methodInternal.receiverType(clazz);
    }

    public final List<Type> exceptions() {
        return methodInternal.exceptions();
    }

    final Type[] copyExceptions() {
        return methodInternal.copyExceptions();
    }

    public final List<Type> typeParameters() {
        return methodInternal.typeParameters();
    }

    public final List<AnnotationInstance> annotations() {
        return methodInternal.annotations();
    }

    public final AnnotationInstance annotation(DotName name) {
        return  methodInternal.annotation(name);
    }

    public final boolean hasAnnotation(DotName name) {
        return methodInternal.hasAnnotation(name);
    }

    /**
     * Returns the access fields of this method. {@link Modifier} can be used on this value.
     *
     * @return the access flags of this method
     */
    public final short flags() {
        return methodInternal.flags();
    }

    public String toString() {
        return methodInternal.toString();
    }

    final MethodInternal methodInternal() {
        return methodInternal;
    }

    final void setMethodInternal(MethodInternal methodInternal) {
        this.methodInternal = methodInternal;
    }

    final void setClassInfo(ClassInfo clazz) {
        this.clazz = clazz;
    }

    final Type[] typeParameterArray() {
        return methodInternal.typeParameterArray();
    }

    void setTypeParameters(Type[] typeParameters) {
        methodInternal.setTypeParameters(typeParameters);
    }

    void setParameters(Type[] parameters) {
        methodInternal.setParameters(parameters);
    }

    void setReturnType(Type returnType) {
        methodInternal.setReturnType(returnType);
    }

    void setExceptions(Type[] exceptions) {
        methodInternal.setExceptions(exceptions);
    }

    void setReceiverType(Type receiverType) {
        methodInternal.setReceiverType(receiverType);
    }

    void setAnnotations(List<AnnotationInstance> annotations) {
        methodInternal.setAnnotations(annotations);
    }
}
