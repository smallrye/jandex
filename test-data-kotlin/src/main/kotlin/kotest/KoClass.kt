package kotest

class KoClass {
    fun classicFunction(foo: Int): String {
        return "foo: $foo"
    }

    suspend fun suspendFunction(bar: Int): String {
        return "bar: $bar"
    }
}
