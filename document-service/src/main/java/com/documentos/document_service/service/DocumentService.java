package com.documentos.document_service.service;

import com.documentos.document_service.dto.InvoiceDocumentRequest;
import com.documentos.document_service.model.Document;
import com.documentos.document_service.pdf.ChromiumPdfRenderer;
import com.documentos.document_service.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final TemplateEngine templateEngine;
    private final ChromiumPdfRenderer chromiumPdfRenderer;

    public DocumentService(
            DocumentRepository repository,
            TemplateEngine templateEngine,
            ChromiumPdfRenderer chromiumPdfRenderer) {

        this.repository = repository;
        this.templateEngine = templateEngine;
        this.chromiumPdfRenderer = chromiumPdfRenderer;
    }

    public byte[] generateTestPdf(
            String title,
            String name,
            String description) {

        Document doc = new Document();

        doc.setTitle(title);
        doc.setName(name);
        doc.setDescription(description);
        doc.setCreatedAt(Instant.now());

        repository.save(doc);

        Context ctx = new Context();

        ctx.setVariable("title", title);
        ctx.setVariable("name", name);
        ctx.setVariable("description", description);

        String html = templateEngine.process(
                "test-pdf",
                ctx
        );

        // Este método lo puedes mantener para las pruebas.
        // Tu implementación actual funciona.
        return chromiumPdfRenderer.render(html);
    }

    public byte[] generateInvoice(InvoiceDocumentRequest invoice) {

        Context context = new Context();

        context.setVariable(
                "invoiceNumber",
                invoice.invoiceNumber()
        );

        context.setVariable(
                "invoiceDate",
                invoice.invoiceDate()
        );

        context.setVariable(
                "company",
                invoice.company()
        );

        context.setVariable(
                "customer",
                invoice.customer()
        );

        context.setVariable(
                "items",
                invoice.items()
        );

        context.setVariable(
                "subtotal",
                invoice.subtotal()
        );

        context.setVariable(
                "taxTotal",
                invoice.taxTotal()
        );

        context.setVariable(
                "total",
                invoice.total()
        );

        String html = templateEngine.process(
                "invoice/standard",
                context
        );

        return chromiumPdfRenderer.render(html);
    }
}