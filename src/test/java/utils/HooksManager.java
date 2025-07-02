package utils;

import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;

public class HooksManager {

    protected PdfReporter pdfReporter;
    private ByteArrayOutputStream baos;
    private PrintStream oldOut;
    private PrintStream oldErr;
    private LocalDateTime testStartTime;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        testStartTime = LocalDateTime.now();

        oldOut = System.out;
        oldErr = System.err;
        baos = new ByteArrayOutputStream();

        TeeOutputStream teeOut = new TeeOutputStream(oldOut, baos);
        TeeOutputStream teeErr = new TeeOutputStream(oldErr, baos);

        System.setOut(new PrintStream(teeOut, true));
        System.setErr(new PrintStream(teeErr, true));

        System.out.println("Starting Appium session...");

        String contextName = testInfo.getTags().stream()
                                     .findFirst()
                                     .orElse("general");

        String fullDisplayName = testInfo.getDisplayName();
        String reportName = fullDisplayName.split(" - ")[0].replaceAll("[^a-zA-Z0-9.-]", "_");

        String platformName = ConfigReader.getProperty("platform.name");

        this.pdfReporter = new PdfReporter(contextName, reportName, platformName.toLowerCase());

        DriverManager.initializeDriver(this.pdfReporter);
        System.out.println("Appium session started.");
    }

    @AfterEach
    public void tearDown(TestInfo testInfo) {
        System.out.println("Closing Appium session...");
        DriverManager.quitDriver();

        System.setOut(oldOut);
        System.setErr(oldErr);

        String capturedLogs = baos != null ? baos.toString() : "";
        if (oldOut != null) {
            oldOut.println(capturedLogs);
        }

        LocalDateTime testEndTime = LocalDateTime.now();

        if (this.pdfReporter != null) {
            String finalTestStatus = "COMPLETED";

            pdfReporter.setLogsContent(capturedLogs);
            pdfReporter.setExecutionTimes(testStartTime, testEndTime);
            pdfReporter.setTestStatus(finalTestStatus);
            pdfReporter.closeReport();
        }

        System.out.println("Appium session closed.");

        try {
            if (baos != null) {
                baos.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing ByteArrayOutputStream: " + e.getMessage());
        }
    }
}
