package org.jboss.jandex;

import java.util.Arrays;

/**
 * Opaque token that stands in for a method and implements equality and hash code based on
 * a method signature. Method signature includes its name, parameter types and return type.
 * Everything else is ignored, including the declaring class, type parameters, thrown types,
 * visibility, etc.
 * <p>
 * Note that method signature keys are <em>not</em> sufficient to detect whether some
 * method overrides another per the Java Language Specification rules.
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe
 * publication.
 */
public final class MethodSignatureKey {
    private final byte[] name;
    private final DotName[] paramTypes;
    private final DotName returnType;
    private final int hashCode;

    MethodSignatureKey(MethodInfo method) {
        MethodInternal internal = method.methodInternal();
        this.name = internal.nameBytes();
        Type[] arr = internal.parameterTypesArray();
        DotName[] paramTypes = new DotName[arr.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = arr[i].name();
        }
        this.paramTypes = paramTypes;
        this.returnType = internal.returnType().name();
        this.hashCode = computeHashCode(name, paramTypes, returnType);
    }

    private static int computeHashCode(byte[] name, DotName[] paramTypes, DotName returnType) {
        int result = Arrays.hashCode(name);
        result = 31 * result + Arrays.hashCode(paramTypes);
        result = 31 * result + returnType.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodSignatureKey)) {
            return false;
        }
        MethodSignatureKey that = (MethodSignatureKey) o;
        return Arrays.equals(name, that.name)
                && Arrays.equals(paramTypes, that.paramTypes)
                && returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
