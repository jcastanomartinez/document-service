package com.documentos.document_service.service;

import com.documentos.document_service.model.Document;
import com.documentos.document_service.repository.DocumentRepository;
import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.documentos.document_service.dto.InvoiceDocumentRequest;
import java.nio.file.Path;
import java.time.Instant;
import com.documentos.document_service.pdf.ChromiumPdfRenderer;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final TemplateEngine templateEngine;
    private final ChromiumPdfRenderer chromiumPdfRenderer;

    public DocumentService(DocumentRepository repository, TemplateEngine templateEngine, ChromiumPdfRenderer chromiumPdfRenderer, ChromiumPdfRenderer chromiumPdfRenderer1) {
        this.repository = repository;
        this.templateEngine = templateEngine;
        this.chromiumPdfRenderer = chromiumPdfRenderer1;
    }

    public byte[] generateTestPdf(String title, String name, String description) {
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

        String html = templateEngine.process("test-pdf", ctx);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();

            page.setContent(html);
            Path pdfPath = Path.of("tmp.pdf");
            page.pdf(new Page.PdfOptions()
                    .setPath(pdfPath)
                    .setFormat("A4"));

            byte[] bytes = java.nio.file.Files.readAllBytes(pdfPath);
            java.nio.file.Files.deleteIfExists(pdfPath);
            browser.close();
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

        public byte[] generateInvoice(InvoiceDocumentRequest invoice) {

            Context context = new Context();

            context.setVariable("invoiceNumber", invoice.invoiceNumber());
            context.setVariable("invoiceDate", invoice.invoiceDate());

            context.setVariable("company", invoice.company());
            context.setVariable("customer", invoice.customer());

            context.setVariable("items", invoice.items());

            context.setVariable("subtotal", invoice.subtotal());
            context.setVariable("taxTotal", invoice.taxTotal());
            context.setVariable("total", invoice.total());

            String html = templateEngine.process(
                    "invoice/standard",
                    context
            );

            return chromiumPdfRenderer.render(html);
        }
    }
