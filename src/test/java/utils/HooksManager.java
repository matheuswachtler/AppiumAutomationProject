package utils;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.extension.*;
import utils.api.ApiOrchestrator;
import utils.report.PdfReporter;
import utils.report.TestReportData;
import utils.report.TimestampedPrintStream;
import utils.tests.TestUtils;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.lang.reflect.Method;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

public class HooksManager implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(HooksManager.class);
    private static final String GIT_USER_NAME_CONFIG_KEY = "user.name";

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream byteArrayOutputStream;

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        // Inicialização do System.out antes de qualquer log do nosso código
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

        // --- Lógica de orquestração de API (movida para depois da inicialização do logger) ---
        String scriptName = context.getRequiredTestMethod().getName();
        Map<String, String> apiConfig = TestUtils.getApiConfig(scriptName);

        if (!apiConfig.isEmpty()) {
            System.out.println("[HOOKS MANAGER] API config found for script: " + scriptName);
            ApiOrchestrator apiOrchestrator = new ApiOrchestrator();
            for (Map.Entry<String, String> entry : apiConfig.entrySet()) {
                String apiName = entry.getKey();
                String trigger = entry.getValue();
                if ("TRUE".equalsIgnoreCase(trigger)) {
                    // Passando o mapa completo caso a API precise de outros dados além do trigger.
                    apiOrchestrator.callApi(apiName, apiConfig);
                }
            }
            System.out.println("[HOOKS MANAGER] API calls finished. Proceeding with test execution.");
        }
        // --- Fim da nova lógica ---

        Method testMethod = context.getRequiredTestMethod();
        String contextName = testMethod.getDeclaringClass().getSimpleName();
        String reportName = testMethod.getName();
        String responsible = getGitConfig();
        String platform = System.getProperty("platformName", "android").toLowerCase();

        PdfReporter pdfReporter = new PdfReporter(contextName, reportName, platform);
        DriverManager.initializeDriver(pdfReporter);

        store.put("pdfReporter", pdfReporter);
        store.put("responsible", responsible);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        PdfReporter pdfReporter = store.get("pdfReporter", PdfReporter.class);
        LocalDateTime testStartTime = store.get("testStartTime", LocalDateTime.class);
        LocalDateTime testEndTime = LocalDateTime.now();
        boolean testFailed = context.getExecutionException().isPresent();
        String testStatus = testFailed ? "FAIL" : "PASS";

        // Restore original System.out and System.err
        System.out.flush();
        System.err.flush();
        System.setOut(originalOut);
        System.setErr(originalErr);

        if (pdfReporter != null) {
            TestReportData reportData = pdfReporter.getReportData();
            reportData.setTestStatus(testStatus);
            reportData.setExecutionTimes(testStartTime, testEndTime);
            reportData.setLogsContent(byteArrayOutputStream.toString());

            String responsible = store.get("responsible", String.class);
            reportData.setResponsibleContent(responsible);

            Method testMethod = context.getRequiredTestMethod();

            // Corrigido: usar @DisplayName se existir, senão nome do método
            String testName;
            if (testMethod.isAnnotationPresent(DisplayName.class)) {
                testName = testMethod.getAnnotation(DisplayName.class).value();
            } else {
                testName = testMethod.getName();
            }
            reportData.setTestName(testName);

            if (testMethod.isAnnotationPresent(jdk.jfr.Description.class)) {
                String description = testMethod.getAnnotation(jdk.jfr.Description.class).value();
                reportData.setTestDescription(description);
            }

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