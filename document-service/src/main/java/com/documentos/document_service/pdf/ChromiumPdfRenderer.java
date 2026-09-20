package com.documentos.document_service.pdf;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChromiumPdfRenderer {

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public synchronized void init() {
        startChromium();
    }

    /**
     * Playwright Java no es thread-safe. Tomcat puede invocar este componente
     * desde varios hilos, por lo que serializamos el acceso al browser.
     */
    public synchronized byte[] render(String html) {
        ensureBrowser();

        try {
            return renderWithBrowser(html);
        } catch (RuntimeException firstFailure) {
            // Si Chromium ha muerto, recreamos Playwright/Browser y reintentamos
            // una vez. Esto evita dejar el microservicio inutilizado hasta reinicio.
            restartChromium();
            try {
                return renderWithBrowser(html);
            } catch (RuntimeException retryFailure) {
                retryFailure.addSuppressed(firstFailure);
                throw retryFailure;
            }
        }
    }

    private byte[] renderWithBrowser(String html) {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();

            page.setContent(
                    html,
                    new Page.SetContentOptions()
                            .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE)
            );

            return page.pdf(
                    new Page.PdfOptions()
                            .setFormat("A4")
                            .setPrintBackground(true)
                            .setPreferCSSPageSize(true)
            );
        }
    }

    private void ensureBrowser() {
        if (playwright == null || browser == null || !browser.isConnected()) {
            restartChromium();
        }
    }

    private void startChromium() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new com.microsoft.playwright.BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setArgs(List.of(
                                "--no-sandbox",
                                "--disable-dev-shm-usage"
                        ))
        );
    }

    private void restartChromium() {
        closeChromium();
        startChromium();
    }

    @PreDestroy
    public synchronized void destroy() {
        closeChromium();
    }

    private void closeChromium() {
        if (browser != null) {
            try {
                browser.close();
            } catch (RuntimeException ignored) {
                // Chromium puede haber terminado por su cuenta.
            } finally {
                browser = null;
            }
        }

        if (playwright != null) {
            try {
                playwright.close();
            } catch (RuntimeException ignored) {
                // Playwright puede haber terminado por su cuenta.
            } finally {
                playwright = null;
            }
        }
    }
}
