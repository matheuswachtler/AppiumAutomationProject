package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.awt.Color;

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
    private String testTestName = "";
    private String testDescription = "";
    private String newInfoFieldContent = "N/A";
    private String responsibleContent = "N/A";

    private static final String BASE_REPORTS_DIR = "target/pdf-reports/";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public PdfReporter(String contextName, String reportName, String platformName) {
        this.document = new PDDocument();
        this.testNumber = reportName;
        this.testTestName = reportName;
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

            addPageWithMarginAndFooter(); // Adiciona a primeira página com margem e rodapé estático
            System.out.println("First blank page added to PDF report.");

        }catch (IOException e) {
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

    public void setTestName(String testName) {
        this.testTestName = testName.toUpperCase();
    }

    public void setTestDescription(String description) {
        this.testDescription = description.toUpperCase();
    }

    public void setNewInfoFieldContent(String content) {
        this.newInfoFieldContent = content;
    }

    public void setResponsibleContent(String content) {
        this.responsibleContent = content;
    }

    public void addScreenshot(byte[] screenshotBytes, String screenshotName) {
        if (document == null) {
            System.err.println("PDF document is not initialized. Cannot add screenshot.");
            return;
        }

        PDImageXObject pdImage = null;
        try {
            pdImage = PDImageXObject.createFromByteArray(document, screenshotBytes, screenshotName);

            PDPage page = addPageWithMarginAndFooter(); // Adiciona nova página com margem e rodapé estático

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

                float tableWidth = pageWidth - (2 * margin);
                float tableHeight = 40;
                float tableY = pageHeight - margin - tableHeight;
                float tableX = margin;

                float tableTopY = tableY + tableHeight;
                float tableBottomY = tableY;

                float rowHeight = tableHeight / 2;
                float col1Width = tableWidth * 0.25f;
                float col2Width = tableWidth - col1Width;

                contentStream.addRect(tableX, tableY, tableWidth, tableHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX, tableY + rowHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + rowHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX + col1Width, tableY);
                contentStream.lineTo(tableX + col1Width, tableY + tableHeight);
                contentStream.stroke();

                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA); // Nova fonte normal
                float headerFontSize = 12; // Alterado para 12 para padronizar

                float textPadding = 5;

                contentStream.beginText();
                contentStream.setFont(boldFont, headerFontSize);
                float scriptTextX = tableX + textPadding;
                float scriptTextY = tableY + rowHeight + (rowHeight - headerFontSize) / 2f;
                contentStream.newLineAtOffset(scriptTextX, scriptTextY);
                contentStream.showText("SCRIPT");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(regularFont, headerFontSize); // Alterado para fonte normal
                float testNumTextX = tableX + col1Width + textPadding;
                float testNumTextY = tableY + rowHeight + (rowHeight - headerFontSize) / 2f;
                contentStream.newLineAtOffset(testNumTextX, testNumTextY);
                contentStream.showText(this.testNumber);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, headerFontSize);
                float stepTextX = tableX + textPadding;
                float stepTextY = tableY + (rowHeight - headerFontSize) / 2f;
                contentStream.newLineAtOffset(stepTextX, stepTextY);
                contentStream.showText("STEP");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(regularFont, headerFontSize); // Alterado para fonte normal
                float stepContentX = tableX + col1Width + textPadding;
                float stepContentY = tableY + (rowHeight - headerFontSize) / 2f;
                contentStream.newLineAtOffset(stepContentX, stepContentY);
                contentStream.showText(screenshotName.toUpperCase());
                contentStream.endText();

                contentStream.drawImage(pdImage, imageX, imageY, scaledWidth, scaledHeight);

            }
            System.out.println("Screenshot '" + screenshotName + "' added to PDF.");

        }catch (IOException e) {
            System.err.println("Error adding screenshot to PDF: " + e.getMessage());
        }
    }

    float adjustVert(float baseY, float rowHeight, float fontSize) {
        return baseY + (rowHeight - (fontSize * 0.7f)) / 2f;
    }

    // Método para adicionar uma nova página com margem e os quadros estáticos do rodapé
    private PDPage addPageWithMarginAndFooter() throws IOException {
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, newPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
            float margin = 30;
            float pageWidth = newPage.getMediaBox().getWidth();
            float pageHeight = newPage.getMediaBox().getHeight();

            contentStream.setStrokingColor(Color.BLACK);
            contentStream.setLineWidth(1f);

            // Desenha a margem principal da página
            contentStream.addRect(margin, margin, pageWidth - (2 * margin), pageHeight - (2 * margin));
            contentStream.stroke();

            float footerTableHeight = 20;
            float footerTableX = margin;
            float footerTableWidth = pageWidth - (2 * margin);
            float footerCol1Width = footerTableWidth * 0.25f; // 25% para a primeira coluna

            // Quadro "PAGE" / número da página
            float pageRowY = margin; // Posiciona o rodapé acima da margem inferior
            contentStream.addRect(footerTableX, pageRowY, footerTableWidth, footerTableHeight);
            contentStream.stroke();

            contentStream.moveTo(footerTableX + footerCol1Width, pageRowY);
            contentStream.lineTo(footerTableX + footerCol1Width, pageRowY + footerTableHeight);
            contentStream.stroke();

            // Célula esquerda: "PAGE"
            contentStream.beginText();
            // Alterado o tamanho da fonte para 12 para padronizar e agora em negrito
            // Instancia boldFont aqui para garantir que esteja disponível
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            contentStream.setFont(boldFont, 12); // Usando boldFont para "PAGE"
            // Ajustado o posicionamento vertical para o novo tamanho da fonte
            float pageTextY = pageRowY + (footerTableHeight - 12) / 2f;
            contentStream.newLineAtOffset(footerTableX + 5, pageTextY);
            contentStream.showText("PAGE");
            contentStream.endText();

        }
        return newPage;
    }

    public void closeReport() {
        if (document != null) {
            PDPageContentStream contentStream = null;
            PDPage currentPage;

            try {
                // Adiciona a página de logs se houver conteúdo
                if (!logsContent.isEmpty()) {
                    currentPage = addPageWithMarginAndFooter(); // Garante que a página de logs tem o rodapé estático

                    contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
                    PDType1Font logFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    float logFontSize = 10;
                    float leading = (float) (1.8f * logFontSize);

                    contentStream.setFont(logFont, logFontSize);
                    contentStream.setLeading(leading);
                    contentStream.beginText();

                    float margin = 30;
                    float currentY;
                    float startX;

                    String logTitle = "EXECUTION LOGS FOR TEST: " + this.testNumber;
                    PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                    float titleFontSize = 12;
                    float titleWidth = titleFont.getStringWidth(logTitle) / 1000f * titleFontSize;
                    float titleX = (currentPage.getMediaBox().getWidth() - titleWidth) / 2f;
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
                        String cleanedLine = line.replaceAll("[\\n\\r]", "");

                        float dummySummaryTableHeight = 60;
                        float dummySummaryMarginFromBottom = 50;
                        float requiredSpaceForDummySummary = dummySummaryTableHeight + dummySummaryMarginFromBottom;

                        if (currentY < margin + leading + requiredSpaceForDummySummary) {
                            contentStream.endText();
                            contentStream.close();

                            currentPage = addPageWithMarginAndFooter(); // Adiciona nova página de logs com rodapé estático
                            contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
                            contentStream.setFont(logFont, logFontSize);
                            contentStream.setLeading(leading);
                            currentY = currentPage.getMediaBox().getHeight() - margin;
                            contentStream.newLineAtOffset(startX, currentY);
                        }
                        contentStream.showText(cleanedLine);
                        contentStream.newLine();
                        currentY -= leading;
                    }
                    contentStream.endText();
                    contentStream.close();
                }

                // --- Desenho da tabela de sumário na PRIMEIRA PÁGINA ---
                PDPage summaryPage = document.getPage(0);
                contentStream = new PDPageContentStream(document, summaryPage, PDPageContentStream.AppendMode.APPEND, true, true);

                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA); // Nova fonte normal

                float fontSize = 12;
                float textPadding = 10;

                float pageMargin = 30;
                float pageCurrentWidth = summaryPage.getMediaBox().getWidth();
                float pageCurrentHeight = summaryPage.getMediaBox().getHeight();

                float tableX = pageMargin;
                float tableWidth = pageCurrentWidth - (2 * pageMargin);
                float col1Width = tableWidth * 0.25f;

                PDType1Font testNameContentFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float testNameContentFontSize = 10;
                float testNameLineLeading = testNameContentFontSize * 1.2f;

                float availableWidthForTestNameText = tableWidth - col1Width - (2 * textPadding);

                java.util.List<String> formattedTestNameLines = new java.util.ArrayList<>();
                String testNameToFormat = (this.testTestName != null && !this.testTestName.isEmpty()) ?
                        this.testTestName : "N/A";

                if ("N/A".equals(testNameToFormat)) {
                    formattedTestNameLines.add("N/A");
                } else {
                    String[] words = testNameToFormat.split(" ");
                    StringBuilder currentLine = new StringBuilder();
                    for (String word : words) {
                        float projectedWidth = testNameContentFont.getStringWidth(currentLine.toString() + " " + word) / 1000f * testNameContentFontSize;
                        if (projectedWidth > availableWidthForTestNameText && currentLine.length() > 0) {
                            formattedTestNameLines.add(currentLine.toString().trim());
                            currentLine = new StringBuilder(word).append(" ");
                        } else {
                            currentLine.append(word).append(" ");
                        }
                    }
                    if (currentLine.length() > 0) {
                        formattedTestNameLines.add(currentLine.toString().trim());
                    }
                }
                float testNameContentHeight = formattedTestNameLines.size() * testNameLineLeading;
                float standardFixedRowHeight = 20f;

                float testNameCellActualHeight;
                if (formattedTestNameLines.size() <= 1) {
                    testNameCellActualHeight = standardFixedRowHeight;
                } else {
                    testNameCellActualHeight = testNameContentHeight + (2 * textPadding);
                }

                PDType1Font descriptionContentFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float descriptionContentFontSize = 10;
                float descriptionLineLeading = descriptionContentFontSize * 1.2f;

                float availableWidthForDescriptionText = tableWidth - col1Width - (2 * textPadding);

                java.util.List<String> formattedDescriptionLines = new java.util.ArrayList<>();
                String descriptionToFormat = (this.testDescription != null && !this.testDescription.isEmpty()) ?
                        this.testDescription.replaceAll("[\\n\\r\\t]", "") : "N/A";

                if ("N/A".equals(descriptionToFormat)) {
                    formattedDescriptionLines.add("N/A");
                } else {
                    String[] words = descriptionToFormat.split(" ");
                    StringBuilder currentLine = new StringBuilder();
                    for (String word : words) {
                        float projectedWidth = descriptionContentFont.getStringWidth(currentLine.toString() + " " + word) / 1000f * descriptionContentFontSize;
                        if (projectedWidth > availableWidthForDescriptionText && currentLine.length() > 0) {
                            formattedDescriptionLines.add(currentLine.toString().trim());
                            currentLine = new StringBuilder(word).append(" ");
                        } else {
                            currentLine.append(word).append(" ");
                        }
                    }
                    if (currentLine.length() > 0) {
                        formattedDescriptionLines.add(currentLine.toString().trim());
                    }
                }

                float descriptionContentHeight = formattedDescriptionLines.size() * descriptionLineLeading;
                float descriptionCellActualHeight;
                if (formattedDescriptionLines.size() <= 1) {
                    descriptionCellActualHeight = standardFixedRowHeight;
                } else {
                    descriptionCellActualHeight = descriptionContentHeight + (2 * textPadding);
                }

                float testCodeRowHeight = standardFixedRowHeight;
                float responsibleRowHeight = standardFixedRowHeight;


                float summaryTableHeight = (3 * standardFixedRowHeight) + responsibleRowHeight + testCodeRowHeight + testNameCellActualHeight + descriptionCellActualHeight;

                float tableY = pageCurrentHeight - pageMargin - summaryTableHeight;

                contentStream.setLineWidth(1f);
                contentStream.setStrokingColor(0, 0, 0);
                contentStream.addRect(tableX, tableY, tableWidth, summaryTableHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX, tableY + descriptionCellActualHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + descriptionCellActualHeight);
                contentStream.stroke();

                // Linha que separa EXECUTION DATE e EXECUTION TIME
                contentStream.moveTo(tableX, tableY + summaryTableHeight - standardFixedRowHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + summaryTableHeight - standardFixedRowHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX, tableY + summaryTableHeight - (2 * standardFixedRowHeight)); // Linha entre EXECUTION TIME e TEST RESULT
                contentStream.lineTo(tableX + tableWidth, tableY + summaryTableHeight - (2 * standardFixedRowHeight));
                contentStream.stroke();


                contentStream.moveTo(tableX, tableY + descriptionCellActualHeight + testNameCellActualHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + descriptionCellActualHeight + testNameCellActualHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX, tableY + descriptionCellActualHeight + testNameCellActualHeight + testCodeRowHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + descriptionCellActualHeight + testNameCellActualHeight + testCodeRowHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX, tableY + descriptionCellActualHeight + testNameCellActualHeight + testCodeRowHeight + responsibleRowHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + descriptionCellActualHeight + testNameCellActualHeight + testCodeRowHeight + responsibleRowHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX, tableY + descriptionCellActualHeight + testNameCellActualHeight + testCodeRowHeight + responsibleRowHeight + standardFixedRowHeight);
                contentStream.lineTo(tableX + tableWidth, tableY + descriptionCellActualHeight + testNameCellActualHeight + testCodeRowHeight + responsibleRowHeight + standardFixedRowHeight);
                contentStream.stroke();

                contentStream.moveTo(tableX + col1Width, tableY);
                contentStream.lineTo(tableX + col1Width, tableY + summaryTableHeight);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                float execDateLabelY = adjustVert(tableY + summaryTableHeight - standardFixedRowHeight, standardFixedRowHeight, fontSize);
                contentStream.newLineAtOffset(tableX + textPadding, execDateLabelY);
                contentStream.showText("EXECUTION DATE");
                contentStream.endText();

                String formattedDate = (startTime != null) ? startTime
                        .format(DISPLAY_DATE_TIME_FORMATTER)
                        .toUpperCase() : "N/A";

                contentStream.beginText();
                contentStream.setFont(descriptionContentFont, descriptionContentFontSize);
                float execDateContentY = adjustVert(tableY + summaryTableHeight - standardFixedRowHeight, standardFixedRowHeight, descriptionContentFontSize);
                contentStream.newLineAtOffset(tableX + col1Width + textPadding, execDateContentY);
                contentStream.showText(formattedDate);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                float execTimeLabelY = adjustVert(tableY + summaryTableHeight - (2 * standardFixedRowHeight), standardFixedRowHeight, fontSize);
                contentStream.newLineAtOffset(tableX + textPadding, execTimeLabelY);
                contentStream.showText("EXECUTION TIME");
                contentStream.endText();

                String durationString = "N/A";
                if (startTime != null && endTime != null) {
                    Duration duration = Duration.between(startTime, endTime);
                    long seconds = duration.getSeconds();
                    durationString = String.format("%d MIN %d SEG", seconds / 60, seconds % 60);
                }

                contentStream.beginText();
                contentStream.setFont(descriptionContentFont, descriptionContentFontSize);
                float execTimeContentY = adjustVert(tableY + summaryTableHeight - (2 * standardFixedRowHeight), standardFixedRowHeight, descriptionContentFontSize);
                contentStream.newLineAtOffset(tableX + col1Width + textPadding, execTimeContentY);
                contentStream.showText(durationString);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                contentStream.setNonStrokingColor(Color.BLACK);
                float testResultLabelY = adjustVert(tableY + summaryTableHeight - (3 * standardFixedRowHeight), standardFixedRowHeight, fontSize);
                contentStream.newLineAtOffset(tableX + textPadding, testResultLabelY);
                contentStream.showText("TEST RESULT");
                contentStream.endText();

                if (this.testStatus.equalsIgnoreCase("SUCCESS")) {
                    contentStream.setNonStrokingColor(Color.GREEN);
                } else if (this.testStatus.equalsIgnoreCase("FAILURE")) {
                    contentStream.setNonStrokingColor(Color.RED);
                } else {
                    contentStream.setNonStrokingColor(Color.BLACK);
                }

                contentStream.beginText();
                contentStream.setFont(descriptionContentFont, descriptionContentFontSize);
                float testResultContentY = adjustVert(tableY + summaryTableHeight - (3 * standardFixedRowHeight), standardFixedRowHeight, descriptionContentFontSize);
                contentStream.newLineAtOffset(tableX + col1Width + textPadding, testResultContentY);
                contentStream.showText(this.testStatus.toUpperCase());
                contentStream.endText();
                contentStream.setNonStrokingColor(Color.BLACK);

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                float responsibleLabelY = adjustVert(tableY + descriptionCellActualHeight + testNameCellActualHeight + testCodeRowHeight, responsibleRowHeight, fontSize);
                contentStream.newLineAtOffset(tableX + textPadding, responsibleLabelY);
                contentStream.showText("RESPONSIBLE");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(descriptionContentFont, descriptionContentFontSize);
                float responsibleContentY = adjustVert(tableY + descriptionCellActualHeight + testNameCellActualHeight + testCodeRowHeight, responsibleRowHeight, descriptionContentFontSize);
                contentStream.newLineAtOffset(tableX + col1Width + textPadding, responsibleContentY);
                contentStream.showText(this.responsibleContent);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                float testCodeLabelY = adjustVert(tableY + descriptionCellActualHeight + testNameCellActualHeight, testCodeRowHeight, fontSize);
                contentStream.newLineAtOffset(tableX + textPadding, testCodeLabelY);
                contentStream.showText("SCRIPT");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(descriptionContentFont, descriptionContentFontSize);
                float testCodeContentY = adjustVert(tableY + descriptionCellActualHeight + testNameCellActualHeight, testCodeRowHeight, descriptionContentFontSize);
                contentStream.newLineAtOffset(tableX + col1Width + textPadding, testCodeContentY);
                contentStream.showText(this.newInfoFieldContent);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                float testNameLabelY;
                float testNameCellBottomY = tableY + descriptionCellActualHeight;
                float testNameCellTopY = testNameCellBottomY + testNameCellActualHeight;

                if (formattedTestNameLines.size() <= 1) {
                    testNameLabelY = adjustVert(testNameCellBottomY, testNameCellActualHeight, fontSize);
                } else {
                    testNameLabelY = testNameCellTopY - textPadding - (boldFont.getFontDescriptor().getAscent() / 1000f * fontSize);
                }
                contentStream.newLineAtOffset(tableX + textPadding, testNameLabelY);
                contentStream.showText("TEST NAME");
                contentStream.endText();

                contentStream.setFont(testNameContentFont, testNameContentFontSize);
                float currentTestNameTextY;

                if (formattedTestNameLines.size() <= 1) {
                    contentStream.beginText();
                    currentTestNameTextY = adjustVert(tableY + descriptionCellActualHeight, testNameCellActualHeight, testNameContentFontSize);
                    contentStream.newLineAtOffset(tableX + col1Width + textPadding, currentTestNameTextY);
                    contentStream.showText(formattedTestNameLines.get(0));
                    contentStream.endText();
                } else {
                    currentTestNameTextY = testNameCellTopY - textPadding - (testNameContentFont.getFontDescriptor().getAscent() / 1000f * testNameContentFontSize);
                    for (String line : formattedTestNameLines) {
                        contentStream.beginText();
                        float textX = tableX + col1Width + textPadding;
                        contentStream.newLineAtOffset(textX, currentTestNameTextY);
                        contentStream.showText(line);
                        contentStream.endText();
                        currentTestNameTextY -= testNameLineLeading;
                    }
                }

                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize);
                float descLabelY;
                float descriptionCellBottomY = tableY;
                float descriptionCellTopY = descriptionCellBottomY + descriptionCellActualHeight;

                if (formattedDescriptionLines.size() <= 1) {
                    descLabelY = adjustVert(descriptionCellBottomY, descriptionCellActualHeight, fontSize);
                } else {
                    descLabelY = descriptionCellTopY - textPadding - (boldFont.getFontDescriptor().getAscent() / 1000f * fontSize);
                }
                contentStream.newLineAtOffset(tableX + textPadding, descLabelY);
                contentStream.showText("DESCRIPTION");
                contentStream.endText();

                contentStream.setFont(descriptionContentFont, descriptionContentFontSize);
                float currentDescriptionTextY;

                if (formattedDescriptionLines.size() <= 1) {
                    contentStream.beginText();
                    currentDescriptionTextY = adjustVert(tableY, descriptionCellActualHeight, descriptionContentFontSize);
                    contentStream.newLineAtOffset(tableX + col1Width + textPadding, currentDescriptionTextY);
                    contentStream.showText(formattedDescriptionLines.get(0));
                    contentStream.endText();
                } else {
                    currentDescriptionTextY = descriptionCellTopY - textPadding - (descriptionContentFont.getFontDescriptor().getAscent() / 1000f * descriptionContentFontSize);
                    for (String line : formattedDescriptionLines) {
                        contentStream.beginText();
                        float textX = tableX + col1Width + textPadding;
                        contentStream.newLineAtOffset(textX, currentDescriptionTextY);
                        contentStream.showText(line);
                        contentStream.endText();
                        currentDescriptionTextY -= descriptionLineLeading;
                    }
                }

                contentStream.close();

                // --- Início do bloco: Preenchimento dinâmico do rodapé ---
                int totalPageCount = document.getPages().getCount();
                float margin = 30;
                float footerTableHeight = 20;
                float footerTextPadding = 5;
                PDType1Font footerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA); // Alterado para fonte normal
                float footerFontSize = 12;


                for (int i = 0; i < totalPageCount; i++) {
                    PDPage page = document.getPage(i);
                    try (PDPageContentStream footerContentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                        float pageWidth = page.getMediaBox().getWidth();
                        float footerTableWidth = pageWidth - (2 * margin);
                        float footerCol1Width = footerTableWidth * 0.25f; // 25% para a primeira coluna
                        float footerTableX = margin; // Definindo aqui para o escopo do loop

                        // Posições para o quadro "PAGE"
                        float pageRowY = margin;
                        String pageNumberText = (i + 1) + " of " + totalPageCount;
                        // Removida a linha pageNumberTextWidth se não for mais usada para cálculo de centralização
                        // float pageNumberTextWidth = footerFont.getStringWidth(pageNumberText) / 1000f * footerFontSize;

                        // Ajustado o posicionamento horizontal para alinhar à esquerda
                        float pageNumberX = footerTableX + footerCol1Width + footerTextPadding; // Alinhado à esquerda

                        // Ajustado o posicionamento vertical para o novo tamanho da fonte
                        float pageTextY = pageRowY + (footerTableHeight - footerFontSize) / 2f;

                        // Preenche o número da página "X de Y"
                        footerContentStream.beginText();
                        footerContentStream.setFont(footerFont, footerFontSize);
                        footerContentStream.newLineAtOffset(pageNumberX, pageTextY);
                        footerContentStream.showText(pageNumberText);
                        footerContentStream.endText();
                    }
                }
                // --- Fim do bloco: Preenchimento dinâmico do rodapé ---


                document.save(this.reportFilePath);
                System.out.println("PDF report saved and closed: " + this.reportFilePath);

            }catch (IOException e) {
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
