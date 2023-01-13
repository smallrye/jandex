package org.jboss.jandex;

import java.util.List;
import java.util.function.Function;

final class GenericSignatureReconstruction {
    static boolean requiresGenericSignature(ClassInfo clazz) {
        // JVMS 17, chapter 4.7.9.1. Signatures:
        //
        // Java compiler must emit ...
        //
        // A class signature for any class or interface declaration which is either generic,
        // or has a parameterized type as a superclass or superinterface, or both.

        if (!clazz.typeParameters().isEmpty()) {
            return true;
        }

        {
            Type superType = clazz.superClassType();
            if (superType.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                return true;
            }
        }

        for (Type superType : clazz.interfaceTypes()) {
            if (superType.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                return true;
            }
        }

        return false;
    }

    static boolean requiresGenericSignature(MethodInfo method) {
        // JVMS 17, chapter 4.7.9.1. Signatures:
        //
        // Java compiler must emit ...
        //
        // A method signature for any method or constructor declaration which is either generic,
        // or has a type variable or parameterized type as the return type or a formal parameter type,
        // or has a type variable in a throws clause, or any combination thereof.
        //
        // If the throws clause of a method or constructor declaration does not involve type variables,
        // then a compiler may treat the declaration as having no throws clause for the purpose of
        // emitting a method signature.

        if (!method.typeParameters().isEmpty()) {
            return true;
        }

        {
            if (requiresGenericSignature(method.returnType())) {
                return true;
            }
        }

        for (Type parameterType : method.parameterTypes()) {
            if (requiresGenericSignature(parameterType)) {
                return true;
            }
        }

        if (hasThrowsSignature(method)) {
            return true;
        }

        return false;
    }

    static boolean requiresGenericSignature(FieldInfo field) {
        // JVMS 17, chapter 4.7.9.1. Signatures:
        //
        // Java compiler must emit ...
        //
        // A field signature for any field, formal parameter, local variable, or record component
        // declaration whose type uses a type variable or a parameterized type.

        return requiresGenericSignature(field.type());
    }

    static boolean requiresGenericSignature(RecordComponentInfo recordComponent) {
        // JVMS 17, chapter 4.7.9.1. Signatures:
        //
        // Java compiler must emit ...
        //
        // A field signature for any field, formal parameter, local variable, or record component
        // declaration whose type uses a type variable or a parameterized type.

        return requiresGenericSignature(recordComponent.type());
    }

    private static boolean requiresGenericSignature(Type type) {
        return type.kind() == Type.Kind.TYPE_VARIABLE
                || type.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE
                || type.kind() == Type.Kind.PARAMETERIZED_TYPE;
    }

    // ---
    // for grammar, see JVMS 17, chapter 4.7.9.1. Signatures

    static String reconstructGenericSignature(ClassInfo clazz, Function<String, Type> typeVariableSubstitution) {
        StringBuilder result = new StringBuilder();

        if (!clazz.typeParameters().isEmpty()) {
            typeParametersSignature(clazz.typeParameters(), typeVariableSubstitution, result);
        }

        typeSignature(clazz.superClassType(), typeVariableSubstitution, result);

        for (Type interfaceType : clazz.interfaceTypes()) {
            typeSignature(interfaceType, typeVariableSubstitution, result);
        }

        return result.toString();
    }

    static String reconstructGenericSignature(MethodInfo method, Function<String, Type> typeVariableSubstitution) {
        StringBuilder result = new StringBuilder();

        if (!method.typeParameters().isEmpty()) {
            typeParametersSignature(method.typeParameters(), typeVariableSubstitution, result);
        }

        result.append('(');
        for (Type parameter : method.parameterTypes()) {
            typeSignature(parameter, typeVariableSubstitution, result);
        }
        result.append(')');

        typeSignature(method.returnType(), typeVariableSubstitution, result);

        if (hasThrowsSignature(method)) {
            for (Type exception : method.exceptions()) {
                result.append('^');
                typeSignature(exception, typeVariableSubstitution, result);
            }
        }

        return result.toString();
    }

    static String reconstructGenericSignature(FieldInfo field, Function<String, Type> typeVariableSubstitution) {
        StringBuilder result = new StringBuilder();

        typeSignature(field.type(), typeVariableSubstitution, result);

        return result.toString();
    }

    static String reconstructGenericSignature(RecordComponentInfo recordComponent,
            Function<String, Type> typeVariableSubstitution) {
        StringBuilder result = new StringBuilder();

        typeSignature(recordComponent.type(), typeVariableSubstitution, result);

        return result.toString();
    }

