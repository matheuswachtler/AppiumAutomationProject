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

    private static float adjustVert(float baseY, float rowHeight, float fontSize) {
        return baseY + (rowHeight - (fontSize * 0.7f)) / 2f;
    }

    public static void generateLogsPage(PDDocument document, TestReportData reportData, PdfPageTemplate pageTemplate) throws IOException {
        if (reportData.getLogsContent().isEmpty()) {
            return;
        }

        PDPage currentPage = pageTemplate.addPageWithMarginAndFooter(document);
        PDPageContentStream contentStream = null;

        try {
            contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);

            PDType1Font logFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float leading = LEADING_FACTOR * LOG_FONT_SIZE;
            float startX = MARGIN;

            String logTitle = "EXECUTION LOGS";
            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            float titleFontSize = 18;

            float logTitleAreaHeight = 20f;
            float logTitleAreaTopY = currentPage.getMediaBox().getHeight() - MARGIN;
            float logTitleAreaBottomY = logTitleAreaTopY - logTitleAreaHeight;

            contentStream.setLineWidth(1f);
            contentStream.setStrokingColor(0, 0, 0);

            contentStream.moveTo(MARGIN, logTitleAreaTopY);
            contentStream.lineTo(currentPage.getMediaBox().getWidth() - MARGIN, logTitleAreaTopY);
            contentStream.stroke();

            contentStream.moveTo(MARGIN, logTitleAreaBottomY);
            contentStream.lineTo(currentPage.getMediaBox().getWidth() - MARGIN, logTitleAreaBottomY);
            contentStream.stroke();

            float titleWidth = titleFont.getStringWidth(logTitle) / 1000f * titleFontSize;
            float titleX = (currentPage.getMediaBox().getWidth() - titleWidth) / 2f;
            float titleY = adjustVert(logTitleAreaBottomY, logTitleAreaHeight, titleFontSize);

            contentStream.beginText();
            contentStream.setFont(titleFont, titleFontSize);
            contentStream.newLineAtOffset(titleX, titleY);
            contentStream.showText(logTitle);
            contentStream.endText();

            float currentY = logTitleAreaBottomY - 10;

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

                    logTitleAreaTopY = currentPage.getMediaBox().getHeight() - MARGIN;
                    logTitleAreaBottomY = logTitleAreaTopY - logTitleAreaHeight;

                    contentStream.setLineWidth(1f);
                    contentStream.setStrokingColor(0, 0, 0);

                    contentStream.moveTo(MARGIN, logTitleAreaTopY);
                    contentStream.lineTo(currentPage.getMediaBox().getWidth() - MARGIN, logTitleAreaTopY);
                    contentStream.stroke();

                    contentStream.moveTo(MARGIN, logTitleAreaBottomY);
                    contentStream.lineTo(currentPage.getMediaBox().getWidth() - MARGIN, logTitleAreaBottomY);
                    contentStream.stroke();

                    titleX = (currentPage.getMediaBox().getWidth() - titleWidth) / 2f;
                    titleY = adjustVert(logTitleAreaBottomY, logTitleAreaHeight, titleFontSize);

                    contentStream.beginText();
                    contentStream.setFont(titleFont, titleFontSize);
                    contentStream.newLineAtOffset(titleX, titleY);
                    contentStream.showText(logTitle + " (Cont.)");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(logFont, LOG_FONT_SIZE);
                    contentStream.setLeading(leading);
                    currentY = logTitleAreaBottomY - 10;
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