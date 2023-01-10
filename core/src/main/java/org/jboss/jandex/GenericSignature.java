package org.jboss.jandex;

import java.util.List;
import java.util.function.Function;

/**
 * Implementations of this interface have a generic signature, as defined in JVMS 17, chapter 4.7.9.1.
 */
public interface GenericSignature {
    Function<String, Type> NO_SUBSTITUTION = ignored -> null;

    /**
     * Returns whether this declaration must have a generic signature. That is, whether the Java compiler
     * when compiling this declaration had to emit the {@code Signature} bytecode attribute.
     *
     * @return whether this declaration must have a generic signature
     */
    boolean requiresGenericSignature();

    /**
     * Returns a generic signature of this declaration, possibly without any generic-related information.
     * That is, produces a correct generic signature even if this declaration is not generic
     * and does not use any type variables.
     * <p>
     * Note that the return value does not come directly from bytecode. Jandex does not store the signature
     * strings. Instead, the return value is reconstructed from the Jandex object model.
     *
     * @return a generic signature of this declaration, never {@code null}
     */
    default String genericSignature() {
        return genericSignature(NO_SUBSTITUTION);
    }

    /**
     * Returns a generic signature of this declaration, possibly without any generic-related information.
     * That is, produces a correct generic signature even if this declaration is not generic
     * and does not use any type variables.
     * <p>
     * Signatures of type variables are substituted for signatures of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, or if it returns the type variable itself, no substitution happens
     * and the type variable signature is used unmodified.
     * <p>
     * Note that the return value does not come directly from bytecode. Jandex does not store the signature
     * strings. Instead, the return value is reconstructed from the Jandex object model.
     *
     * @param typeVariableSubstitution a substitution function from type variable identifiers to types
     * @return a generic signature of this declaration with type variables substituted, never {@code null}
     */
    String genericSignature(Function<String, Type> typeVariableSubstitution);

    /**
     * Returns a {@linkplain #genericSignature() generic signature} of this declaration
     * if {@linkplain #requiresGenericSignature() required}.
     *
     * @return a generic signature of this declaration, or {@code null} if this declaration doesn't have to have one
     */
    default String genericSignatureIfRequired() {
        return genericSignatureIfRequired(NO_SUBSTITUTION);
    }

    /**
     * Returns a {@linkplain #genericSignature(Function) generic signature} of this declaration
     * if {@linkplain #requiresGenericSignature() required}. Type variable signatures are substituted.
     *
     * @param typeVariableSubstitution a substitution function from type variable identifiers to types
     * @return a generic signature of this declaration with type variables substituted, or {@code null}
     *         if this declaration does not have to have one
     */
    default String genericSignatureIfRequired(Function<String, Type> typeVariableSubstitution) {
        return requiresGenericSignature() ? genericSignature(typeVariableSubstitution) : null;
    }

    // ---
    // utilities for advanced use cases

    /**
     * Appends a generic signature of a type parameter list, including the {@code <} at the beginning and
     * {@code >} at the end, to given {@code StringBuilder}.
     * <p>
     * Signatures of type variables are substituted for signatures of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, or if it returns the type variable itself, no substitution happens
     * and the type variable signature is used unmodified.
     *
     * @param typeParameters a list of type parameters whose generic signature is appended to {@code result}
     * @param typeVariableSubstitution a substitution function from type variable identifiers to types
     * @param result the {@code StringBuilder} to which the generic signature is appended
     */
    static void forTypeParameters(List<TypeVariable> typeParameters, Function<String, Type> typeVariableSubstitution,
            StringBuilder result) {
        GenericSignatureReconstruction.typeParametersSignature(typeParameters, typeVariableSubstitution, result);
    }

    /**
     * Appends a generic signature of a single type to given {@code StringBuilder}.
     * <p>
     * Signatures of type variables are substituted for signatures of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, or if it returns the type variable itself, no substitution happens
     * and the type variable signature is used unmodified.
     *
     * @param type a type parameters whose generic signature is appended to {@code result}
     * @param typeVariableSubstitution a substitution function from type variable identifiers to types
     * @param result the {@code StringBuilder} to which the generic signature is appended
     */
    static void forType(Type type, Function<String, Type> typeVariableSubstitution, StringBuilder result) {
        GenericSignatureReconstruction.typeSignature(type, typeVariableSubstitution, result);
    }
}
