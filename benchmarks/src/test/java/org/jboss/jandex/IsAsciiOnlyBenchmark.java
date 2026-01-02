package org.jboss.jandex;

import java.nio.charset.StandardCharsets;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.Throughput)
@Fork(5)
@Warmup(iterations = 5, time = 1, batchSize = 8192)
@Measurement(iterations = 5, time = 1, batchSize = 8192)
@State(Scope.Benchmark)
public class IsAsciiOnlyBenchmark {
    private static byte[] bytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    private static final byte[] EMPTY = bytes("");
    private static final byte[] SHORT = bytes("Test");
    private static final byte[] MEDIUM = bytes("com.example.FooBar");
    private static final byte[] LONG = bytes("org.jboss.jandex.long.pkg.name.ExampleClassThatHasAFairlyLongName");

    @Benchmark
    public boolean emptyString() {
        return BitTricks.isAsciiOnly(EMPTY, 0, EMPTY.length);
    }

    @Benchmark
    public boolean shortString() {
        return BitTricks.isAsciiOnly(SHORT, 0, SHORT.length);
    }

    @Benchmark
    public boolean mediumString() {
        return BitTricks.isAsciiOnly(MEDIUM, 0, MEDIUM.length);
    }

    @Benchmark
    public boolean longString() {
        return BitTricks.isAsciiOnly(LONG, 0, LONG.length);
    }
}
