package br.com.codegroup.teste.modulos.comum.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringUtilsTest {

    @Test
    @DisplayName("Deve normalizar texto removendo acentos, espaços laterais e convertendo para maiúsculo")
    void deveNormalizarTexto() {
        var resultado = StringUtils.normalizar("  João da Silva  ");

        assertEquals("JOAO DA SILVA", resultado);
    }

    @Test
    @DisplayName("Deve normalizar texto com cedilha e caracteres acentuados")
    void deveNormalizarTextoComCedilhaEAcentos() {
        var resultado = StringUtils.normalizar("ação pública");

        assertEquals("ACAO PUBLICA", resultado);
    }

    @Test
    @DisplayName("Deve formatar valor monetário no padrão brasileiro")
    void deveFormatarValorMonetarioNoPadraoBrasileiro() {
        var resultado = StringUtils.adicionarMascaraValor(new BigDecimal("1234.50"));

        assertEquals("1.234,50", resultado);
    }

    @Test
    @DisplayName("Deve formatar valor com arredondamento")
    void deveFormatarValorComArredondamento() {
        var resultado = StringUtils.adicionarMascaraValor(new BigDecimal("10.555"));

        assertEquals("10,56", resultado);
    }

    @Test
    @DisplayName("Deve formatar valor inteiro com duas casas decimais")
    void deveFormatarValorInteiroComDuasCasasDecimais() {
        var resultado = StringUtils.adicionarMascaraValor(new BigDecimal("100"));

        assertEquals("100,00", resultado);
    }

    @Test
    @DisplayName("Deve retornar zero formatado quando valor for null")
    void deveRetornarZeroFormatadoQuandoValorForNull() {
        var resultado = StringUtils.adicionarMascaraValor(null);

        assertEquals("0,00", resultado);
    }

    @Test
    @DisplayName("Deve tratar nome de download removendo acentos, deixando minúsculo e trocando espaços por hífen")
    void deveTratarNomeDownload() {
        var resultado = StringUtils.tratarNomeDownload("Relatório de Projetos.pdf");

        assertEquals("relatorio-de-projetos.pdf", resultado);
    }

    @Test
    @DisplayName("Deve tratar nome de download com múltiplos pontos mantendo apenas o último como extensão")
    void deveTratarNomeDownloadComMultiplosPontos() {
        var resultado = StringUtils.tratarNomeDownload("Relatório.Final.Projetos.pdf");

        assertEquals("relatoriofinalprojetos.pdf", resultado);
    }

    @Test
    @DisplayName("Deve tratar nome de download com acentos e múltiplos espaços")
    void deveTratarNomeDownloadComAcentosEEspacos() {
        var resultado = StringUtils.tratarNomeDownload("Meu Arquivo Çom Acentuação.xlsx");

        assertEquals("meu-arquivo-com-acentuacao.xlsx", resultado);
    }

    @Test
    @DisplayName("Deve retornar string vazia quando nome de download for vazio")
    void deveRetornarStringVaziaQuandoNomeDownloadForVazio() {
        var resultado = StringUtils.tratarNomeDownload("");

        assertEquals("", resultado);
    }

    @Test
    @DisplayName("Deve retornar null quando nome de download for null")
    void deveRetornarNullQuandoNomeDownloadForNull() {
        var resultado = StringUtils.tratarNomeDownload(null);

        assertNull(resultado);
    }

    @Test
    @DisplayName("Deve tratar nome sem extensão")
    void deveTratarNomeSemExtensao() {
        var resultado = StringUtils.tratarNomeDownload("Relatório Geral");

        assertEquals("relatorio-geral", resultado);
    }

    @Test
    @DisplayName("Deve tratar nome com um único ponto")
    void deveTratarNomeComUnicoPonto() {
        var resultado = StringUtils.tratarNomeDownload("Arquivo Final.pdf");

        assertEquals("arquivo-final.pdf", resultado);
    }
}
