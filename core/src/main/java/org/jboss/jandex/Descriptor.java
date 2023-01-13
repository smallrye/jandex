package org.jboss.jandex;

import java.util.function.Function;

/**
 * Implementations of this interface have a bytecode descriptor, as defined in JVMS 17, chapter 4.3.
 */
public interface Descriptor {
    Function<String, Type> NO_SUBSTITUTION = ignored -> null;

    /**
     * Returns a bytecode descriptor of this element.
     * <p>
     * Note that the return value does not come directly from bytecode. Jandex does not store the descriptor
     * strings. Instead, the return value is reconstructed from the Jandex object model.
     *
     * @return a bytecode descriptor of this declaration, never {@code null}
     */
    default String descriptor() {
        return descriptor(NO_SUBSTITUTION);
    }

    /**
     * Returns a bytecode descriptor of this element.
     * <p>
     * Descriptors of type variables are substituted for descriptors of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, or if it returns the type variable itself, no substitution happens
     * and the type variable descriptor is used unmodified.
     * <p>
     * Note that the return value does not come directly from bytecode. Jandex does not store the descriptor
     * strings. Instead, the return value is reconstructed from the Jandex object model.
     *
     * @param typeVariableSubstitution a substitution function from type variable identifiers to types
     * @return a bytecode descriptor of this declaration, never {@code null}
     */
    String descriptor(Function<String, Type> typeVariableSubstitution);

    // ---
    // utilities for advanced use cases

    /**
     * Appends a bytecode descriptor of a single type to given {@code StringBuilder}.
     * <p>
     * Descriptors of type variables are substituted for descriptors of types provided by the substitution
     * function {@code typeVariableSubstitution}. If the substitution function returns {@code null}
     * for some type variable identifier, or if it returns the type variable itself, no substitution happens
     * and the type variable descriptor is used unmodified.
     *
     * @param type a type whose bytecode descriptor is appended to {@code result}
     * @param typeVariableSubstitution a substitution function from type variable identifiers to types
     * @param result the {@code StringBuilder} to which the bytecode descriptor is appended
     */
    static void forType(Type type, Function<String, Type> typeVariableSubstitution, StringBuilder result) {
        DescriptorReconstruction.typeDescriptor(type, typeVariableSubstitution, result);
    }
}
