package br.com.codegroup.teste.modulos.comum.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

    @Test
    @DisplayName("Deve retornar null quando total de dias for null")
    void deveRetornarNullQuandoTotalDiasForNull() {
        var resultado = DateUtils.getDuracaoFromDias(null);

        assertNull(resultado);
    }

    @Test
    @DisplayName("Deve retornar 0 dias quando total de dias for zero")
    void deveRetornarZeroDiasQuandoTotalDiasForZero() {
        var resultado = DateUtils.getDuracaoFromDias(0L);

        assertEquals("0 dias", resultado);
    }

    @Test
    @DisplayName("Deve lançar exception quando total de dias for negativo")
    void deveLancarExceptionQuandoTotalDiasForNegativo() {
        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> DateUtils.getDuracaoFromDias(-1L)
        );

        assertEquals("A quantidade de dias não pode ser negativa", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar 1 dia")
    void deveRetornarUmDia() {
        var resultado = DateUtils.getDuracaoFromDias(1L);

        assertEquals("1 dia", resultado);
    }

    @Test
    @DisplayName("Deve retornar apenas dias no plural")
    void deveRetornarApenasDiasNoPlural() {
        var resultado = DateUtils.getDuracaoFromDias(29L);

        assertEquals("29 dias", resultado);
    }

    @Test
    @DisplayName("Deve retornar 1 mês quando total for 30 dias")
    void deveRetornarUmMesQuandoTotalForTrintaDias() {
        var resultado = DateUtils.getDuracaoFromDias(30L);

        assertEquals("1 mês", resultado);
    }

    @Test
    @DisplayName("Deve retornar meses no plural quando não houver dias restantes")
    void deveRetornarMesesNoPluralQuandoNaoHouverDiasRestantes() {
        var resultado = DateUtils.getDuracaoFromDias(60L);

        assertEquals("2 meses", resultado);
    }

    @Test
    @DisplayName("Deve retornar mês e dia no singular")
    void deveRetornarMesEDiaNoSingular() {
        var resultado = DateUtils.getDuracaoFromDias(31L);

        assertEquals("1 mês e 1 dia", resultado);
    }

    @Test
    @DisplayName("Deve retornar mês no singular e dias no plural")
    void deveRetornarMesNoSingularEDiasNoPlural() {
        var resultado = DateUtils.getDuracaoFromDias(45L);

        assertEquals("1 mês e 15 dias", resultado);
    }

    @Test
    @DisplayName("Deve retornar meses no plural e dia no singular")
    void deveRetornarMesesNoPluralEDiaNoSingular() {
        var resultado = DateUtils.getDuracaoFromDias(61L);

        assertEquals("2 meses e 1 dia", resultado);
    }

    @Test
    @DisplayName("Deve retornar meses e dias no plural")
    void deveRetornarMesesEDiasNoPlural() {
        var resultado = DateUtils.getDuracaoFromDias(75L);

        assertEquals("2 meses e 15 dias", resultado);
    }
}
