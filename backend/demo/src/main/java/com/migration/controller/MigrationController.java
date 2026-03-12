package com.migration.controller;

import com.migration.model.MigrationResponse;
import com.migration.service.Document360Service;
import com.migration.service.DocxParserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class MigrationController {

    private static final Logger logger = Logger.getLogger(MigrationController.class.getName());

    private final DocxParserService parserService;
    private final Document360Service document360Service;

    public MigrationController(DocxParserService parserService, Document360Service document360Service) {
        this.parserService = parserService;
        this.document360Service = document360Service;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Migration backend is running");
    }

    @PostMapping("/parse")
    public ResponseEntity<MigrationResponse> parseDocument(
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".docx")) {
            return ResponseEntity.badRequest().body(MigrationResponse.error("Invalid file. Please upload a .docx file."));
        }

        try {
            String html = parserService.convertToHtml(file);
            return ResponseEntity.ok(MigrationResponse.ok(html));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in /parse endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MigrationResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<MigrationResponse> uploadToDocument360(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {

        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".docx")) {
            return ResponseEntity.badRequest().body(MigrationResponse.error("Invalid file. Please upload a .docx file."));
        }

        if (title == null || title.trim().isEmpty()) {
            title = file.getOriginalFilename().replaceFirst("[.][^.]+$", "");
        }

        try {
            // 1. Convert DOCX to HTML
            String html = parserService.convertToHtml(file);

            // 2. Upload to Document360
            String doc360Response = document360Service.createArticle(title, html);

            // 3. Extract Article ID
            String articleId = document360Service.extractArticleId(doc360Response);

            // 4. Build Response
            MigrationResponse response = MigrationResponse.ok(html);
            response.setDocument360Response(doc360Response);
            response.setArticleId(articleId);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in /upload endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MigrationResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/download-html")
    public ResponseEntity<ByteArrayResource> downloadHtml(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".docx")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String html = parserService.convertToHtml(file);
            byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
            ByteArrayResource resource = new ByteArrayResource(htmlBytes);

            String filename = file.getOriginalFilename().replaceFirst("[.][^.]+$", "") + ".html";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_HTML)
                    .contentLength(htmlBytes.length)
                    .body(resource);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in /download-html endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
