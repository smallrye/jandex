package org.jboss.jandex;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.Throughput)
@Fork(5)
@Warmup(iterations = 5, time = 1, batchSize = 8192)
@Measurement(iterations = 5, time = 1, batchSize = 8192)
@State(Scope.Benchmark)
public class IndexContainsAnnotationBenchmark {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Existing {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface NonExisting {
    }

    public static class First {
        @Existing
        private String a;

        @Existing
        private String b;
    }

    public static class Second {
        @Existing
        private String a;
    }

    @Param({ "Existing", "NonExisting" })
    private String paramAnnotationName;

    private DotName annotationName;

    private Index index;
    private CompositeIndex compositeIndex;

    @Setup
    public void setup() throws IOException {
        annotationName = DotName.createSimple(IndexContainsAnnotationBenchmark.class.getName() + "$" + paramAnnotationName);

        {
            Indexer indexer = new Indexer();
            indexer.indexClass(First.class);
            indexer.indexClass(Existing.class);
            indexer.indexClass(NonExisting.class);
            index = indexer.complete();
        }

        {
            List<IndexView> indexes = new ArrayList<>();
            {
                Indexer indexer = new Indexer();
                indexer.indexClass(First.class);
                indexer.indexClass(Existing.class);
                indexer.indexClass(NonExisting.class);
                indexes.add(indexer.complete());
            }

            {
                Indexer indexer = new Indexer();
                indexer.indexClass(Second.class);
                indexer.indexClass(Existing.class);
                indexer.indexClass(NonExisting.class);
                indexes.add(indexer.complete());
            }

            {
                Indexer indexer = new Indexer();
                indexer.indexClass(Second.class);
                for (int i = 0; i < 10000; i++) {
                    indexer.indexClass(Existing.class);
                }
                indexes.add(indexer.complete());
            }

            this.compositeIndex = CompositeIndex.create(indexes);
        }
    }

    @Benchmark
    public boolean indexContainsAnnotation() {
        return index.containsAnnotation(annotationName);
    }

    @Benchmark
    public boolean indexGetAnnotationNonEmpty() {
        return !index.getAnnotations(annotationName).isEmpty();
    }

    @Benchmark
    public boolean compositeIndexContainsAnnotation() {
        return compositeIndex.containsAnnotation(annotationName);
    }

    @Benchmark
    public boolean compositeIndexGetAnnotationNonEmpty() {
        return !compositeIndex.getAnnotations(annotationName).isEmpty();
    }
}
