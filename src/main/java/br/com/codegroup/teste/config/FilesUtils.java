package br.com.codegroup.teste.config;

import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Margin;
import com.microsoft.playwright.options.ServiceWorkerPolicy;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;

import static br.com.codegroup.teste.modulos.comum.utils.StringUtils.tratarNomeDownload;

@Component
@RequiredArgsConstructor
public class FilesUtils {

    private final TemplateEngine engine;
    private final ReentrantLock pdfLock = new ReentrantLock();
    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    public byte[] htmlToPdf(String html) {
        pdfLock.lock();
        try (BrowserContext context = browser.newContext(
            new Browser.NewContextOptions()
                .setJavaScriptEnabled(false)
                .setServiceWorkers(ServiceWorkerPolicy.BLOCK)
        )) {
            var page = context.newPage();
            page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.LOAD));
            page.evaluate("() => document.fonts.ready");
            return page.pdf(new Page.PdfOptions()
                .setFormat("A4")
                .setPrintBackground(true)
                .setMargin(new Margin().setTop("0mm").setRight("0mm").setBottom("0mm").setLeft("0mm")));
        } catch (Exception ex) {
            throw new ValidacaoException("Erro ao gerar PDF");
        } finally {
            pdfLock.unlock();
        }
    }

    public InputStream htmlToPdfStream(String html) {
        return new ByteArrayInputStream(htmlToPdf(html));
    }

    public File htmlToPdfFile(String html) {
        try {
            var path = Files.createTempFile("arquivo", ".pdf");
            Files.write(path, htmlToPdf(html));
            return path.toFile();
        } catch (Exception ex) {
            throw new ValidacaoException("Erro ao criar PDF");
        }
    }

    public String processTemplate(String templateName, Map<String, Object> map) {
        var ctx = new Context();
        map.forEach(ctx::setVariable);
        return engine.process(templateName, ctx);
    }

    public void baixarArquivo(HttpServletResponse response, InputStream file, String nome, String contentType) {
        try {
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=".concat(
                tratarNomeDownload(nome)));
            IOUtils.copy(file, response.getOutputStream());
        } catch (Exception ex) {
            throw new ValidacaoException("Erro ao transferir arquivo");
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
