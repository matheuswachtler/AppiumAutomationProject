package utils.report;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import utils.report.drawing.PdfTableDrawer;
import utils.report.drawing.PdfPageTemplate;
import utils.report.drawing.PdfLogWriter;

public class PdfReporter {

    private PDDocument document;
    private final String reportFilePath;
    private final TestReportData reportData;
    private final PdfPageTemplate pdfPageTemplate;

    private static final String BASE_REPORTS_DIR = "target/pdf-reports/";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public PdfReporter(String contextName, String reportName, String platformName) {
        this.document = new PDDocument();
        this.reportData = new TestReportData(reportName);
        this.pdfPageTemplate = new PdfPageTemplate();

        try {
            Path contextDirPath = Paths.get(BASE_REPORTS_DIR, contextName);
            if (!Files.exists(contextDirPath)) {
                Files.createDirectories(contextDirPath);
            }
            String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            this.reportFilePath = contextDirPath
                    .resolve(reportName + "_" + platformName + "_" + timestamp + ".pdf")
                    .toString();
            System.out.println("PDF report initialized at: " + this.reportFilePath);

            pdfPageTemplate.addPageWithMarginAndFooter(document);

        } catch (IOException e) {
            System.err.println("Error initializing PDF report directory or file path: " + e.getMessage());
            throw new RuntimeException("Failed to initialize PDF report", e);
        }
    }

    public TestReportData getReportData() {
        return reportData;
    }

    public void addScreenshot(byte[] screenshotBytes, String screenshotName) {
        if (document == null) {
            System.err.println("PDF document is not initialized. Cannot add screenshot.");
            return;
        }

        try {
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, screenshotBytes, screenshotName);

            PDPage page = pdfPageTemplate.addPageWithMarginAndFooter(document);

            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox.getWidth();
            float pageHeight = mediaBox.getHeight();
            float margin = 30;

            float imageWidth = pdImage.getWidth();
            float imageHeight = pdImage.getHeight();

            float scaleX = pageWidth / imageWidth;
            float scaleY = pageHeight / imageHeight;
            float finalScale = Math.min(scaleX, scaleY) * 0.7f;

            float scaledWidth = imageWidth * finalScale;
            float scaledHeight = imageHeight * finalScale;

            float imageX = (pageWidth - scaledWidth) / 2;
            float imageY = (pageHeight - scaledHeight) / 2;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.setLineWidth(1f);
                contentStream.setStrokingColor(Color.BLACK);

                PdfTableDrawer.drawScreenshotInfoTable(contentStream, pageWidth, pageHeight, margin, screenshotName, reportData);

                contentStream.drawImage(pdImage, imageX, imageY, scaledWidth, scaledHeight);

            }
            System.out.println("Screenshot '" + screenshotName + "' added to PDF.");

        } catch (IOException e) {
            System.err.println("Error adding screenshot to PDF: " + e.getMessage());
        }
    }

    public void closeReport() {
        if (document != null) {
            try {
                PdfLogWriter.generateLogsPage(document, reportData, pdfPageTemplate);

                PDPage summaryPage = document.getPage(0);
                PdfTableDrawer.drawSummaryTable(document, summaryPage, reportData);

                pdfPageTemplate.updatePageNumbersInFooter(document);

                document.save(this.reportFilePath);
                System.out.println("PDF report saved and closed: " + this.reportFilePath);

            } catch (IOException e) {
                System.err.println("Error saving or closing PDF report: " + e.getMessage());
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException e) {
                        System.err.println("Error closing document in finally block: " + e.getMessage());
                    }
                    document = null;
                }
            }
        }
    }
}