package test.expr;

public sealed abstract class Arith implements Expr permits Add, Mul {
}
