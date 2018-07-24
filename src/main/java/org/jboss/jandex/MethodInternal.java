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
 *  The shared internal representation for MethodInfo objects.
 *
 * @author Jason T. Greene
 */
final class MethodInternal {
    static final int SYNTHETIC = 0x1000;
    static final int BRIDGE    = 0x0040;
    static final MethodInternal[] EMPTY_ARRAY = new MethodInternal[0];
    static final NameAndParameterComponentComparator NAME_AND_PARAMETER_COMPONENT_COMPARATOR = new NameAndParameterComponentComparator();

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

            int min = Math.min(instance.parameters.length, instance2.parameters.length);
            for (int i = 0; i < min; i++) {
                Type t1 = instance.parameters[i];
                Type t2 = instance2.parameters[i];

                x = t1.name().compareTo(t2.name());
                if (x != 0) {
                    return x;
                }
            }

            x = instance.parameters.length - instance2.parameters.length;
            if (x != 0) {
                return x;
            }

            // Prefer non-synthetic methods when matching
            return (instance.flags & (SYNTHETIC| BRIDGE)) - (instance2.flags & (SYNTHETIC| BRIDGE));
        }
    }

    private byte[] name;
    private Type[] parameters;
    private Type returnType;
    private Type[] exceptions;
    private Type receiverType;
    private Type[] typeParameters;
    private AnnotationInstance[] annotations;
    private AnnotationValue defaultValue;
    private short flags;

    MethodInternal(byte[] name, Type[] parameters, Type returnType, short flags) {
        this(name, parameters, returnType, flags, Type.EMPTY_ARRAY);
    }

    MethodInternal(byte[] name, Type[] parameters, Type returnType, short flags, Type[] typeParameters) {
        this(name, parameters, returnType, flags, null, typeParameters, Type.EMPTY_ARRAY, AnnotationInstance.EMPTY_ARRAY, null);
    }

    MethodInternal(byte[] name, Type[] parameters, Type returnType, short flags,
                   Type receiverType, Type[] typeParameters, Type[] exceptions,
                   AnnotationInstance[] annotations, AnnotationValue defaultValue) {
        this.name = name;
        this.parameters = parameters.length == 0 ? Type.EMPTY_ARRAY : parameters;
        this.returnType = returnType;
        this.flags = flags;
        this.annotations = annotations;
        this.exceptions = exceptions;
        this.typeParameters = typeParameters;
        this.receiverType = receiverType;
        this.defaultValue = defaultValue;
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
        if (!Arrays.equals(parameters, methodInternal.parameters)) {
            return false;
        }
        if (receiverType != null ? !receiverType.equals(methodInternal.receiverType) : methodInternal.receiverType != null) {
            return false;
        }
        if (!returnType.equals(methodInternal.returnType)) {
            return false;
        }
        return Arrays.equals(typeParameters, methodInternal.typeParameters);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(name);
        result = 31 * result + Arrays.hashCode(parameters);
        result = 31 * result + returnType.hashCode();
        result = 31 * result + Arrays.hashCode(exceptions);
        result = 31 * result + (receiverType != null ? receiverType.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(typeParameters);
        result = 31 * result + Arrays.hashCode(annotations);
        result = 31 * result + (int) flags;
        return result;
    }

    final String name() {
        return Utils.fromUTF8(name);
    }

    final byte[] nameBytes() {
        return name;
    }

    final Type[] copyParameters() {
        return parameters.clone();
    }

    final Type[] parameterArray() {
        return parameters;
    }

    final Type[] copyExceptions() {
        return exceptions.clone();
    }

    final List<Type> parameters() {
        return Collections.unmodifiableList(Arrays.asList(parameters));
    }

    final Type returnType() {
        return returnType;
    }

    final Type receiverType(ClassInfo clazz) {
        return receiverType != null ? receiverType : new ClassType(clazz.name());
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
        AnnotationInstance key = new AnnotationInstance(name, null, null);
        int i = Arrays.binarySearch(annotations, key, AnnotationInstance.NAME_COMPARATOR);
        return i >= 0 ? annotations[i] : null;
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

    public String toString() {
        StringBuilder builder = new StringBuilder();
        String name = name();
        builder.append(returnType).append(' ').append(name).append('(');
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

    void setTypeParameters(Type[] typeParameters) {
        if (typeParameters.length > 0) {
            this.typeParameters = typeParameters;
        }
    }

    void setParameters(Type[] parameters) {
        this.parameters = parameters.length == 0 ? Type.EMPTY_ARRAY : parameters;
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
