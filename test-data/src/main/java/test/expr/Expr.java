package test.expr;

public sealed interface Expr permits Value, Arith {
    int eval();
}
