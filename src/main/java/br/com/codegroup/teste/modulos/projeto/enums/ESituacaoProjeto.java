package br.com.codegroup.teste.modulos.projeto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.thymeleaf.util.StringUtils;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static br.com.codegroup.teste.modulos.comum.utils.StringUtils.normalizar;

@Getter
@AllArgsConstructor
public enum ESituacaoProjeto {

    EM_ANALISE("EM ANÁLISE", null),
    ANALISE_REALIZADA("ANÁLISE REALIZADA", EM_ANALISE),
    ANALISE_APROVADA("ANÁLISE APROVADA", ANALISE_REALIZADA),
    INICIADO("INICIADO", ANALISE_APROVADA),
    PLANEJADO("PLANEJADO", INICIADO),
    EM_ANDAMENTO("EM ANDAMENTO", PLANEJADO),
    ENCERRADO("ENCERRADO", EM_ANDAMENTO),
    CANCELADO("CANCELADO", null),
    EXCLUIDO("EXCLUÍDO", null);

    private final String descricao;
    private final ESituacaoProjeto situacaoAnterior;

    @JsonCreator
    public static ESituacaoProjeto from(String value) {
        if (!StringUtils.isEmpty(value)) {
            var valueNormalizado = normalizar(value);

            return Stream.of(values()).filter(atribuicao -> normalizar(atribuicao.name()).equals(valueNormalizado)
                    || normalizar(atribuicao.getDescricao()).equals(valueNormalizado))
                .findFirst()
                .orElse(null);
        }

        return null;
    }
}
