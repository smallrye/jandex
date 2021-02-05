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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Java method, constructor, or static initializer.
 *
 * <p><b>Thread-Safety</b></p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 */
public final class MethodInfo implements ContainingAnnotationTarget {

    static final String[] EMPTY_PARAMETER_NAMES = new String[0];
    private MethodInternal methodInternal;
    private ClassInfo clazz;


    MethodInfo() {
    }

    MethodInfo(ClassInfo clazz, MethodInternal methodInternal) {
        this.methodInternal = methodInternal;
        this.clazz = clazz;
    }

    MethodInfo(ClassInfo clazz, byte[] name, byte[][] parameterNames, Type[] parameters, Type returnType,  short flags) {
        this(clazz, new MethodInternal(name, parameterNames, parameters, returnType, flags));
    }

    MethodInfo(ClassInfo clazz, byte[] name, byte[][] parameterNames, Type[] parameters, Type returnType,  short flags, Type[] typeParameters, Type[] exceptions) {
        this(clazz, new MethodInternal(name, parameterNames, parameters, returnType, flags, typeParameters, exceptions));
    }

    /**
     * Construct a new mock Method instance.
     *
     * @param clazz the class declaring the field
     * @param name the name of the field
     * @param args a read only array containing the types of each parameter in parameter order
     * @param returnType the return value type
     * @param flags the method attributes
     * @return a mock method
     */
     public static MethodInfo create(ClassInfo clazz, String name, Type[] args, Type returnType, short flags) {
         return create(clazz, name, args, returnType, flags, null, null);
     }

    /**
     * Construct a new mock Method instance.
     *
     * @param clazz the class declaring the field
     * @param name the name of the field
     * @param args a read only array containing the types of each parameter in parameter order
     * @param returnType the return value type
     * @param flags the method attributes
     * @param typeParameters the generic type parameters for this method
     * @param exceptions the exceptions declared as thrown by this method
     * @return a mock method
     *
     * @since 2.1
     */
     public static MethodInfo create(ClassInfo clazz, String name, Type[] args, Type returnType, short flags, TypeVariable[] typeParameters, Type[] exceptions) {
         return create(clazz, name, EMPTY_PARAMETER_NAMES, args, returnType, flags, typeParameters, exceptions);
     }
     
     /**
      * Construct a new mock Method instance.
      *
      * @param clazz the class declaring the field
      * @param name the name of the field
      * @param parameterNames the names of the method parameter 
      * @param args a read only array containing the types of each parameter in parameter order
      * @param returnType the return value type
      * @param flags the method attributes
      * @param typeParameters the generic type parameters for this method
      * @param exceptions the exceptions declared as thrown by this method
      * @return a mock method
      *
      * @since 2.2
      */
     public static MethodInfo create(ClassInfo clazz, String name, String[] parameterNames, Type[] args, Type returnType, short flags, TypeVariable[] typeParameters, Type[] exceptions) {
         if (clazz == null)
             throw new IllegalArgumentException("Clazz can't be null");

         if (name == null)
             throw new IllegalArgumentException("Name can't be null");

         if (args == null)
             throw new IllegalArgumentException("Values can't be null");

         if (parameterNames == null)
             throw new IllegalArgumentException("Parameter names can't be null");

         if (returnType == null)
            throw new IllegalArgumentException("returnType can't be null");

         byte[] bytes;
         byte[][] parameterNameBytes;
         try {
             bytes = name.getBytes("UTF-8");
             parameterNameBytes = new byte[parameterNames.length][];
             for (int i = 0; i < parameterNames.length; i++) {
                parameterNameBytes[i] = parameterNames[i].getBytes("UTF-8");
            }
         } catch (UnsupportedEncodingException e) {
             throw new IllegalArgumentException(e);
         }
         return new MethodInfo(clazz, bytes, parameterNameBytes, args, returnType, flags, typeParameters, exceptions);
     }


    /**
     * Returns the name of this method
     *
     * @return the name of the method
     */
    public final String name() {
        return methodInternal.name();
    }

