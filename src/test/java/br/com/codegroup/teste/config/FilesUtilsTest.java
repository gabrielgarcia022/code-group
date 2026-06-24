package br.com.codegroup.teste.config;

import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FilesUtilsTest {

    private static final String HTML = "<html><body><h1>Relatório</h1></body></html>";

    @Mock
    private TemplateEngine engine;
    @Mock
    private Browser browser;
    @Mock
    private BrowserContext browserContext;
    @Mock
    private Page page;
    @Mock
    private Playwright playwright;
    private FilesUtils filesUtils;

    @BeforeEach
    void setUp() {
        filesUtils = new FilesUtils(engine);
    }

    @Test
    @DisplayName("Deve converter HTML para PDF")
    void deveConverterHtmlParaPdf() {
        var pdfBytes = "conteudo-pdf".getBytes(StandardCharsets.UTF_8);
        ReflectionTestUtils.setField(filesUtils, "browser", browser);
        when(page.pdf(any(Page.PdfOptions.class))).thenReturn(pdfBytes);
        when(browser.newContext(any(Browser.NewContextOptions.class))).thenReturn(browserContext);
        when(browserContext.newPage()).thenReturn(page);
        when(page.evaluate("() => document.fonts.ready")).thenReturn(null);

        var resultado = filesUtils.htmlToPdf(HTML);
        assertThat(resultado).isEqualTo(pdfBytes);

        verify(browser).newContext(any(Browser.NewContextOptions.class));
        verify(browserContext).newPage();
        verify(page).setContent(eq(HTML), any(Page.SetContentOptions.class));
        verify(page).evaluate("() => document.fonts.ready");
        verify(page).pdf(any(Page.PdfOptions.class));
        verify(browserContext).close();
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException ao falhar na geração do PDF")
    void deveLancarValidacaoExceptionAoFalharNaGeracaoDoPdf() {
        ReflectionTestUtils.setField(filesUtils, "browser", browser);

        when(browser.newContext(any(Browser.NewContextOptions.class)))
            .thenThrow(new RuntimeException("erro playwright"));

        assertThatThrownBy(() -> filesUtils.htmlToPdf(HTML))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Erro ao gerar PDF");
    }

    @Test
    @DisplayName("Deve converter HTML para InputStream de PDF")
    void deveConverterHtmlParaPdfStream() throws Exception {
        var pdfBytes = "conteudo-pdf".getBytes(StandardCharsets.UTF_8);

        var filesUtilsSpy = spy(filesUtils);

        doReturn(pdfBytes).when(filesUtilsSpy).htmlToPdf(HTML);

        var resultado = filesUtilsSpy.htmlToPdfStream(HTML);

        assertThat(resultado).isNotNull();
        assertThat(resultado.readAllBytes()).isEqualTo(pdfBytes);

        verify(filesUtilsSpy).htmlToPdf(HTML);
    }

    @Test
    @DisplayName("Deve converter HTML para arquivo PDF temporário")
    void deveConverterHtmlParaPdfFile() throws Exception {
        var pdfBytes = "conteudo-pdf".getBytes(StandardCharsets.UTF_8);

        var filesUtilsSpy = spy(filesUtils);

        doReturn(pdfBytes).when(filesUtilsSpy).htmlToPdf(HTML);

        var arquivo = filesUtilsSpy.htmlToPdfFile(HTML);

        assertThat(arquivo).isNotNull();
        assertThat(arquivo).exists();
        assertThat(arquivo.getName()).endsWith(".pdf");
        assertThat(java.nio.file.Files.readAllBytes(arquivo.toPath())).isEqualTo(pdfBytes);

        verify(filesUtilsSpy).htmlToPdf(HTML);

        java.nio.file.Files.deleteIfExists(arquivo.toPath());
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException ao falhar na criação do arquivo PDF")
    void deveLancarValidacaoExceptionAoFalharNaCriacaoDoArquivoPdf() {
        var filesUtilsSpy = spy(filesUtils);

        doReturn(null).when(filesUtilsSpy).htmlToPdf(HTML);

        assertThatThrownBy(() -> filesUtilsSpy.htmlToPdfFile(HTML))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Erro ao criar PDF");
    }

    @Test
    @DisplayName("Deve processar template Thymeleaf")
    void deveProcessarTemplateThymeleaf() {
        var variaveis = java.util.Map.<String, Object>of(
            "nome", "Gabriel",
            "titulo", "Relatório"
        );

        when(engine.process(eq("template-teste"), any(Context.class)))
            .thenReturn("<html>template processado</html>");

        var resultado = filesUtils.processTemplate("template-teste", variaveis);

        assertThat(resultado).isEqualTo("<html>template processado</html>");

        verify(engine).process(eq("template-teste"), any(Context.class));
    }

    @Test
    @DisplayName("Deve baixar arquivo na response")
    void deveBaixarArquivoNaResponse() throws Exception {
        var response = new MockHttpServletResponse();
        var conteudo = "conteudo do arquivo";
        var inputStream = new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8));

        filesUtils.baixarArquivo(
            response,
            inputStream,
            "portfolio.pdf",
            "application/pdf"
        );

        assertThat(response.getContentType()).isEqualTo("application/pdf");
        assertThat(response.getHeader("Content-Disposition"))
            .isEqualTo("attachment; filename=portfolio.pdf");
        assertThat(response.getContentAsString()).isEqualTo(conteudo);
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException ao falhar na transferência do arquivo")
    void deveLancarValidacaoExceptionAoFalharNaTransferenciaDoArquivo() throws Exception {
        var response = org.mockito.Mockito.mock(HttpServletResponse.class);
        var inputStream = new ByteArrayInputStream("arquivo".getBytes(StandardCharsets.UTF_8));

        when(response.getOutputStream()).thenThrow(new IOException("erro output stream"));

        assertThatThrownBy(() -> filesUtils.baixarArquivo(
            response,
            inputStream,
            "portfolio.pdf",
            "application/pdf"
        ))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Erro ao transferir arquivo");
    }

    @Test
    @DisplayName("Deve fechar browser e playwright no destroy")
    void deveFecharBrowserEPlaywrightNoDestroy() {
        ReflectionTestUtils.setField(filesUtils, "browser", browser);
        ReflectionTestUtils.setField(filesUtils, "playwright", playwright);

        filesUtils.destroy();

        verify(browser).close();
        verify(playwright).close();
    }

    @Test
    @DisplayName("Não deve lançar erro no destroy quando browser e playwright forem null")
    void naoDeveLancarErroNoDestroyQuandoBrowserEPlaywrightForemNull() {
        filesUtils.destroy();
    }
}
