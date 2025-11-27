package org.jboss.jandex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

import org.jboss.jandex.PrimitiveType.Primitive;

/**
 * Establishes a notion of <em>equivalence</em> of Jandex objects. Two Jandex objects are equivalent if and only if
 * they denote the same Java declaration or type, without taking into account any annotations. The prime use case
 * is to assist with building annotation overlays on top of Jandex, where it is common to have multiple Jandex
 * objects with different annotations, but otherwise equivalent.
 * <p>
 * In contrast, the common Jandex classes such as {@link ClassInfo}, {@link MethodInfo} or {@link FieldInfo}
 * either don't provide equality at all (and hence can only be compared by identity) or provide strict equality,
 * which includes presence or absence of annotations and comparison of their members, as well as other details.
 * <p>
 * An instance of this class, also called an <em>equivalence key</em>, provides 3 fundamental operations,
 * corresponding to the 3 common methods all Java classes have:
 * <ul>
 * <li>{@code equals()}: when two Jandex objects are equivalent, their equivalence keys are equal</li>
 * <li>{@code hashCode()}: consistent with {@code equals()} described above</li>
 * <li>{@code toString()}: human-readable representation of the equivalence key;
 * format of the value is not guaranteed and may change without notice</li>
 * </ul>
 * In addition, equivalence keys are structured in an inheritance hierarchy that corresponds
 * to the inheritance hierarchy of Jandex objects. Therefore, the kind of the "source" Jandex object may be found
 * by inspecting the class of the equivalence key:
 * <ul>
 * <li>{@code EquivalenceKey}
 * <ul>
 * <li>{@code DeclarationEquivalenceKey}
 * <ul>
 * <li>{@code ClassEquivalenceKey}</li>
 * <li>{@code FieldEquivalenceKey}</li>
 * <li>{@code MethodEquivalenceKey}</li>
 * <li>{@code MethodParameterEquivalenceKey}</li>
 * <li>{@code RecordComponentEquivalenceKey}</li>
 * </ul>
 * </li>
 * <li>{@code TypeEquivalenceKey}
 * <ul>
 * <li>{@code ArrayTypeEquivalenceKey}</li>
 * <li>{@code ClassTypeEquivalenceKey}</li>
 * <li>{@code ParameterizedTypeEquivalenceKey}</li>
 * <li>{@code PrimitiveTypeEquivalenceKey}</li>
 * <li>{@code TypeVariableEquivalenceKey}</li>
 * <li>{@code TypeVariableReferenceEquivalenceKey}</li>
 * <li>{@code UnresolvedTypeVariableEquivalenceKey}</li>
 * <li>{@code VoidTypeEquivalenceKey}</li>
 * <li>{@code WildcardTypeEquivalenceKey}</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public abstract class EquivalenceKey {
    /**
     * Returns an equivalence key for given {@linkplain AnnotationTarget annotation target}.
     *
     * @param annotationTarget the annotation target, may be {@code null}
     * @return equivalence key for given annotation target, only {@code null} if {@code annotationTarget == null}
     */
    public static EquivalenceKey of(AnnotationTarget annotationTarget) {
        if (annotationTarget == null) {
            return null;
        }

        if (annotationTarget.isDeclaration()) {
            return of(annotationTarget.asDeclaration());
        }
        if (annotationTarget.kind() == AnnotationTarget.Kind.TYPE) {
            return of(annotationTarget.asType());
        }

        throw new IllegalArgumentException("Unknown annotation target: " + annotationTarget);
    }

    /**
     * Returns an equivalence key for given {@linkplain Declaration declaration}.
     *
     * @param declaration the declaration, may be {@code null}
     * @return equivalence key for given declaration, only {@code null} if {@code declaration == null}
     * @since 3.1.0
     */
    public static DeclarationEquivalenceKey of(Declaration declaration) {
        if (declaration == null) {
            return null;
        }

        switch (declaration.kind()) {
            case CLASS:
                return of(declaration.asClass());
            case METHOD:
                return of(declaration.asMethod());
            case METHOD_PARAMETER:
                return of(declaration.asMethodParameter());
            case FIELD:
                return of(declaration.asField());
            case RECORD_COMPONENT:
                return of(declaration.asRecordComponent());
            default:
                throw new IllegalArgumentException("Unknown declaration: " + declaration);
        }
    }

    /**
     * Returns an equivalence key for given {@linkplain ClassInfo class}.
     *
     * @param clazz the class, may be {@code null}
     * @return equivalence key for given class, only {@code null} if {@code clazz == null}
     */
    public static ClassEquivalenceKey of(ClassInfo clazz) {
        if (clazz == null) {
            return null;
        }
        return new ClassEquivalenceKey(clazz.name());
    }

    /**
     * Returns an equivalence key for given {@linkplain MethodInfo method}.
     *
     * @param method the method, may be {@code null}
     * @return equivalence key for given method, only {@code null} if {@code method == null}
     */
    public static MethodEquivalenceKey of(MethodInfo method) {
        if (method == null) {
            return null;
        }
        return new MethodEquivalenceKey(method.declaringClass().name(), method.methodInternal().nameBytes(),
                of(method.methodInternal().parameterTypesArray()), of(method.returnType()));
    }

    /**
     * Returns an equivalence key for given {@linkplain MethodParameterInfo method parameter}.
     *
     * @param parameter the method parameter, may be {@code null}
     * @return equivalence key for given method parameter, only {@code null} if {@code parameter == null}
     */
    public static MethodParameterEquivalenceKey of(MethodParameterInfo parameter) {
        if (parameter == null) {
            return null;
        }
        return new MethodParameterEquivalenceKey(of(parameter.method()), parameter.position());
    }

    /**
     * Returns an equivalence key for given {@linkplain FieldInfo field}.
     *
     * @param field the field, may be {@code null}
     * @return equivalence key for given field, only {@code null} if {@code field == null}
     */
    public static FieldEquivalenceKey of(FieldInfo field) {
        if (field == null) {
            return null;
        }
        return new FieldEquivalenceKey(field.declaringClass().name(), field.fieldInternal().nameBytes(), of(field.type()));
    }

    /**
     * Returns an equivalence key for given {@linkplain RecordComponentInfo record component}.
     *
     * @param recordComponent the record component, may be {@code null}
     * @return equivalence key for given record component, only {@code null} if {@code recordComponent == null}
     */
    public static RecordComponentEquivalenceKey of(RecordComponentInfo recordComponent) {
        if (recordComponent == null) {
            return null;
        }
        return new RecordComponentEquivalenceKey(recordComponent.declaringClass().name(),
                recordComponent.recordComponentInternal().nameBytes(), of(recordComponent.type()));
    }

    /**
     * Returns an equivalence key for given {@linkplain TypeTarget type annotation target}.
     * It is the equivalence key of the annotated type.
     *
     * @param typeTarget the type target, may be {@code null}
     * @return equivalence key for given type target, only {@code null} if {@code typeTarget == null}
     */
    public static TypeEquivalenceKey of(TypeTarget typeTarget) {
        if (typeTarget == null) {
            return null;
        }
        return of(typeTarget.target());
    }

    /**
     * Returns an equivalence key for given {@linkplain Type type}.
     *
     * @param type the type, may be {@code null}
     * @return equivalence key for given type, only {@code null} if {@code type == null}
     */
    public static TypeEquivalenceKey of(Type type) {
        if (type == null) {
            return null;
        }

        switch (type.kind()) {
            case ARRAY:
                return new ArrayTypeEquivalenceKey(of(type.asArrayType().constituent()),
                        type.asArrayType().dimensions());
            case CLASS:
                return ClassTypeEquivalenceKey.of(type.asClassType().name());
            case PARAMETERIZED_TYPE:
                return new ParameterizedTypeEquivalenceKey(type.asParameterizedType().name(),
                        of(type.asParameterizedType().argumentsArray()));
            case PRIMITIVE:
                return PrimitiveTypeEquivalenceKey.of(type.asPrimitiveType().primitive());
            case TYPE_VARIABLE:
                return new TypeVariableEquivalenceKey(type.asTypeVariable().identifier(),
                        of(type.asTypeVariable().boundArray()));
            case TYPE_VARIABLE_REFERENCE:
                return new TypeVariableReferenceEquivalenceKey(type.asTypeVariableReference().identifier());
            case UNRESOLVED_TYPE_VARIABLE:
                return new UnresolvedTypeVariableEquivalenceKey(type.asUnresolvedTypeVariable().identifier());
            case VOID:
                return VoidTypeEquivalenceKey.SINGLETON;
            case WILDCARD_TYPE:
                return new WildcardTypeEquivalenceKey(of(type.asWildcardType().bound()), type.asWildcardType().isExtends(),
                        type.asWildcardType().hasImplicitObjectBound());
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private static TypeEquivalenceKey[] of(Type[] types) {
        TypeEquivalenceKey[] result = new TypeEquivalenceKey[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = of(types[i]);
        }
        return result;
    }

    // ---

    private EquivalenceKey() {
    }

    String toStringWithWhere(Set<TypeVariableEquivalenceKey> typeVariables) {
        return this.toString();
    }

    private static String toStringWhereClause(Set<TypeVariableEquivalenceKey> typeVariables) {
        if (typeVariables == null || typeVariables.isEmpty()) {
            return "";
        }

        StringJoiner typeVariablesJoiner = new StringJoiner(", ");
        for (TypeVariableEquivalenceKey typeVariable : typeVariables) {
            typeVariablesJoiner.add(typeVariable.toString());
        }
        return " where " + typeVariablesJoiner;
    }

    // ---

    public static abstract class DeclarationEquivalenceKey extends EquivalenceKey {
        private DeclarationEquivalenceKey() {
        }
    }

    public static final class ClassEquivalenceKey extends DeclarationEquivalenceKey {
        private final DotName className;

        private ClassEquivalenceKey(DotName className) {
            this.className = className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ClassEquivalenceKey))
                return false;
            ClassEquivalenceKey that = (ClassEquivalenceKey) o;
            return className.equals(that.className);
        }

        @Override
        public int hashCode() {
            return className.hashCode();
        }

        @Override
        public String toString() {
            return "class " + className.toString();
        }
    }

    public static final class MethodEquivalenceKey extends DeclarationEquivalenceKey {
        private final DotName className;
        private final byte[] methodName;
        private final TypeEquivalenceKey[] parameterTypes;
        private final TypeEquivalenceKey returnType; // needed e.g. to distinguish bridge methods from "real" methods

        private MethodEquivalenceKey(DotName className, byte[] methodName, TypeEquivalenceKey[] parameterTypes,
                TypeEquivalenceKey returnType) {
            this.className = className;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.returnType = returnType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof MethodEquivalenceKey))
                return false;
            MethodEquivalenceKey that = (MethodEquivalenceKey) o;
            return className.equals(that.className)
                    && returnType.equals(that.returnType)
                    && Arrays.equals(methodName, that.methodName)
                    && Arrays.equals(parameterTypes, that.parameterTypes);
        }

        @Override
        public int hashCode() {
            int result = className.hashCode();
            result = 31 * result + returnType.hashCode();
            result = 31 * result + Arrays.hashCode(methodName);
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }

        @Override
        public String toString() {
            Set<TypeVariableEquivalenceKey> typeVariables = new HashSet<>();
            StringJoiner parameterTypesJoiner = new StringJoiner(", ", "(", ")");
            for (TypeEquivalenceKey parameterType : parameterTypes) {
                parameterTypesJoiner.add(parameterType.toStringWithWhere(typeVariables));
            }
            return "method " + className + "#" + Utils.fromUTF8(methodName) + parameterTypesJoiner + " -> "
                    + returnType.toStringWithWhere(typeVariables) + toStringWhereClause(typeVariables);
        }
    }

    public static final class MethodParameterEquivalenceKey extends DeclarationEquivalenceKey {
        private final MethodEquivalenceKey method;
        private final short position;

        private MethodParameterEquivalenceKey(MethodEquivalenceKey method, short position) {
            this.method = method;
            this.position = position;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof MethodParameterEquivalenceKey))
                return false;
            MethodParameterEquivalenceKey that = (MethodParameterEquivalenceKey) o;
            return method.equals(that.method) && position == that.position;
        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            result = 31 * result + position;
            return result;
        }

        @Override
        public String toString() {
            return "parameter " + position + " of " + method;
        }
    }

    public static final class FieldEquivalenceKey extends DeclarationEquivalenceKey {
        private final DotName className;
        private final byte[] fieldName;
        private final TypeEquivalenceKey type;

        private FieldEquivalenceKey(DotName className, byte[] fieldName, TypeEquivalenceKey type) {
            this.className = className;
            this.fieldName = fieldName;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof FieldEquivalenceKey))
                return false;
            FieldEquivalenceKey that = (FieldEquivalenceKey) o;
            return className.equals(that.className)
                    && type.equals(that.type)
                    && Arrays.equals(fieldName, that.fieldName);
        }

        @Override
        public int hashCode() {
            int result = className.hashCode();
            result = 31 * result + type.hashCode();
            result = 31 * result + Arrays.hashCode(fieldName);
            return result;
        }

        @Override
        public String toString() {
            Set<TypeVariableEquivalenceKey> typeVariables = new HashSet<>();
            return "field " + className + "#" + Utils.fromUTF8(fieldName) + " of type "
                    + type.toStringWithWhere(typeVariables) + toStringWhereClause(typeVariables);
        }
    }

    public static final class RecordComponentEquivalenceKey extends DeclarationEquivalenceKey {
        private final DotName className;
        private final byte[] recordComponentName;
        private final TypeEquivalenceKey type;

        private RecordComponentEquivalenceKey(DotName className, byte[] recordComponentName, TypeEquivalenceKey type) {
            this.className = className;
            this.recordComponentName = recordComponentName;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof RecordComponentEquivalenceKey))
                return false;
            RecordComponentEquivalenceKey that = (RecordComponentEquivalenceKey) o;
            return className.equals(that.className)
                    && type.equals(that.type)
                    && Arrays.equals(recordComponentName, that.recordComponentName);
        }

        @Override
        public int hashCode() {
            int result = className.hashCode();
            result = 31 * result + type.hashCode();
            result = 31 * result + Arrays.hashCode(recordComponentName);
            return result;
        }

        @Override
        public String toString() {
            Set<TypeVariableEquivalenceKey> typeVariables = new HashSet<>();
            return "record component " + className + "#" + Utils.fromUTF8(recordComponentName) + " of type "
                    + type.toStringWithWhere(typeVariables) + toStringWhereClause(typeVariables);
        }
    }

    public static abstract class TypeEquivalenceKey extends EquivalenceKey {
        private TypeEquivalenceKey() {
        }
    }

    public static final class ArrayTypeEquivalenceKey extends TypeEquivalenceKey {
        private final TypeEquivalenceKey constituent;
        private final int dimensions;

        private ArrayTypeEquivalenceKey(TypeEquivalenceKey constituent, int dimensions) {
            this.constituent = constituent;
            this.dimensions = dimensions;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ArrayTypeEquivalenceKey))
                return false;
            ArrayTypeEquivalenceKey that = (ArrayTypeEquivalenceKey) o;
            return dimensions == that.dimensions && constituent.equals(that.constituent);
        }

        @Override
        public int hashCode() {
            int result = constituent.hashCode();
            result = 31 * result + dimensions;
            return result;
        }

        @Override
        public String toString() {
            Set<TypeVariableEquivalenceKey> typeVariables = new HashSet<>();
            StringBuilder result = new StringBuilder();
            result.append(constituent.toStringWithWhere(typeVariables));
            for (int i = 0; i < dimensions; i++) {
                result.append("[]");
            }
            return result + toStringWhereClause(typeVariables);
        }

        @Override
        String toStringWithWhere(Set<TypeVariableEquivalenceKey> typeVariables) {
            StringBuilder result = new StringBuilder();
            result.append(constituent.toStringWithWhere(typeVariables));
            for (int i = 0; i < dimensions; i++) {
                result.append("[]");
            }
            return result.toString();
        }
    }

    public static final class ClassTypeEquivalenceKey extends TypeEquivalenceKey {
        // `CACHE_SIZE` must be power of 2

        private static final int JAVA_CACHE_SIZE = 128;
        private static final int JAVA_CACHE_MASK = JAVA_CACHE_SIZE - 1;
        private static final ClassTypeEquivalenceKey[] JAVA_CACHE = new ClassTypeEquivalenceKey[JAVA_CACHE_SIZE];

        private static final int OTHER_CACHE_SIZE = 1024;
        private static final int OTHER_CACHE_MASK = OTHER_CACHE_SIZE - 1;
        private static final ClassTypeEquivalenceKey[] OTHER_CACHE = new ClassTypeEquivalenceKey[OTHER_CACHE_SIZE];

        private final DotName name;

        // this is thread-safe even without synchronization or volatile access
        // due to `ClassTypeEquivalenceKey` immutability and JMM guarantees
        private static ClassTypeEquivalenceKey of(DotName name) {
            int mask;
            ClassTypeEquivalenceKey[] cache;
            if (name.startsWithJava()) {
                mask = JAVA_CACHE_MASK;
                cache = JAVA_CACHE;
            } else {
                mask = OTHER_CACHE_MASK;
                cache = OTHER_CACHE;
            }

            // `x & (SIZE - 1) is the same as `x % SIZE` iff `SIZE` is a power of 2
            int index = name.hashCode() & mask;

            ClassTypeEquivalenceKey key = cache[index];
            if (key != null && key.name.equals(name)) {
                return key;
            }

            ClassTypeEquivalenceKey newKey = new ClassTypeEquivalenceKey(name);
            cache[index] = newKey;
            return newKey;
        }

        private ClassTypeEquivalenceKey(DotName name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ClassTypeEquivalenceKey))
                return false;
            ClassTypeEquivalenceKey that = (ClassTypeEquivalenceKey) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name.toString();
        }
    }

    public static final class ParameterizedTypeEquivalenceKey extends TypeEquivalenceKey {
        private final DotName genericClass;
        private final TypeEquivalenceKey[] typeArguments;

        private ParameterizedTypeEquivalenceKey(DotName genericClass, TypeEquivalenceKey[] typeArguments) {
            this.genericClass = genericClass;
            this.typeArguments = typeArguments;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof ParameterizedTypeEquivalenceKey))
                return false;
            ParameterizedTypeEquivalenceKey that = (ParameterizedTypeEquivalenceKey) o;
            return genericClass.equals(that.genericClass) && Arrays.equals(typeArguments, that.typeArguments);
        }

        @Override
        public int hashCode() {
            int result = genericClass.hashCode();
            result = 31 * result + Arrays.hashCode(typeArguments);
            return result;
        }

        @Override
        public String toString() {
            Set<TypeVariableEquivalenceKey> typeVariables = new HashSet<>();
            StringJoiner typeArgumentsJoiner = new StringJoiner(", ", "<", ">");
            for (TypeEquivalenceKey typeArgument : typeArguments) {
                typeArgumentsJoiner.add(typeArgument.toStringWithWhere(typeVariables));
            }
            return genericClass + typeArgumentsJoiner.toString() + toStringWhereClause(typeVariables);
        }

        @Override
        String toStringWithWhere(Set<TypeVariableEquivalenceKey> typeVariables) {
            StringJoiner typeArgumentsJoiner = new StringJoiner(", ", "<", ">");
            for (TypeEquivalenceKey typeArgument : typeArguments) {
                typeArgumentsJoiner.add(typeArgument.toStringWithWhere(typeVariables));
            }
            return genericClass + typeArgumentsJoiner.toString();
        }
    }

    public static final class PrimitiveTypeEquivalenceKey extends TypeEquivalenceKey {
        private static final PrimitiveTypeEquivalenceKey[] INSTANCES = instances();

        private static PrimitiveTypeEquivalenceKey[] instances() {
            PrimitiveTypeEquivalenceKey[] instances = new PrimitiveTypeEquivalenceKey[Primitive.values().length];
            for (Primitive value : Primitive.values()) {
                instances[value.ordinal()] = new PrimitiveTypeEquivalenceKey(value);
            }
            return instances;
        }

        private final PrimitiveType.Primitive kind;

        private PrimitiveTypeEquivalenceKey(PrimitiveType.Primitive kind) {
            this.kind = kind;
        }

        public static TypeEquivalenceKey of(PrimitiveType.Primitive kind) {
            return INSTANCES[kind.ordinal()];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PrimitiveTypeEquivalenceKey))
                return false;
            PrimitiveTypeEquivalenceKey that = (PrimitiveTypeEquivalenceKey) o;
            return kind == that.kind;
        }

        @Override
        public int hashCode() {
            return kind.hashCode();
        }

        @Override
        public String toString() {
            return kind.name().toLowerCase(Locale.ROOT);
        }
    }

    public static final class TypeVariableEquivalenceKey extends TypeEquivalenceKey {
        private final String name;
        private final TypeEquivalenceKey[] bounds;

        private TypeVariableEquivalenceKey(String name, TypeEquivalenceKey[] bounds) {
            this.name = name;
            this.bounds = bounds;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TypeVariableEquivalenceKey))
                return false;
            TypeVariableEquivalenceKey that = (TypeVariableEquivalenceKey) o;
            return name.equals(that.name) && Arrays.equals(bounds, that.bounds);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + Arrays.hashCode(bounds);
            return result;
        }

        @Override
        public String toString() {
            if (bounds.length == 0) {
                return name;
            }
            StringJoiner boundsJoiner = new StringJoiner(" & ");
            for (TypeEquivalenceKey bound : bounds) {
                boundsJoiner.add(bound.toString());
            }
            return name + " extends " + boundsJoiner;
        }

        String toStringWithWhere(Set<TypeVariableEquivalenceKey> typeVariables) {
            typeVariables.add(this);
            return name;
        }
    }

    public static final class TypeVariableReferenceEquivalenceKey extends TypeEquivalenceKey {
        private final String name;

        private TypeVariableReferenceEquivalenceKey(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TypeVariableReferenceEquivalenceKey))
                return false;
            TypeVariableReferenceEquivalenceKey that = (TypeVariableReferenceEquivalenceKey) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final class UnresolvedTypeVariableEquivalenceKey extends TypeEquivalenceKey {
        private final String name;

        private UnresolvedTypeVariableEquivalenceKey(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof UnresolvedTypeVariableEquivalenceKey))
                return false;
            UnresolvedTypeVariableEquivalenceKey that = (UnresolvedTypeVariableEquivalenceKey) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final class VoidTypeEquivalenceKey extends TypeEquivalenceKey {
        private static final VoidTypeEquivalenceKey SINGLETON = new VoidTypeEquivalenceKey();

        private VoidTypeEquivalenceKey() {
        }

        @Override
        public String toString() {
            return "void";
        }
    }

    public static final class WildcardTypeEquivalenceKey extends TypeEquivalenceKey {
        private final TypeEquivalenceKey bound;
        private final boolean isExtends;
        private final boolean hasImplicitObjectBound;

        private WildcardTypeEquivalenceKey(TypeEquivalenceKey bound, boolean isExtends, boolean hasImplicitObjectBound) {
            this.bound = bound;
            this.isExtends = isExtends;
            this.hasImplicitObjectBound = hasImplicitObjectBound;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof WildcardTypeEquivalenceKey))
                return false;
            WildcardTypeEquivalenceKey that = (WildcardTypeEquivalenceKey) o;
            return bound.equals(that.bound)
                    && isExtends == that.isExtends
                    && hasImplicitObjectBound == that.hasImplicitObjectBound;
        }

        @Override
        public int hashCode() {
            int result = bound.hashCode();
            result = 31 * result + Boolean.hashCode(isExtends);
            result = 31 * result + Boolean.hashCode(hasImplicitObjectBound);
            return result;
        }

        @Override
        public String toString() {
            if (bound == null || hasImplicitObjectBound) {
                return "?";
            }
            if (isExtends) {
                return "? extends " + bound;
            }
            return "? super " + bound;
        }
    }
}
