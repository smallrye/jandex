package org.jboss.jandex;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

// this is just for manual usage, the file path below needs to be adjusted
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(5)
@State(Scope.Benchmark)
public class IndexerBenchmark {
    @Benchmark
    public Index index() throws IOException {
        return Index.of(new File("/home/ladicek/work/monster.jar"));
    }
}
