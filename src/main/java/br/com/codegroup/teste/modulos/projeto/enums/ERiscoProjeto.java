package br.com.codegroup.teste.modulos.projeto.enums;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ERiscoProjeto {

    BAIXO_RISCO("BAIXO RISCO"),
    MEDIO_RISCO("MÉDIO RISCO"),
    ALTO_RISCO("ALTO RISCO"),
    FORA_DE_PADRAO("FORA DE PADRÃO");

    private final String descricao;

    @SuppressWarnings({"checkstyle:MagicNumber"})
    public static ERiscoProjeto analisarRisco(BigDecimal orcamento, LocalDate dataInicio, LocalDate previsaoTermino) {
        dataInicio = Optional.ofNullable(dataInicio).orElse(LocalDate.now());

        if (Objects.nonNull(orcamento) && Objects.nonNull(previsaoTermino)) {
            var diasPrazo = ChronoUnit.DAYS.between(dataInicio, previsaoTermino);

            if (orcamento.compareTo(BigDecimal.valueOf(500000)) > 0 || diasPrazo > 180) {
                return ALTO_RISCO;
            } else if (orcamento.compareTo(BigDecimal.valueOf(100000)) > 0
                && orcamento.compareTo(BigDecimal.valueOf(500000)) <= 0 || diasPrazo > 90) {
                return MEDIO_RISCO;
            } else if (orcamento.compareTo(BigDecimal.valueOf(100000)) <= 0) {
                return BAIXO_RISCO;
            }
        }

        return FORA_DE_PADRAO;
    }
}
