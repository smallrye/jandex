package org.jboss.jandex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Simulates what frameworks typically do with an index: look up a handful of well-known annotations
 * (created with {@code DotName.createSimple()}, as framework constants usually are) on a method
 * that carries a few annotations created by the indexer (componentized names).
 * <p>
 * For example, Quarkus Hibernate Validator checks every method of every bean class for each of
 * the ~25 constraint annotations, and ArC checks every method for {@code @AroundInvoke}.
 */
@BenchmarkMode(Mode.Throughput)
@Fork(3)
@Warmup(iterations = 5, time = 1, batchSize = 1024)
@Measurement(iterations = 5, time = 1, batchSize = 1024)
@State(Scope.Benchmark)
public class AnnotationLookupBenchmark {
    // annotations present on the method, as the indexer would create them
    private static final String[] PRESENT = {
            "jakarta.transaction.Transactional",
            "jakarta.ws.rs.GET",
            "jakarta.ws.rs.Path",
            "jakarta.ws.rs.Produces",
            "org.eclipse.microprofile.openapi.annotations.Operation",
            "io.quarkus.security.Authenticated",
    };

    // annotations the framework looks for, none of them present
    private static final String[] LOOKED_UP = {
            "jakarta.validation.Valid",
            "jakarta.validation.constraints.AssertFalse",
            "jakarta.validation.constraints.AssertTrue",
            "jakarta.validation.constraints.DecimalMax",
            "jakarta.validation.constraints.DecimalMin",
            "jakarta.validation.constraints.Digits",
            "jakarta.validation.constraints.Email",
            "jakarta.validation.constraints.Future",
            "jakarta.validation.constraints.FutureOrPresent",
            "jakarta.validation.constraints.Max",
            "jakarta.validation.constraints.Min",
            "jakarta.validation.constraints.Negative",
            "jakarta.validation.constraints.NegativeOrZero",
            "jakarta.validation.constraints.NotBlank",
            "jakarta.validation.constraints.NotEmpty",
            "jakarta.validation.constraints.NotNull",
            "jakarta.validation.constraints.Null",
            "jakarta.validation.constraints.Past",
            "jakarta.validation.constraints.PastOrPresent",
            "jakarta.validation.constraints.Pattern",
            "jakarta.validation.constraints.Positive",
            "jakarta.validation.constraints.PositiveOrZero",
            "jakarta.validation.constraints.Size",
            "jakarta.interceptor.AroundInvoke",
            "jakarta.annotation.PostConstruct",
    };

    private AnnotationInstance[] annotations;
    private DotName[] simpleNames;
    private DotName[] componentizedNames;
    private DotName presentSimple;
    private DotName presentComponentized;

    @Setup
    public void setup() {
        NameTable names = new NameTable();
        List<AnnotationInstance> list = new ArrayList<>();
        for (String name : PRESENT) {
            list.add(AnnotationInstance.builder(names.convertToName(name)).build());
        }
        list.sort(AnnotationInstance.NAME_COMPARATOR);
        annotations = list.toArray(new AnnotationInstance[0]);

        simpleNames = new DotName[LOOKED_UP.length];
        componentizedNames = new DotName[LOOKED_UP.length];
        for (int i = 0; i < LOOKED_UP.length; i++) {
            simpleNames[i] = DotName.createSimple(LOOKED_UP[i]);
            componentizedNames[i] = names.convertToName(LOOKED_UP[i]);
        }
        presentSimple = DotName.createSimple(PRESENT[1]);
        presentComponentized = new NameTable().convertToName(PRESENT[1]);

        for (DotName name : simpleNames) {
            if (AnnotationInstance.binarySearch(annotations, name) != null) {
                throw new IllegalStateException();
            }
        }
        if (AnnotationInstance.binarySearch(annotations, presentSimple) == null
                || AnnotationInstance.binarySearch(annotations, presentComponentized) == null) {
            throw new IllegalStateException();
        }
    }

    @Benchmark
    public void lookupMissingSimpleNames(Blackhole blackhole) {
        for (DotName name : simpleNames) {
            blackhole.consume(AnnotationInstance.binarySearch(annotations, name));
        }
    }

    @Benchmark
    public void lookupMissingComponentizedNames(Blackhole blackhole) {
        for (DotName name : componentizedNames) {
            blackhole.consume(AnnotationInstance.binarySearch(annotations, name));
        }
    }

    @Benchmark
    public AnnotationInstance lookupPresentSimpleName() {
        return AnnotationInstance.binarySearch(annotations, presentSimple);
    }

    @Benchmark
    public AnnotationInstance lookupPresentComponentizedName() {
        return AnnotationInstance.binarySearch(annotations, presentComponentized);
    }

    @Benchmark
    public void sortIndexerNames(Blackhole blackhole) {
        // sorting annotations by name is what the indexer does for every class, method and field
        AnnotationInstance[] copy = Arrays.copyOf(annotations, annotations.length);
        Arrays.sort(copy, AnnotationInstance.NAME_COMPARATOR);
        blackhole.consume(copy);
    }
}
