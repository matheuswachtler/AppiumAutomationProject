package utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo; // Importar TestInfo para obter o nome do teste
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Optional;

public class HooksManager {

    protected PdfReporter pdfReporter;
    private ByteArrayOutputStream baos;
    private PrintStream oldOut;
    private PrintStream oldErr;
    private LocalDateTime testStartTime; // Para capturar o tempo de início

    // Usaremos o TestInfo para obter o nome do teste e tags
    @BeforeEach
    public void setup(TestInfo testInfo) {
        testStartTime = LocalDateTime.now(); // Captura o tempo de início

        // Redireciona System.out e System.err para capturar logs
        oldOut = System.out;
        oldErr = System.err;
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        System.setErr(new PrintStream(baos));

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
    public void tearDown(TestInfo testInfo) { // Injetar TestInfo aqui também para acessar o status
        System.out.println("Closing Appium session...");
        DriverManager.quitDriver();

        // Restaurar os streams originais ANTES de imprimir os logs capturados
        System.setOut(oldOut);
        System.setErr(oldErr);

        String capturedLogs = baos != null ? baos.toString() : "";
        if (oldOut != null) {
            oldOut.println(capturedLogs);
        }

        LocalDateTime testEndTime = LocalDateTime.now();

        if (this.pdfReporter != null) {
            String finalTestStatus = "UNKNOWN";
            if (testInfo.getTestClass().isPresent() && testInfo.getTestMethod().isPresent()) {
                finalTestStatus = "COMPLETED";
            }

            pdfReporter.setLogsContent(capturedLogs);
            pdfReporter.setExecutionTimes(testStartTime, testEndTime);
            pdfReporter.setTestStatus(finalTestStatus); // Define o status no reporter
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
