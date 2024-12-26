package com.utiltools.imageConverter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

@RestController
@RequestMapping("/word")
public class WordToPdfController {

        private static final String LIBREOFFICE_PATH = "C:\\Program Files\\LibreOffice\\program\\soffice"; // Adjust if not in PATH

        @PostMapping("/pdf")
        public ResponseEntity<byte[]> convertWordToPdf(@RequestParam("file") MultipartFile file) {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            File tempWordFile = null;
            File tempPdfFile = null;

            try {
                // Save uploaded file to a temporary file
                tempWordFile = File.createTempFile("temp", ".docx");
                try (FileOutputStream fos = new FileOutputStream(tempWordFile)) {
                    fos.write(file.getBytes());
                }

                // Prepare output PDF file
                tempPdfFile = File.createTempFile("temp", ".pdf");

                // Execute LibreOffice conversion command
                ProcessBuilder processBuilder = new ProcessBuilder(
                        LIBREOFFICE_PATH,
                        "--headless",
                        "--convert-to",
                        "pdf",
                        "--outdir",
                        tempPdfFile.getParent(),
                        tempWordFile.getAbsolutePath()
                );
                Process process = processBuilder.start();
                if (process.waitFor() != 0) {
                    throw new RuntimeException("LibreOffice conversion failed.");
                }

                // Read the generated PDF file
                byte[] pdfBytes = Files.readAllBytes(tempPdfFile.toPath());

                // Prepare response
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition", "attachment; filename=document.pdf");

                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            } finally {
                // Cleanup temporary files
                if (tempWordFile != null && tempWordFile.exists()) {
                    tempWordFile.delete();
                }
                if (tempPdfFile != null && tempPdfFile.exists()) {
                    tempPdfFile.delete();
                }
            }
        }

}
