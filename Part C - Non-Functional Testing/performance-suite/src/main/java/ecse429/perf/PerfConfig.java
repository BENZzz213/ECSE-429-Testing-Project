package ecse429.perf;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PerfConfig {
    private final Operation operation;
    private final int startingCount;
    private final int warmupIterations;
    private final int measuredIterations;
    private final String baseUrl;
    private final Path transactionCsv;
    private final Path summaryCsv;

    private PerfConfig(
            final Operation operation,
            final int startingCount,
            final int warmupIterations,
            final int measuredIterations,
            final String baseUrl,
            final Path transactionCsv,
            final Path summaryCsv) {
        this.operation = operation;
        this.startingCount = startingCount;
        this.warmupIterations = warmupIterations;
        this.measuredIterations = measuredIterations;
        this.baseUrl = baseUrl;
        this.transactionCsv = transactionCsv;
        this.summaryCsv = summaryCsv;
    }

    public static PerfConfig fromArgs(final String[] args) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String arg : args) {
            if (!arg.startsWith("--") || !arg.contains("=")) {
                throw new IllegalArgumentException("Expected --key=value argument but received: " + arg);
            }

            int separator = arg.indexOf('=');
            String key = arg.substring(2, separator);
            String value = arg.substring(separator + 1);
            values.put(key, value);
        }

        return new PerfConfig(
                Operation.from(values.get(required("operation", values))),
                parsePositiveInt("starting-count", values.get(required("starting-count", values))),
                parsePositiveInt("warmup", values.getOrDefault("warmup", "20")),
                parsePositiveInt("measured", values.getOrDefault("measured", "100")),
                values.getOrDefault("base-url", "http://localhost:4567"),
                Path.of(values.get(required("transaction-csv", values))),
                Path.of(values.get(required("summary-csv", values))));
    }

    private static String required(final String key, final Map<String, String> values) {
        if (!values.containsKey(key) || values.get(key).isBlank()) {
            throw new IllegalArgumentException("Missing required argument --" + key + "=...");
        }
        return key;
    }

    private static int parsePositiveInt(final String label, final String value) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(label + " must be > 0");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be an integer: " + value, exception);
        }
    }

    public Operation operation() {
        return operation;
    }

    public int startingCount() {
        return startingCount;
    }

    public int warmupIterations() {
        return warmupIterations;
    }

    public int measuredIterations() {
        return measuredIterations;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public Path transactionCsv() {
        return transactionCsv;
    }

    public Path summaryCsv() {
        return summaryCsv;
    }

    public enum Operation {
        CREATE,
        UPDATE,
        DELETE;

        public static Operation from(final String value) {
            if (value == null) {
                throw new IllegalArgumentException("Operation must not be null");
            }
            return Operation.valueOf(value.trim().toUpperCase());
        }
    }
}
