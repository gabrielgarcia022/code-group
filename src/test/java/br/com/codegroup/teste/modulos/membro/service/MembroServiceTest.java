package br.com.codegroup.teste.modulos.membro.service;

import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.comum.exception.NotFoundException;
import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import br.com.codegroup.teste.modulos.membro.MembroHelper;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import br.com.codegroup.teste.modulos.membro.filtros.MembroFiltros;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.membro.repository.MembroRepository;
import br.com.codegroup.teste.modulos.projeto.service.ProjetoService;
import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembroServiceTest {

    private static final String MEMBRO_ID = IdUtils.generateId();
    private static final String NOME = "GABRIEL GARCIA";
    private static final String NOVO_NOME = "GABRIEL ATUALIZADO";

    @Mock
    private ProjetoService projetoService;
    @Mock
    private MembroRepository repository;
    private MembroService service;

    @BeforeEach
    void setUp() {
        service = new MembroService(projetoService, repository);
    }

    @Test
    @DisplayName("Deve buscar membros paginados")
    void deveBuscarMembrosPaginados() {
        var filtros = mock(MembroFiltros.class, RETURNS_DEEP_STUBS);
        var predicate = mock(BooleanBuilder.class);
        var pageRequest = new PageRequest();
        var membro = membroAtivo();
        var page = new PageImpl<>(List.of(membro), pageRequest, 1);
        when(filtros.toPredicate().build()).thenReturn(predicate);
        when(repository.findAll(eq(predicate), eq(pageRequest))).thenReturn(page);
        var resultado = service.getAll(filtros, pageRequest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getId()).isEqualTo(MEMBRO_ID);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo(NOME);
        assertThat(resultado.getContent().get(0).getAtribuicao()).isEqualTo(EAtribuicao.GERENTE);
        assertThat(resultado.getContent().get(0).getSituacao()).isEqualTo(ESituacaoMembro.ATIVO);

        verify(repository).findAll(eq(predicate), eq(pageRequest));
    }

    @Test
    @DisplayName("Deve detalhar membro")
    void deveDetalharMembro() {
        var membro = membroAtivo();
        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.of(membro));
        var resultado = service.detalhar(MEMBRO_ID);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(MEMBRO_ID);
        assertThat(resultado.getNome()).isEqualTo(NOME);
        assertThat(resultado.getAtribuicao()).isEqualTo(EAtribuicao.GERENTE);
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoMembro.ATIVO);

        verify(repository).findById(MEMBRO_ID);
    }

    @Test
    @DisplayName("Deve salvar membro")
    void deveSalvarMembro() {
        var request = MembroHelper.membroRequest(null, NOME, EAtribuicao.GERENTE);

        when(repository.save(any(Membro.class))).thenAnswer(invocation -> {
            var membro = invocation.<Membro>getArgument(0);
            membro.setId(MEMBRO_ID);
            return membro;
        });

        var resultado = service.salvar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(MEMBRO_ID);
        assertThat(resultado.getNome()).isEqualTo(NOME);
        assertThat(resultado.getAtribuicao()).isEqualTo(EAtribuicao.GERENTE);
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoMembro.ATIVO);

        verify(repository).save(any(Membro.class));
    }

    @Test
    @DisplayName("Deve editar membro ativo")
    void deveEditarMembroAtivo() {
        var membro = membroAtivo();
        var request = MembroHelper.membroRequest(MEMBRO_ID, NOVO_NOME, EAtribuicao.FUNCIONARIO);
        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.of(membro));
        when(repository.save(any(Membro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var resultado = service.editar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(MEMBRO_ID);
        assertThat(resultado.getNome()).isEqualTo(NOVO_NOME);
        assertThat(resultado.getAtribuicao()).isEqualTo(EAtribuicao.FUNCIONARIO);
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoMembro.ATIVO);

        verify(repository).findById(MEMBRO_ID);
        verify(repository).save(membro);
    }

    @Test
    @DisplayName("Deve lançar erro ao editar membro excluído")
    void deveLancarErroAoEditarMembroExcluido() {
        var membro = membroExcluido();
        var request = MembroHelper.membroRequest(MEMBRO_ID, NOVO_NOME, EAtribuicao.FUNCIONARIO);
        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.of(membro));

        assertThatThrownBy(() -> service.editar(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Você não pode editar um membro excluido");

        verify(repository).findById(MEMBRO_ID);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve validar edição de membro ativo")
    void deveValidarEdicaoDeMembroAtivo() {
        var membro = membroAtivo();
        service.validarEdicao(membro);

        assertThat(membro.getSituacao()).isEqualTo(ESituacaoMembro.ATIVO);
    }

    @Test
    @DisplayName("Deve lançar erro na validação de edição de membro excluído")
    void deveLancarErroNaValidacaoDeEdicaoDeMembroExcluido() {
        var membro = membroExcluido();

        assertThatThrownBy(() -> service.validarEdicao(membro))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Você não pode editar um membro excluido");
    }

    @Test
    @DisplayName("Deve excluir membro ativo")
    void deveExcluirMembroAtivo() {
        var membro = membroAtivo();
        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.of(membro));
        when(repository.save(any(Membro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = service.excluir(MEMBRO_ID);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(MEMBRO_ID);
        assertThat(resultado.getNome()).isEqualTo(NOME);
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoMembro.EXCLUIDO);
        assertThat(membro.getDataExclusao()).isNotNull();

        verify(repository).findById(MEMBRO_ID);
        verify(projetoService).excluirMembro(MEMBRO_ID);
        verify(repository).save(membro);
    }

    @Test
    @DisplayName("Deve lançar erro ao excluir membro já excluído")
    void deveLancarErroAoExcluirMembroJaExcluido() {
        var membro = membroExcluido();
        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.of(membro));

        assertThatThrownBy(() -> service.excluir(MEMBRO_ID))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Este membro já está excluído");

        verify(repository).findById(MEMBRO_ID);
        verify(projetoService, never()).excluirMembro(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve reativar membro excluído")
    void deveReativarMembroExcluido() {
        var membro = membroExcluido();
        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.of(membro));
        when(repository.save(any(Membro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var resultado = service.reativar(MEMBRO_ID);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(MEMBRO_ID);
        assertThat(resultado.getNome()).isEqualTo(NOME);
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoMembro.ATIVO);
        assertThat(membro.getDataExclusao()).isNull();

        verify(repository).findById(MEMBRO_ID);
        verify(repository).save(membro);
    }

    @Test
    @DisplayName("Deve lançar erro ao reativar membro já ativo")
    void deveLancarErroAoReativarMembroJaAtivo() {
        var membro = membroAtivo();
        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.of(membro));

        assertThatThrownBy(() -> service.reativar(MEMBRO_ID))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Este membro já está ativo");

        verify(repository).findById(MEMBRO_ID);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar membro por id")
    void deveBuscarMembroPorId() {
        var membro = membroAtivo();
        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.of(membro));
        var resultado = service.getById(MEMBRO_ID);
        assertThat(resultado).isSameAs(membro);

        verify(repository).findById(MEMBRO_ID);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando membro não existir")
    void deveLancarNotFoundExceptionQuandoMembroNaoExistir() {
        when(repository.findById(MEMBRO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(MEMBRO_ID))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Membro não encontrado");

        verify(repository).findById(MEMBRO_ID);
    }

    private Membro membroAtivo() {
        return MembroHelper.membro(
            MEMBRO_ID,
            NOME,
            EAtribuicao.GERENTE,
            LocalDateTime.of(2026, 6, 18, 10, 30),
            null,
            ESituacaoMembro.ATIVO);
    }

    private Membro membroExcluido() {
        return MembroHelper.membro(
            MEMBRO_ID,
            NOME,
            EAtribuicao.GERENTE,
            LocalDateTime.of(2026, 6, 18, 10, 30),
            LocalDateTime.of(2026, 6, 19, 10, 30),
            ESituacaoMembro.EXCLUIDO);
    }
}
