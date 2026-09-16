package com.documentos.document_service.controller;

import com.documentos.document_service.service.DocumentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.documentos.document_service.dto.InvoiceDocumentRequest;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @GetMapping("/test-pdf")
    public ResponseEntity<byte[]> generateTestPdf(
            @RequestParam(defaultValue = "Documento de prueba") String title,
            @RequestParam(defaultValue = "Usuario") String name,
            @RequestParam(defaultValue = "Descripción de prueba") String description) {

        byte[] pdf = service.generateTestPdf(title, name, description);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping(
            value = "/invoices",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> generateInvoice(
            @RequestBody InvoiceDocumentRequest request) {

        byte[] pdf = service.generateInvoice(request);

        String fileName = request.invoiceNumber() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(fileName)
                                .build()
                                .toString()
                )
                .body(pdf);
    }
}