package br.com.codegroup.teste.modulos.projeto.model;

import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import br.com.codegroup.teste.modulos.projeto.ProjetoHelper;
import br.com.codegroup.teste.modulos.projeto.dto.ProjetoRequest;
import br.com.codegroup.teste.modulos.projeto.enums.ERiscoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("all")
public class ProjetoTest {

    private static final String PROJETO_ID = IdUtils.generateId();
    private static final String NOME = "PROJETO TESTE";
    private static final String NOVO_NOME = "PROJETO ATUALIZADO";
    private static final String DESCRICAO = "DESCRIÇÃO DO PROJETO";
    private static final String NOVA_DESCRICAO = "DESCRIÇÃO ATUALIZADA";
    private static final BigDecimal ORCAMENTO_TOTAL = new BigDecimal("10000.00");
    private static final BigDecimal NOVO_ORCAMENTO_TOTAL = new BigDecimal("25000.00");
    private static final LocalDate PREVISAO_TERMINO = LocalDate.now().plusMonths(2);
    private static final LocalDate NOVA_PREVISAO_TERMINO = LocalDate.now().plusMonths(4);

    @Test
    @DisplayName("Deve gerar id quando id estiver null")
    void deveGerarIdQuandoIdEstiverNull() {
        var projeto = ProjetoHelper.projeto(null);
        projeto.generateId();

        assertNotNull(projeto.getId());
        assertFalse(projeto.getId().isBlank());
    }

