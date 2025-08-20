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

    private static final DotName[] NO_PARAMS = new DotName[0];

    private static final MethodSignatureKey INIT_NO_ARGS = new MethodSignatureKey(Utils.INIT_METHOD_NAME, NO_PARAMS,
            VoidType.VOID.name());
    private static final MethodSignatureKey TO_STRING = new MethodSignatureKey(Utils.TO_STRING_METHOD_NAME, NO_PARAMS,
            DotName.STRING_NAME);
    private static final MethodSignatureKey EQUALS = new MethodSignatureKey(Utils.EQUALS_METHOD_NAME,
            new DotName[] { DotName.OBJECT_NAME }, PrimitiveType.BOOLEAN.name());
    private static final MethodSignatureKey HASH_CODE = new MethodSignatureKey(Utils.HASH_CODE_METHOD_NAME, NO_PARAMS,
            PrimitiveType.INT.name());

    private final byte[] name;
    private final DotName[] paramTypes;
    private final DotName returnType;
    private final int hashCode;

    static MethodSignatureKey of(MethodInfo method) {
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

    private static MethodSignatureKey getStaticEntry(MethodInternal internal) {
        // we don't exect this to become much longer thus why we opted for this very verbose programming style
        if (internal.parameterTypesArray().length == 0) {
            if (Arrays.equals(INIT_NO_ARGS.name, internal.nameBytes())
                    && INIT_NO_ARGS.returnType.equals(internal.returnType().name())) {
                return INIT_NO_ARGS;
            }
            if (Arrays.equals(TO_STRING.name, internal.nameBytes())
                    && TO_STRING.returnType.equals(internal.returnType().name())) {
                return TO_STRING;
            }
            if (Arrays.equals(HASH_CODE.name, internal.nameBytes())
                    && HASH_CODE.returnType.equals(internal.returnType().name())) {
                return HASH_CODE;
            }
        }
        if (internal.parameterTypesArray().length == 1) {
            if (Arrays.equals(EQUALS.name, internal.nameBytes()) && EQUALS.returnType.equals(internal.returnType().name())
                    && EQUALS.paramTypes[0].equals(internal.parameterTypesArray()[0].name())) {
                return EQUALS;
            }
        }

        return null;
    }

    private MethodSignatureKey(byte[] name, DotName[] paramTypes, DotName returnType) {
        this.name = name;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.hashCode = computeHashCode(this.name, paramTypes, returnType);
    }

    private static int computeHashCode(byte[] name, DotName[] paramTypes, DotName returnType) {
        int result = Arrays.hashCode(name);
        result = 31 * result + Arrays.hashCode(paramTypes);
        result = 31 * result + returnType.hashCode();
        return result;
    }

    private static boolean matches(MethodSignatureKey methodSignatureKey, MethodInternal method) {
        if (method.parameterTypesArray().length == 0 && methodSignatureKey.paramTypes.length == 0 &&
                Arrays.equals(methodSignatureKey.name, method.nameBytes()) &&
                methodSignatureKey.returnType.equals(method.returnType().name())) {
            return true;
        }
        if (method.parameterTypesArray().length == 1 && methodSignatureKey.paramTypes.length == 1 &&
                Arrays.equals(methodSignatureKey.name, method.nameBytes())
                && methodSignatureKey.returnType.equals(method.returnType().name())
                && methodSignatureKey.paramTypes[0].equals(method.parameterTypesArray()[0].name())) {
            return true;
        }

        return false;
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
