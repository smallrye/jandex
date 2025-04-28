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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a Java method, constructor, or static initializer.
 * <p>
 * Jandex makes reasonable attempts to not include implicitly declared (aka mandated)
 * and synthetic parameters in {@link #parameters()}, {@link #parameterTypes()},
 * {@link #parameterName(int)}, {@link #parameterType(int)} and {@link #parametersCount()}.
 * However, {@link #descriptorParameterTypes()} and {@link #descriptorParametersCount()}
 * may be used to obtain information about all parameters, including mandated and synthetic.
 * <p>
 * As an exception to the rule above, implicitly declared parameters are always included
 * in case of methods that don't have an explicit parameter list. These are:
 * <ul>
 * <li>the implicitly declared {@code valueOf()} method in enums;</li>
 * <li>the compact constructor in records.</li>
 * </ul>
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 */
public final class MethodInfo implements Declaration, Descriptor, GenericSignature {

    static final String[] EMPTY_PARAMETER_NAMES = new String[0];
    private MethodInternal methodInternal;
    private ClassInfo clazz;

    MethodInfo() {
    }

    MethodInfo(ClassInfo clazz, MethodInternal methodInternal) {
        this.methodInternal = methodInternal;
        this.clazz = clazz;
    }

    MethodInfo(ClassInfo clazz, byte[] name, byte[][] parameterNames, Type[] parameterTypes, Type returnType, short flags) {
        this(clazz, new MethodInternal(name, parameterNames, parameterTypes, returnType, flags));
    }

    MethodInfo(ClassInfo clazz, byte[] name, byte[][] parameterNames, Type[] parameterTypes, Type returnType, short flags,
            Type[] typeParameters, Type[] exceptions) {
        this(clazz, new MethodInternal(name, parameterNames, parameterTypes, returnType, flags, typeParameters, exceptions));
    }

    /**
     * Construct a new mock Method instance.
     *
     * @param clazz the class declaring the field
     * @param name the name of the field
     * @param parameterTypes a read only array containing the types of each parameter in parameter order
     * @param returnType the return value type
     * @param flags the method attributes
     * @return a mock method
     */
    public static MethodInfo create(ClassInfo clazz, String name, Type[] parameterTypes, Type returnType, short flags) {
        return create(clazz, name, parameterTypes, returnType, flags, null, null);
    }

    /**
     * Construct a new mock Method instance.
     *
     * @param clazz the class declaring the field
     * @param name the name of the field
     * @param parameterTypes a read only array containing the types of each parameter in parameter order
     * @param returnType the return value type
     * @param flags the method attributes
     * @param typeParameters the generic type parameters for this method
     * @param exceptions the exceptions declared as thrown by this method
     * @return a mock method
     *
     * @since 2.1
     */
    public static MethodInfo create(ClassInfo clazz, String name, Type[] parameterTypes, Type returnType, short flags,
            TypeVariable[] typeParameters, Type[] exceptions) {
        return create(clazz, name, EMPTY_PARAMETER_NAMES, parameterTypes, returnType, flags, typeParameters, exceptions);
    }

    /**
     * Construct a new mock Method instance.
     *
     * @param clazz the class declaring the field
     * @param name the name of the field
     * @param parameterNames the names of the method parameter
     * @param parameterTypes a read only array containing the types of each parameter in parameter order
     * @param returnType the return value type
     * @param flags the method attributes
     * @param typeParameters the generic type parameters for this method
     * @param exceptions the exceptions declared as thrown by this method
     * @return a mock method
     *
     * @since 2.2
     */
    public static MethodInfo create(ClassInfo clazz, String name, String[] parameterNames, Type[] parameterTypes,
            Type returnType, short flags, TypeVariable[] typeParameters, Type[] exceptions) {
        if (clazz == null)
            throw new IllegalArgumentException("Clazz can't be null");

        if (name == null)
            throw new IllegalArgumentException("Name can't be null");

        if (parameterTypes == null)
            throw new IllegalArgumentException("Parameter types can't be null");

        if (parameterNames == null)
            throw new IllegalArgumentException("Parameter names can't be null");

        if (returnType == null)
            throw new IllegalArgumentException("Return type can't be null");

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
        return new MethodInfo(clazz, bytes, parameterNameBytes, parameterTypes, returnType, flags, typeParameters, exceptions);
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
     *
     * @param i the parameter index, zero-based
     * @return the name of the given parameter, or {@code null} if not known
     */
    public final String parameterName(int i) {
        return methodInternal.parameterName(i);
    }

    /**
     * Returns the type of the given parameter.
     *
     * @param i the parameter index, zero-based
     * @return the type of the given parameter
     */
    public final Type parameterType(int i) {
        return methodInternal.parameterTypesArray()[i];
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
     * Returns an array of parameter types in declaration order.
     * Jandex makes reasonable attempts to not include implicitly declared (aka mandated) and synthetic parameters.
     * <p>
     * This method performs a defensive array copy per call, and should be avoided.
     * Instead, the {@link #parameterTypes()} method should be used.
     *
     * @deprecated use {@link #parameterTypes()}
     * @return an array copy contain parameter types
     */
    @Deprecated
    public final Type[] args() {
        return methodInternal.copyParameterTypes();
    }

    final Type[] copyParameters() {
        return methodInternal.copyParameterTypes();
    }

    /**
     * Returns the number of parameters this method declares.
     * Jandex makes reasonable attempts to not count implicitly declared (aka mandated) and synthetic parameters.
     *
     * @return the number of parameters this method declares
     */
    public final int parametersCount() {
        return methodInternal.parametersCount();
    }

    /**
     * Returns a list of types of parameters declared on this method, in declaration order.
     * Jandex makes reasonable attempts to not include implicitly declared (aka mandated) and synthetic parameters.
     * Positions of types in this list may be used to retrieve a name using {@link #parameterName(int)}
     * or look for annotations.
     *
     * @return immutable list of parameter types of this method, never {@code null}
     */
    public final List<Type> parameterTypes() {
        return methodInternal.parameterTypes();
    }

    /**
     * Returns a list of parameters declared on this method, in declaration order.
     * Jandex makes reasonable attempts to not include implicitly declared (aka mandated) and synthetic parameters.
     *
     * @return immutable list of parameter types of this method, never {@code null}
     */
    public final List<MethodParameterInfo> parameters() {
        int parametersCount = methodInternal.parametersCount();
        List<MethodParameterInfo> parameters = new ArrayList<>(parametersCount);
        for (short i = 0; i < parametersCount; i++) {
            parameters.add(new MethodParameterInfo(this, i));
        }
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Returns the number of all parameters present on this method, based on the method descriptor.
     * This always includes implicitly declared (aka mandated) and synthetic parameters.
     *
     * @return the number of all parameters present on this method
     */
    public final int descriptorParametersCount() {
        return methodInternal.descriptorParameterTypesArray().length;
    }

    /**
     * Returns a list of types of all parameters present on this method, based on the method descriptor.
     * This always includes implicitly declared (aka mandated) and synthetic parameters. These types are
     * never annotated and their position in the list <em>cannot</em> be used to retrieve a name using
     * {@link #parameterName(int)} or look for annotations.
     */
    public final List<Type> descriptorParameterTypes() {
        return methodInternal.descriptorParameterTypes();
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
     * @return immutable list of throwable classes thrown by this method
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
     * @return immutable list of generic type parameters for this method, or an empty list if none
     */
    public final List<TypeVariable> typeParameters() {
        return methodInternal.typeParameters();
    }

    /**
     * Returns whether an annotation instance with given name is declared on this method, its parameters
     * or any type within its signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @see #annotation(DotName)
     */
    public final boolean hasAnnotation(DotName name) {
        return methodInternal.hasAnnotation(name);
    }

    /**
     * Returns the annotation instance with given name declared on this method, any of its parameters or any type
     * within its signature. The {@code target()} method of the returned annotation instance may be used to determine
     * the exact location of the annotation instance.
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     *
     * <pre class="brush:java">
     * {@literal @}MyMethodAnnotation
     * public void foo() {...}
     *
     * public void foo({@literal @}MyParamAnnotation int param) {...}
     *
     * public void foo(List&lt;{@literal @}MyTypeAnnotation String&gt; list) {...}
     *
     * public &lt;{@literal @}MyTypeAnnotation T&gt; void foo(T t) {...}
     * </pre>
     * <p>
     * In case an annotation with given name occurs more than once, the result of this method is not deterministic.
     * For such situations, {@link #annotations(DotName)} is preferable.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotations(DotName)
     */
    public final AnnotationInstance annotation(DotName name) {
        return methodInternal.annotation(name);
    }

    /**
     * Returns the annotation instances with given name declared on this method, any of its parameters or any type
     * within its signature. The {@code target()} method of the returned annotation instances may be used to determine
     * the exact location of the respective annotation instance.
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     *
     * <pre class="brush:java">
     * {@literal @}MyMethodAnnotation
     * public void foo() {...}
     *
     * public void foo({@literal @}MyParamAnnotation int param) {...}
     *
     * public void foo(List&lt;{@literal @}MyTypeAnnotation String&gt; list) {...}
     *
     * public &lt;{@literal @}MyTypeAnnotation T&gt; void foo(T t) {...}
     * </pre>
     *
     * @param name name of the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @see #annotationsWithRepeatable(DotName, IndexView)
     * @see #annotations()
     */
    public final List<AnnotationInstance> annotations(DotName name) {
        List<AnnotationInstance> instances = new ArrayList<>();
        for (AnnotationInstance instance : methodInternal.annotationArray()) {
            if (instance.name().equals(name)) {
                instances.add(instance);
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances with given name declared on this method, any of its parameters or any type
     * within its signature. The {@code target()} method of the returned annotation instances may be used to determine
     * the exact location of the respective annotation instance.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @see #annotations(DotName)
     * @see #annotations()
     */
    public final List<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
        if (index == null) {
            throw new IllegalArgumentException("Index must not be null");
        }
        List<AnnotationInstance> instances = new ArrayList<>(annotations(name));
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.declaredAnnotation(DotName.REPEATABLE_NAME);
        if (repeatable != null) {
            Type containingType = repeatable.value().asClass();
            for (AnnotationInstance container : annotations(containingType.name())) {
                for (AnnotationInstance nestedInstance : container.value().asNestedArray()) {
                    instances.add(AnnotationInstance.create(nestedInstance, container.target()));
                }
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances declared on this method, any of its parameters or any type
     * within its signature. The {@code target()} method of the returned annotation instances may be used to determine
     * the exact location of the respective annotation instance.
     * <p>
     * The following is a non-exhaustive list of examples of annotations returned by this method:
     *
     * <pre class="brush:java">
     * {@literal @}MyMethodAnnotation
     * public void foo() {...}
     *
     * public void foo({@literal @}MyParamAnnotation int param) {...}
     *
     * public void foo(List&lt;{@literal @}MyTypeAnnotation String&gt; list) {...}
     *
     * public &lt;{@literal @}AnotherTypeAnnotation T&gt; void foo(T t) {...}
     * </pre>
     *
     * @return immutable list of annotation instances, never {@code null}
     */
    public final List<AnnotationInstance> annotations() {
        return methodInternal.annotations();
    }

    /**
     * Returns whether an annotation instance with given name is declared on this method.
     * <p>
     * Unlike {@link #hasAnnotation(DotName)}, this method ignores annotations declared on the method parameters
     * and types within the method signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return {@code true} if the annotation is present, {@code false} otherwise
     * @since 3.0
     * @see #hasAnnotation(DotName)
     */
    @Override
    public boolean hasDeclaredAnnotation(DotName name) {
        return declaredAnnotation(name) != null;
    }

    /**
     * Returns the annotation instance with given name declared on this method.
     * <p>
     * Unlike {@link #annotation(DotName)}, this method doesn't return annotations declared on the method parameters
     * and types within the method signature.
     *
     * @param name name of the annotation type to look for, must not be {@code null}
     * @return the annotation instance, or {@code null} if not found
     * @since 3.0
     * @see #annotation(DotName)
     */
    @Override
    public AnnotationInstance declaredAnnotation(DotName name) {
        for (AnnotationInstance instance : methodInternal.annotationArray()) {
            if (instance.target().kind() == Kind.METHOD && instance.name().equals(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Returns the annotation instances with given name declared on this method.
     * <p>
     * If the specified annotation is repeatable, the result also contains all values from the container annotation
     * instance. In this case, the {@link AnnotationInstance#target()} returns the target of the container annotation
     * instance.
     * <p>
     * Unlike {@link #annotationsWithRepeatable(DotName, IndexView)}, this method doesn't return annotations
     * declared on the method parameters and types within the method signature.
     *
     * @param name name of the annotation type, must not be {@code null}
     * @param index index used to obtain the annotation type, must not be {@code null}
     * @return immutable list of annotation instances, never {@code null}
     * @throws IllegalArgumentException if the index is {@code null}, if the index does not contain the annotation type
     *         or if {@code name} does not identify an annotation type
     * @since 3.0
     * @see #annotationsWithRepeatable(DotName, IndexView)
     */
    @Override
    public List<AnnotationInstance> declaredAnnotationsWithRepeatable(DotName name, IndexView index) {
        if (index == null) {
            throw new IllegalArgumentException("Index must not be null");
        }
        List<AnnotationInstance> instances = new ArrayList<>();
        AnnotationInstance declaredInstance = declaredAnnotation(name);
        if (declaredInstance != null) {
            instances.add(declaredInstance);
        }
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.declaredAnnotation(DotName.REPEATABLE_NAME);
        if (repeatable != null) {
            Type containingType = repeatable.value().asClass();
            AnnotationInstance container = declaredAnnotation(containingType.name());
            if (container != null) {
                for (AnnotationInstance nestedInstance : container.value().asNestedArray()) {
                    instances.add(AnnotationInstance.create(nestedInstance, container.target()));
                }
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the annotation instances declared on this method.
     * <p>
     * Unlike {@link #annotations()}, this method doesn't return annotations declared on the method parameters
     * and types within the method signature.
     *
     * @return immutable list of annotation instances, never {@code null}
     * @since 3.0
     * @see #annotations()
     */
    @Override
    public List<AnnotationInstance> declaredAnnotations() {
        List<AnnotationInstance> instances = new ArrayList<>();
        for (AnnotationInstance instance : methodInternal.annotationArray()) {
            if (instance.target().kind() == Kind.METHOD) {
                instances.add(instance);
            }
        }
        return Collections.unmodifiableList(instances);
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
    public boolean isSynthetic() {
        return Modifiers.isSynthetic(methodInternal.flags());
    }

    /**
     *
     * @return {@code true} if this method is a bridge method as defined by the JLS, {@code false} otherwise
     */
    public boolean isBridge() {
        return Modifiers.isBridge(methodInternal.flags());
    }

    /**
     * @return {@code true} if this method is a constructor
     */
    public boolean isConstructor() {
        return Arrays.equals(Utils.INIT_METHOD_NAME, methodInternal.nameBytes());
    }

    /**
     * @return {@code true} if this method is a static initializer
     */
    public boolean isStaticInitializer() {
        return Arrays.equals(Utils.CLINIT_METHOD_NAME, methodInternal.nameBytes());
    }

    /**
     * A default method is a public non-abstract non-static method declared in an interface.
     *
     * @return {@code true} if this method is a default interface method, {@code false} otherwise
     */
    public boolean isDefault() {
        if (!clazz.isInterface()) {
            return false;
        }
        short flags = methodInternal.flags();
        return Modifier.isPublic(flags) && !Modifier.isStatic(flags) && !Modifier.isAbstract(flags);
    }

    /**
     * @return {@code true} if this method is {@code abstract}
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(methodInternal.flags());
    }

    /**
     * Returns whether this method must have a generic signature. That is, whether the Java compiler
     * when compiling this method had to emit the {@code Signature} bytecode attribute.
     *
     * @return whether this method must have a generic signature
     */
    @Override
    public boolean requiresGenericSignature() {
        return GenericSignatureReconstruction.requiresGenericSignature(this);
    }

    /**
     * Returns a generic signature of this method, possibly without any generic-related information.
     * That is, produces a correct generic signature even if this method is not generic
     * and does not use any type variables.
     * <p>
     * Signatures of type variables are substituted for signatures of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, no substitution happens and the type variable signature is used
     * unmodified.
     * <p>
     * Note that the return value does not come directly from bytecode. Jandex does not store the signature
     * strings. Instead, the return value is reconstructed from the Jandex object model.
     *
     * @param typeVariableSubstitution a substitution function from type variable identifiers to types
     * @return a generic signature of this method with type variables substituted, never {@code null}
     */
    @Override
    public String genericSignature(Function<String, Type> typeVariableSubstitution) {
        return GenericSignatureReconstruction.reconstructGenericSignature(this, typeVariableSubstitution);
    }

    /**
     * Returns a bytecode descriptor of this method.
     * <p>
     * Descriptors of type variables are substituted for descriptors of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, or if it returns the type variable itself, no substitution happens
     * and the type variable descriptor is used unmodified.
     * <p>
     * Note that the return value does not come directly from bytecode. Jandex does not store the descriptor
     * strings. Instead, the return value is reconstructed from the Jandex object model.
     *
     * @return the bytecode descriptor of this method
     */
    public String descriptor(Function<String, Type> typeVariableSubstitution) {
        return DescriptorReconstruction.methodDescriptor(this, typeVariableSubstitution);
    }

    /**
     * Returns a string representation describing this method. It is similar although not
     * necessarily identical to a Java source code declaration of this method.
     *
     * @return a string representation of this method
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

    @Override
    public final RecordComponentInfo asRecordComponent() {
        throw new IllegalArgumentException("Not a record component");
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
        methodInternal.setParameterTypes(parameters);
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
