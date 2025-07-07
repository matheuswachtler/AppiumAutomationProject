package utils.report;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class TestReportData {
    private final String testNumber;
    private String logsContent = "";
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String testStatus = "N/A";
    private String testName = "";
    private String testDescription = "";
    private String newInfoFieldContent = "N/A";
    private String responsibleContent = "N/A";

    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public TestReportData(String reportName) {
        this.testNumber = reportName;
        this.testName = reportName;
    }

    public String getTestNumber() {
        return testNumber;
    }

    public String getLogsContent() {
        return logsContent;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getTestStatus() {
        return testStatus;
    }

    public String getTestName() {
        return testName;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public String getNewInfoFieldContent() {
        return newInfoFieldContent;
    }

    public String getResponsibleContent() {
        return responsibleContent;
    }

    public void setLogsContent(String logs) {
        this.logsContent = logs;
    }

    public void setExecutionTimes(LocalDateTime start, LocalDateTime end) {
        this.startTime = start;
        this.endTime = end;
    }

    public void setTestStatus(String status) {
        this.testStatus = status != null ? status.toUpperCase() : "N/A";
    }

    public void setTestName(String testName) {
        this.testName = testName != null ? testName.toUpperCase() : "";
    }

    public void setTestDescription(String description) {
        this.testDescription = description != null ? description.toUpperCase() : "";
    }

    public void setNewInfoFieldContent(String content) {
        this.newInfoFieldContent = content != null ? content : "N/A";
    }

    public void setResponsibleContent(String content) {
        this.responsibleContent = content != null ? content : "N/A";
    }

    public String getFormattedExecutionDate() {
        return (startTime != null) ? startTime.format(DISPLAY_DATE_TIME_FORMATTER).toUpperCase() : "N/A";
    }

    public String getFormattedExecutionTime() {
        if (startTime != null && endTime != null) {
            Duration duration = Duration.between(startTime, endTime);
            long seconds = duration.getSeconds();
            return String.format("%d MIN %d SEG", seconds / 60, seconds % 60);
        }
        return "N/A";
    }
}