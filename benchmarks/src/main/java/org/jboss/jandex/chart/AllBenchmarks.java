package org.jboss.jandex.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.json.BenchmarkDto;
import org.openjdk.jmh.annotations.Mode;

public class AllBenchmarks {
    public final Map<Mode, List<BenchmarksForVersion>> map = new HashMap<>();

    public void add(String version, BenchmarkDto benchmark) {
        Mode mode = Mode.deepValueOf(benchmark.mode);
        List<BenchmarksForVersion> list = map.computeIfAbsent(mode, ignored -> new ArrayList<>());

        // find existing `BenchmarksForVersion`
        for (BenchmarksForVersion benchmarks : list) {
            if (version.equals(benchmarks.version)) {
                benchmarks.add(benchmark);
                return;
            }
        }

        // no existing `BenchmarksForVersion` found
        BenchmarksForVersion benchmarks = new BenchmarksForVersion(version);
        benchmarks.add(benchmark);
        list.add(benchmarks);
    }
}