    private static boolean hasThrowsSignature(MethodInfo method) {
        // JVMS 17, chapter 4.7.9.1. Signatures:
        //
        // If the throws clause of a method or constructor declaration does not involve type variables,
        // then a compiler may treat the declaration as having no throws clause for the purpose of
        // emitting a method signature.

        // also, no need to check if an exception type is of kind PARAMETERIZED_TYPE, because
        //
        // JLS 17, chapter 8.1.2. Generic Classes and Type Parameters:
        //
        // It is a compile-time error if a generic class is a direct or indirect subclass of Throwable.

        for (Type type : method.exceptions()) {
            if (type.kind() == Type.Kind.TYPE_VARIABLE
                    || type.kind() == Type.Kind.UNRESOLVED_TYPE_VARIABLE) {
                return true;
            }
        }
        return false;
    }

    static void typeParametersSignature(List<TypeVariable> typeParameters,
            Function<String, Type> substitution, StringBuilder result) {

        if (typeParameters.isEmpty()) {
            return;
        }

        // it is not clear whether we should substitute type variables in type parameter bounds;
        // we currently do, because the Quarkus original (where this is adapted from) also did

        result.append('<');
        for (TypeVariable typeParameter : typeParameters) {
            result.append(typeParameter.identifier());

            if (typeParameter.hasImplicitObjectBound()) {
                result.append(':');
            }
            for (Type bound : typeParameter.bounds()) {
                result.append(':');
                typeSignature(bound, substitution, result);
            }
        }
        result.append('>');
    }

    static void typeSignature(Type type, Function<String, Type> substitution, StringBuilder result) {
        switch (type.kind()) {
            case VOID:
                result.append('V');
                break;
            case PRIMITIVE:
                PrimitiveType.Primitive primitive = type.asPrimitiveType().primitive();
                switch (primitive) {
                    case BOOLEAN:
                        result.append('Z');
                        return;
                    case CHAR:
                        result.append('C');
                        return;
                    case BYTE:
                        result.append('B');
                        return;
                    case SHORT:
                        result.append('S');
                        return;
                    case INT:
                        result.append('I');
                        return;
                    case LONG:
                        result.append('J');
                        return;
                    case FLOAT:
                        result.append('F');
                        return;
                    case DOUBLE:
                        result.append('D');
                        return;
                    default:
                        throw new IllegalArgumentException("unkown primitive type " + primitive);
                }
            case CLASS:
                ClassType classType = type.asClassType();
                result.append('L').append(classType.name().toString('/')).append(';');
                break;
            case ARRAY:
                ArrayType arrayType = type.asArrayType();
                for (int i = 0; i < arrayType.dimensions(); i++) {
                    result.append('[');
                }
                typeSignature(arrayType.component(), substitution, result);
                break;
            case PARAMETERIZED_TYPE:
                ParameterizedType parameterizedType = type.asParameterizedType();
                Type owner = parameterizedType.owner();
                if (owner != null && owner.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                    typeSignature(owner, substitution, result);
                    // the typeSignature call on previous line always takes the PARAMETERIZED_TYPE branch,
                    // so at this point, result ends with a ';', which we just replace with '.'
                    assert result.charAt(result.length() - 1) == ';';
                    result.setCharAt(result.length() - 1, '.');
                    result.append(parameterizedType.name().local());
                } else {
                    result.append('L').append(parameterizedType.name().toString('/'));
                }
                if (!parameterizedType.arguments().isEmpty()) {
                    result.append('<');
                    for (Type argument : parameterizedType.arguments()) {
                        typeSignature(argument, substitution, result);
                    }
                    result.append('>');
                }
                result.append(';');
                break;
            case TYPE_VARIABLE:
                typeVariableSignature(type, type.asTypeVariable().identifier(), substitution, result);
                break;
            case UNRESOLVED_TYPE_VARIABLE:
                typeVariableSignature(type, type.asUnresolvedTypeVariable().identifier(), substitution, result);
                break;
            case TYPE_VARIABLE_REFERENCE:
                typeVariableSignature(type, type.asTypeVariableReference().identifier(), substitution, result);
                break;
            case WILDCARD_TYPE:
                WildcardType wildcardType = type.asWildcardType();
                if (wildcardType.superBound() != null) {
                    result.append('-');
                    typeSignature(wildcardType.superBound(), substitution, result);
                } else if (ClassType.OBJECT_TYPE.equals(wildcardType.extendsBound())) {
                    result.append('*');
                } else {
                    result.append('+');
                    typeSignature(wildcardType.extendsBound(), substitution, result);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown type " + type);
        }
    }

    // `typeVariable` is always the type variable whose identifier is `typeVariableIdentifier`; its purpose is
    // to prevent possible cycle when `substitution.apply(typeVariableIdentifier)` returns `typeVariable`
    private static void typeVariableSignature(Type typeVariable, String typeVariableIdentifier,
            Function<String, Type> substitution, StringBuilder result) {
        Type type = substitution == null ? null : substitution.apply(typeVariableIdentifier);
        if (type == null || type == typeVariable) {
            result.append('T').append(typeVariableIdentifier).append(';');
        } else {
            typeSignature(type, substitution, result);
        }
    }
}
