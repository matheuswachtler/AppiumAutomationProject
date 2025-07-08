package utils.report.drawing;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import utils.report.TestReportData;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class PdfTableDrawer {

    private static float adjustVert(float baseY, float rowHeight, float fontSize) {
        return baseY + (rowHeight - (fontSize * 0.7f)) / 2f;
    }

    public static void drawScreenshotInfoTable(PDPageContentStream contentStream, float pageWidth, float pageHeight, float margin, String screenshotName, TestReportData reportData) throws IOException {
        float tableWidth = pageWidth - (2 * margin);
        float tableHeight = 40;
        float tableY = pageHeight - margin - tableHeight;

        float rowHeight = tableHeight / 2;
        float col1Width = tableWidth * 0.25f;

        contentStream.addRect(margin, tableY, tableWidth, tableHeight);
        contentStream.stroke();

        contentStream.moveTo(margin, tableY + rowHeight);
        contentStream.lineTo(margin + tableWidth, tableY + rowHeight);
        contentStream.stroke();

        contentStream.moveTo(margin + col1Width, tableY);
        contentStream.lineTo(margin + col1Width, tableY + tableHeight);
        contentStream.stroke();

        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float headerFontSize = 12;
        float textPadding = 5;

        float headerRowTextY = tableY + rowHeight + (rowHeight - headerFontSize) / 2f;

        contentStream.beginText();
        contentStream.setFont(boldFont, headerFontSize);
        float scriptTextX = margin + textPadding;
        contentStream.newLineAtOffset(scriptTextX, headerRowTextY);
        contentStream.showText("SCRIPT");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(regularFont, headerFontSize);
        float testNumTextX = margin + col1Width + textPadding;
        contentStream.newLineAtOffset(testNumTextX, headerRowTextY);
        contentStream.showText(reportData.getTestNumber());
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, headerFontSize);
        float stepTextX = margin + textPadding;
        float stepTextY = tableY + (rowHeight - headerFontSize) / 2f;
        contentStream.newLineAtOffset(stepTextX, stepTextY);
        contentStream.showText("STEP");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(regularFont, headerFontSize);
        float stepContentX = margin + col1Width + textPadding;
        float stepContentY = tableY + (rowHeight - headerFontSize) / 2f;
        contentStream.newLineAtOffset(stepContentX, stepContentY);
        contentStream.showText(screenshotName.toUpperCase());
        contentStream.endText();
    }


    public static void drawSummaryTable(PDDocument document, PDPage summaryPage, TestReportData reportData) throws IOException {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, summaryPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font contentFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float fontSize = 12;
            float textPadding = 5;

            float pageMargin = 30;
            float pageCurrentWidth = summaryPage.getMediaBox().getWidth();
            float pageCurrentHeight = summaryPage.getMediaBox().getHeight();

            float tableWidth = pageCurrentWidth - (2 * pageMargin);
            float col1Width = tableWidth * 0.25f;

            float contentFontSize = 10;
            float lineLeading = contentFontSize * 1.3f;

            float labelAscent = boldFont.getFontDescriptor().getAscent() / 1000f * fontSize;
            float contentAscent = contentFont.getFontDescriptor().getAscent() / 1000f * contentFontSize;


            float availableWidthForTestNameText = tableWidth - col1Width - (2 * textPadding);
            List<String> formattedTestNameLines = new ArrayList<>();
            String testNameToFormat = (reportData.getTestName() != null && !reportData.getTestName().isEmpty()) ?
                    reportData.getTestName() : "N/A";

            if ("N/A".equals(testNameToFormat)) {
                formattedTestNameLines.add("N/A");
            } else {
                String[] words = testNameToFormat.split(" ");
                StringBuilder currentLine = new StringBuilder();
                for (String word : words) {
                    float projectedWidth = contentFont.getStringWidth(currentLine + (!currentLine.isEmpty() ? " " : "") + word) / 1000f * contentFontSize;
                    if (projectedWidth > availableWidthForTestNameText && !currentLine.isEmpty()) {
                        formattedTestNameLines.add(currentLine.toString().trim());
                        currentLine = new StringBuilder(word);
                    } else {
                        if (!currentLine.isEmpty()) {
                            currentLine.append(" ");
                        }
                        currentLine.append(word);
                    }
                }
                if (!currentLine.isEmpty()) {
                    formattedTestNameLines.add(currentLine.toString().trim());
                }
            }
            float testNameContentHeight = (formattedTestNameLines.size() * lineLeading) - (lineLeading - contentAscent);
            float standardFixedRowHeight = 20f;

            float testNameCellActualHeight;
            if (formattedTestNameLines.size() <= 1) {
                testNameCellActualHeight = standardFixedRowHeight;
            } else {
                testNameCellActualHeight = testNameContentHeight + (2 * textPadding);
            }
            if (testNameCellActualHeight < standardFixedRowHeight) {
                testNameCellActualHeight = standardFixedRowHeight;
            }


            float availableWidthForDescriptionText = tableWidth - col1Width - (2 * textPadding);
            List<String> formattedDescriptionLines = new ArrayList<>();
            String descriptionToFormat = (reportData.getTestDescription() != null && !reportData.getTestDescription().isEmpty()) ?
                    reportData.getTestDescription().replaceAll("[\\n\\r\\t]", " ") : "N/A";

            if ("N/A".equals(descriptionToFormat)) {
                formattedDescriptionLines.add("N/A");
            } else {
                String[] words = descriptionToFormat.split(" ");
                StringBuilder currentLine = new StringBuilder();
                for (String word : words) {
                    float projectedWidth = contentFont.getStringWidth(currentLine + (!currentLine.isEmpty() ? " " : "") + word) / 1000f * contentFontSize;
                    if (projectedWidth > availableWidthForDescriptionText && !currentLine.isEmpty()) {
                        formattedDescriptionLines.add(currentLine.toString().trim());
                        currentLine = new StringBuilder(word);
                    } else {
                        if (!currentLine.isEmpty()) {
                            currentLine.append(" ");
                        }
                        currentLine.append(word);
                    }
                }
                if (!currentLine.isEmpty()) {
                    formattedDescriptionLines.add(currentLine.toString().trim());
                }
            }

            float descriptionContentHeight = (formattedDescriptionLines.size() * lineLeading) - (lineLeading - contentAscent);
            float descriptionCellActualHeight;
            if (formattedDescriptionLines.size() <= 1) {
                descriptionCellActualHeight = standardFixedRowHeight;
            } else {
                descriptionCellActualHeight = descriptionContentHeight + (2 * textPadding);
            }
            if (descriptionCellActualHeight < standardFixedRowHeight) {
                descriptionCellActualHeight = standardFixedRowHeight;
            }

            float testReportRowHeight = 20f;
            float summaryTableHeight = (3 * standardFixedRowHeight) + standardFixedRowHeight + standardFixedRowHeight + testNameCellActualHeight + descriptionCellActualHeight + testReportRowHeight;

            float tableY = pageCurrentHeight - pageMargin - summaryTableHeight;

            contentStream.setLineWidth(1f);
            contentStream.setStrokingColor(0, 0, 0);
            contentStream.addRect(pageMargin, tableY, tableWidth, summaryTableHeight);
            contentStream.stroke();

            contentStream.moveTo(pageMargin, tableY + summaryTableHeight - testReportRowHeight);
            contentStream.lineTo(pageMargin + tableWidth, tableY + summaryTableHeight - testReportRowHeight);
            contentStream.stroke();

            contentStream.moveTo(pageMargin, tableY + descriptionCellActualHeight);
            contentStream.lineTo(pageMargin + tableWidth, tableY + descriptionCellActualHeight);
            contentStream.stroke();

            contentStream.moveTo(pageMargin, tableY + descriptionCellActualHeight + testNameCellActualHeight);
            contentStream.lineTo(pageMargin + tableWidth, tableY + descriptionCellActualHeight + testNameCellActualHeight);
            contentStream.stroke();

            contentStream.moveTo(pageMargin, tableY + descriptionCellActualHeight + testNameCellActualHeight + standardFixedRowHeight);
            contentStream.lineTo(pageMargin + tableWidth, tableY + descriptionCellActualHeight + testNameCellActualHeight + standardFixedRowHeight);
            contentStream.stroke();

            contentStream.moveTo(pageMargin, tableY + descriptionCellActualHeight + testNameCellActualHeight + standardFixedRowHeight + standardFixedRowHeight);
            contentStream.lineTo(pageMargin + tableWidth, tableY + descriptionCellActualHeight + testNameCellActualHeight + standardFixedRowHeight + standardFixedRowHeight);
            contentStream.stroke();

            contentStream.moveTo(pageMargin, tableY + summaryTableHeight - (3 * standardFixedRowHeight));
            contentStream.lineTo(pageMargin + tableWidth, tableY + summaryTableHeight - (3 * standardFixedRowHeight));
            contentStream.stroke();

            contentStream.moveTo(pageMargin, tableY + summaryTableHeight - (2 * standardFixedRowHeight));
            contentStream.lineTo(pageMargin + tableWidth, tableY + summaryTableHeight - (2 * standardFixedRowHeight));
            contentStream.stroke();

            contentStream.moveTo(pageMargin, tableY + summaryTableHeight - standardFixedRowHeight);
            contentStream.lineTo(pageMargin + tableWidth, tableY + summaryTableHeight - standardFixedRowHeight);
            contentStream.stroke();

            contentStream.moveTo(pageMargin + col1Width, tableY);
            contentStream.lineTo(pageMargin + col1Width, tableY + summaryTableHeight - testReportRowHeight);
            contentStream.stroke();

            float testReportTitleFontSize = 18;
            float testReportTextY = tableY + summaryTableHeight - testReportRowHeight;
            contentStream.beginText();
            contentStream.setFont(boldFont, testReportTitleFontSize);
            String testReportString = "TEST REPORT";
            float testReportStringWidth = boldFont.getStringWidth(testReportString) / 1000f * testReportTitleFontSize;
            float testReportX = pageMargin + (tableWidth - testReportStringWidth) / 2f;
            float testReportLabelY = adjustVert(testReportTextY, testReportRowHeight, testReportTitleFontSize);
            contentStream.newLineAtOffset(testReportX, testReportLabelY);
            contentStream.showText(testReportString);
            contentStream.endText();


            float execDateRowBottomY = tableY + summaryTableHeight - standardFixedRowHeight - testReportRowHeight;
            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            float execDateLabelY = adjustVert(execDateRowBottomY, standardFixedRowHeight, fontSize);
            contentStream.newLineAtOffset(pageMargin + textPadding, execDateLabelY);
            contentStream.showText("EXECUTION DATE");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(contentFont, contentFontSize);
            float execDateContentY = adjustVert(execDateRowBottomY, standardFixedRowHeight, contentFontSize);
            contentStream.newLineAtOffset(pageMargin + col1Width + textPadding, execDateContentY);
            contentStream.showText(reportData.getFormattedExecutionDate());
            contentStream.endText();

            float execTimeRowBottomY = tableY + summaryTableHeight - (2 * standardFixedRowHeight) - testReportRowHeight;
            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            float execTimeLabelY = adjustVert(execTimeRowBottomY, standardFixedRowHeight, fontSize);
            contentStream.newLineAtOffset(pageMargin + textPadding, execTimeLabelY);
            contentStream.showText("EXECUTION TIME");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(contentFont, contentFontSize);
            float execTimeContentY = adjustVert(execTimeRowBottomY, standardFixedRowHeight, contentFontSize);
            contentStream.newLineAtOffset(pageMargin + col1Width + textPadding, execTimeContentY);
            contentStream.showText(reportData.getFormattedExecutionTime());
            contentStream.endText();

            float testResultRowBottomY = tableY + summaryTableHeight - (3 * standardFixedRowHeight) - testReportRowHeight;
            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            contentStream.setNonStrokingColor(Color.BLACK);
            float testResultLabelY = adjustVert(testResultRowBottomY, standardFixedRowHeight, fontSize);
            contentStream.newLineAtOffset(pageMargin + textPadding, testResultLabelY);
            contentStream.showText("TEST RESULT");
            contentStream.endText();

            if (reportData.getTestStatus().equalsIgnoreCase("SUCCESS")) {
                contentStream.setNonStrokingColor(Color.GREEN);
            } else if (reportData.getTestStatus().equalsIgnoreCase("FAILURE")) {
                contentStream.setNonStrokingColor(Color.RED);
            } else {
                contentStream.setNonStrokingColor(Color.BLACK);
            }

            contentStream.beginText();
            contentStream.setFont(contentFont, contentFontSize);
            float testResultContentY = adjustVert(testResultRowBottomY, standardFixedRowHeight, contentFontSize);
            contentStream.newLineAtOffset(pageMargin + col1Width + textPadding, testResultContentY);
            contentStream.showText(reportData.getTestStatus().toUpperCase());
            contentStream.endText();
            contentStream.setNonStrokingColor(Color.BLACK);

            float responsibleRowBottomY = tableY + descriptionCellActualHeight + testNameCellActualHeight + standardFixedRowHeight;
            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            float responsibleLabelY = adjustVert(responsibleRowBottomY, standardFixedRowHeight, fontSize);
            contentStream.newLineAtOffset(pageMargin + textPadding, responsibleLabelY);
            contentStream.showText("USER");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(contentFont, contentFontSize);
            float responsibleContentY = adjustVert(responsibleRowBottomY, standardFixedRowHeight, contentFontSize);
            contentStream.newLineAtOffset(pageMargin + col1Width + textPadding, responsibleContentY);
            contentStream.showText(reportData.getResponsibleContent());
            contentStream.endText();

            float scriptRowBottomY = tableY + descriptionCellActualHeight + testNameCellActualHeight;
            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            float testCodeLabelY = adjustVert(scriptRowBottomY, standardFixedRowHeight, fontSize);
            contentStream.newLineAtOffset(pageMargin + textPadding, testCodeLabelY);
            contentStream.showText("SCRIPT");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(contentFont, contentFontSize);
            float testCodeContentY = adjustVert(scriptRowBottomY, standardFixedRowHeight, contentFontSize);
            contentStream.newLineAtOffset(pageMargin + col1Width + textPadding, testCodeContentY);
            contentStream.showText(reportData.getNewInfoFieldContent());
            contentStream.endText();

            float testNameRowTopY = tableY + descriptionCellActualHeight + testNameCellActualHeight;
            float testNameRowBottomY = tableY + descriptionCellActualHeight;
            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            float testNameLabelY;
            if (formattedTestNameLines.size() <= 1) {
                testNameLabelY = adjustVert(testNameRowBottomY, testNameCellActualHeight, fontSize);
            } else {
                testNameLabelY = testNameRowTopY - textPadding - labelAscent;
            }
            contentStream.newLineAtOffset(pageMargin + textPadding, testNameLabelY);
            contentStream.showText("TEST NAME");
            contentStream.endText();

            contentStream.setFont(contentFont, contentFontSize);
            float currentTestNameTextY;
            if (formattedTestNameLines.size() <= 1) {
                currentTestNameTextY = adjustVert(testNameRowBottomY, testNameCellActualHeight, contentFontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(pageMargin + col1Width + textPadding, currentTestNameTextY);
                contentStream.showText(formattedTestNameLines.get(0));
                contentStream.endText();
            } else {
                currentTestNameTextY = testNameRowTopY - textPadding - contentAscent;
                for (String line : formattedTestNameLines) {
                    contentStream.beginText();
                    float textX = pageMargin + col1Width + textPadding;
                    contentStream.newLineAtOffset(textX, currentTestNameTextY);
                    contentStream.showText(line);
                    contentStream.endText();
                    currentTestNameTextY -= lineLeading;
                }
            }

            float descriptionRowTopY = tableY + descriptionCellActualHeight;
            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            float descLabelY;
            if (formattedDescriptionLines.size() <= 1) {
                descLabelY = adjustVert(tableY, descriptionCellActualHeight, fontSize);
            } else {
                descLabelY = descriptionRowTopY - textPadding - labelAscent;
            }
            contentStream.newLineAtOffset(pageMargin + textPadding, descLabelY);
            contentStream.showText("DESCRIPTION");
            contentStream.endText();

            contentStream.setFont(contentFont, contentFontSize);
            float currentDescriptionTextY;
            if (formattedDescriptionLines.size() <= 1) {
                currentDescriptionTextY = adjustVert(tableY, descriptionCellActualHeight, contentFontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(pageMargin + col1Width + textPadding, currentDescriptionTextY);
                contentStream.showText(formattedDescriptionLines.get(0));
                contentStream.endText();
            } else {
                currentDescriptionTextY = descriptionRowTopY - textPadding - contentAscent;
                for (String line : formattedDescriptionLines) {
                    contentStream.beginText();
                    float textX = pageMargin + col1Width + textPadding;
                    contentStream.newLineAtOffset(textX, currentDescriptionTextY);
                    contentStream.showText(line);
                    contentStream.endText();
                    currentDescriptionTextY -= lineLeading;
                }
            }
        }
    }
}