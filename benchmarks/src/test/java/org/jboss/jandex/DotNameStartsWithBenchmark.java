package org.jboss.jandex;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.Throughput)
@Fork(5)
@Warmup(iterations = 5, time = 1, batchSize = 8192)
@Measurement(iterations = 5, time = 1, batchSize = 8192)
@State(Scope.Benchmark)
public class DotNameStartsWithBenchmark {
    private DotName simpleCom;
    private DotName simpleComExample;
    private DotName simpleComExampleFooBar;
    private DotName componentizedCom;
    private DotName componentizedComExample;
    private DotName componentizedComExampleFooBar;

    @Setup
    public void setup() {
        simpleCom = DotName.createSimple("com");
        simpleComExample = DotName.createSimple("com.example");
        simpleComExampleFooBar = DotName.createSimple("com.example.FooBar");
        componentizedCom = DotName.createComponentized(null, "com");
        componentizedComExample = DotName.createComponentized(componentizedCom, "example");
        componentizedComExampleFooBar = DotName.createComponentized(componentizedComExample, "FooBar");
    }

    @Benchmark
    public boolean simpleSimple() {
        return simpleComExample.startsWith(simpleCom)
                && simpleComExampleFooBar.startsWith(simpleComExample);
    }

    @Benchmark
    public boolean simpleComponentized() {
        return simpleComExample.startsWith(componentizedCom)
                && simpleComExampleFooBar.startsWith(componentizedComExample);
    }

    @Benchmark
    public boolean componentizedSimple() {
        return componentizedComExample.startsWith(simpleCom)
                && componentizedComExampleFooBar.startsWith(simpleComExample);
    }

    @Benchmark
    public boolean componentizedComponentized() {
        return componentizedComExample.startsWith(componentizedCom)
                && componentizedComExampleFooBar.startsWith(componentizedComExample);
    }
}
