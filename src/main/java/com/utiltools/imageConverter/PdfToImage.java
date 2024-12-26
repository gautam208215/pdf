package com.utiltools.imageConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/images")
public class PdfToImage {

    @Autowired
    private PdfToJpegService pdfToJpegService;

    @PostMapping("/convert")
    public ResponseEntity<byte[]> uploadAndConvertPdf(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty. Please upload a valid PDF file.");
        }

        // Save the uploaded file temporarily
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempFile = new File(tempDir, file.getOriginalFilename());
        file.transferTo(tempFile);

        // Output directory for JPEG files
        String outputDir = tempDir + "/pdf_to_jpeg";
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // Convert PDF to JPEG
        List<String> imagePaths = pdfToJpegService.convertPdfToJpeg(tempFile, outputDir);

        // Create a ZIP file from the images
        byte[] zipData = createZipFromFiles(imagePaths);

        // Cleanup temporary files
        tempFile.delete();
        for (String path : imagePaths) {
            new File(path).delete();
        }

        // Return ZIP file as a response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted_images.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipData);
    }

    private byte[] createZipFromFiles(List<String> filePaths) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {
            for (String filePath : filePaths) {
                File file = new File(filePath);
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                }
            }
        }
        return byteArrayOutputStream.toByteArray();
    }
}
