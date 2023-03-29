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
public class DotNameCompareToBenchmark {
    private DotName simpleFoo1;
    private DotName simpleFoo2;
    private DotName simpleBar;
    private DotName componentizedFoo1;
    private DotName componentizedBar;
    private DotName componentizedFoo2;

    @Setup
    public void setup() {
        simpleFoo1 = DotName.createSimple("org.acme.Foo");
        simpleFoo2 = DotName.createSimple("org.acme.Foo");
        simpleBar = DotName.createSimple("org.acme.next.Bar");
        DotName org = DotName.createComponentized(null, "org");
        DotName acme = DotName.createComponentized(org, "acme");
        componentizedFoo1 = DotName.createComponentized(acme, "Foo");
        componentizedFoo2 = DotName.createComponentized(acme, "Foo");
        DotName next = DotName.createComponentized(acme, "next");
        componentizedBar = DotName.createComponentized(next, "Bar");
    }

    @Benchmark
    public int simpleSimpleEqual() {
        int test = simpleFoo1.compareTo(simpleFoo2);
        if (test != 0) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public int simpleSimpleNotEqual() {
        int test = simpleFoo1.compareTo(simpleBar);
        if (test == 0) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public int simpleComponentizedEqual() {
        int test = simpleFoo1.compareTo(componentizedFoo1);
        if (test != 0) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public int simpleComponentizedNotEqual() {
        int test = simpleFoo1.compareTo(componentizedBar);
        if (test == 0) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public int componentizedComponentizedEqual() {
        int test = componentizedFoo1.compareTo(componentizedFoo2);
        if (test != 0) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public int componentizedComponentizedNotEqual() {
        int test = componentizedFoo1.compareTo(componentizedBar);
        if (test == 0) {
            throw new IllegalStateException();
        }
        return test;
    }
}
