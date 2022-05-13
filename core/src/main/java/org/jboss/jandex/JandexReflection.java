package org.jboss.jandex;

import java.lang.reflect.Array;

/**
 * Utilities that allow moving from the Jandex world to the runtime world using reflection.
 * To maintain stratification, these methods are intentionally <em>not</em> present
 * on the respective Jandex classes.
 */
public class JandexReflection {
    /**
     * Loads a class corresponding to the raw type of given {@link Type} from the thread context classloader.
     * If there is no TCCL, the classloader that loaded {@code JandexReflection} is used.
     * Returns {@code null} when {@code type} is {@code null}.
     * <p>
     * Specifically:
     * <ul>
     * <li>for the {@link VoidType void} pseudo-type, returns {@code void.class};</li>
     * <li>for {@linkplain PrimitiveType primitive} types, returns the corresponding class object (e.g. {@code int.class});</li>
     * <li>for {@linkplain ClassType class} types, returns the corresponding class object (e.g. {@code String.class});</li>
     * <li>for {@linkplain ArrayType array} types, returns the corresponding class object (e.g. {@code String[][].class});</li>
     * <li>for {@linkplain ParameterizedType parameterized} types, returns the class object of the generic class
     * (e.g. {@code List.class} for {@code List<String>});</li>
     * <li>for {@linkplain WildcardType wildcard} types, returns the class object of the upper bound type
     * (e.g. {@code Number.class} for {@code ? extends Number}), or {@code Object.class} if the wildcard type
     * has no upper bound (e.g. {@code ? super Integer});</li>
     * <li>for {@linkplain TypeVariable type variables}, returns the class object of the first bound
     * (e.g. {@code Number.class} for {@code T extends Number & Comparable<T>}), or {@code Object.class}
     * if the type variable has no bounds (e.g. just {@code T});</li>
     * <li>for {@linkplain UnresolvedTypeVariable unresolved type variables}, returns {@code Object.class}.</li>
     * </ul>
     *
     * @param type a Jandex {@link Type}
     * @return the corresponding {@link Class}
     */
    public static Class<?> loadRawType(Type type) {
        if (type == null) {
            return null;
        }

        switch (type.kind()) {
            case VOID:
                return void.class;
            case PRIMITIVE:
                switch (type.asPrimitiveType().primitive()) {
                    case BOOLEAN:
                        return boolean.class;
                    case BYTE:
                        return byte.class;
                    case SHORT:
                        return short.class;
                    case INT:
                        return int.class;
                    case LONG:
                        return long.class;
                    case FLOAT:
                        return float.class;
                    case DOUBLE:
                        return double.class;
                    case CHAR:
                        return char.class;
                    default:
                        throw new IllegalArgumentException("Unknown primitive type: " + type);
                }
            case CLASS:
                return load(type.asClassType().name());
            case PARAMETERIZED_TYPE:
                return load(type.asParameterizedType().name());
            case ARRAY:
                // this handles nested array types correctly, see Array.newInstance
                // it is unfortunate that we have to instantiate the array type,
                // but I don't know a better way
                Class<?> component = loadRawType(type.asArrayType().component());
                int dimensions = type.asArrayType().dimensions();
                return Array.newInstance(component, new int[dimensions]).getClass();
            case WILDCARD_TYPE:
                return loadRawType(type.asWildcardType().extendsBound());
            case TYPE_VARIABLE:
                return load(type.asTypeVariable().name());
            case UNRESOLVED_TYPE_VARIABLE:
                return Object.class; // can't do better here
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    /**
     * Loads a class corresponding to given {@link ClassInfo} from the thread context classloader.
     * If there is no TCCL, the classloader that loaded {@code JandexReflection} is used.
     * Returns {@code null} when {@code clazz} is {@code null}.
     *
     * @param clazz a Jandex {@link ClassInfo}
     * @return the corresponding {@link Class}
     */
    public static Class<?> loadClass(ClassInfo clazz) {
        if (clazz == null) {
            return null;
        }

        return load(clazz.name());
    }

    private static Class<?> load(DotName name) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = JandexReflection.class.getClassLoader();
            }
            return cl.loadClass(name.toString());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
