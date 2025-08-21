package utils;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.extension.*;
import jdk.jfr.Description;
import utils.report.PdfReporter;
import utils.report.TestReportData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.lang.reflect.Method;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HooksManager implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(HooksManager.class);
    private static final String GIT_USER_NAME_CONFIG_KEY = "user.name";

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream byteArrayOutputStream;

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);

        LocalDateTime testStartTime = LocalDateTime.now();
        store.put("testStartTime", testStartTime);

        this.originalOut = System.out;
        this.originalErr = System.err;
        this.byteArrayOutputStream = new ByteArrayOutputStream();

        TeeOutputStream teeOut = new TeeOutputStream(this.originalOut, this.byteArrayOutputStream);
        TeeOutputStream teeErr = new TeeOutputStream(this.originalErr, this.byteArrayOutputStream);

        System.setOut(new TimestampedPrintStream(teeOut));
        System.setErr(new TimestampedPrintStream(teeErr));

        Method testMethod = context.getRequiredTestMethod();
        String testCaseId = context.getDisplayName();
        String description = null;
        if (testMethod.isAnnotationPresent(Description.class)) {
            description = testMethod.getAnnotation(Description.class).value();
        }

        String testPackageName = context.getTestClass()
                .map(Class::getPackageName)
                .orElse("");
        String contextName = testPackageName.substring(testPackageName.lastIndexOf('.') + 1);

        String reportName = testCaseId.split(" ")[0];

        PdfReporter pdfReporter = new PdfReporter(
                contextName,
                reportName,
                ConfigReader.getProperty("platform.name")
        );

        pdfReporter.getReportData().setTestDescription(description);
        pdfReporter.getReportData().setTestName(testCaseId);

        String responsible = getGitConfig();
        pdfReporter.getReportData().setResponsibleContent(responsible);

        DriverManager.initializeDriver(pdfReporter);

        store.put("pdfReporter", pdfReporter);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);

        System.setOut(this.originalOut);
        System.setErr(this.originalErr);

        TestReportData reportData = DriverManager.getPdfReporter().getReportData();
        reportData.setLogsContent(byteArrayOutputStream.toString());

        LocalDateTime testEndTime = LocalDateTime.now();
        reportData.setExecutionTimes((LocalDateTime) store.get("testStartTime"), testEndTime);

        if (context.getExecutionException().isPresent()) {
            reportData.setTestStatus("Failed");
            System.err.println("TEST FAILED: " + context.getExecutionException().get().getMessage());
        } else {
            reportData.setTestStatus("Passed");
            System.out.println("TEST PASSED.");
        }

        DriverManager.quitDriver();

        PdfReporter pdfReporter = (PdfReporter) store.get("pdfReporter");
        if (pdfReporter != null) {
            pdfReporter.closeReport();
        }

        try {
            if (byteArrayOutputStream != null) byteArrayOutputStream.close();
        } catch (IOException e) {
            System.err.println("Error closing ByteArrayOutputStream: " + e.getMessage());
        }
    }

    private String getGitConfig() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("git", "config", GIT_USER_NAME_CONFIG_KEY);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return output.toString().trim();
            } else {
                System.err.println("Git command 'git config " + GIT_USER_NAME_CONFIG_KEY + "' failed with exit code: " + exitCode);
                System.err.println("Git command output: " + output);
                return null;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing git command 'git config " + GIT_USER_NAME_CONFIG_KEY + "': " + e.getMessage());
            return null;
        }
    }
}