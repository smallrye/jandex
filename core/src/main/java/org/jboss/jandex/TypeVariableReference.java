package org.jboss.jandex;

/**
 * Represents a reference to a type variable in the bound of a recursive type parameter.
 * For example, if a class or method declares a type parameter {@code T extends Comparable<T>},
 * then the second occurence of {@code T} is represented as a type variable reference and not
 * as a type variable, because it occurs in its own definition. A type variable which is fully
 * defined before its occurence in a recursive type parameter is still represented as a type
 * variable. For example, the recursive type parameter in the following class includes
 * one type variable reference:
 *
 * <pre class="brush:java">
 * abstract class Builder&lt;T, THIS extends Builder&lt;T, THIS&gt;&gt; {
 *     abstract T build();
 *
 *     final THIS self() {
 *         return (THIS) this;
 *     }
 * }
 * </pre>
 *
 * The identifier of the reference is {@code THIS}. The occurence of type variable {@code T}
 * in the recursive type parameter is <em>not</em> represented as a reference, because it does not
 * occur in its own definition. It is fully defined before.
 * <p>
 * The same holds for mutually recursive type parameters. If a type variable is fully defined
 * before the type parameter in whose bound it occurs, it is represented as a type variable.
 * It is represented as a type variable reference if it is defined after the type parameter in whose
 * bound it occurs.
 * <p>
 * The type variable reference may be {@linkplain #follow() followed} to obtain the original
 * type variable.
 * <p>
 * Note that a type variable and a reference to that type variable may have different
 * type annotations. Type annotations on the reference may be looked up from the reference,
 * but when the reference is followed, the result is the original type variable with its
 * own type annotations.
 */
public final class TypeVariableReference extends Type {
    private final String name;
    private TypeVariable target;

    TypeVariableReference(String name) {
        this(name, null, null);
    }

    TypeVariableReference(String name, TypeVariable target, AnnotationInstance[] annotations) {
        super(DotName.OBJECT_NAME, annotations);
        this.name = name;
        this.target = target;
    }

    @Override
    public DotName name() {
        if (target == null) {
            throw new IllegalStateException("Type variable reference " + name + " was not patched correctly");
        }
        return target.name();
    }

    /**
     * Returns the identifier of this type variable reference as it appears in Java source code.
     * <p>
     * For example, the following class has a recursive type parameter {@code E} with one reference:
     *
     * <pre class="brush:java">
     * abstract class MyEnum&lt;E extends MyEnum&lt;E&gt;&gt; {
     * }
     * </pre>
     *
     * The identifier of the reference is {@code E}.
     *
     * @return the identifier of this type variable reference
     */
    public String identifier() {
        return name;
    }

    /**
     * Returns the type variable referred to by this reference.
     */
    public TypeVariable follow() {
        if (target == null) {
            throw new IllegalStateException("Type variable reference " + name + " was not patched correctly");
        }
        return target;
    }

    @Override
    public Kind kind() {
        return Kind.TYPE_VARIABLE_REFERENCE;
    }

    @Override
    public TypeVariableReference asTypeVariableReference() {
        return this;
    }

    @Override
    Type copyType(AnnotationInstance[] newAnnotations) {
        return new TypeVariableReference(name, target, newAnnotations);
    }

    void setTarget(TypeVariable target) {
        if (target == null) {
            throw new IllegalArgumentException("Type variable reference target must not be null");
        }
        this.target = target;
    }

    @Override
    String toString(boolean simple) {
        StringBuilder builder = new StringBuilder();
        appendAnnotations(builder);
        builder.append(name);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this.target == null) {
            throw new IllegalStateException("Type variable reference " + name + " was not patched correctly");
        }

        if (this == o) {
            return true;
        }

        if (!super.equals(o)) {
            return false;
        }

        TypeVariableReference that = (TypeVariableReference) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        if (this.target == null) {
            throw new IllegalStateException("Type variable reference " + name + " was not patched correctly");
        }

        int hash = super.hashCode();
        hash = 31 * hash + name.hashCode();
        return hash;
    }

    // unlike all other subclasses of `Type`, this class is mutable, so identity is the only option
    @Override
    boolean internEquals(Object o) {
        return this == o;
    }

    @Override
    int internHashCode() {
        // must produce predictable hash code (for reproducibility) consistent with `internEquals`
        return System.identityHashCode(this);
    }
}
