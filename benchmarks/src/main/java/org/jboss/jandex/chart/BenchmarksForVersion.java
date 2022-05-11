package org.jboss.jandex.chart;

import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.json.BenchmarkDto;

public class BenchmarksForVersion {
    public final String version;
    public final List<BenchmarkDto> benchmarks = new ArrayList<>();

    public BenchmarksForVersion(String version) {
        this.version = version;
    }

    public void add(BenchmarkDto benchmark) {
        benchmarks.add(benchmark);
    }
}
