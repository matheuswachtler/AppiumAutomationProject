package utils.report.drawing;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;

public class PdfPageTemplate {

    private static final float MARGIN = 30;
    private static final float FOOTER_TABLE_HEIGHT = 20;
    private static final float FOOTER_TEXT_PADDING = 5;
    private static final PDType1Font FOOTER_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final float FOOTER_FONT_SIZE = 12;

    public PDPage addPageWithMarginAndFooter(PDDocument document) throws IOException {
        PDPage newPage = new PDPage(PDRectangle.A4);
        document.addPage(newPage);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, newPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
            float pageWidth = newPage.getMediaBox().getWidth();

            contentStream.setStrokingColor(Color.BLACK);
            contentStream.setLineWidth(1f);

            contentStream.addRect(MARGIN, MARGIN, pageWidth - (2 * MARGIN), newPage.getMediaBox().getHeight() - (2 * MARGIN));
            contentStream.stroke();

            float footerTableWidth = pageWidth - (2 * MARGIN);
            float footerCol1Width = footerTableWidth * 0.25f;

            contentStream.addRect(MARGIN, MARGIN, footerTableWidth, FOOTER_TABLE_HEIGHT);
            contentStream.stroke();

            contentStream.moveTo(MARGIN + footerCol1Width, MARGIN);
            contentStream.lineTo(MARGIN + footerCol1Width, MARGIN + FOOTER_TABLE_HEIGHT);
            contentStream.stroke();

            contentStream.beginText();
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            contentStream.setFont(boldFont, FOOTER_FONT_SIZE);
            float pageTextY = MARGIN + (FOOTER_TABLE_HEIGHT - FOOTER_FONT_SIZE) / 2f;
            contentStream.newLineAtOffset(MARGIN + FOOTER_TEXT_PADDING, pageTextY);
            contentStream.showText("PAGE");
            contentStream.endText();
        }
        return newPage;
    }

    public void updatePageNumbersInFooter(PDDocument document) throws IOException {
        int totalPageCount = document.getPages().getCount();

        for (int i = 0; i < totalPageCount; i++) {
            PDPage page = document.getPage(i);
            try (PDPageContentStream footerContentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                float pageWidth = page.getMediaBox().getWidth();
                float footerTableWidth = pageWidth - (2 * MARGIN);
                float footerCol1Width = footerTableWidth * 0.25f;

                String pageNumberText = (i + 1) + " of " + totalPageCount;

                float pageNumberX = MARGIN + footerCol1Width + FOOTER_TEXT_PADDING;
                float pageTextY = MARGIN + (FOOTER_TABLE_HEIGHT - FOOTER_FONT_SIZE) / 2f;

                footerContentStream.beginText();
                footerContentStream.setFont(FOOTER_FONT, FOOTER_FONT_SIZE);
                footerContentStream.newLineAtOffset(pageNumberX, pageTextY);
                footerContentStream.showText(pageNumberText);
                footerContentStream.endText();
            }
        }
    }
}