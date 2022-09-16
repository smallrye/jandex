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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The shared internal representation for MethodInfo objects.
 *
 * @author Jason T. Greene
 */
final class MethodInternal implements Interned {
    static final MethodInternal[] EMPTY_ARRAY = new MethodInternal[0];
    static final NameAndParameterComponentComparator NAME_AND_PARAMETER_COMPONENT_COMPARATOR = new NameAndParameterComponentComparator();
    static final byte[][] EMPTY_PARAMETER_NAMES = new byte[0][];

    static class NameAndParameterComponentComparator implements Comparator<MethodInternal> {
        private int compare(byte[] left, byte[] right) {
            for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
                int a = (left[i] & 0xff);
                int b = (right[j] & 0xff);
                if (a != b) {
                    return a - b;
                }
            }
            return left.length - right.length;
        }

        public int compare(MethodInternal instance, MethodInternal instance2) {
            int x = compare(instance.name, instance2.name);
            if (x != 0) {
                return x;
            }

            int min = Math.min(instance.parameterTypes.length, instance2.parameterTypes.length);
            for (int i = 0; i < min; i++) {
                Type t1 = instance.parameterTypes[i];
                Type t2 = instance2.parameterTypes[i];

                x = t1.name().compareTo(t2.name());
                if (x != 0) {
                    return x;
                }
            }

            x = instance.parameterTypes.length - instance2.parameterTypes.length;
            if (x != 0) {
                return x;
            }

            // Prefer non-synthetic methods when matching
            return (instance.flags & (Modifiers.SYNTHETIC | Modifiers.BRIDGE))
                    - (instance2.flags & (Modifiers.SYNTHETIC | Modifiers.BRIDGE));
        }
    }

    private byte[] name;
    private byte[][] parameterNames;
    private Type[] parameterTypes;
    private Type returnType;
    private Type[] exceptions;
    private Type receiverType;
    private Type[] typeParameters;
    private AnnotationInstance[] annotations;
    private AnnotationValue defaultValue;
    private short flags;

    private final Type[] descriptorParameterTypes;

    MethodInternal(byte[] name, byte[][] parameterNames, Type[] parameterTypes, Type returnType, short flags) {
        this(name, parameterNames, parameterTypes, returnType, flags, Type.EMPTY_ARRAY, Type.EMPTY_ARRAY);
    }

    MethodInternal(byte[] name, byte[][] parameterNames, Type[] parameterTypes, Type returnType, short flags,
            Type[] typeParameters, Type[] exceptions) {
        this(name, parameterNames, parameterTypes, returnType, flags, null, typeParameters, exceptions,
                AnnotationInstance.EMPTY_ARRAY, null);
    }

    MethodInternal(byte[] name, byte[][] parameterNames, Type[] parameterTypes, Type returnType, short flags,
            Type receiverType, Type[] typeParameters, Type[] exceptions,
            AnnotationInstance[] annotations, AnnotationValue defaultValue) {
        this.name = name;
        this.parameterNames = parameterNames;
        this.parameterTypes = parameterTypes.length == 0 ? Type.EMPTY_ARRAY : parameterTypes;
        this.returnType = returnType;
        this.flags = flags;
        this.annotations = annotations;
        this.exceptions = exceptions;
        this.typeParameters = typeParameters;
        this.receiverType = receiverType;
        this.defaultValue = defaultValue;

        this.descriptorParameterTypes = this.parameterTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodInternal methodInternal = (MethodInternal) o;

        if (flags != methodInternal.flags) {
            return false;
        }
        if (!Arrays.equals(annotations, methodInternal.annotations)) {
            return false;
        }
        if (!Arrays.equals(exceptions, methodInternal.exceptions)) {
            return false;
        }
        if (!Arrays.equals(name, methodInternal.name)) {
            return false;
        }
        if (!Arrays.deepEquals(parameterNames, methodInternal.parameterNames)) {
            return false;
        }
        if (!Arrays.equals(parameterTypes, methodInternal.parameterTypes)) {
            return false;
        }
        if (!Arrays.equals(descriptorParameterTypes, methodInternal.descriptorParameterTypes)) {
            return false;
        }
        if (receiverType != null ? !receiverType.equals(methodInternal.receiverType) : methodInternal.receiverType != null) {
            return false;
        }
        if (!returnType.equals(methodInternal.returnType)) {
            return false;
        }
        if (defaultValue != null ? !defaultValue.equals(methodInternal.defaultValue) : methodInternal.defaultValue != null) {
            return false;
        }
        return Arrays.equals(typeParameters, methodInternal.typeParameters);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(name);
        result = 31 * result + Arrays.deepHashCode(parameterNames);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        result = 31 * result + Arrays.hashCode(descriptorParameterTypes);
        result = 31 * result + returnType.hashCode();
        result = 31 * result + Arrays.hashCode(exceptions);
        result = 31 * result + (receiverType != null ? receiverType.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(typeParameters);
        result = 31 * result + Arrays.hashCode(annotations);
        result = 31 * result + (int) flags;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }

    @Override
    public boolean internEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodInternal methodInternal = (MethodInternal) o;

        if (flags != methodInternal.flags) {
            return false;
        }
        if (!Arrays.equals(annotations, methodInternal.annotations)) {
            return false;
        }
        if (!Interned.arrayEquals(exceptions, methodInternal.exceptions)) {
            return false;
        }
        if (!Arrays.equals(name, methodInternal.name)) {
            return false;
        }
        if (!Arrays.deepEquals(parameterNames, methodInternal.parameterNames)) {
            return false;
        }
        if (!Interned.arrayEquals(parameterTypes, methodInternal.parameterTypes)) {
            return false;
        }
        if (!Interned.arrayEquals(descriptorParameterTypes, methodInternal.descriptorParameterTypes)) {
            return false;
        }
        if (receiverType != null ? !receiverType.internEquals(methodInternal.receiverType)
                : methodInternal.receiverType != null) {
            return false;
        }
        if (!returnType.internEquals(methodInternal.returnType)) {
            return false;
        }
        if (defaultValue != null ? !defaultValue.equals(methodInternal.defaultValue) : methodInternal.defaultValue != null) {
            return false;
        }
        return Interned.arrayEquals(typeParameters, methodInternal.typeParameters);
    }

    @Override
    public int internHashCode() {
        int result = Arrays.hashCode(name);
        result = 31 * result + Arrays.deepHashCode(parameterNames);
        result = 31 * result + Interned.arrayHashCode(parameterTypes);
        result = 31 * result + Interned.arrayHashCode(descriptorParameterTypes);
        result = 31 * result + returnType.internHashCode();
        result = 31 * result + Interned.arrayHashCode(exceptions);
        result = 31 * result + (receiverType != null ? receiverType.internHashCode() : 0);
        result = 31 * result + Interned.arrayHashCode(typeParameters);
        result = 31 * result + Arrays.hashCode(annotations);
        result = 31 * result + (int) flags;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }

    final String name() {
        return Utils.fromUTF8(name);
    }

    final int parametersCount() {
        return parameterTypes.length;
    }

    final String parameterName(int i) {
        if (i >= parameterNames.length)
            return null;
        return Utils.fromUTF8(parameterNames[i]);
    }

    final byte[] nameBytes() {
        return name;
    }

    final byte[][] parameterNamesBytes() {
        return parameterNames;
    }

    final Type[] copyParameterTypes() {
        return parameterTypes.clone();
    }

    final Type[] parameterTypesArray() {
        return parameterTypes;
    }

    final Type[] copyExceptions() {
        return exceptions.clone();
    }

    final List<Type> parameterTypes() {
        return Collections.unmodifiableList(Arrays.asList(parameterTypes));
    }

    final List<Type> descriptorParameterTypes() {
        return Collections.unmodifiableList(Arrays.asList(descriptorParameterTypes));
    }

    final Type[] descriptorParameterTypesArray() {
        return descriptorParameterTypes;
    }

    final Type returnType() {
        return returnType;
    }

    final Type receiverType(ClassInfo clazz) {
        if (receiverType != null) {
            return receiverType;
        }

        // don't assign the `receiverType` field here! receiver type declaration is only useful
        // if annotated, and in that case, the type annotation processing code calls `setReceiverType`
        if (clazz.typeParameterArray().length > 0) {
            Type[] classTypeParameters = clazz.typeParameterArray();
            Type[] receiverTypeArguments = new Type[classTypeParameters.length];
            for (int i = 0; i < classTypeParameters.length; i++) {
                receiverTypeArguments[i] = classTypeParameters[i].copyType(AnnotationInstance.EMPTY_ARRAY);
            }
            return new ParameterizedType(clazz.name(), receiverTypeArguments, null);
        }
        return new ClassType(clazz.name());
    }

    final Type receiverTypeField() {
        return receiverType;
    }

    final List<Type> exceptions() {
        return Collections.unmodifiableList(Arrays.asList(exceptions));
    }

    final Type[] exceptionArray() {
        return exceptions;
    }

    final List<TypeVariable> typeParameters() {
        @SuppressWarnings("unchecked") // type parameters will always be TypeVariable[]
        List<TypeVariable> list = (List) Arrays.asList(typeParameters);
        return Collections.unmodifiableList(list);
    }

    final List<AnnotationInstance> annotations() {
        return Collections.unmodifiableList(Arrays.asList(annotations));
    }

    final AnnotationInstance[] annotationArray() {
        return annotations;
    }

    final AnnotationInstance annotation(DotName name) {
        return AnnotationInstance.binarySearch(annotations, name);
    }

    final boolean hasAnnotation(DotName name) {
        return annotation(name) != null;
    }

    final Type[] typeParameterArray() {
        return typeParameters;
    }

    final AnnotationValue defaultValue() {
        return defaultValue;
    }

    final short flags() {
        return flags;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String name = name();
        builder.append(returnType.toString(true)).append(' ').append(name).append('(');
        if (receiverType != null) {
            builder.append(receiverType.toString(true)).append(" this");
            if (parameterTypes.length > 0) {
                builder.append(", ");
            }
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            builder.append(parameterTypes[i].toString(true));
            String parameterName = parameterName(i);
            if (parameterName != null) {
                builder.append(' ');
                builder.append(parameterName);
            }
            if (i + 1 < parameterTypes.length)
                builder.append(", ");
        }
        builder.append(')');

        if (exceptions.length > 0) {
            builder.append(" throws ");
            for (int i = 0; i < exceptions.length; i++) {
                builder.append(exceptions[i].toString(true));
                if (i < exceptions.length - 1) {
                    builder.append(", ");
                }
            }
        }

        return builder.toString();
    }

    void setTypeParameters(Type[] typeParameters) {
        if (typeParameters.length > 0) {
            this.typeParameters = typeParameters;
        }
    }

    void setParameterNames(byte[][] parameterNames) {
        this.parameterNames = parameterNames;
    }

    void setParameterTypes(Type[] parameterTypes) {
        this.parameterTypes = parameterTypes.length == 0 ? Type.EMPTY_ARRAY : parameterTypes;
    }

    void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    void setExceptions(Type[] exceptions) {
        this.exceptions = exceptions.length == 0 ? Type.EMPTY_ARRAY : exceptions;
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

    void setDefaultValue(AnnotationValue defaultValue) {
        this.defaultValue = defaultValue;
    }
}
