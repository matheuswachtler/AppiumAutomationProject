package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class PdfReporter {

    private PDDocument document;
    private final String reportFilePath;
    private final String testNumber;
    private String logsContent = "";
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String testStatus = "N/A";

    private static final String BASE_REPORTS_DIR = "target/pdf-reports/";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public PdfReporter(String contextName, String reportName, String platformName) {
        this.document = new PDDocument();
        this.testNumber = reportName;
        try {
            Path contextDirPath = Paths.get(BASE_REPORTS_DIR, contextName);
            if (!Files.exists(contextDirPath)) {
                Files.createDirectories(contextDirPath);
            }
            String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            this.reportFilePath = contextDirPath.resolve(reportName + "_" + platformName + "_" + timestamp + ".pdf").toString();
            System.out.println("PDF report initialized at: " + this.reportFilePath);
        } catch (IOException e) {
            System.err.println("Error initializing PDF report directory or file path: " + e.getMessage());
            throw new RuntimeException("Failed to initialize PDF report", e);
        }
    }

    public void setLogsContent(String logs) {
        this.logsContent = logs;
    }

    public void setExecutionTimes(LocalDateTime start, LocalDateTime end) {
        this.startTime = start;
        this.endTime = end;
    }

    public void setTestStatus(String status) {
        this.testStatus = status.toUpperCase();
    }

    public void addScreenshot(byte[] screenshotBytes, String screenshotName) {
        if (document == null) {
            System.err.println("PDF document is not initialized. Cannot add screenshot.");
            return;
        }

        try {
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, screenshotBytes, screenshotName);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox.getWidth();
            float pageHeight = mediaBox.getHeight();

            float imageWidth = pdImage.getWidth();
            float imageHeight = pdImage.getHeight();

            float scaleX = pageWidth / imageWidth;
            float scaleY = pageHeight / imageHeight;
            float finalScale = Math.min(scaleX, scaleY) * 0.6f;

            float scaledWidth = imageWidth * finalScale;
            float scaledHeight = imageHeight * finalScale;

            float rightMargin = 20;
            float x = pageWidth - scaledWidth - rightMargin;
            float y = (pageHeight - scaledHeight) / 2;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float tableWidth = pageWidth - 40;
                float tableHeight = 40;
                float tableY = pageHeight - 60;
                float tableX = (pageWidth - tableWidth) / 2;

                float rowHeight = tableHeight / 2;
                float col1Width = tableWidth / 3;
                float col2Width = tableWidth - col1Width;

                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                float headerFontSize = 10;
                float textPadding = 5;

                contentStream.setLineWidth(0.5f);
                contentStream.setStrokingColor(0, 0, 0);

                contentStream.addRect(tableX, tableY, tableWidth, tableHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX, tableY + rowHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + rowHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX + col1Width, tableY);
                contentStream.lineTo(tableX + col1Width, tableY + tableHeight);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(boldFont, headerFontSize);
                float scriptTextX = tableX + textPadding;
                float scriptTextY = tableY + rowHeight + (rowHeight - headerFontSize) / 2;
                contentStream.newLineAtOffset(scriptTextX, scriptTextY);
                contentStream.showText("SCRIPT");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, headerFontSize);
                float testNumTextX = tableX + col1Width + textPadding;
                float testNumTextY = tableY + rowHeight + (rowHeight - headerFontSize) / 2;
                contentStream.newLineAtOffset(testNumTextX, testNumTextY);
                contentStream.showText(this.testNumber);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, headerFontSize);
                float stepTextX = tableX + textPadding;
                float stepTextY = tableY + (rowHeight - headerFontSize) / 2;
                contentStream.newLineAtOffset(stepTextX, stepTextY);
                contentStream.showText("STEP");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, headerFontSize);
                float stepContentX = tableX + col1Width + textPadding;
                float stepContentY = tableY + (rowHeight - headerFontSize) / 2;
                contentStream.newLineAtOffset(stepContentX, stepContentY);
                contentStream.showText(screenshotName.toUpperCase());
                contentStream.endText();

                contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
            }
            System.out.println("Screenshot '" + screenshotName + "' added to PDF.");

        } catch (IOException e) {
            System.err.println("Error adding screenshot to PDF: " + e.getMessage());
        }
    }

    public void closeReport() {
        if (document != null) {
            PDPageContentStream contentStream = null;
            PDPage currentPage;

            try {
                if (!logsContent.isEmpty()) {
                    currentPage = new PDPage(PDRectangle.A4);
                    document.addPage(currentPage);

                    contentStream = new PDPageContentStream(document, currentPage);
                    PDType1Font logFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    float logFontSize = 10;
                    float leading = 1.8f * logFontSize;

                    contentStream.setFont(logFont, logFontSize);
                    contentStream.setLeading(leading);
                    contentStream.beginText();

                    float margin = 30;
                    float currentY;
                    float startX;

                    String logTitle = "EXECUTION LOGS FOR TEST: " + this.testNumber;
                    PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                    float titleFontSize = 12;
                    float titleWidth = titleFont.getStringWidth(logTitle) / 1000 * titleFontSize;
                    float titleX = (currentPage.getMediaBox().getWidth() - titleWidth) / 2;
                    float titleY = currentPage.getMediaBox().getHeight() - margin + 15;

                    contentStream.setFont(titleFont, titleFontSize);
                    contentStream.newLineAtOffset(titleX, titleY);
                    contentStream.showText(logTitle);
                    contentStream.endText();

                    contentStream.setFont(logFont, logFontSize);
                    contentStream.setLeading(leading);
                    contentStream.beginText();
                    currentY = currentPage.getMediaBox().getHeight() - margin - 30;
                    startX = margin;
                    contentStream.newLineAtOffset(startX, currentY);

                    String[] lines = logsContent.split("\\r?\\n");

                    for (String line : lines) {
                        float summaryTableHeight = 60;
                        float summaryMarginFromBottom = 50;
                        float requiredSpaceForSummary = summaryTableHeight + summaryMarginFromBottom;

                        if (currentY < margin + leading + requiredSpaceForSummary) {
                            contentStream.endText();
                            contentStream.close();

                            currentPage = new PDPage(PDRectangle.A4);
                            document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage);
                            contentStream.setFont(logFont, logFontSize);
                            contentStream.setLeading(leading);
                            contentStream.beginText();
                            currentY = currentPage.getMediaBox().getHeight() - margin;
                            contentStream.newLineAtOffset(startX, currentY);
                        }
                        contentStream.showText(line);
                        contentStream.newLine();
                        currentY -= leading;
                    }
                    contentStream.endText();
                } else {
                    currentPage = new PDPage(PDRectangle.A4);
                    document.addPage(currentPage);
                }

                if (contentStream == null) {
                    contentStream = new PDPageContentStream(document, currentPage);
                }

                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float fontSize = 12;
                float textPadding = 10;

                float tableWidth = 300;
                float summaryTableHeight = 60;
                float tableY = 50;
                float tableX = (currentPage.getMediaBox().getWidth() - tableWidth) / 2;

                float rowHeight = summaryTableHeight / 3;
                float col1Width = tableWidth / 2;
                float col2Width = tableWidth / 2;

                contentStream.setLineWidth(0.5f);
                contentStream.setStrokingColor(0, 0, 0);
                contentStream.addRect(tableX, tableY, tableWidth, summaryTableHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX, tableY + rowHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + rowHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX, tableY + 2 * rowHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + 2 * rowHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX + col1Width, tableY);
                contentStream.lineTo(tableX + col1Width, tableY + summaryTableHeight);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                contentStream.newLineAtOffset(tableX + textPadding, tableY + 2 * rowHeight + (rowHeight - fontSize) / 2);
                contentStream.showText("Data");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(regularFont, fontSize);
                String formattedDate = (startTime != null) ? startTime.format(DISPLAY_DATE_TIME_FORMATTER) : "N/A";
                contentStream.newLineAtOffset(tableX + col1Width + textPadding, tableY + 2 * rowHeight + (rowHeight - fontSize) / 2);
                contentStream.showText(formattedDate);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                contentStream.newLineAtOffset(tableX + textPadding, tableY + rowHeight + (rowHeight - fontSize) / 2);
                contentStream.showText("Tempo");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(regularFont, fontSize);
                String durationString = "N/A";
                if (startTime != null && endTime != null) {
                    Duration duration = Duration.between(startTime, endTime);
                    long seconds = duration.getSeconds();
                    durationString = String.format("%d min %d seg", seconds / 60, seconds % 60);
                }
                contentStream.newLineAtOffset(tableX + col1Width + textPadding, tableY + rowHeight + (rowHeight - fontSize) / 2);
                contentStream.showText(durationString);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                contentStream.newLineAtOffset(tableX + textPadding, tableY + (rowHeight - fontSize) / 2);
                contentStream.showText("Status");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(regularFont, fontSize);
                contentStream.newLineAtOffset(tableX + col1Width + textPadding, tableY + (rowHeight - fontSize) / 2);
                contentStream.showText(this.testStatus);
                contentStream.endText();

                contentStream.close();

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
