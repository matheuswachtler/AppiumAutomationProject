package utils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class TimestampedPrintStream extends PrintStream {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\| ");

    public TimestampedPrintStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        String line = new String(buf, off, len);
        if (line.trim().isEmpty()) {
            super.write(buf, off, len);
            return;
        }

        if (TIMESTAMP_PATTERN.matcher(line).find()) {
            super.write(buf, off, len);
        } else {
            String timestampedLine = LocalDateTime.now().format(FORMATTER) + " | " + line;
            super.write(timestampedLine.getBytes(), 0, timestampedLine.length());
        }
    }

    @Override
    public void print(String s) {
        if (s != null && !s.trim().isEmpty()) {
            if (TIMESTAMP_PATTERN.matcher(s).find()) {
                super.print(s);
            } else {
                String timestamp = LocalDateTime.now().format(FORMATTER);
                super.print(timestamp + " | " + s);
            }
        } else {
            super.print(s);
        }
    }
}
