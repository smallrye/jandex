package org.jboss.jandex;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Utilities that allow moving from the Jandex world to the runtime world using reflection.
 * To maintain stratification, these methods are intentionally <em>not</em> present
 * on the respective Jandex classes.
 */
public class JandexReflection {
    /**
     * An implementation of {@link java.lang.reflect.GenericArrayType} that is compatible with the JDK's implementation
     * (as in, the {@code equals()} and {@code hashCode()} methods work as expected).
     */
    private static class GenericArrayTypeImpl implements java.lang.reflect.GenericArrayType {
        private final java.lang.reflect.Type genericComponentType;

        GenericArrayTypeImpl(java.lang.reflect.Type genericComponentType) {
            this.genericComponentType = genericComponentType;
        }

        @Override
        public java.lang.reflect.Type getGenericComponentType() {
            return genericComponentType;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof java.lang.reflect.GenericArrayType) {
                java.lang.reflect.GenericArrayType that = (java.lang.reflect.GenericArrayType) obj;
                return Objects.equals(genericComponentType, that.getGenericComponentType());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(genericComponentType);
        }

        @Override
        public String toString() {
            return genericComponentType + "[]";
        }
    }

    /**
     * An implementation of {@link java.lang.reflect.ParameterizedType} that is compatible with the JDK's implementation
     * (as in, the {@code equals()} and {@code hashCode()} methods work as expected).
     */
    private static class ParameterizedTypeImpl implements java.lang.reflect.ParameterizedType {
        private final java.lang.reflect.Type ownerType;
        private final java.lang.reflect.Type rawType;
        private final java.lang.reflect.Type[] actualTypeArguments;

        ParameterizedTypeImpl(java.lang.reflect.Type rawType, java.lang.reflect.Type[] actualTypeArguments,
                java.lang.reflect.Type ownerType) {
            this.ownerType = ownerType;
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
        }

        @Override
        public java.lang.reflect.Type[] getActualTypeArguments() {
            return Arrays.copyOf(actualTypeArguments, actualTypeArguments.length);
        }

        @Override
        public java.lang.reflect.Type getOwnerType() {
            return ownerType;
        }

        @Override
        public java.lang.reflect.Type getRawType() {
            return rawType;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType that = (java.lang.reflect.ParameterizedType) obj;
                return Objects.equals(ownerType, that.getOwnerType())
                        && Objects.equals(rawType, that.getRawType())
                        && Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(actualTypeArguments)
                    ^ Objects.hashCode(ownerType)
                    ^ Objects.hashCode(rawType);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(rawType.getTypeName());
            StringJoiner joiner = new StringJoiner(", ", "<", ">");
            joiner.setEmptyValue("");
            for (java.lang.reflect.Type typeArgument : actualTypeArguments) {
                joiner.add(typeArgument.getTypeName());
            }
            result.append(joiner);
            return result.toString();
        }
    }

    /**
     * An implementation of {@link java.lang.reflect.TypeVariable} that is <strong>NOT</strong> compatible with
     * the JDK's implementation (as in, the {@code equals()} and {@code hashCode()} methods <em>do not</em> work
     * as expected).
     */
    private static class TypeVariableImpl<D extends java.lang.reflect.GenericDeclaration>
            implements java.lang.reflect.TypeVariable<D> {
        private final String name;
        private final java.lang.reflect.Type[] bounds;

        TypeVariableImpl(String name, java.lang.reflect.Type... bounds) {
            this.name = name;
            this.bounds = bounds;
        }

        @Override
        public java.lang.reflect.Type[] getBounds() {
            return Arrays.copyOf(bounds, bounds.length);
        }

        @Override
        public D getGenericDeclaration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public java.lang.reflect.AnnotatedType[] getAnnotatedBounds() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Annotation[] getAnnotations() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object obj) {
            // note that the JDK does not make it possible to implement a compatible `equals()`,
            // because it checks a specific implementation class in its `equals()`
            if (this == obj) {
                return true;
            } else if (obj instanceof java.lang.reflect.TypeVariable) {
                java.lang.reflect.TypeVariable<?> that = (java.lang.reflect.TypeVariable<?>) obj;
                return Objects.equals(name, that.getName()) && Arrays.equals(bounds, that.getBounds());
            }
            return false;
        }

