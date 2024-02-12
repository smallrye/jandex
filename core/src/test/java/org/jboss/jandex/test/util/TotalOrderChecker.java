package org.jboss.jandex.test.util;

import java.util.Collection;
import java.util.Comparator;

/**
 * Verifies that a given comparator establishes a total order on the given collection of values.
 * <p>
 * From {@link Comparator}:
 * <p>
 * The <i>relation</i> that defines the <i>imposed ordering</i> that a given comparator {@code c} imposes
 * on a given set of objects {@code S} is:
 *
 * <pre>
 *        {(x, y) such that c.compare(x, y) &lt;= 0}.
 * </pre>
 *
 * The <i>quotient</i> for this total order is:
 *
 * <pre>
 *        {(x, y) such that c.compare(x, y) == 0}.
 * </pre>
 * <p>
 * From <a href="https://en.wikipedia.org/wiki/Total_order">https://en.wikipedia.org/wiki/Total_order</a>:
 * <p>
 * A total order is a binary relation &le; on some set {@code X}, which satisfies the following for all {@code a},
 * {@code b} and {@code c} in {@code X}:
 * <ol>
 * <li>a &le; a (reflexive)</li>
 * <li>if a &le; b and b &le; c then a &le; c (transitive)</li>
 * <li>if a &le; b and b &le; a then a = b (antisymmetric)</li>
 * <li>a &le; b or b &le; a (strongly connected)</li>
 * </ol>
 *
 * @param <T> the type of values
 */
public class TotalOrderChecker<T> {
    private final Collection<T> values;
    private final Comparator<T> comparator;
    private final boolean throwOnFailure;

    public TotalOrderChecker(Collection<T> values, Comparator<T> comparator, boolean throwOnFailure) {
        this.values = values;
        this.comparator = comparator;
        this.throwOnFailure = throwOnFailure;
    }

    public void check() {
        checkReflexive();
        checkTransitive();
        checkAntisymmetric();
        checkStronglyConnected();
    }

    // ---

    private boolean isEqual(T a, T b) {
        return comparator.compare(a, b) == 0;
    }

    private boolean isInRelation(T a, T b) {
        return comparator.compare(a, b) <= 0;
    }

    private void fail(String message) {
        if (throwOnFailure) {
            throw new AssertionError(message);
        } else {
            System.out.println(message);
        }
    }

    private void checkReflexive() {
        for (T a : values) {
            if (!isInRelation(a, a)) {
                fail("not reflexive due to " + a);
            }
        }
    }

    private void checkTransitive() {
        for (T a : values) {
            for (T b : values) {
                for (T c : values) {
                    if (isInRelation(a, b) && isInRelation(b, c)) {
                        if (!isInRelation(a, c)) {
                            fail("not transitive due to " + a + ", " + b + " and " + c);
                        }
                    }
                }
            }
        }
    }

    private void checkAntisymmetric() {
        for (T a : values) {
            for (T b : values) {
                if (isInRelation(a, b) && isInRelation(b, a)) {
                    if (!isEqual(a, b)) {
                        fail("not antisymmetric due to " + a + " and " + b);
                    }
                }
            }
        }
    }

    private void checkStronglyConnected() {
        for (T a : values) {
            for (T b : values) {
                if (!isInRelation(a, b) && !isInRelation(b, a)) {
                    fail("not strongly connected due to " + a + " and " + b);
                }
            }
        }
    }
}
