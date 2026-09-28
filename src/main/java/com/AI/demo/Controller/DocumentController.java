package com.AI.demo.Controller;

import com.AI.demo.Serivce.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final GeminiService geminiService;

    @Value("${rapidapi.proxy.secret}")
    private String expectedProxySecret;

    public DocumentController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)    public ResponseEntity<String> extractDetails(
            @RequestHeader(value = "X-RapidAPI-Proxy-Secret", required = false) String incomingSecret,
            @RequestPart("file") MultipartFile file) {

        // Validate that request originated exclusively from RapidAPI
        if (expectedProxySecret == null || !expectedProxySecret.equals(incomingSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\": \"Unauthorized: Direct calls not permitted.\"}");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"No file uploaded.\"}");
        }

        try {
            String result = geminiService.extractInformation(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to process document: " + e.getMessage());
        }
    }

}
