package utils.report.drawing;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import utils.report.TestReportData;

public class PdfLogWriter {

    private static final float MARGIN = 30;
    private static final float LOG_FONT_SIZE = 10;
    private static final float LEADING_FACTOR = 1.8f;
    private static final float LOG_AREA_TOP_PADDING = 10;
    private static final float TITLE_AREA_HEIGHT = 20;
    private static final float LOG_INDENT = 5;

    public static void generateLogsPage(PDDocument document, TestReportData reportData, PdfPageTemplate pageTemplate) throws IOException {
        if (reportData.getLogsContent().isEmpty()) {
            return;
        }

        PDPage currentPage = pageTemplate.addPageWithMarginAndFooter(document);
        PDPageContentStream contentStream = null;
        try {
            contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
            float pageWidth = currentPage.getMediaBox().getWidth();
            float pageHeight = currentPage.getMediaBox().getHeight();

            float titleY = pageHeight - MARGIN - TITLE_AREA_HEIGHT;
            float textY = titleY - LOG_AREA_TOP_PADDING;

            drawLogTitleArea(contentStream, currentPage, titleY);

            PDType1Font logFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float logFontSize = LOG_FONT_SIZE;
            float leading = logFontSize * LEADING_FACTOR;
            float margin = MARGIN;
            float maxLineWidth = pageWidth - (2 * margin) - LOG_INDENT;

            contentStream.setFont(logFont, logFontSize);
            contentStream.setLeading(leading);

            String[] logLines = reportData.getLogsContent().split("\\r?\\n");
            float currentY = textY;

            for (String line : logLines) {
                if (currentY <= (margin + (LOG_FONT_SIZE * 2))) {
                    contentStream.close();
                    currentPage = pageTemplate.addPageWithMarginAndFooter(document);
                    contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true, true);
                    contentStream.setFont(logFont, logFontSize);
                    contentStream.setLeading(leading);
                    currentY = pageHeight - margin - leading;
                }

                List<String> wrappedLines = wrapTextToLines(line, logFont, logFontSize, maxLineWidth);

                for (String wrappedLine : wrappedLines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin + LOG_INDENT, currentY);
                    contentStream.showText(wrappedLine);
                    contentStream.endText();
                    currentY -= leading;
                }
            }
        } finally {
            if (contentStream != null) {
                contentStream.close();
            }
        }
    }

    private static void drawLogTitleArea(PDPageContentStream contentStream, PDPage page, float titleY) throws IOException {
        PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        float titleFontSize = 18;
        String title = "EXECUTION LOGS";
        float titleAreaTopY = titleY + TITLE_AREA_HEIGHT;

        contentStream.setLineWidth(1f);
        contentStream.setStrokingColor(0, 0, 0);

        contentStream.moveTo(MARGIN, titleAreaTopY);
        contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, titleAreaTopY);
        contentStream.stroke();

        contentStream.moveTo(MARGIN, titleY);
        contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, titleY);
        contentStream.stroke();

        float titleWidth = titleFont.getStringWidth(title) / 1000f * titleFontSize;
        float titleX = (page.getMediaBox().getWidth() - titleWidth) / 2f;
        float titleTextY = titleY + (TITLE_AREA_HEIGHT - (titleFontSize * 0.7f)) / 2f;

        contentStream.beginText();
        contentStream.setFont(titleFont, titleFontSize);
        contentStream.newLineAtOffset(titleX, titleTextY);
        contentStream.showText(title);
        contentStream.endText();
    }

    private static List<String> wrapTextToLines(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> result = new ArrayList<>();
        String[] words = text.split(" ");
        if (words.length == 0) {
            result.add("");
            return result;
        }

        StringBuilder line = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            String word = words[i];
            float currentWidth = font.getStringWidth(line.toString()) / 1000 * fontSize;
            float wordWidth = font.getStringWidth(" " + word) / 1000 * fontSize;

            if (currentWidth + wordWidth < maxWidth) {
                line.append(" ").append(word);
            } else {
                result.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        result.add(line.toString());
        return result;
    }
}