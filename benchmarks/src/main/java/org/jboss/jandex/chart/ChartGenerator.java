package org.jboss.jandex.chart;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.jandex.json.BenchmarkDto;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;

import com.google.gson.Gson;
import org.openjdk.jmh.annotations.Mode;

public class ChartGenerator {
    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            return;
        }

        List<Path> files = new ArrayList<>();
        for (String arg : args) {
            Path file = Paths.get(arg);
            if (!Files.isRegularFile(file) && !Files.isReadable(file)) {
                throw new IllegalArgumentException("Can't read results file: " + file);
            }
            files.add(file);
        }

        AllBenchmarks allBenchmarks = new AllBenchmarks();
        for (Path file : files) {
            String version = file.getFileName().toString()
                    .replace("results-", "")
                    .replace(".json", "");
            for (BenchmarkDto benchmark : readJson(file)) {
                allBenchmarks.add(version, benchmark);
            }
        }

        for (Map.Entry<Mode, List<BenchmarksForVersion>> entry : allBenchmarks.map.entrySet()) {
            Mode mode = entry.getKey();
            List<BenchmarksForVersion> allVersions = entry.getValue();

            String unit = findCommonUnit(allVersions);

            CategoryChart chart = new CategoryChartBuilder()
                    .width(1280)
                    .height(1024)
                    .title("Jandex Microbenchmarks")
                    .xAxisTitle("Benchmark")
                    .yAxisTitle(mode.longLabel() + " (" + unit + ")")
                    .build();
            chart.getStyler()
                    .setXAxisTicksVisible(true)
                    .setXAxisLabelRotation(90);

            for (BenchmarksForVersion benchmarksForVersion : allVersions) {
                List<String> names = new ArrayList<>();
                List<BigDecimal> scores = new ArrayList<>();
                List<BigDecimal> errors = new ArrayList<>();

                for (BenchmarkDto benchmark : benchmarksForVersion.benchmarks) {
                    names.add(benchmark.shortName());
                    scores.add(benchmark.primaryMetric.score);
                    errors.add(benchmark.primaryMetric.scoreError);
                }

                chart.addSeries(benchmarksForVersion.version, names, scores, errors);
            }

            BitmapEncoder.saveBitmap(chart, "target/jandex-microbenchmarks-" + mode.shortLabel(), BitmapFormat.PNG);
        }
    }

    private static BenchmarkDto[] readJson(Path inputFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, BenchmarkDto[].class);
        }
    }

    private static String findCommonUnit(List<BenchmarksForVersion> allVersions) {
        Set<String> allUnits = allVersions.stream()
                .flatMap(it -> it.benchmarks.stream())
                .map(it -> it.primaryMetric.scoreUnit)
                .collect(Collectors.toSet());
        if (allUnits.size() != 1) {
            // this should never happen
            throw new IllegalStateException("Same mode benchmarks have different units: " + allUnits);
        }
        return allUnits.iterator().next();
    }
}
