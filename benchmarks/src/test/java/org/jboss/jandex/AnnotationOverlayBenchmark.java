package org.jboss.jandex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

@BenchmarkMode(Mode.Throughput)
@Fork(5)
@Warmup(iterations = 5, time = 1, batchSize = 8192)
@Measurement(iterations = 5, time = 1, batchSize = 8192)
@State(Scope.Benchmark)
public class AnnotationOverlayBenchmark {
    private Index index;
    private AnnotationOverlay overlay;

    @Setup
    public void setup() throws IOException {
        index = Index.of(MyAnn1.class, MyAnn2.class, MyClass1.class, MyClass2.class, MyClass3.class,
                MyClass4.class, MyClass5.class, MyClass6.class, MyClass7.class, MyClass8.class, MyClass9.class);

        AnnotationTransformation transformation = new AnnotationTransformation() {
            @Override
            public boolean supports(AnnotationTarget.Kind kind) {
                return kind == AnnotationTarget.Kind.CLASS;
            }

            @Override
            public void apply(TransformationContext context) {
                String name = context.declaration().asClass().name().toString();
                if (name.endsWith("2") || name.endsWith("4")) {
                    context.removeAll();
                } else if (name.endsWith("6") || name.endsWith("8")) {
                    context.add(MyAnn2.class);
                }
            }
        };
        overlay = AnnotationOverlay.builder(index, Collections.singleton(transformation)).build();
    }

    @Benchmark
    public List<AnnotationInstance> noTransformation() {
        List<AnnotationInstance> result = new ArrayList<>(5);

        ClassInfo clazz = index.getClassByName(MyClass1.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass3.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass5.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass7.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass9.class);
        result.addAll(overlay.annotations(clazz));

        return result;
    }

    @Benchmark
    public List<AnnotationInstance> removedAnnotations() {
        List<AnnotationInstance> result = new ArrayList<>(0);

        ClassInfo clazz = index.getClassByName(MyClass2.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass4.class);
        result.addAll(overlay.annotations(clazz));

        return result;
    }

    @Benchmark
    public List<AnnotationInstance> addedAnnotations() {
        List<AnnotationInstance> result = new ArrayList<>(4);

        ClassInfo clazz = index.getClassByName(MyClass6.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass8.class);
        result.addAll(overlay.annotations(clazz));

        return result;
    }

    @Benchmark
    public List<AnnotationInstance> addedAndRemovedAnnotations() {
        List<AnnotationInstance> result = new ArrayList<>(4);

        ClassInfo clazz = index.getClassByName(MyClass2.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass4.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass6.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass8.class);
        result.addAll(overlay.annotations(clazz));

        return result;
    }

    @Benchmark
    public List<AnnotationInstance> allClasses() {
        List<AnnotationInstance> result = new ArrayList<>(9);

        ClassInfo clazz = index.getClassByName(MyClass1.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass2.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass3.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass4.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass5.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass6.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass7.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass8.class);
        result.addAll(overlay.annotations(clazz));

        clazz = index.getClassByName(MyClass9.class);
        result.addAll(overlay.annotations(clazz));

        return result;
    }

    public @interface MyAnn1 {
    }

    public @interface MyAnn2 {
    }

    @MyAnn1
    public static class MyClass1 {
    }

    @MyAnn1
    public static class MyClass2 {
    }

    @MyAnn1
    public static class MyClass3 {
    }

    @MyAnn1
    public static class MyClass4 {
    }

    @MyAnn1
    public static class MyClass5 {
    }

    @MyAnn1
    public static class MyClass6 {
    }

    @MyAnn1
    public static class MyClass7 {
    }

    @MyAnn1
    public static class MyClass8 {
    }

    @MyAnn1
    public static class MyClass9 {
    }
}
