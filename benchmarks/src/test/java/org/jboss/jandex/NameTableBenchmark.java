package org.jboss.jandex;

import java.nio.charset.StandardCharsets;

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
public class NameTableBenchmark {
    private NameTable names;

    @Setup
    public void setup() {
        names = new NameTable();
    }

    @Benchmark
    public NameTable interning() {
        names.intern("java.lang.String");
        names.intern(ClassType.create(DotName.STRING_NAME));
        names.intern(new Type[] {
                PrimitiveType.INT,
                ClassType.create(DotName.STRING_NAME),
                ArrayType.create(PrimitiveType.INT, 1),
                ArrayType.create(ClassType.create(DotName.STRING_NAME), 2),
                ParameterizedType.create(DotName.OBJECT_NAME, ClassType.create(DotName.STRING_NAME)),
                TypeVariable.create("T"),
                WildcardType.createUpperBound(ClassType.create(DotName.OBJECT_NAME))
        });
        names.intern("java.lang.Object".getBytes(StandardCharsets.UTF_8));
        names.intern(DotName.STRING_NAME, '/');
        return names;
    }
}
