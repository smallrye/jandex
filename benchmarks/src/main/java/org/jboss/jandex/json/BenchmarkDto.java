package org.jboss.jandex.json;

public class BenchmarkDto {
    public String benchmark;
    public String mode;
    public PrimaryMetricDto primaryMetric;

    public String shortName() {
        String[] parts = benchmark.split("\\.");
        return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }
}
