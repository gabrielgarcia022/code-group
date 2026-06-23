package br.com.codegroup.teste.modulos.projeto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
