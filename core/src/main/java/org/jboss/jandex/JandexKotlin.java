package org.jboss.jandex;

/**
 * Utilities to simplify handling Kotlin classes with Jandex.
 */
public class JandexKotlin {
    /**
     * The name of {@code kotlin.Metadata}. This is an annotation that is present on all
     * classes emitted by the Kotlin compiler.
     */
    public static final DotName METADATA = DotName.createSimple("kotlin.Metadata");

    /**
     * The name of {@code kotlin.Unit}. This is a unit type (type that is inhabited by
     * exactly one value). When used as a return type, the Kotlin compiler usually emits
     * {@code void} for compatibility with Java, but if a Kotlin {@code suspend} function
     * has a return type of {@code Unit}, the type is retained in the {@link #CONTINUATION}
     * parameter.
     */
    public static final DotName UNIT = DotName.createSimple("kotlin.Unit");

    /**
     * The name of {@code kotlin.coroutines.Continuation}. A synthetic parameter of this type
     * is added by the Kotlin compiler to generated {@code suspend} functions and may be used
     * to signal completion. A {@code suspend} function may complete synchronously, in which case
     * completion is signaled directly by the return value, or asynchronously, in which case
     * the return value is the {@code COROUTINE_SUSPENDED} sentinel and completion is signaled
     * through the continuation argument.
     */
    public static final DotName CONTINUATION = DotName.createSimple("kotlin.coroutines.Continuation");

    /**
     * Returns whether the given {@code clazz} is a Kotlin class.
     *
     * @param clazz the class to inspect, must not be {@code null}
     * @return whether the given {@code clazz} is a Kotlin class
     */
    public static boolean isKotlinClass(ClassInfo clazz) {
        return clazz.hasDeclaredAnnotation(METADATA);
    }

    /**
     * Returns whether the given {@code method} is a Kotlin function.
     *
     * @param method the method to inspect, must not be {@code null}
     * @return whether the given {@code method} is a Kotlin function
     */
    public static boolean isKotlinMethod(MethodInfo method) {
        return isKotlinClass(method.declaringClass());
    }

    /**
     * Returns whether the given {@code method} is a Kotlin {@code suspend} function.
     *
     * @param method the method to inspect, must not be {@code null}
     * @return whether the given {@code method} is a Kotlin {@code suspend} function
     */
    public static boolean isKotlinSuspendMethod(MethodInfo method) {
        if (!isKotlinMethod(method)) {
            return false;
        }
        if (method.parametersCount() == 0) {
            return false;
        }

        Type lastParameter = method.parameterType(method.parametersCount() - 1);
        return CONTINUATION.equals(lastParameter.name());
    }

    /**
     * Returns whether the given {@code parameter} is a {@link #CONTINUATION} parameter
     * of a Kotlin {@code suspend} function.
     *
     * @param parameter the method parameter to inspect, must not be {@code null}
     * @return whether the given {@code parameter} is a {@link #CONTINUATION} parameter
     *         of a Kotlin {@code suspend} function
     */
    public static boolean isKotlinContinuationParameter(MethodParameterInfo parameter) {
        return isKotlinSuspendMethod(parameter.method()) && CONTINUATION.equals(parameter.type().name());
    }

    /**
     * Returns the return type of the given Kotlin {@code suspend} function.
     * If the given {@code method} is not a Kotlin {@code suspend} function or if
     * the {@link #CONTINUATION} parameter has an unexpected type signature,
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param method the Kotlin {@code suspend} function, must not be {@code null}
     * @return the return type of the given Kotlin {@code suspend} function
     */
    public static Type getKotlinSuspendMethodResult(MethodInfo method) {
        if (!isKotlinSuspendMethod(method)) {
            throw new IllegalArgumentException("Not a suspend function: " + method);
        }

        Type lastParameter = method.parameterType(method.parametersCount() - 1);
        if (lastParameter.kind() != Type.Kind.PARAMETERIZED_TYPE) {
            throw new IllegalArgumentException("Continuation parameter type not parameterized: " + lastParameter);
        }
        Type resultType = lastParameter.asParameterizedType().arguments().get(0);
        if (resultType.kind() != Type.Kind.WILDCARD_TYPE) {
            throw new IllegalArgumentException("Continuation parameter type argument not wildcard: " + resultType);
        }
        Type lowerBound = resultType.asWildcardType().superBound();
        if (lowerBound == null) {
            throw new IllegalArgumentException("Continuation parameter type argument without lower bound: " + resultType);
        }
        return lowerBound;
    }
}