    /**
     * Returns the name of the given parameter.
     * @param i the parameter index
     * @return the name of the given parameter, or null.
     */
    public final String parameterName(int i) {
        return methodInternal.parameterName(i);
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
     * Returns an array containing parameter types in parameter order. This method performs a defensive array
     * copy per call, and should be avoided. Instead the {@link #parameters()} method should be used.
     *
     * @return an array copy contain parameter types
     */
    @Deprecated
    public final Type[] args() {
        return methodInternal.copyParameters();
    }

    final Type[] copyParameters() {
        return methodInternal.copyParameters();
    }

    /**
     * Returns a list containing the types of all parameters declared on this method, in parameter order.
     * This method may return an empty list, but never null.
     *
     * @return all parameter types on this method
     */
    public final List<Type> parameters() {
        return methodInternal.parameters();
    }

    /**
     * Returns this method's return parameter type.
     * If this method has a void return, a special void type is returned. This method will never return null.
     *
     * @return the type of this method's return value
     */
    public final Type returnType() {
        return methodInternal.returnType();
    }

    /**
     * Returns the receiver type of this method (a declaration of the "this" reference), if specified.
     * This is used to convey annotations on the "this" instance.
     *
     * @return the receiver type of this method
     */
    public final Type receiverType() {
        return methodInternal.receiverType(clazz);
    }


    /**
     * Returns the list of throwable classes declared to be thrown by this method. This method may return an
     * empty list, but never null.
     *
     * @return the list of throwable classes thrown by this method
     */
    public final List<Type> exceptions() {
        return methodInternal.exceptions();
    }

    final Type[] copyExceptions() {
        return methodInternal.copyExceptions();
    }

    /**
     * Returns the generic type parameters defined by this method. This list will contain resolved type variables
     * which may reference other type parameters, including those declared by the enclosing class of this method.
     *
     * @return the list of generic type parameters for this method, or an empty list if none
     */
    public final List<TypeVariable> typeParameters() {
        return methodInternal.typeParameters();
    }

    /**
     * Returns the annotation instances declared on this method. This includes annotations which are defined
     * against method parameters, as well as type annotations declared on any usage within the method signature.
     * The <code>target()</code> of the returned annotation instances may be used to determine the
     * exact location of the respective annotation instance.
     *
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     *
     * <pre class="brush:java; gutter: false;">
     *     {@literal @}MyMethodAnnotation
     *     public void foo() {...}
     *
     *     public void foo({@literal @}MyParamAnnotation int param) {...}
     *
     *     public void foo(List&lt;{@literal @}MyTypeAnnotation&gt; list) {...}
     *
     *     public &lt;{@literal @}AnotherTypeAnnotation T&gt; void foo(T t) {...}
     * </pre>
     *
     * @return the annotation instances declared on this method or its parameters, or an empty list if none
     */
    @Override
    public final List<AnnotationInstance> annotations() {
        return methodInternal.annotations();
    }

    /**
     * Retrieves an annotation instance declared on this method, it parameters, or any type within the signature
     * of the method, by the name of the annotation. If an annotation by that name is not present, null will
     * be returned.
     *
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     *
     * <pre class="brush:java; gutter: false;">
     *     {@literal @}MyMethodAnnotation
     *     public void foo() {...}
     *
     *     public void foo({@literal @}MyParamAnnotation int param) {...}
     *
     *     public void foo(List&lt;{@literal @}MyTypeAnnotation&gt; list) {...}
     *
     *     public &lt;{@literal @}AnotherTypeAnnotation T&gt; void foo(T t) {...}
     * </pre>
     *
     * @param name the name of the annotation to locate within the method
     * @return the annotation if found, otherwise, null
     */
    @Override
    public final AnnotationInstance annotation(DotName name) {
        return  methodInternal.annotation(name);
    }
    
    /**
     * Retrieves annotations declared on this method, by the name of the annotation. This includes annotations which are defined against method parameters, as
     * well as type annotations declared on any usage within the method signature. The <code>target()</code> of the returned annotation instances may be used to
     * determine the exact location of the respective annotation instance.
     * 
     * If the specified annotation is repeatable (JLS 9.6), the result also contains all values from the container annotation instances. In this case, the
     * {@link AnnotationInstance#target()} returns the target of the container annotation instance.
     * 
     * @param name the name of the annotation
     * @param index the index used to obtain the annotation class
     * @return the annotation instances declared on this method or its parameters, or an empty list if none
     * @throws IllegalArgumentException If the index does not contain the annotation definition or if it does not represent an annotation type
     */
    public final List<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
        if (index == null) {
            throw new IllegalArgumentException("Index must not be null");
        }
        // First retrieve directly present annotations
        List<AnnotationInstance> instances = annotations(name);
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.classAnnotation(Index.REPEATABLE);
        if (repeatable != null) {
            Type containingType = repeatable.value().asClass();
            for (AnnotationInstance container : annotations(containingType.name())) {
                for (AnnotationInstance nestedInstance : container.value().asNestedArray()) {
                    instances.add(new AnnotationInstance(nestedInstance, container.target()));
                }
            }
        }
        return instances;
    }

    /**
     * Retrieves annotations declared on this method, by the name of the annotation. This includes annotations which are defined against method parameters, as
     * well as type annotations declared on any usage within the method signature. The <code>target()</code> of the returned annotation instances may be used to
     * determine the exact location of the respective annotation instance.
     * 
     * @param name
     * @return the annotation instances declared on this method or its parameters, or an empty list if none
     */
    public final List<AnnotationInstance> annotations(DotName name) {
        List<AnnotationInstance> instances = new ArrayList<AnnotationInstance>();
        for (AnnotationInstance instance : methodInternal.annotationArray()) {
            if (instance.name().equals(name)) {
                instances.add(instance);
            }
        }
        return instances;
    }

    /**
     * Returns whether or not the annotation instance with the given name occurs on this method, its parameters
     * or its signature
     *
     * @see #annotations()
     * @see #annotation(DotName)
     * @param name the name of the annotation to look for
     * @return true if the annotation is present, false otherwise
     */
    @Override
    public final boolean hasAnnotation(DotName name) {
        return methodInternal.hasAnnotation(name);
    }

    /**
     * Returns the default annotation value if this method represents an annotation member with a default value.
     * Otherwise null is returned
     *
     * @since 2.1
     * @return default annotation value if available, otherwise null
     */
    public AnnotationValue defaultValue() {
        return methodInternal.defaultValue();
    }

    /**
     * Returns the access fields of this method. {@link Modifier} can be used on this value.
     *
     * @return the access flags of this method
     */
    public final short flags() {
        return methodInternal.flags();
    }
    
    /**
     * 
     * @return {@code true} if this method is a synthetic method
     */
    public final boolean isSynthetic() {
        return Modifiers.isSynthetic(methodInternal.flags());
    }

    /**
     * Returns a string representation describing this field. It is similar although not
     * necessarily equivalent to a Java source code expression representing this field.
     *
     * @return a string representation for this field
     */
    public String toString() {
        return methodInternal.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MethodInfo that = (MethodInfo) o;
        return clazz.equals(that.clazz) && methodInternal.equals(that.methodInternal);
    }

    @Override
    public int hashCode() {
        return 31 * clazz.hashCode() + methodInternal.hashCode();
    }

    @Override
    public final ClassInfo asClass() {
        throw new IllegalArgumentException("Not a class");
    }

    @Override
    public final FieldInfo asField() {
        throw new IllegalArgumentException("Not a field");
    }

    @Override
    public final MethodInfo asMethod() {
        return this;
    }

    @Override
    public final MethodParameterInfo asMethodParameter() {
        throw new IllegalArgumentException("Not a method parameter");
    }

    @Override
    public final TypeTarget asType() {
        throw new IllegalArgumentException("Not a type");
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

    void setDefaultValue(AnnotationValue defaultValue) {
        methodInternal.setDefaultValue(defaultValue);
    }
}
