package org.jboss.jandex;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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
    private static final DotName[] NO_PARAMS = new DotName[0];

    private static final Map<MethodInfo, MethodSignatureKey> JAVA_METHOD_SIGNATURES = new ConcurrentHashMap<>();

    private static final MethodSignatureKey INIT_NO_ARGS = new MethodSignatureKey(Utils.INIT_METHOD_NAME,
            NO_PARAMS, VoidType.VOID.name());
    private static final MethodSignatureKey TO_STRING = new MethodSignatureKey(Utils.TO_STRING_METHOD_NAME,
            NO_PARAMS, DotName.STRING_NAME);
    private static final MethodSignatureKey EQUALS = new MethodSignatureKey(Utils.EQUALS_METHOD_NAME,
            new DotName[] { DotName.OBJECT_NAME }, PrimitiveType.BOOLEAN.name());
    private static final MethodSignatureKey HASH_CODE = new MethodSignatureKey(Utils.HASH_CODE_METHOD_NAME,
            NO_PARAMS, PrimitiveType.INT.name());

    private final byte[] name;
    private final DotName[] parameterTypes;
    private final DotName returnType;
    private final int hashCode;

    private static final Function<MethodInfo, MethodSignatureKey> CREATE_METHOD_SIGNATURE = new Function<MethodInfo, MethodSignatureKey>() {
        @Override
        public MethodSignatureKey apply(MethodInfo method) {
            MethodInternal internal = method.methodInternal();

            MethodSignatureKey staticEntry = getStaticEntry(internal);
            if (staticEntry != null) {
                return staticEntry;
            }

            Type[] arr = internal.parameterTypesArray();
            DotName[] paramTypes;
            if (arr.length > 0) {
                paramTypes = new DotName[arr.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    paramTypes[i] = arr[i].name();
                }
            } else {
                paramTypes = NO_PARAMS;
            }

            return new MethodSignatureKey(internal.nameBytes(), paramTypes, internal.returnType().name());
        }
    };

    static MethodSignatureKey of(MethodInfo method) {
        if (method.declaringClass().name().startsWithJava()) {
            return JAVA_METHOD_SIGNATURES.computeIfAbsent(method, CREATE_METHOD_SIGNATURE);
        }
        return CREATE_METHOD_SIGNATURE.apply(method);
    }

    private static MethodSignatureKey getStaticEntry(MethodInternal internal) {
        byte[] name = internal.nameBytes();
        DotName returnType = internal.returnType().name();
        Type[] parameterTypes = internal.parameterTypesArray();

        if (parameterTypes.length == 0) {
            if (Arrays.equals(INIT_NO_ARGS.name, name)
                    && INIT_NO_ARGS.returnType.equals(returnType)) {
                return INIT_NO_ARGS;
            }
            if (Arrays.equals(TO_STRING.name, name)
                    && TO_STRING.returnType.equals(returnType)) {
                return TO_STRING;
            }
            if (Arrays.equals(HASH_CODE.name, name)
                    && HASH_CODE.returnType.equals(returnType)) {
                return HASH_CODE;
            }
        }

        if (parameterTypes.length == 1) {
            if (Arrays.equals(EQUALS.name, name)
                    && EQUALS.returnType.equals(returnType)
                    && EQUALS.parameterTypes[0].equals(parameterTypes[0].name())) {
                return EQUALS;
            }
        }

        return null;
    }

    private MethodSignatureKey(byte[] name, DotName[] parameterTypes, DotName returnType) {
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.hashCode = computeHashCode(this.name, parameterTypes, returnType);
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
                && Arrays.equals(parameterTypes, that.parameterTypes)
                && returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
