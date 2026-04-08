package ecse429.perf;

import ecse429.perf.support.ApiClient;
import ecse429.perf.support.CsvWriter;
import ecse429.perf.support.RandomTodoFactory;
import ecse429.perf.support.TodoSeeder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class TodoPerformanceRunner {
    private TodoPerformanceRunner() {
    }

    public static void main(final String[] args) {
        PerfConfig config = PerfConfig.fromArgs(args);
        ApiClient client = new ApiClient(config.baseUrl());
        RandomTodoFactory factory = new RandomTodoFactory();
        TodoSeeder seeder = new TodoSeeder(client, factory);

        client.requireServiceUp();
        List<String> availableIds = new ArrayList<>(seeder.seedToCount(config.startingCount()));

        List<Sample> samples = switch (config.operation()) {
            case CREATE -> runCreateExperiment(config, client, factory, availableIds);
            case UPDATE -> runUpdateExperiment(config, client, factory, availableIds);
            case DELETE -> runDeleteExperiment(config, client, factory, seeder, availableIds);
        };

        writeTransactionCsv(config, samples);
        writeSummaryCsv(config, samples);
    }

    private static List<Sample> runCreateExperiment(
            final PerfConfig config,
            final ApiClient client,
            final RandomTodoFactory factory,
            final List<String> availableIds) {
        List<Sample> samples = new ArrayList<>();

        for (int iteration = 1; iteration <= config.warmupIterations(); iteration++) {
            RandomTodoFactory.TodoPayload payload = factory.nextPayload();
            long started = System.nanoTime();
            String todoId = client.createTodo(payload.title(), payload.doneStatus(), payload.description());
            long elapsed = System.nanoTime() - started;
            availableIds.add(todoId);
            samples.add(Sample.warmup(config.startingCount(), config.operation(), iteration, todoId, 201, elapsed));
        }

        for (int iteration = 1; iteration <= config.measuredIterations(); iteration++) {
            RandomTodoFactory.TodoPayload payload = factory.nextPayload();
            long started = System.nanoTime();
            String todoId = client.createTodo(payload.title(), payload.doneStatus(), payload.description());
            long elapsed = System.nanoTime() - started;
            availableIds.add(todoId);
            samples.add(Sample.measured(config.startingCount(), config.operation(), iteration, todoId, 201, elapsed));
        }

        return samples;
    }

    private static List<Sample> runUpdateExperiment(
            final PerfConfig config,
            final ApiClient client,
            final RandomTodoFactory factory,
            final List<String> availableIds) {
        List<Sample> samples = new ArrayList<>();

        for (int iteration = 1; iteration <= config.warmupIterations(); iteration++) {
            String todoId = pickRandomId(availableIds);
            RandomTodoFactory.TodoPayload payload = factory.nextPayload();
            long started = System.nanoTime();
            client.updateTodo(todoId, payload.title(), payload.doneStatus(), payload.description());
            long elapsed = System.nanoTime() - started;
            samples.add(Sample.warmup(config.startingCount(), config.operation(), iteration, todoId, 200, elapsed));
        }

        for (int iteration = 1; iteration <= config.measuredIterations(); iteration++) {
            String todoId = pickRandomId(availableIds);
            RandomTodoFactory.TodoPayload payload = factory.nextPayload();
            long started = System.nanoTime();
            client.updateTodo(todoId, payload.title(), payload.doneStatus(), payload.description());
            long elapsed = System.nanoTime() - started;
            samples.add(Sample.measured(config.startingCount(), config.operation(), iteration, todoId, 200, elapsed));
        }

        return samples;
    }

    private static List<Sample> runDeleteExperiment(
            final PerfConfig config,
            final ApiClient client,
            final RandomTodoFactory factory,
            final TodoSeeder seeder,
            final List<String> availableIds) {
        List<Sample> samples = new ArrayList<>();

        List<String> warmupIds = seeder.createAdditionalTodos(config.warmupIterations());
        for (int iteration = 1; iteration <= config.warmupIterations(); iteration++) {
            String todoId = warmupIds.get(iteration - 1);
            long started = System.nanoTime();
            client.deleteTodo(todoId);
            long elapsed = System.nanoTime() - started;
            samples.add(Sample.warmup(config.startingCount(), config.operation(), iteration, todoId, 200, elapsed));
        }

        if (availableIds.size() < config.measuredIterations()) {
            throw new IllegalStateException("Need at least " + config.measuredIterations()
                    + " existing todos for measured delete run, but only have " + availableIds.size());
        }

        Collections.shuffle(availableIds, ThreadLocalRandom.current());
        for (int iteration = 1; iteration <= config.measuredIterations(); iteration++) {
            String todoId = availableIds.remove(0);
            long started = System.nanoTime();
            client.deleteTodo(todoId);
            long elapsed = System.nanoTime() - started;
            samples.add(Sample.measured(config.startingCount(), config.operation(), iteration, todoId, 200, elapsed));
        }

        return samples;
    }

    private static String pickRandomId(final List<String> ids) {
        if (ids.isEmpty()) {
            throw new IllegalStateException("No todo ids are available for the requested operation");
        }
        int index = ThreadLocalRandom.current().nextInt(ids.size());
        return ids.get(index);
    }

    private static void writeTransactionCsv(final PerfConfig config, final List<Sample> samples) {
        List<List<String>> rows = new ArrayList<>();
        for (Sample sample : samples) {
            rows.add(List.of(
                    Integer.toString(sample.startingObjectCount()),
                    sample.operation().name().toLowerCase(Locale.ROOT),
                    sample.phase(),
                    Integer.toString(sample.iteration()),
                    sample.todoId(),
                    Integer.toString(sample.expectedStatus()),
                    formatMillis(sample.latencyNanos())));
        }

        CsvWriter.write(
                config.transactionCsv(),
                List.of("startingObjectCount", "operation", "phase", "iteration", "todoId", "statusCode", "latencyMs"),
                rows);
    }

    private static void writeSummaryCsv(final PerfConfig config, final List<Sample> samples) {
        List<Double> measuredLatencies = samples.stream()
                .filter(sample -> sample.phase().equals("measured"))
                .map(sample -> sample.latencyNanos() / 1_000_000.0)
                .sorted(Comparator.naturalOrder())
                .toList();

        if (measuredLatencies.isEmpty()) {
            throw new IllegalStateException("No measured samples were collected");
        }

        double mean = measuredLatencies.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        double median = percentile(measuredLatencies, 0.50);
        double p95 = percentile(measuredLatencies, 0.95);

        CsvWriter.write(
                config.summaryCsv(),
                List.of("startingObjectCount", "operation", "measuredIterations", "meanLatencyMs", "medianLatencyMs", "p95LatencyMs"),
                List.of(List.of(
                        Integer.toString(config.startingCount()),
                        config.operation().name().toLowerCase(Locale.ROOT),
                        Integer.toString(config.measuredIterations()),
                        formatMillis(mean),
                        formatMillis(median),
                        formatMillis(p95))));
    }

    private static double percentile(final List<Double> sortedValues, final double percentile) {
        int index = (int) Math.ceil(percentile * sortedValues.size()) - 1;
        int boundedIndex = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(boundedIndex);
    }

    private static String formatMillis(final long nanos) {
        return formatMillis(nanos / 1_000_000.0);
    }

    private static String formatMillis(final double millis) {
        return String.format(Locale.US, "%.3f", millis);
    }

    private record Sample(
            int startingObjectCount,
            PerfConfig.Operation operation,
            String phase,
            int iteration,
            String todoId,
            int expectedStatus,
            long latencyNanos) {

        private static Sample warmup(
                final int startingObjectCount,
                final PerfConfig.Operation operation,
                final int iteration,
                final String todoId,
                final int expectedStatus,
                final long latencyNanos) {
            return new Sample(startingObjectCount, operation, "warmup", iteration, todoId, expectedStatus, latencyNanos);
        }

        private static Sample measured(
                final int startingObjectCount,
                final PerfConfig.Operation operation,
                final int iteration,
                final String todoId,
                final int expectedStatus,
                final long latencyNanos) {
            return new Sample(startingObjectCount, operation, "measured", iteration, todoId, expectedStatus, latencyNanos);
        }
    }
}
