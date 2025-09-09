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
final class MethodInternal {
    static final MethodInternal[] EMPTY_ARRAY = new MethodInternal[0];
    static final NameAndParameterComponentComparator NAME_AND_PARAMETER_COMPONENT_COMPARATOR = new NameAndParameterComponentComparator();
    static final byte[][] EMPTY_PARAMETER_NAMES = new byte[0][];

    private static final class NameAndParameterComponentComparator implements Comparator<MethodInternal> {
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

            Type[] parameterTypes1 = instance.parameterTypes;
            Type[] parameterTypes2 = instance2.parameterTypes;

            int min = Math.min(parameterTypes1.length, parameterTypes2.length);
            for (int i = 0; i < min; i++) {
                Type t1 = parameterTypes1[i];
                Type t2 = parameterTypes2[i];

                x = t1.name().compareTo(t2.name());
                if (x != 0) {
                    return x;
                }
            }

            x = parameterTypes1.length - parameterTypes2.length;
            if (x != 0) {
                return x;
            }

            // Prefer non-synthetic methods when matching
            return (instance.flags & (Modifiers.SYNTHETIC | Modifiers.BRIDGE))
                    - (instance2.flags & (Modifiers.SYNTHETIC | Modifiers.BRIDGE));
        }
    }

    // contains fields that are only seldom used, to make the `MethodInternal` class smaller
    private static final class ExtraInfo {
        Type receiverType;
        Type[] typeParameters;
        AnnotationValue defaultValue;
        AnnotationInstance[] annotations;
    }

    private byte[] name;
    private byte[][] parameterNames;
    private Type[] parameterTypes;
    private Type returnType;
    private Type[] exceptions;
    private short flags;
    private ExtraInfo extra;

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
        this.exceptions = exceptions;
        if (annotations != null && annotations.length > 0) {
            if (extra == null) {
                extra = new ExtraInfo();
            }
            extra.annotations = annotations;
        }
        if (typeParameters != null && typeParameters.length > 0) {
            if (extra == null) {
                extra = new ExtraInfo();
            }
            extra.typeParameters = typeParameters;
        }
        if (receiverType != null) {
            if (extra == null) {
                extra = new ExtraInfo();
            }
            extra.receiverType = receiverType;
        }
        if (defaultValue != null) {
            if (extra == null) {
                extra = new ExtraInfo();
            }
            extra.defaultValue = defaultValue;
        }

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

        if (extra != null && methodInternal.extra == null || extra == null && methodInternal.extra != null) {
            return false;
        }

        if (flags != methodInternal.flags) {
            return false;
        }
        if (extra != null && !Arrays.equals(extra.annotations, methodInternal.extra.annotations)) {
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
        if (extra != null && (extra.receiverType != null ? !extra.receiverType.equals(methodInternal.extra.receiverType)
                : methodInternal.extra.receiverType != null)) {
            return false;
        }
        if (!returnType.equals(methodInternal.returnType)) {
            return false;
        }
        if (extra != null && (extra.defaultValue != null ? !extra.defaultValue.equals(methodInternal.extra.defaultValue)
                : methodInternal.extra.defaultValue != null)) {
            return false;
        }
        if (extra != null && !Arrays.equals(extra.typeParameters, methodInternal.extra.typeParameters)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(name);
        result = 31 * result + Arrays.deepHashCode(parameterNames);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        result = 31 * result + Arrays.hashCode(descriptorParameterTypes);
        result = 31 * result + returnType.hashCode();
        result = 31 * result + Arrays.hashCode(exceptions);
        result = 31 * result + (extra != null && extra.receiverType != null ? extra.receiverType.hashCode() : 0);
        result = 31 * result + (extra != null ? Arrays.hashCode(extra.typeParameters) : 0);
        result = 31 * result + (extra != null && extra.defaultValue != null ? extra.defaultValue.hashCode() : 0);
        result = 31 * result + (int) flags;
        return result;
    }

    boolean internEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MethodInternal methodInternal = (MethodInternal) o;

        if (extra != null && methodInternal.extra == null || extra == null && methodInternal.extra != null) {
            return false;
        }

        if (flags != methodInternal.flags) {
            return false;
        }
        if (extra != null && !Arrays.equals(extra.annotations, methodInternal.extra.annotations)) {
            return false;
        }
        if (!TypeInterning.arrayEquals(exceptions, methodInternal.exceptions)) {
            return false;
        }
        if (!Arrays.equals(name, methodInternal.name)) {
            return false;
        }
        if (!Arrays.deepEquals(parameterNames, methodInternal.parameterNames)) {
            return false;
        }
        if (!TypeInterning.arrayEquals(parameterTypes, methodInternal.parameterTypes)) {
            return false;
        }
        if (!TypeInterning.arrayEquals(descriptorParameterTypes, methodInternal.descriptorParameterTypes)) {
            return false;
        }
        if (extra != null && (extra.receiverType != null ? !extra.receiverType.internEquals(methodInternal.extra.receiverType)
                : methodInternal.extra.receiverType != null)) {
            return false;
        }
        if (!returnType.internEquals(methodInternal.returnType)) {
            return false;
        }
        if (extra != null && (extra.defaultValue != null ? !extra.defaultValue.equals(methodInternal.extra.defaultValue)
                : methodInternal.extra.defaultValue != null)) {
            return false;
        }
        if (extra != null && !TypeInterning.arrayEquals(extra.typeParameters, methodInternal.extra.typeParameters)) {
            return false;
        }
        return true;
    }

    int internHashCode() {
        int result = Arrays.hashCode(name);
        result = 31 * result + Arrays.deepHashCode(parameterNames);
        result = 31 * result + TypeInterning.arrayHashCode(parameterTypes);
        result = 31 * result + TypeInterning.arrayHashCode(descriptorParameterTypes);
        result = 31 * result + returnType.internHashCode();
        result = 31 * result + TypeInterning.arrayHashCode(exceptions);
        result = 31 * result + (extra != null && extra.receiverType != null ? extra.receiverType.internHashCode() : 0);
        result = 31 * result + (extra != null ? TypeInterning.arrayHashCode(extra.typeParameters) : 0);
        result = 31 * result + (extra != null ? Arrays.hashCode(extra.annotations) : 0);
        result = 31 * result + (extra != null && extra.defaultValue != null ? extra.defaultValue.hashCode() : 0);
        result = 31 * result + (int) flags;
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
        return new ImmutableArrayList<>(parameterTypes);
    }

    final List<Type> descriptorParameterTypes() {
        return new ImmutableArrayList<>(descriptorParameterTypes);
    }

    final Type[] descriptorParameterTypesArray() {
        return descriptorParameterTypes;
    }

    final Type returnType() {
        return returnType;
    }

    final Type receiverType(ClassInfo clazz) {
        if (extra != null && extra.receiverType != null) {
            return extra.receiverType;
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
        return ClassType.create(clazz.name());
    }

    final Type receiverTypeField() {
        return extra != null ? extra.receiverType : null;
    }

    final List<Type> exceptions() {
        return new ImmutableArrayList<>(exceptions);
    }

    final Type[] exceptionArray() {
        return exceptions;
    }

    final List<TypeVariable> typeParameters() {
        // type parameters are always `TypeVariable`
        return extra != null && extra.typeParameters != null ? new ImmutableArrayList(extra.typeParameters)
                : Collections.emptyList();
    }

    final List<AnnotationInstance> annotations() {
        return extra != null && extra.annotations != null ? new ImmutableArrayList<>(extra.annotations)
                : Collections.emptyList();
    }

    final AnnotationInstance[] annotationArray() {
        return extra != null && extra.annotations != null ? extra.annotations : AnnotationInstance.EMPTY_ARRAY;
    }

    final AnnotationInstance annotation(DotName name) {
        return extra != null && extra.annotations != null ? AnnotationInstance.binarySearch(extra.annotations, name) : null;
    }

    final boolean hasAnnotation(DotName name) {
        return annotation(name) != null;
    }

    final Type[] typeParameterArray() {
        return extra != null && extra.typeParameters != null ? extra.typeParameters : Type.EMPTY_ARRAY;
    }

    final AnnotationValue defaultValue() {
        return extra != null ? extra.defaultValue : null;
    }

    final short flags() {
        return flags;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String name = name();
        builder.append(returnType.toString(true)).append(' ').append(name).append('(');
        if (extra != null && extra.receiverType != null) {
            builder.append(extra.receiverType.toString(true)).append(" this");
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
            if (extra == null) {
                extra = new ExtraInfo();
            }
            extra.typeParameters = typeParameters;
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
        if (extra == null) {
            extra = new ExtraInfo();
        }
        extra.receiverType = receiverType;
    }

    void setAnnotations(List<AnnotationInstance> annotations) {
        if (!annotations.isEmpty()) {
            if (extra == null) {
                extra = new ExtraInfo();
            }
            extra.annotations = annotations.toArray(new AnnotationInstance[0]);
            Arrays.sort(extra.annotations, AnnotationInstance.NAME_COMPARATOR);
        }
    }

    void setDefaultValue(AnnotationValue defaultValue) {
        if (extra == null) {
            extra = new ExtraInfo();
        }
        extra.defaultValue = defaultValue;
    }
}
