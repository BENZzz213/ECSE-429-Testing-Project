package ecse429.perf.support;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class CsvWriter {
    private CsvWriter() {
    }

    public static void write(final Path path, final List<String> header, final List<List<String>> rows) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(
                    path,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                writer.write(toCsvLine(header));
                writer.newLine();
                for (List<String> row : rows) {
                    writer.write(toCsvLine(row));
                    writer.newLine();
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write CSV file: " + path, exception);
        }
    }

    private static String toCsvLine(final List<String> columns) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(escape(columns.get(index)));
        }
        return builder.toString();
    }

    private static String escape(final String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
