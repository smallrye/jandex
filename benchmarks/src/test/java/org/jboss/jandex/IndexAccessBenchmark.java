package org.jboss.jandex;

import java.io.IOException;

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
public class IndexAccessBenchmark {
    private IndexView index;
    private DotName simpleLump;
    private DotName simpleFool;
    private DotName componentizedLump;
    private DotName componentizedFool;

    @Setup
    public void setup() {
        try {
            index = Index.of(Lump.class);
        } catch (IOException e) {
            throw new IllegalStateException();
        }
        simpleLump = DotName.createSimple(Lump.class.getName());
        simpleFool = DotName.createSimple("org.acme.Fool");
        DotName org = DotName.createComponentized(null, "org");
        DotName jboss = DotName.createComponentized(org, "jboss");
        DotName jandex = DotName.createComponentized(jboss, "jandex");
        componentizedLump = DotName.createComponentized(jandex, "Lump");
        DotName acme = DotName.createComponentized(org, "acme");
        componentizedFool = DotName.createComponentized(acme, "Fool");
    }

    @Benchmark
    public ClassInfo getClassByNameSimple() {
        ClassInfo clazz = index.getClassByName(simpleLump);
        if (clazz == null) {
            throw new IllegalStateException();
        }
        return clazz;
    }

    @Benchmark
    public ClassInfo getClassByNameSimpleMiss() {
        ClassInfo clazz = index.getClassByName(simpleFool);
        if (clazz != null) {
            throw new IllegalStateException();
        }
        return clazz;
    }

    @Benchmark
    public ClassInfo getClassByNameComponentized() {
        ClassInfo clazz = index.getClassByName(componentizedLump);
        if (clazz == null) {
            throw new IllegalStateException();
        }
        return clazz;
    }

    @Benchmark
    public ClassInfo getClassByNameComponentizedMiss() {
        ClassInfo clazz = index.getClassByName(componentizedFool);
        if (clazz != null) {
            throw new IllegalStateException();
        }
        return clazz;
    }
}
