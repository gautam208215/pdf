package com.utiltools.imageConverter;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/image")
@CrossOrigin(origins = "http://localhost:3000")
public class ImageToPdfController {

    @PostMapping("/convert-jpg-to-pdf/v1")
    public ResponseEntity<byte[]> convertJpgToPdfV1(@RequestParam("file") MultipartFile file) {
        // Validate file type
        if (file.isEmpty() ||
                !(file.getContentType().equalsIgnoreCase("image/jpeg") ||
                        file.getContentType().equalsIgnoreCase("image/jpg"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            // Convert the JPG to a PDF
            byte[] pdfBytes = convertImageToPdfV1(file);

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=image.pdf");
            headers.add("Content-Type", "application/pdf");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private byte[] convertImageToPdfV1(MultipartFile file) throws IOException {
        // Create a new PDF document
        PDDocument document = new PDDocument();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            // Save the uploaded file to a temporary location
            File tempFile = File.createTempFile("uploaded", ".jpg");
            file.transferTo(tempFile);

            // Load the image into the PDF document
            PDPage page = new PDPage();
            document.addPage(page);

            PDImageXObject pdImage = PDImageXObject.createFromFile(tempFile.getAbsolutePath(), document);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Scale image to fit page
            float scale = Math.min(page.getMediaBox().getWidth() / pdImage.getWidth(),
                    page.getMediaBox().getHeight() / pdImage.getHeight());
            float imageWidth = pdImage.getWidth() * scale;
            float imageHeight = pdImage.getHeight() * scale;
            float xOffset = (page.getMediaBox().getWidth() - imageWidth) / 2;
            float yOffset = (page.getMediaBox().getHeight() - imageHeight) / 2;

            contentStream.drawImage(pdImage, xOffset, yOffset, imageWidth, imageHeight);
            contentStream.close();

            // Save PDF to ByteArrayOutputStream
            document.save(byteArrayOutputStream);

            // Delete temporary file
            tempFile.delete();

        } finally {
            document.close();
        }

        return byteArrayOutputStream.toByteArray();
    }

    @PostMapping("/convert-jpg-to-pdf/v2")
    public ResponseEntity<byte[]> convertJpgToPdfV2(@RequestParam("file") MultipartFile file) {
        // Validate file type
        if (file.isEmpty() ||
                !(file.getContentType().equalsIgnoreCase("image/jpeg") ||
                        file.getContentType().equalsIgnoreCase("image/jpg"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            // Convert image to PDF using iText
            byte[] pdfBytes = convertImageToPdfV2(file);

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=image.pdf");
            headers.add("Content-Type", "application/pdf");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private byte[] convertImageToPdfV2(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Create PdfWriter object
        PdfWriter writer = new PdfWriter(outputStream);

        // Initialize PdfDocument
        PdfDocument pdfDocument = new PdfDocument(writer);

        // Create Document
        Document document = new Document(pdfDocument);

        try {
            // Save the uploaded file to a temporary location
            File tempFile = File.createTempFile("uploaded", ".jpg");
            file.transferTo(tempFile);

            // Load the image
            ImageData imageData = ImageDataFactory.create(tempFile.getAbsolutePath());
            Image image = new Image(imageData);

            // Scale the image to fit the page
            image.setAutoScale(true);

            // Add the image to the document
            document.add(image);

            // Close the document
            document.close();

            // Delete the temporary file
            tempFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error during PDF generation", e);
        }

        return outputStream.toByteArray();
    }

}
