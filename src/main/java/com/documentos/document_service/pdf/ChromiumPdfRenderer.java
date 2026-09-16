package com.documentos.document_service.pdf;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class ChromiumPdfRenderer {

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {

        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new com.microsoft.playwright.BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setArgs(java.util.List.of(
                                "--no-sandbox",
                                "--disable-dev-shm-usage"
                        ))
        );
    }

    public byte[] render(String html) {

        try (BrowserContext context = browser.newContext()) {

            Page page = context.newPage();

            page.setContent(
                    html,
                    new Page.SetContentOptions()
                            .setWaitUntil(
                                    com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE
                            )
            );

            return page.pdf(
                    new Page.PdfOptions()
                            .setFormat("A4")
                            .setPrintBackground(true)
                            .setPreferCSSPageSize(true)
            );
        }
    }

    @PreDestroy
    public void destroy() {

        if (browser != null) {
            browser.close();
        }

        if (playwright != null) {
            playwright.close();
        }
    }
}