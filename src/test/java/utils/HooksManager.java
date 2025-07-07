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

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);

        LocalDateTime testStartTime = LocalDateTime.now();
        store.put("testStartTime", testStartTime);

        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        TeeOutputStream teeOut = new TeeOutputStream(oldOut, baos);
        TeeOutputStream teeErr = new TeeOutputStream(oldErr, baos);

        System.setOut(new PrintStream(teeOut, true));
        System.setErr(new PrintStream(teeErr, true));

        store.put("oldOut", oldOut);
        store.put("oldErr", oldErr);
        store.put("baos", baos);

        String contextName = context.getTags().stream().findFirst().orElse("general");

        String fullDisplayName = context.getDisplayName();
        String testCode = fullDisplayName;
        String descriptiveTestName = fullDisplayName;

        int dashIndex = fullDisplayName.indexOf(" - ");
        if (dashIndex != -1) {
            testCode = fullDisplayName.substring(0, dashIndex);
            descriptiveTestName = fullDisplayName.substring(dashIndex + 3);
        }

        String reportFileName = testCode.replaceAll("[^a-zA-Z0-9.-]", "_");
        String platformName = ConfigReader.getProperty("platform.name");

        PdfReporter pdfReporter = new PdfReporter(contextName, reportFileName, platformName.toLowerCase());
        store.put("pdfReporter", pdfReporter);

        TestReportData reportData = pdfReporter.getReportData();

        reportData.setTestName(descriptiveTestName);
        reportData.setNewInfoFieldContent(testCode);

        String gitUserName = getGitConfig("user.name");
        if (gitUserName != null && !gitUserName.isEmpty()) {
            reportData.setResponsibleContent(gitUserName.toUpperCase());
        } else {
            String systemUserName = System.getProperty("user.name");
            reportData.setResponsibleContent(systemUserName != null ? systemUserName.toUpperCase() : "N/A");
            System.err.println("Could not get Git user.name. Falling back to system user.name: " + (systemUserName != null ? systemUserName : "N/A"));
        }

        context.getElement()
               .filter(Method.class::isInstance)
               .map(method -> (Method) method)
               .map(method -> method.getAnnotation(Description.class))
               .ifPresent(descriptionAnnotation -> reportData.setTestDescription(descriptionAnnotation.value()));

        DriverManager.initializeDriver(pdfReporter);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);

        String finalTestStatus = "SUCCESS";
        Throwable throwable = context.getExecutionException().orElse(null);
        if (throwable != null) {
            finalTestStatus = "FAILURE";
        }

        System.out.println("Closing Appium session...");
        DriverManager.quitDriver();

        PrintStream oldOut = store.remove("oldOut", PrintStream.class);
        PrintStream oldErr = store.remove("oldErr", PrintStream.class);
        ByteArrayOutputStream baos = store.remove("baos", ByteArrayOutputStream.class);

        if (oldOut != null) System.setOut(oldOut);
        if (oldErr != null) System.setErr(oldErr);

        String capturedLogs = baos != null ? baos.toString() : "";
        if (oldOut != null) oldOut.println(capturedLogs);

        LocalDateTime testStartTime = store.remove("testStartTime", LocalDateTime.class);
        LocalDateTime testEndTime = LocalDateTime.now();

        PdfReporter pdfReporter = store.remove("pdfReporter", PdfReporter.class);
        if (pdfReporter != null) {
            TestReportData reportData = pdfReporter.getReportData();

            reportData.setLogsContent(capturedLogs);
            reportData.setExecutionTimes(testStartTime, testEndTime);
            reportData.setTestStatus(finalTestStatus);
            pdfReporter.closeReport();
        }

        try {
            if (baos != null) baos.close();
        } catch (IOException e) {
            System.err.println("Error closing ByteArrayOutputStream: " + e.getMessage());
        }
    }

    private String getGitConfig(String configKey) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("git", "config", configKey);
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
                System.err.println("Git command 'git config " + configKey + "' failed with exit code: " + exitCode);
                System.err.println("Git command output: " + output.toString());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing git command 'git config " + configKey + "': " + e.getMessage());
            return null;
        }
    }
}