    @Test
    @DisplayName("Deve manter id existente ao executar generateId")
    void deveManterIdExistenteAoExecutarGenerateId() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID);
        projeto.generateId();

        assertEquals(PROJETO_ID, projeto.getId());
    }

    @Test
    @DisplayName("Deve criar projeto usando construtor com id")
    void deveCriarProjetoUsandoConstrutorComId() {
        var projeto = new Projeto(PROJETO_ID);

        assertEquals(PROJETO_ID, projeto.getId());
        assertNull(projeto.getNome());
        assertNull(projeto.getDataCadastro());
        assertNull(projeto.getDataInicio());
        assertNull(projeto.getPrevisaoTermino());
        assertNull(projeto.getDataRealTermino());
        assertNull(projeto.getDataExclusao());
        assertNull(projeto.getDataCancelamento());
        assertNull(projeto.getOrcamentoTotal());
        assertNull(projeto.getDescricao());
        assertNull(projeto.getMembros());
        assertNull(projeto.getSituacao());
        assertNull(projeto.getRisco());
    }

    @Test
    @DisplayName("Deve criar projeto a partir do request")
    void deveCriarProjetoAPartirDoRequest() {
        var request = mock(ProjetoRequest.class);
        when(request.getNome()).thenReturn(NOME);
        when(request.getPrevisaoTermino()).thenReturn(PREVISAO_TERMINO);
        when(request.getOrcamentoTotal()).thenReturn(ORCAMENTO_TOTAL);
        when(request.getDescricao()).thenReturn(DESCRICAO);
        var antes = LocalDateTime.now();
        var projeto = Projeto.of(request);
        var depois = LocalDateTime.now();

        assertNotNull(projeto);
        assertNull(projeto.getId());
        assertEquals(NOME, projeto.getNome());
        assertEquals(PREVISAO_TERMINO, projeto.getPrevisaoTermino());
        assertEquals(ORCAMENTO_TOTAL, projeto.getOrcamentoTotal());
        assertEquals(DESCRICAO, projeto.getDescricao());
        assertEquals(ESituacaoProjeto.EM_ANALISE, projeto.getSituacao());
        assertNotNull(projeto.getRisco());
        assertNull(projeto.getDataInicio());
        assertNull(projeto.getDataRealTermino());
        assertNull(projeto.getDataExclusao());
        assertNull(projeto.getDataCancelamento());
        assertNotNull(projeto.getDataCadastro());

        assertTrue(!projeto.getDataCadastro().isBefore(antes) && !projeto.getDataCadastro().isAfter(depois));
    }

    @Test
    @DisplayName("Deve atualizar projeto usando merge")
    void deveAtualizarProjetoUsandoMerge() {
        var request = mock(ProjetoRequest.class);

        when(request.getNome()).thenReturn(NOVO_NOME);
        when(request.getPrevisaoTermino()).thenReturn(NOVA_PREVISAO_TERMINO);
        when(request.getOrcamentoTotal()).thenReturn(NOVO_ORCAMENTO_TOTAL);
        when(request.getDescricao()).thenReturn(NOVA_DESCRICAO);

        var dataCadastro = LocalDateTime.of(2026, 6, 18, 10, 30);
        var dataInicio = LocalDate.of(2026, 6, 20);
        var projeto = ProjetoHelper.projeto(PROJETO_ID, ESituacaoProjeto.INICIADO, ORCAMENTO_TOTAL, PREVISAO_TERMINO,
            DESCRICAO, ERiscoProjeto.BAIXO_RISCO, dataInicio, dataCadastro);
        projeto.merge(request);

        assertEquals(PROJETO_ID, projeto.getId());
        assertEquals(NOVO_NOME, projeto.getNome());
        assertEquals(NOVA_PREVISAO_TERMINO, projeto.getPrevisaoTermino());
        assertEquals(NOVO_ORCAMENTO_TOTAL, projeto.getOrcamentoTotal());
        assertEquals(NOVA_DESCRICAO, projeto.getDescricao());
        assertEquals(dataCadastro, projeto.getDataCadastro());
        assertEquals(dataInicio, projeto.getDataInicio());
        assertEquals(ESituacaoProjeto.INICIADO, projeto.getSituacao());
        assertNotNull(projeto.getRisco());
    }

    @Test
    @DisplayName("Deve iniciar projeto")
    void deveIniciarProjeto() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID, ESituacaoProjeto.ANALISE_APROVADA, ORCAMENTO_TOTAL, PREVISAO_TERMINO);
        var hoje = LocalDate.now();
        projeto.iniciar();

        assertEquals(hoje, projeto.getDataInicio());
        assertEquals(ESituacaoProjeto.INICIADO, projeto.getSituacao());
        assertNotNull(projeto.getRisco());
    }

    @Test
    @DisplayName("Deve encerrar projeto")
    void deveEncerrarProjeto() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID, ESituacaoProjeto.EM_ANDAMENTO);
        var hoje = LocalDate.now();
        projeto.encerrar();

        assertEquals(hoje, projeto.getDataRealTermino());
        assertEquals(ESituacaoProjeto.ENCERRADO, projeto.getSituacao());
    }

    @Test
    @DisplayName("Deve cancelar projeto")
    void deveCancelarProjeto() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID);
        var antes = LocalDateTime.now();
        projeto.cancelar();
        var depois = LocalDateTime.now();

        assertEquals(ESituacaoProjeto.CANCELADO, projeto.getSituacao());
        assertNotNull(projeto.getDataCancelamento());
        assertTrue(!projeto.getDataCancelamento().isBefore(antes) && !projeto.getDataCancelamento().isAfter(depois));
    }

    @Test
    @DisplayName("Deve excluir projeto")
    void deveExcluirProjeto() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID);
        var antes = LocalDateTime.now();
        projeto.excluir();
        var depois = LocalDateTime.now();

        assertEquals(ESituacaoProjeto.EXCLUIDO, projeto.getSituacao());
        assertNotNull(projeto.getDataExclusao());
        assertTrue(!projeto.getDataExclusao().isBefore(antes) && !projeto.getDataExclusao().isAfter(depois));
    }

    @Test
    @DisplayName("Deve atualizar situação para iniciado")
    void deveAtualizarSituacaoParaIniciado() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID, ESituacaoProjeto.ANALISE_APROVADA, ORCAMENTO_TOTAL, PREVISAO_TERMINO);
        var hoje = LocalDate.now();
        projeto.atualizarSituacao(ESituacaoProjeto.INICIADO);

        assertEquals(ESituacaoProjeto.INICIADO, projeto.getSituacao());
        assertEquals(hoje, projeto.getDataInicio());
        assertNotNull(projeto.getRisco());
    }

    @Test
    @DisplayName("Deve atualizar situação para encerrado")
    void deveAtualizarSituacaoParaEncerrado() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID, ESituacaoProjeto.EM_ANDAMENTO);
        var hoje = LocalDate.now();
        projeto.atualizarSituacao(ESituacaoProjeto.ENCERRADO);

        assertEquals(ESituacaoProjeto.ENCERRADO, projeto.getSituacao());
        assertEquals(hoje, projeto.getDataRealTermino());
    }

    @Test
    @DisplayName("Deve atualizar situação para cancelado")
    void deveAtualizarSituacaoParaCancelado() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID);
        var antes = LocalDateTime.now();
        projeto.atualizarSituacao(ESituacaoProjeto.CANCELADO);
        var depois = LocalDateTime.now();

        assertEquals(ESituacaoProjeto.CANCELADO, projeto.getSituacao());
        assertNotNull(projeto.getDataCancelamento());
        assertTrue(!projeto.getDataCancelamento().isBefore(antes) && !projeto.getDataCancelamento().isAfter(depois));
    }

    @Test
    @DisplayName("Deve atualizar situação para excluído")
    void deveAtualizarSituacaoParaExcluido() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID);
        var antes = LocalDateTime.now();
        projeto.atualizarSituacao(ESituacaoProjeto.EXCLUIDO);
        var depois = LocalDateTime.now();

        assertEquals(ESituacaoProjeto.EXCLUIDO, projeto.getSituacao());
        assertNotNull(projeto.getDataExclusao());
        assertTrue(!projeto.getDataExclusao().isBefore(antes) && !projeto.getDataExclusao().isAfter(depois));
    }

    @Test
    @DisplayName("Deve apenas atualizar situação quando não houver ação específica")
    void deveApenasAtualizarSituacaoQuandoNaoHouverAcaoEspecifica() {
        var projeto = ProjetoHelper.projeto(PROJETO_ID);
        projeto.atualizarSituacao(ESituacaoProjeto.ANALISE_REALIZADA);

        assertEquals(ESituacaoProjeto.ANALISE_REALIZADA, projeto.getSituacao());
        assertNull(projeto.getDataInicio());
        assertNull(projeto.getDataRealTermino());
        assertNull(projeto.getDataCancelamento());
        assertNull(projeto.getDataExclusao());
    }
}
