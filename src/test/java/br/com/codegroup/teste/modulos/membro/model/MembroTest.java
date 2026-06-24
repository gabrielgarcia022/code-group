package br.com.codegroup.teste.modulos.membro.model;

import br.com.codegroup.teste.modulos.membro.MembroHelper;
import br.com.codegroup.teste.modulos.membro.dto.MembroRequest;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("all")
public class MembroTest {

    private static final String MEMBRO_ID = "01kv-w0e9-va8yca92m6m30q7whe";

    @Test
    @DisplayName("Deve gerar id quando id estiver null")
    void deveGerarIdQuandoIdEstiverNull() {
        var membro = Membro.builder().id(null).build();
        membro.generateId();

        assertNotNull(membro.getId());
        assertFalse(membro.getId().isBlank());
    }

    @Test
    @DisplayName("Deve manter id existente ao executar generateId")
    void deveManterIdExistenteAoExecutarGenerateId() {
        var membro = Membro.builder().id(MEMBRO_ID).build();
        membro.generateId();

        assertEquals(MEMBRO_ID, membro.getId());
    }

    @Test
    @DisplayName("Deve criar membro usando construtor com id")
    void deveCriarMembroUsandoConstrutorComId() {
        var membro = new Membro(MEMBRO_ID);

        assertEquals(MEMBRO_ID, membro.getId());
        assertNull(membro.getNome());
        assertNull(membro.getAtribuicao());
        assertNull(membro.getDataCadastro());
        assertNull(membro.getDataExclusao());
        assertNull(membro.getSituacao());
    }

    @Test
    @DisplayName("Deve criar membro a partir do request")
    void deveCriarMembroAPartirDoRequest() {
        var request = mock(MembroRequest.class);
        when(request.getNome()).thenReturn("GABRIEL GARCIA");
        when(request.getAtribuicao()).thenReturn(EAtribuicao.GERENTE);
        var antes = LocalDateTime.now();
        var membro = Membro.of(request);
        var depois = LocalDateTime.now();

        assertNotNull(membro);
        assertNull(membro.getId());
        assertEquals("GABRIEL GARCIA", membro.getNome());
        assertEquals(EAtribuicao.GERENTE, membro.getAtribuicao());
        assertEquals(ESituacaoMembro.ATIVO, membro.getSituacao());
        assertNull(membro.getDataExclusao());
        assertNotNull(membro.getDataCadastro());
        assertTrue(!membro.getDataCadastro().isBefore(antes) && !membro.getDataCadastro().isAfter(depois));
    }

    @Test
    @DisplayName("Deve atualizar membro usando merge")
    void deveAtualizarMembroUsandoMerge() {
        var request = mock(MembroRequest.class);
        when(request.getNome()).thenReturn("NOVO NOME");
        when(request.getAtribuicao()).thenReturn(EAtribuicao.FUNCIONARIO);
        var dataCadastro = LocalDateTime.of(2026, 6, 18, 10, 30);
        var membro = MembroHelper.membro(MEMBRO_ID, "GABRIEL GARCIA", EAtribuicao.GERENTE, dataCadastro,
            null, ESituacaoMembro.ATIVO);
        membro.merge(request);

        assertEquals(MEMBRO_ID, membro.getId());
        assertEquals("NOVO NOME", membro.getNome());
        assertEquals(EAtribuicao.FUNCIONARIO, membro.getAtribuicao());
        assertEquals(dataCadastro, membro.getDataCadastro());
        assertEquals(ESituacaoMembro.ATIVO, membro.getSituacao());
    }

    @Test
    @DisplayName("Deve excluir membro")
    void deveExcluirMembro() {
        var data = LocalDateTime.now();
        var membro = MembroHelper.membro(MEMBRO_ID, "GABRIEL GARCIA", EAtribuicao.GERENTE, data, null, ESituacaoMembro.EXCLUIDO);
        membro.excluir();
        var depois = LocalDateTime.now();

        assertEquals(ESituacaoMembro.EXCLUIDO, membro.getSituacao());
        assertNotNull(membro.getDataExclusao());
        assertTrue(!membro.getDataExclusao().isBefore(data) && !membro.getDataExclusao().isAfter(depois));
    }

    @Test
    @DisplayName("Deve reativar membro")
    void deveReativarMembro() {
        var data = LocalDateTime.now();
        var membro = MembroHelper.membro(MEMBRO_ID, "GABRIEL GARCIA", EAtribuicao.GERENTE, data, data, ESituacaoMembro.EXCLUIDO);
        membro.reativar();

        assertEquals(ESituacaoMembro.ATIVO, membro.getSituacao());
        assertNull(membro.getDataExclusao());
    }
}
