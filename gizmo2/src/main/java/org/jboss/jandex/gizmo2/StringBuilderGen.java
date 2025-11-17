package org.jboss.jandex.gizmo2;

import static io.quarkus.gizmo2.desc.Descs.MD_StringBuilder;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ops.ComparableOps;
import io.quarkus.gizmo2.creator.ops.ObjectOps;

/**
 * Generator of {@link StringBuilder} call chains. The expected usage pattern is:
 * <ol>
 * <li>Create an instance using {@link #ofNew(BlockCreator)}</li>
 * <li>Append to it using {@link #append(Expr)}</li>
 * <li>Create the final string using {@link #toString_()}</li>
 * </ol>
 * If you need to perform other operations on the {@code StringBuilder}
 * that this class doesn't provide, you should create an instance
 * using {@link #of(Var, BlockCreator)}, which allows passing an already
 * created {@code StringBuilder}. This class itself doesn't provide access
 * to the underlying object.
 */
public final class StringBuilderGen extends ObjectOps implements ComparableOps {
    /**
     * Allocates a local variable in the given block as if by {@code new StringBuilder()}
     * and passes it to {@link #of(Var, BlockCreator)}.
     *
     * @param bc the block in which the new {@code StringBuilder} should be created
     * @return the result of {@link #of(Var, BlockCreator)} called on the new {@code StringBuilder}
     */
    public static StringBuilderGen ofNew(BlockCreator bc) {
        return new StringBuilderGen(bc, bc.localVar("$$stringBuilder", bc.new_(StringBuilder.class)));
    }

    /**
     * Allocates a local variable in the given block as if by {@code new StringBuilder(capacity)}
     * and passes it to {@link #of(Var, BlockCreator)}.
     *
     * @param bc the block in which the new {@code StringBuilder} should be created
     * @return the result of {@link #of(Var, BlockCreator)} called on the new {@code StringBuilder}
     */
    public static StringBuilderGen ofNew(int capacity, BlockCreator bc) {
        return new StringBuilderGen(bc, bc.localVar("$$stringBuilder", bc.new_(StringBuilder.class, Const.of(capacity))));
    }

    /**
     * Creates a {@code StringBuilder} generator that helps to generate a chain of
     * {@code append} calls and a final {@code toString} call.
     *
     * <pre>
     * StringBuilderGen str = StringBuilderGen.of(theStringBuilder, bc);
     * str.append("constant");
     * str.append(someExpr);
     * Expr result = str.toString_();
     * </pre>
     *
     * The {@code append} method mimics the regular {@code StringBuilder.append}, so
     * it accepts {@code Expr}s of all types for which {@code StringBuilder}
     * has an overload:
     * <ul>
     * <li>primitive types</li>
     * <li>{@code char[]}</li>
     * <li>{@code java.lang.String}</li>
     * <li>{@code java.lang.CharSequence}</li>
     * <li>{@code java.lang.Object}</li>
     * </ul>
     *
     * Notably, arrays except of {@code char[]} are appended using {@code Object.toString}
     * and if {@code Arrays.toString} should be used, it must be generated manually
     * (see {@link BlockCreator#arrayToString(Expr)}).
     * <p>
     * Methods for appending only a part of {@code char[]} or {@code CharSequence} are not
     * provided.
     * <p>
     * Note that the returned instance <em>may be reused</em> to append to the same {@code StringBuilder}
     * in the same {@code BlockCreator} multiple times. This allows using {@code StringBuilderGen}
     * in the same manner a {@code StringBuilder} would normally be used.
     *
     * @param stringBuilder the {@link StringBuilder}
     * @param bc the {@link BlockCreator}
     * @return a convenience wrapper for accessing instance methods of the given {@link StringBuilder}
     */
    public static StringBuilderGen of(Var stringBuilder, BlockCreator bc) {
        return new StringBuilderGen(bc, stringBuilder);
    }

    private StringBuilderGen(final BlockCreator bc, final Var obj) {
        super(bc, obj);
    }

    /**
     * Appends the string value of given {@code expr} to this {@code StringBuilder}.
     *
     * @param expr the value to append
     * @return this instance
     */
    public StringBuilderGen append(final Expr expr) {
        bc.invokeVirtual(MD_StringBuilder.append(expr.type()), obj, expr);
        return this;
    }

    /**
     * Appends the given {@code char} constant to this {@code StringBuilder}.
     *
     * @param constant the value to append
     * @return this instance
     */
    public StringBuilderGen append(final char constant) {
        return append(Const.of(constant));
    }

    /**
     * Appends the given {@code String} constant to this {@code StringBuilder}.
     *
     * @param constant the value to append
     * @return this instance
     */
    public StringBuilderGen append(final String constant) {
        return append(Const.of(constant));
    }

    /**
     * Appends the given code point to this {@code StringBuilder}.
     *
     * @param codePoint the value to append (must not be {@code null})
     * @return this instance
     */
    public StringBuilderGen appendCodePoint(final Expr codePoint) {
        bc.invokeVirtual(MD_StringBuilder.appendCodePoint, obj, codePoint);
        return this;
    }

    /**
     * Appends the given code point to this {@code StringBuilder}.
     *
     * @param codePoint the value to append
     * @return this instance
     */
    public StringBuilderGen appendCodePoint(final int codePoint) {
        return appendCodePoint(Const.of(codePoint));
    }

    /**
     * Set the length of this {@code StringBuilder}.
     *
     * @param length the length expression (must not be {@code null})
     */
    public void setLength(Expr length) {
        bc.invokeVirtual(MD_StringBuilder.setLength, obj, length);
    }

    /**
     * Set the length of this {@code StringBuilder}.
     *
     * @param length the constant length
     */
    public void setLength(int length) {
        setLength(Const.of(length));
    }

    @Override
    public Expr compareTo(Expr other) {
        return bc.invokeVirtual(MD_StringBuilder.compareTo, obj, other);
    }
}
