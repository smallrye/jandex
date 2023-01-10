package org.jboss.jandex;

import java.util.function.Function;

final class DescriptorReconstruction {
    static String fieldDescriptor(FieldInfo field, Function<String, Type> typeVariableSubstitution) {
        FieldInternal internal = field.fieldInternal();

        StringBuilder result = new StringBuilder();
        typeDescriptor(internal.type(), typeVariableSubstitution, result);
        return result.toString();
    }

    static String methodDescriptor(MethodInfo method, Function<String, Type> typeVariableSubstitution) {
        MethodInternal internal = method.methodInternal();

        StringBuilder result = new StringBuilder();
        result.append('(');
        for (Type parameterType : internal.parameterTypesArray()) {
            typeDescriptor(parameterType, typeVariableSubstitution, result);
        }
        result.append(')');
        typeDescriptor(internal.returnType(), typeVariableSubstitution, result);
        return result.toString();
    }

    static String recordComponentDescriptor(RecordComponentInfo recordComponent,
            Function<String, Type> typeVariableSubstitution) {
        RecordComponentInternal internal = recordComponent.recordComponentInternal();

        StringBuilder result = new StringBuilder();
        typeDescriptor(internal.type(), typeVariableSubstitution, result);
        return result.toString();
    }

    static void typeDescriptor(Type type, Function<String, Type> substitution, StringBuilder result) {
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
                    case CHAR:
                        result.append('C');
                        return;
                    default:
                        throw new IllegalArgumentException("unkown primitive type " + primitive);
                }
            case ARRAY:
                ArrayType arrayType = type.asArrayType();
                for (int i = 0; i < arrayType.dimensions(); i++) {
                    result.append('[');
                }
                typeDescriptor(arrayType.component(), substitution, result);
                break;
            case CLASS:
            case PARAMETERIZED_TYPE:
            case WILDCARD_TYPE:
                objectTypeDescriptor(type.name(), result);
                break;
            case TYPE_VARIABLE:
                typeVariableDescriptor(type, type.asTypeVariable().identifier(), substitution, result);
                break;
            case UNRESOLVED_TYPE_VARIABLE:
                typeVariableDescriptor(type, type.asUnresolvedTypeVariable().identifier(), substitution, result);
                break;
            case TYPE_VARIABLE_REFERENCE:
                typeVariableDescriptor(type, type.asTypeVariableReference().identifier(), substitution, result);
                break;
            default:
                throw new IllegalArgumentException("unknown type " + type);
        }
    }

    static void objectTypeDescriptor(DotName name, StringBuilder result) {
        result.append('L').append(name.toString('/')).append(';');
    }

    // `typeVariable` is always the type variable whose identifier is `typeVariableIdentifier`; its purpose is
    // to prevent possible cycle when `substitution.apply(typeVariableIdentifier)` returns `typeVariable`
    private static void typeVariableDescriptor(Type typeVariable, String typeVariableIdentifier,
            Function<String, Type> substitution, StringBuilder result) {
        Type type = substitution == null ? null : substitution.apply(typeVariableIdentifier);
        if (type == null || type == typeVariable) {
            objectTypeDescriptor(typeVariable.name(), result);
        } else {
            typeDescriptor(type, substitution, result);
        }
    }
}
