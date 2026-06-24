package br.com.codegroup.teste.modulos.projeto;

import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.projeto.dto.*;
import br.com.codegroup.teste.modulos.projeto.enums.ERiscoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import br.com.codegroup.teste.modulos.projeto.model.ProjetoMembro;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings({"checkstyle:ParameterNumber"})
public class ProjetoHelper {

    public static ProjetoResponse projetoResponse(String id, String nome, ESituacaoProjeto situacao) {
        return ProjetoResponse.builder()
            .id(id)
            .nome(nome)
            .descricao("DESCRIÇÃO DO PROJETO")
            .dataCadastro(LocalDateTime.of(2026, 6, 18, 10, 30))
            .dataInicio(LocalDate.of(2026, 6, 20))
            .previsaoTermino(LocalDate.of(2026, 12, 31))
            .dataRealTermino(null)
            .dataExclusao(ESituacaoProjeto.EXCLUIDO.equals(situacao)
                ? LocalDateTime.of(2026, 7, 1, 10, 30)
                : null)
            .dataCancelamento(ESituacaoProjeto.CANCELADO.equals(situacao)
                ? LocalDateTime.of(2026, 7, 1, 10, 30)
                : null)
            .orcamentoTotal(new BigDecimal("10000.00"))
            .situacao(situacao)
            .risco("BAIXO RISCO")
            .build();
    }

    public static ProjetoDetalharResponse projetoDetalharResponse(String id, String membroId) {
        return ProjetoDetalharResponse.builder()
            .id(id)
            .nome("PROJETO TESTE")
            .descricao("DESCRIÇÃO DO PROJETO")
            .dataCadastro(LocalDateTime.of(2026, 6, 18, 10, 30))
            .dataInicio(LocalDate.of(2026, 6, 20))
            .previsaoTermino(LocalDate.of(2026, 12, 31))
            .dataRealTermino(null)
            .dataExclusao(null)
            .dataCancelamento(null)
            .orcamentoTotal(new BigDecimal("10000.00"))
            .situacao(ESituacaoProjeto.EM_ANALISE)
            .risco("BAIXO RISCO")
            .membros(List.of(projetoMembroResponse(membroId, ESituacaoProjetoMembro.PARTICIPANTE)))
            .membrosAnteriores(List.of(projetoMembroResponse(membroId, ESituacaoProjetoMembro.EXCLUIDO)))
            .build();
    }

    public static ProjetoMembroResponse projetoMembroResponse(String id, ESituacaoProjetoMembro situacao) {
        return ProjetoMembroResponse.builder()
            .membroId(id)
            .membroNome("GABRIEL GARCIA")
            .dataCadastro(LocalDateTime.of(2026, 6, 18, 10, 30))
            .dataExclusao(ESituacaoProjetoMembro.EXCLUIDO.equals(situacao)
                ? LocalDateTime.of(2026, 6, 19, 10, 30)
                : null)
            .isResponsavel(false)
            .situacao(situacao)
            .build();
    }

    public static ProjetoMembroRequest projetoMembroRequest(String projetoId, String membroId) {
        var request = new ProjetoMembroRequest();
        request.setProjetoId(projetoId);
        request.setMembroId(membroId);

        return request;
    }

    public static AtualizarSituacaoRequest atualizarSituacaoRequest(String projetoId, ESituacaoProjeto situacao) {
        var request = new AtualizarSituacaoRequest();
        request.setProjetoId(projetoId);
        request.setSituacao(situacao);

        return request;
    }

    public static ProjetoRequest projetoRequest(String id) {
        var request = new ProjetoRequest();
        request.setId(id);
        request.setNome("PROJETO TESTE");
        request.setPrevisaoTermino(LocalDate.of(2026, 12, 31));
        request.setOrcamentoTotal(new BigDecimal("10000.00"));
        request.setDescricao("DESCRIÇÃO DO PROJETO");
        request.setGerenteId(IdUtils.generateId());

        return request;
    }

    public static Projeto projeto(String projetoId) {
        return Projeto.builder()
            .id(projetoId)
            .situacao(ESituacaoProjeto.EM_ANALISE)
            .build();
    }

    public static Projeto projeto(String projetoId, ESituacaoProjeto situacao) {
        return Projeto.builder()
            .id(projetoId)
            .situacao(situacao)
            .build();
    }

    public static Projeto projeto(String projetoId, String nome) {
        return Projeto.builder()
            .id(projetoId)
            .nome(nome)
            .build();
    }

    public static Projeto projeto(String projetoId, String nome, ESituacaoProjeto situacao) {
        return Projeto.builder()
            .id(projetoId)
            .nome(nome)
            .situacao(situacao)
            .build();
    }

    public static Projeto projeto(String projetoId, ESituacaoProjeto situacao, BigDecimal orcamentoTotal,
                                  LocalDate previsaoTermino) {
        return Projeto.builder()
            .id(projetoId)
            .orcamentoTotal(orcamentoTotal)
            .previsaoTermino(previsaoTermino)
            .situacao(situacao)
            .build();
    }

    public static Projeto projeto(String projetoId, ESituacaoProjeto situacao, BigDecimal orcamentoTotal,
                                  LocalDate previsaoTermino, String descricao, ERiscoProjeto riscoProjeto,
                                  LocalDate dataInicio, LocalDateTime dataCadastro) {
        return Projeto.builder()
            .id(projetoId)
            .orcamentoTotal(orcamentoTotal)
            .previsaoTermino(previsaoTermino)
            .situacao(situacao)
            .descricao(descricao)
            .risco(riscoProjeto)
            .dataInicio(dataInicio)
            .dataCadastro(dataCadastro)
            .build();
    }

    public static Projeto projeto(String projetoId, String nome, LocalDateTime dataCadastro, LocalDate dataInicio,
                                  LocalDate previsaoTermino, BigDecimal orcamentoTotal, String descricao,
                                  ESituacaoProjeto situacao, ERiscoProjeto riscoProjeto) {
        return Projeto.builder()
            .id(projetoId)
            .nome(nome)
            .orcamentoTotal(orcamentoTotal)
            .previsaoTermino(previsaoTermino)
            .situacao(situacao)
            .descricao(descricao)
            .risco(riscoProjeto)
            .dataInicio(dataInicio)
            .dataCadastro(dataCadastro)
            .build();
    }

    public static ProjetoMembro projetoMembro(String id) {
        return ProjetoMembro.builder()
            .id(id)
            .build();
    }

    public static ProjetoMembro projetoMembro(String id, Projeto projeto, Membro membro) {
        return ProjetoMembro.builder()
            .id(id)
            .projeto(projeto)
            .membro(membro)
            .build();
    }

    public static ProjetoMembro projetoMembro(String id, Projeto projeto, Membro membro, Boolean isResponsavel,
                                              ESituacaoProjetoMembro situacao) {
        return ProjetoMembro.builder()
            .id(id)
            .projeto(projeto)
            .membro(membro)
            .isResponsavel(isResponsavel)
            .situacao(situacao)
            .build();
    }

    public static ProjetoMembro projetoMembro(String id, LocalDateTime dataCadastro, LocalDateTime dataExclusao,
                                              Projeto projeto, Membro membro, Boolean isResponsavel,
                                              ESituacaoProjetoMembro situacao) {
        return ProjetoMembro.builder()
            .id(id)
            .dataCadastro(dataCadastro)
            .dataExclusao(dataExclusao)
            .projeto(projeto)
            .membro(membro)
            .isResponsavel(isResponsavel)
            .situacao(situacao)
            .build();
    }
}
