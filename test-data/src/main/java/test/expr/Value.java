package test.expr;

public non-sealed class Value implements Expr {
    private final int value;

    public Value(int value) {
        this.value = value;
    }

    @Override
    public int eval() {
        return value;
    }
}
