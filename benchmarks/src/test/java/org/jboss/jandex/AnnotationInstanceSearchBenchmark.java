package org.jboss.jandex;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@Fork(5)
@Warmup(iterations = 5, time = 1, batchSize = 8192)
@Measurement(iterations = 5, time = 1, batchSize = 8192)
@State(Scope.Benchmark)
public class AnnotationInstanceSearchBenchmark {
    // arrays must be sorted by annotation name
    static AnnotationInstance[] ZERO = new AnnotationInstance[0];
    static AnnotationInstance[] ONE = new AnnotationInstance[] {
            AnnotationInstance.builder(DotName.createSimple("org.acme.FooBar")).build()
    };
    static AnnotationInstance[] TWO = new AnnotationInstance[] {
            AnnotationInstance.builder(DotName.createSimple("com.example.MyAnn")).build(),
            AnnotationInstance.builder(DotName.createSimple("org.acme.FooBar")).build(),
    };
    static AnnotationInstance[] FOUR = new AnnotationInstance[] {
            AnnotationInstance.builder(DotName.createSimple("com.example.MyAnn")).build(),
            AnnotationInstance.builder(DotName.createSimple("HelloWorld")).build(),
            AnnotationInstance.builder(DotName.createSimple("org.acme.FooBar")).build(),
            AnnotationInstance.builder(DotName.createSimple("org.jboss.jandex.SimpleAnnotation")).build(),
    };
    static AnnotationInstance[] EIGHT = new AnnotationInstance[] {
            AnnotationInstance.builder(DotName.createSimple("com.example.MyAnn")).build(),
            AnnotationInstance.builder(DotName.createSimple("com.example.OtherAnn")).build(),
            AnnotationInstance.builder(DotName.createSimple("HelloWorld")).build(),
            AnnotationInstance.builder(DotName.createSimple("Nullable")).build(),
            AnnotationInstance.builder(DotName.createSimple("org.acme.FooBar")).build(),
            AnnotationInstance.builder(DotName.createSimple("org.acme.Quux")).build(),
            AnnotationInstance.builder(DotName.createSimple("org.jboss.jandex.AnotherAnnotation")).build(),
            AnnotationInstance.builder(DotName.createSimple("org.jboss.jandex.SimpleAnnotation")).build(),
    };

    static AnnotationInstance[][] ARRAYS = new AnnotationInstance[][] {
            ZERO, ONE, TWO, FOUR, EIGHT
    };

    @Benchmark
    public void missing(Blackhole blackhole) {
        for (AnnotationInstance[] array : ARRAYS) {
            blackhole.consume(AnnotationInstance.binarySearch(array, DotName.createSimple("nonexisting")));
        }
    }

    @Benchmark
    public void existingUnlessEmpty(Blackhole blackhole) {
        for (AnnotationInstance[] array : ARRAYS) {
            blackhole.consume(AnnotationInstance.binarySearch(array, DotName.createSimple("com.example.MyAnn")));
        }
    }
}
