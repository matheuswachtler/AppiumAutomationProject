package utils;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.extension.*;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName; // Importar DisplayName
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.lang.reflect.Method;

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

        // --- Lógica de extração do Display Name ---
        String fullDisplayName = context.getDisplayName();
        String testCode = fullDisplayName; // Valor padrão: o display name completo
        String descriptiveTestName = fullDisplayName; // Valor padrão: o display name completo

        int dashIndex = fullDisplayName.indexOf(" - ");
        if (dashIndex != -1) {
            testCode = fullDisplayName.substring(0, dashIndex);
            descriptiveTestName = fullDisplayName.substring(dashIndex + 3);
        }
        // --- Fim da lógica de extração do Display Name ---

        String reportFileName = testCode.replaceAll("[^a-zA-Z0-9.-]", "_");
        String platformName = ConfigReader.getProperty("platform.name");

        PdfReporter pdfReporter = new PdfReporter(contextName, reportFileName, platformName.toLowerCase());
        store.put("pdfReporter", pdfReporter);

        // Passa o nome descritivo para o PdfReporter
        pdfReporter.setTestName(descriptiveTestName);

        // NOVO: Passa o código do teste para o novo campo no PdfReporter
        pdfReporter.setNewInfoFieldContent(testCode); // <--- ALTERAÇÃO AQUI

        context.getElement()
               .filter(Method.class::isInstance)
               .map(method -> (Method) method)
               .map(method -> method.getAnnotation(Description.class))
               .ifPresent(descriptionAnnotation -> pdfReporter.setTestDescription(descriptionAnnotation.value()));

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
            pdfReporter.setLogsContent(capturedLogs);
            pdfReporter.setExecutionTimes(testStartTime, testEndTime);
            pdfReporter.setTestStatus(finalTestStatus);
            pdfReporter.closeReport();
        }

        try {
            if (baos != null) baos.close();
        } catch (IOException e) {
            System.err.println("Error closing ByteArrayOutputStream: " + e.getMessage());
        }
    }
}
