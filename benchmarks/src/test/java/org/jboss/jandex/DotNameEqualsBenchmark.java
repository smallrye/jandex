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
public class DotNameEqualsBenchmark {
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
    public boolean simpleSimpleEquals() {
        boolean test = simpleFoo1.equals(simpleFoo2);
        if (!test) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public boolean simpleSimpleNotEquals() {
        boolean test = simpleFoo1.equals(simpleBar);
        if (test) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public boolean simpleComponentizedEquals() {
        boolean test = simpleFoo1.equals(componentizedFoo1);
        if (!test) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public boolean simpleComponentizedNotEquals() {
        boolean test = simpleFoo1.equals(componentizedBar);
        if (test) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public boolean componentizedComponentizedEquals() {
        boolean test = componentizedFoo1.equals(componentizedFoo2);
        if (!test) {
            throw new IllegalStateException();
        }
        return test;
    }

    @Benchmark
    public boolean componentizedComponentizedNotEquals() {
        boolean test = componentizedFoo1.equals(componentizedBar);
        if (test) {
            throw new IllegalStateException();
        }
        return test;
    }
}
