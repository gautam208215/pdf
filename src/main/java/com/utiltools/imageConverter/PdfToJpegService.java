package com.utiltools.imageConverter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfToJpegService {

    public List<String> convertPdfToJpeg(File pdfFile, String outputDir) throws IOException {
        List<String> outputFilePaths = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                // Render each page as an image
                BufferedImage image = pdfRenderer.renderImageWithDPI(i, 300); // 300 DPI for high quality

                // Save as a JPEG file
                File outputFile = new File(outputDir, "page_" + (i + 1) + ".jpg");
                ImageIO.write(image, "JPEG", outputFile);

                outputFilePaths.add(outputFile.getAbsolutePath());
            }
        }
        return outputFilePaths;
    }
}