        @Override
        public int hashCode() {
            // this `hashCode()` is not compatible with the JDK, but that doesn't really matter,
            // because it's not possible to implement a compatible `equals()` anyway
            int result = Objects.hashCode(name);
            result = 31 * result + Arrays.hashCode(bounds);
            return result;
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(" & ", " extends ", "");
            joiner.setEmptyValue("");
            for (java.lang.reflect.Type bound : bounds) {
                if (bound instanceof Class) {
                    joiner.add(((Class<?>) bound).getName());
                } else {
                    joiner.add(bound.toString());
                }
            }
            return name + joiner;
        }
    }

    /**
     * A delegating implementation of {@link java.lang.reflect.TypeVariable} that is <strong>NOT</strong> compatible
     * with the JDK's implementation (as in, the {@code equals()} and {@code hashCode()} methods <em>do not</em> work
     * as expected).
     * <p>
     * The delegate is expected to be set <em>after</em> construction using {@link #setDelegate(TypeVariableImpl)}.
     * It is useful to represent recursive type variables.
     */
    private static class TypeVariableReferenceImpl<D extends java.lang.reflect.GenericDeclaration>
            implements java.lang.reflect.TypeVariable<D> {
        private TypeVariableImpl<D> delegate;

        void setDelegate(TypeVariableImpl<D> delegate) {
            this.delegate = delegate;
        }

        @Override
        public java.lang.reflect.Type[] getBounds() {
            return delegate.getBounds();
        }

        @Override
        public D getGenericDeclaration() {
            return delegate.getGenericDeclaration();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public java.lang.reflect.AnnotatedType[] getAnnotatedBounds() {
            return delegate.getAnnotatedBounds();
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return delegate.getAnnotation(annotationClass);
        }

        @Override
        public Annotation[] getAnnotations() {
            return delegate.getAnnotations();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return delegate.getDeclaredAnnotations();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    /**
     * An implementation of {@link java.lang.reflect.WildcardType} that is compatible with the JDK's implementation
     * (as in, the {@code equals()} and {@code hashCode()} methods work as expected).
     */
    private static class WildcardTypeImpl implements java.lang.reflect.WildcardType {
        private static final java.lang.reflect.Type[] DEFAULT_UPPER_BOUND = new java.lang.reflect.Type[] { Object.class };
        private static final java.lang.reflect.Type[] DEFAULT_LOWER_BOUND = new java.lang.reflect.Type[0];
        private static final java.lang.reflect.WildcardType UNBOUNDED = new WildcardTypeImpl(DEFAULT_UPPER_BOUND,
                DEFAULT_LOWER_BOUND);

        static java.lang.reflect.WildcardType unbounded() {
            return UNBOUNDED;
        }

        static java.lang.reflect.WildcardType withUpperBound(java.lang.reflect.Type type) {
            return new WildcardTypeImpl(new java.lang.reflect.Type[] { type }, DEFAULT_LOWER_BOUND);
        }

        static java.lang.reflect.WildcardType withLowerBound(java.lang.reflect.Type type) {
            return new WildcardTypeImpl(DEFAULT_UPPER_BOUND, new java.lang.reflect.Type[] { type });
        }

        private final java.lang.reflect.Type[] upperBound;
        private final java.lang.reflect.Type[] lowerBound;

        private WildcardTypeImpl(java.lang.reflect.Type[] upperBound, java.lang.reflect.Type[] lowerBound) {
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
        }

        @Override
        public java.lang.reflect.Type[] getUpperBounds() {
            return upperBound;
        }

        @Override
        public java.lang.reflect.Type[] getLowerBounds() {
            return lowerBound;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof java.lang.reflect.WildcardType) {
                java.lang.reflect.WildcardType that = (java.lang.reflect.WildcardType) obj;
                return Arrays.equals(lowerBound, that.getLowerBounds())
                        && Arrays.equals(upperBound, that.getUpperBounds());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(lowerBound) ^ Arrays.hashCode(upperBound);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("?");
            if (upperBound.length > 0 && !upperBound[0].equals(Object.class)) {
                result.append(" extends ").append(upperBound[0]);
            } else if (lowerBound.length > 0) {
                result.append(" super ").append(lowerBound[0]);
            }
            return result.toString();
        }
    }

    private static class TypeVariables {
        private Map<String, TypeVariableImpl<?>> typeVariables;
        private Map<String, TypeVariableReferenceImpl<?>> typeVariableReferences;

        TypeVariableImpl<?> getTypeVariable(String identifier) {
            return typeVariables != null ? typeVariables.get(identifier) : null;
        }

        void setTypeVariable(String identifier, TypeVariableImpl<?> typeVariable) {
            if (typeVariables == null) {
                typeVariables = new HashMap<>();
            }
            typeVariables.put(identifier, typeVariable);
        }

        TypeVariableReferenceImpl<?> getTypeVariableReference(String identifier) {
            return typeVariableReferences != null ? typeVariableReferences.get(identifier) : null;
        }

        void setTypeVariableReference(String identifier, TypeVariableReferenceImpl<?> typeVariableReference) {
            if (typeVariableReferences == null) {
                typeVariableReferences = new HashMap<>();
            }
            typeVariableReferences.put(identifier, typeVariableReference);
        }

        void patchReferences() {
            if (typeVariableReferences == null) {
                return;
            }
            typeVariableReferences.forEach((identifier, reference) -> {
                TypeVariableImpl<?> typeVar = typeVariables.get(identifier);
                if (typeVar != null) {
                    reference.setDelegate((TypeVariableImpl) typeVar);
                }
            });
        }
    }

    /**
     * Loads a Reflection {@link java.lang.reflect.Type Type} corresponding to the given Jandex {@link Type}.
     * Classes are loaded from the thread context classloader. If there is no TCCL, the classloader
     * that loaded {@code JandexReflection} is used. Returns {@code null} when {@code type} is {@code null}.
     * <p>
     * The result is {@linkplain Object#equals(Object) equal} to the corresponding {@code Type} obtained
     * from Reflection and has the same {@linkplain Object#hashCode() hash code}, as long as it doesn't
     * contain any type variables.
     *
     * @param type a Jandex {@link Type}
     * @return the corresponding Reflection {@link java.lang.reflect.Type Type}
     */
    public static java.lang.reflect.Type loadType(Type type) {
        TypeVariables typeVariables = new TypeVariables();
        java.lang.reflect.Type result = loadType(type, typeVariables);
        typeVariables.patchReferences();
        return result;
    }

    private static java.lang.reflect.Type loadType(Type type, TypeVariables typeVariables) {
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
                return load(type.name());
            case PARAMETERIZED_TYPE:
                java.lang.reflect.Type ownerType = null;
                if (type.asParameterizedType().owner() != null) {
                    // the `owner()` does not always correspond to the JDK's owner,
                    // but there's nothing we can do about that here
                    ownerType = loadType(type.asParameterizedType().owner(), typeVariables);
                }
                java.lang.reflect.Type rawType = load(type.name());
                Type[] typeArguments = type.asParameterizedType().argumentsArray();
                java.lang.reflect.Type[] typeArgumentsReflective = new java.lang.reflect.Type[typeArguments.length];
                for (int i = 0; i < typeArguments.length; i++) {
                    typeArgumentsReflective[i] = loadType(typeArguments[i], typeVariables);
                }
                return new ParameterizedTypeImpl(rawType, typeArgumentsReflective, ownerType);
            case ARRAY:
                Type.Kind elementTypeKind = type.asArrayType().elementType().kind();
                if (elementTypeKind == Type.Kind.PRIMITIVE || elementTypeKind == Type.Kind.CLASS) {
                    return load(type.name());
                }
                java.lang.reflect.Type componentType = loadType(type.asArrayType().componentType(), typeVariables);
                return new GenericArrayTypeImpl(componentType);
            case WILDCARD_TYPE:
                if (type.asWildcardType().hasImplicitObjectBound()) {
                    return WildcardTypeImpl.unbounded();
                }
                java.lang.reflect.Type bound = loadType(type.asWildcardType().bound(), typeVariables);
                if (type.asWildcardType().isExtends()) {
                    return WildcardTypeImpl.withUpperBound(bound);
                } else {
                    return WildcardTypeImpl.withLowerBound(bound);
                }
            case TYPE_VARIABLE:
                String tvIdentifier = type.asTypeVariable().identifier();
                TypeVariableImpl<?> typeVariable = typeVariables.getTypeVariable(tvIdentifier);
                if (typeVariable == null) {
                    Type[] bounds = type.asTypeVariable().boundArray();
                    java.lang.reflect.Type[] boundsReflective = new java.lang.reflect.Type[bounds.length];
                    for (int i = 0; i < bounds.length; i++) {
                        boundsReflective[i] = loadType(bounds[i], typeVariables);
                    }
                    typeVariable = new TypeVariableImpl<>(tvIdentifier, boundsReflective);
                    typeVariables.setTypeVariable(tvIdentifier, typeVariable);
                }
                return typeVariable;
            case TYPE_VARIABLE_REFERENCE:
                String tvrIdentifier = type.asTypeVariableReference().identifier();
                TypeVariableReferenceImpl<?> typeVariableReference = typeVariables.getTypeVariableReference(tvrIdentifier);
                if (typeVariableReference == null) {
                    typeVariableReference = new TypeVariableReferenceImpl<>();
                    typeVariables.setTypeVariableReference(tvrIdentifier, typeVariableReference);
                }
                return typeVariableReference;
            case UNRESOLVED_TYPE_VARIABLE:
                String utvIdentifier = type.asUnresolvedTypeVariable().identifier();
                TypeVariableImpl<?> unresolvedTypeVariable = typeVariables.getTypeVariable(utvIdentifier);
                if (unresolvedTypeVariable == null) {
                    unresolvedTypeVariable = new TypeVariableImpl<>(utvIdentifier);
                    typeVariables.setTypeVariable(utvIdentifier, unresolvedTypeVariable);
                }
                return unresolvedTypeVariable;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

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
     * <li>for {@linkplain TypeVariableReference type variables references}, follows the reference to obtain the type
     * variable and then returns the class object of the first bound (e.g. {@code Number.class} for
     * {@code T extends Number & Comparable<T>}), or {@code Object.class} if the type variable has no bounds
     * (e.g. just {@code T});</li>
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
            case PARAMETERIZED_TYPE:
            case ARRAY:
            case WILDCARD_TYPE:
            case TYPE_VARIABLE:
            case TYPE_VARIABLE_REFERENCE:
                return load(type.name());
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
            return Class.forName(name.toString(), false, cl);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
