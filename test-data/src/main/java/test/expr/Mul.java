package test.expr;

public final class Mul extends Arith {
    private final Expr left;
    private final Expr right;

    public Mul(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int eval() {
        return left.eval() * right.eval();
    }
}
