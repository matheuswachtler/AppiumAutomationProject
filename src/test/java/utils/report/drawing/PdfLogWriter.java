package utils.report.drawing;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;

import utils.report.TestReportData;

public class PdfLogWriter {

    private static final float MARGIN = 30;
    private static final float LOG_FONT_SIZE = 10;
    private static final float LEADING_FACTOR = 1.8f;
    private static final float DUMMY_SUMMARY_TABLE_HEIGHT = 60;
    private static final float DUMMY_SUMMARY_MARGIN_FROM_BOTTOM = 50;

    public static void generateLogsPage(PDDocument document, TestReportData reportData, PdfPageTemplate pageTemplate) throws IOException {
        if (reportData.getLogsContent().isEmpty()) {
            return;
        }

        PDPage currentPage = null;
        PDPageContentStream contentStream = null;

        try {
            currentPage = pageTemplate.addPageWithMarginAndFooter(document);
            contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);

            PDType1Font logFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float leading = LEADING_FACTOR * LOG_FONT_SIZE;
            float startX = MARGIN;

            String logTitle = "EXECUTION LOGS FOR TEST: " + reportData.getTestNumber();
            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            float titleFontSize = 12;
            float titleWidth = titleFont.getStringWidth(logTitle) / 1000f * titleFontSize;

            float currentY = currentPage.getMediaBox().getHeight() - MARGIN - 30;

            contentStream.beginText();
            contentStream.setFont(titleFont, titleFontSize);
            float titleX = (currentPage.getMediaBox().getWidth() - titleWidth) / 2f;
            float titleY = currentPage.getMediaBox().getHeight() - MARGIN + 15;
            contentStream.newLineAtOffset(titleX, titleY);
            contentStream.showText(logTitle);
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(logFont, LOG_FONT_SIZE);
            contentStream.setLeading(leading);
            contentStream.newLineAtOffset(startX, currentY);

            String[] lines = reportData.getLogsContent().split("\\r?\\n");

            for (String line : lines) {
                String cleanedLine = line.replaceAll("[\\n\\r]", "");

                float requiredSpaceForSummary = DUMMY_SUMMARY_TABLE_HEIGHT + DUMMY_SUMMARY_MARGIN_FROM_BOTTOM;

                if (currentY < MARGIN + leading + requiredSpaceForSummary) {
                    contentStream.endText();
                    contentStream.close();

                    currentPage = pageTemplate.addPageWithMarginAndFooter(document);
                    contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);

                    contentStream.beginText();
                    contentStream.setFont(titleFont, titleFontSize);
                    contentStream.newLineAtOffset(titleX, titleY);
                    contentStream.showText(logTitle + " (Cont.)");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(logFont, LOG_FONT_SIZE);
                    contentStream.setLeading(leading);
                    currentY = currentPage.getMediaBox().getHeight() - MARGIN - 30;
                    contentStream.newLineAtOffset(startX, currentY);
                }
                contentStream.showText(cleanedLine);
                contentStream.newLine();
                currentY -= leading;
            }
            contentStream.endText();

        } finally {
            if (contentStream != null) {
                contentStream.close();
            }
        }
    }
}