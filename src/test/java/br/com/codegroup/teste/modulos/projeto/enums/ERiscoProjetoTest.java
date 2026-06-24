package br.com.codegroup.teste.modulos.projeto.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class ERiscoProjetoTest {

    @Test
    @DisplayName("Deve retornar descrição dos riscos")
    void deveRetornarDescricaoDosRiscos() {
        assertThat(ERiscoProjeto.BAIXO_RISCO.getDescricao()).isEqualTo("BAIXO RISCO");
        assertThat(ERiscoProjeto.MEDIO_RISCO.getDescricao()).isEqualTo("MÉDIO RISCO");
        assertThat(ERiscoProjeto.ALTO_RISCO.getDescricao()).isEqualTo("ALTO RISCO");
        assertThat(ERiscoProjeto.FORA_DE_PADRAO.getDescricao()).isEqualTo("FORA DE PADRÃO");
    }

    @Test
    @DisplayName("Deve retornar ALTO_RISCO quando orçamento for maior que 500000")
    void deveRetornarAltoRiscoQuandoOrcamentoForMaiorQueQuinhentosMil() {
        var dataInicio = LocalDate.of(2026, 1, 1);
        var previsaoTermino = LocalDate.of(2026, 2, 1);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("500000.01"), dataInicio, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.ALTO_RISCO);
    }

    @Test
    @DisplayName("Deve retornar ALTO_RISCO quando prazo for maior que 180 dias")
    void deveRetornarAltoRiscoQuandoPrazoForMaiorQueCentoEOitentaDias() {
        var dataInicio = LocalDate.of(2026, 1, 1);
        var previsaoTermino = LocalDate.of(2026, 7, 1);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("10000.00"), dataInicio, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.ALTO_RISCO);
    }

    @Test
    @DisplayName("Deve retornar MEDIO_RISCO quando orçamento for maior que 100000 e menor ou igual a 500000")
    void deveRetornarMedioRiscoQuandoOrcamentoForMaiorQueCemMilEMenorOuIgualAQuinhentosMil() {
        var dataInicio = LocalDate.of(2026, 1, 1);
        var previsaoTermino = LocalDate.of(2026, 2, 1);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("250000.00"), dataInicio, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.MEDIO_RISCO);
    }

    @Test
    @DisplayName("Deve retornar MEDIO_RISCO quando orçamento for exatamente 500000")
    void deveRetornarMedioRiscoQuandoOrcamentoForExatamenteQuinhentosMil() {
        var dataInicio = LocalDate.of(2026, 1, 1);
        var previsaoTermino = LocalDate.of(2026, 2, 1);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("500000.00"), dataInicio, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.MEDIO_RISCO);
    }

    @Test
    @DisplayName("Deve retornar MEDIO_RISCO quando prazo for maior que 90 dias")
    void deveRetornarMedioRiscoQuandoPrazoForMaiorQueNoventaDias() {
        var dataInicio = LocalDate.of(2026, 1, 1);
        var previsaoTermino = LocalDate.of(2026, 4, 2);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("50000.00"), dataInicio, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.MEDIO_RISCO);
    }

    @Test
    @DisplayName("Deve retornar BAIXO_RISCO quando orçamento for menor ou igual a 100000")
    void deveRetornarBaixoRiscoQuandoOrcamentoForMenorOuIgualACemMil() {
        var dataInicio = LocalDate.of(2026, 1, 1);
        var previsaoTermino = LocalDate.of(2026, 2, 1);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("100000.00"), dataInicio, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.BAIXO_RISCO);
    }

    @Test
    @DisplayName("Deve retornar BAIXO_RISCO quando orçamento for abaixo de 100000 e prazo menor ou igual a 90 dias")
    void deveRetornarBaixoRiscoQuandoOrcamentoForAbaixoDeCemMilEPrazoMenorOuIgualANoventaDias() {
        var dataInicio = LocalDate.of(2026, 1, 1);
        var previsaoTermino = LocalDate.of(2026, 4, 1);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("99999.99"), dataInicio, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.BAIXO_RISCO);
    }

    @Test
    @DisplayName("Deve retornar FORA_DE_PADRAO quando orçamento for null")
    void deveRetornarForaDePadraoQuandoOrcamentoForNull() {
        var dataInicio = LocalDate.of(2026, 1, 1);
        var previsaoTermino = LocalDate.of(2026, 2, 1);
        var resultado = ERiscoProjeto.analisarRisco(null, dataInicio, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.FORA_DE_PADRAO);
    }

    @Test
    @DisplayName("Deve retornar FORA_DE_PADRAO quando previsão de término for null")
    void deveRetornarForaDePadraoQuandoPrevisaoTerminoForNull() {
        var dataInicio = LocalDate.of(2026, 1, 1);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("10000.00"), dataInicio, null);

        assertThat(resultado).isEqualTo(ERiscoProjeto.FORA_DE_PADRAO);
    }

    @Test
    @DisplayName("Deve usar data atual quando data de início for null")
    void deveUsarDataAtualQuandoDataInicioForNull() {
        var previsaoTermino = LocalDate.now().plusDays(30);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("10000.00"), null, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.BAIXO_RISCO);
    }

    @Test
    @DisplayName("Deve retornar ALTO_RISCO quando data de início for null e previsão for maior que 180 dias")
    void deveRetornarAltoRiscoQuandoDataInicioForNullEPrevisaoForMaiorQueCentoEOitentaDias() {
        var previsaoTermino = LocalDate.now().plusDays(181);
        var resultado = ERiscoProjeto.analisarRisco(new BigDecimal("10000.00"), null, previsaoTermino);

        assertThat(resultado).isEqualTo(ERiscoProjeto.ALTO_RISCO);
    }
}
