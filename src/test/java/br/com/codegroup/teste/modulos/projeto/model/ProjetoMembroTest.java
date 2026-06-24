package br.com.codegroup.teste.modulos.projeto.model;

import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.projeto.ProjetoHelper;
import br.com.codegroup.teste.modulos.projeto.enums.ERiscoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("all")
public class ProjetoMembroTest {

    private static final String PROJETO_MEMBRO_ID = IdUtils.generateId();
    private static final String PROJETO_ID = IdUtils.generateId();
    private static final String MEMBRO_ID = IdUtils.generateId();

    @Test
    @DisplayName("Deve gerar id quando id estiver null")
    void deveGerarIdQuandoIdEstiverNull() {
        var projetoMembro = ProjetoHelper.projetoMembro(null);
        projetoMembro.generateId();

        assertNotNull(projetoMembro.getId());
        assertFalse(projetoMembro.getId().isBlank());
    }

    @Test
    @DisplayName("Deve manter id existente ao executar generateId")
    void deveManterIdExistenteAoExecutarGenerateId() {
        var projetoMembro = ProjetoHelper.projetoMembro(PROJETO_MEMBRO_ID);
        projetoMembro.generateId();

        assertEquals(PROJETO_MEMBRO_ID, projetoMembro.getId());
    }

    @Test
    @DisplayName("Deve criar projeto membro a partir de membro e projeto")
    void deveCriarProjetoMembroAPartirDeMembroEProjeto() {
        var membro = membro();
        var projeto = projeto();
        var antes = LocalDateTime.now();
        var projetoMembro = ProjetoMembro.of(membro, projeto);
        var depois = LocalDateTime.now();

        assertNotNull(projetoMembro);
        assertNull(projetoMembro.getId());
        assertSame(membro, projetoMembro.getMembro());
        assertSame(projeto, projetoMembro.getProjeto());
        assertEquals(ESituacaoProjetoMembro.PARTICIPANTE, projetoMembro.getSituacao());
        assertNull(projetoMembro.getDataExclusao());
        assertNull(projetoMembro.getIsResponsavel());
        assertNotNull(projetoMembro.getDataCadastro());
        assertTrue(!projetoMembro.getDataCadastro().isBefore(antes) && !projetoMembro.getDataCadastro().isAfter(depois));
    }

    @Test
    @DisplayName("Deve excluir projeto membro")
    void deveExcluirProjetoMembro() {
        var projetoMembro = ProjetoHelper.projetoMembro(PROJETO_MEMBRO_ID, LocalDateTime.now(), null, projeto(),
            membro(), false, ESituacaoProjetoMembro.PARTICIPANTE);
        var antes = LocalDateTime.now();
        projetoMembro.excluir();
        var depois = LocalDateTime.now();

        assertEquals(ESituacaoProjetoMembro.EXCLUIDO, projetoMembro.getSituacao());
        assertNotNull(projetoMembro.getDataExclusao());

        assertTrue(!projetoMembro.getDataExclusao().isBefore(antes) && !projetoMembro.getDataExclusao().isAfter(depois));
    }

    private Projeto projeto() {
        return ProjetoHelper.projeto(
            PROJETO_ID,
            "PROJETO TESTE",
            LocalDateTime.now(),
            LocalDate.now(),
            LocalDate.now().plusMonths(1),
            new BigDecimal("10000.00"),
            "DESCRIÇÃO DO PROJETO",
            ESituacaoProjeto.EM_ANALISE,
            ERiscoProjeto.BAIXO_RISCO);
    }

    private Membro membro() {
        return Membro.builder()
            .id(MEMBRO_ID)
            .nome("GABRIEL GARCIA")
            .atribuicao(EAtribuicao.FUNCIONARIO)
            .dataCadastro(LocalDateTime.now())
            .situacao(ESituacaoMembro.ATIVO)
            .build();
    }
}
