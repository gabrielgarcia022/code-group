package br.com.codegroup.teste.modulos.projeto.service;

import br.com.codegroup.teste.config.FilesUtils;
import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.comum.exception.NotFoundException;
import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
import br.com.codegroup.teste.modulos.membro.MembroHelper;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.membro.repository.MembroRepository;
import br.com.codegroup.teste.modulos.projeto.ProjetoHelper;
import br.com.codegroup.teste.modulos.projeto.dto.AtualizarSituacaoRequest;
import br.com.codegroup.teste.modulos.projeto.dto.ProjetoMembroRequest;
import br.com.codegroup.teste.modulos.projeto.dto.ProjetoRequest;
import br.com.codegroup.teste.modulos.projeto.enums.ERiscoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.filtros.ProjetoFiltros;
import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import br.com.codegroup.teste.modulos.projeto.model.ProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.repository.ProjetoMembroRepository;
import br.com.codegroup.teste.modulos.projeto.repository.ProjetoRepository;
import com.querydsl.core.BooleanBuilder;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjetoServiceTest {

    private static final String PROJETO_ID = "projeto-123";
    private static final String GERENTE_ID = "gerente-123";
    private static final String FUNCIONARIO_ID = "funcionario-123";
    private static final String PROJETO_MEMBRO_ID = "projeto-membro-123";

    @Mock
    private ProjetoMembroRepository projetoMembroRepository;
    @Mock
    private MembroRepository membroRepository;
    @Mock
    private ProjetoRepository repository;
    @Mock
    private FilesUtils filesUtils;
    @Mock
    private HttpServletResponse httpServletResponse;
    private ProjetoService service;

    @BeforeEach
    void setUp() {
        service = new ProjetoService(
            projetoMembroRepository,
            membroRepository,
            repository,
            filesUtils
        );
    }

    @Test
    @DisplayName("Deve buscar projetos paginados")
    void deveBuscarProjetosPaginados() {
        var filtros = mock(ProjetoFiltros.class, RETURNS_DEEP_STUBS);
        var predicate = mock(BooleanBuilder.class);
        var pageRequest = new PageRequest();
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        var page = new PageImpl<>(List.of(projeto), pageRequest, 1);

        when(filtros.toPredicate().build()).thenReturn(predicate);
        when(repository.findAll(eq(predicate), eq(pageRequest))).thenReturn(page);

        var resultado = service.getAll(filtros, pageRequest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getId()).isEqualTo(PROJETO_ID);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("PROJETO TESTE");
        assertThat(resultado.getContent().get(0).getSituacao()).isEqualTo(ESituacaoProjeto.EM_ANALISE);

        verify(repository).findAll(eq(predicate), eq(pageRequest));
    }

    @Test
    @DisplayName("Deve detalhar projeto")
    void deveDetalharProjeto() {
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        var gerente = gerente();
        var funcionario = funcionario();
        var membroAtivo = projetoMembro("pm-1", projeto, gerente, true, ESituacaoProjetoMembro.PARTICIPANTE);
        var membroExcluido = projetoMembro("pm-2", projeto, funcionario, false, ESituacaoProjetoMembro.EXCLUIDO);

        projeto.setMembros(List.of(membroAtivo, membroExcluido));

        when(repository.findCompleteById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        var resultado = service.detalhar(PROJETO_ID);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(PROJETO_ID);
        assertThat(resultado.getNome()).isEqualTo("PROJETO TESTE");
        assertThat(resultado.getMembros()).hasSize(1);
        assertThat(resultado.getMembrosAnteriores()).hasSize(1);
        assertThat(resultado.getMembros().get(0).getMembroId()).isEqualTo(GERENTE_ID);
        assertThat(resultado.getMembrosAnteriores().get(0).getMembroId()).isEqualTo(FUNCIONARIO_ID);

        verify(repository).findCompleteById(PROJETO_ID);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao detalhar projeto inexistente")
    void deveLancarNotFoundExceptionAoDetalharProjetoInexistente() {
        when(repository.findCompleteById(PROJETO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalhar(PROJETO_ID))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Projeto não encontrado");

        verify(repository).findCompleteById(PROJETO_ID);
    }

    @Test
    @DisplayName("Deve salvar projeto com gerente responsável")
    void deveSalvarProjetoComGerenteResponsavel() {
        var request = projetoRequest(null, GERENTE_ID);
        var gerente = gerente();

        when(membroRepository.findGerenteById(GERENTE_ID)).thenReturn(Optional.of(gerente));
        when(projetoMembroRepository.findByMembroIdAndProjetoSituacaoNotIn(eq(GERENTE_ID), anyList()))
            .thenReturn(List.of());

        when(projetoMembroRepository.save(any(ProjetoMembro.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(repository.save(any(Projeto.class))).thenAnswer(invocation -> {
            var projeto = invocation.<Projeto>getArgument(0);
            projeto.setId(PROJETO_ID);
            return projeto;
        });

        var resultado = service.salvar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(PROJETO_ID);
        assertThat(resultado.getNome()).isEqualTo("PROJETO TESTE");
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoProjeto.EM_ANALISE);

        var captor = ArgumentCaptor.forClass(ProjetoMembro.class);
        verify(projetoMembroRepository).save(captor.capture());

        var projetoMembroSalvo = captor.getValue();

        assertThat(projetoMembroSalvo.getMembro()).isSameAs(gerente);
        assertThat(projetoMembroSalvo.getIsResponsavel()).isTrue();
        assertThat(projetoMembroSalvo.getSituacao()).isEqualTo(ESituacaoProjetoMembro.PARTICIPANTE);

        verify(repository).save(any(Projeto.class));
    }

    @Test
    @DisplayName("Deve lançar erro ao salvar projeto quando gerente não existir")
    void deveLancarErroAoSalvarProjetoQuandoGerenteNaoExistir() {
        var request = projetoRequest(null, GERENTE_ID);

        when(membroRepository.findGerenteById(GERENTE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.salvar(request))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Gerente não encontrado");

        verify(repository, never()).save(any());
        verify(projetoMembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao salvar projeto quando gerente já estiver em 3 projetos")
    void deveLancarErroAoSalvarProjetoQuandoGerenteJaEstiverEmTresProjetos() {
        var request = projetoRequest(null, GERENTE_ID);
        var gerente = gerente();

        when(membroRepository.findGerenteById(GERENTE_ID)).thenReturn(Optional.of(gerente));
        when(projetoMembroRepository.findByMembroIdAndProjetoSituacaoNotIn(eq(GERENTE_ID), anyList()))
            .thenReturn(List.of(projetoMembro("pm-1"), projetoMembro("pm-2"), projetoMembro("pm-3")));

        assertThatThrownBy(() -> service.salvar(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Este gerente já esta alocado em 3 projetos em andamento");

        verify(repository, never()).save(any());
        verify(projetoMembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve editar projeto mantendo o mesmo responsável")
    void deveEditarProjetoMantendoMesmoResponsavel() {
        var request = projetoRequest(PROJETO_ID, GERENTE_ID);
        request.setNome("Projeto Atualizado");
        var projeto = projeto(PROJETO_ID, "Projeto Antigo", ESituacaoProjeto.EM_ANALISE);
        var responsavel = projetoMembro("pm-responsavel", projeto, gerente(), true, ESituacaoProjetoMembro.PARTICIPANTE);
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(projetoMembroRepository.findByProjetoIdAndIsResponsavel(PROJETO_ID, true))
            .thenReturn(Optional.of(responsavel));
        when(repository.save(any(Projeto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = service.editar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(PROJETO_ID);
        assertThat(resultado.getNome()).isEqualTo("Projeto Atualizado");

        verify(repository).save(projeto);
        verify(membroRepository, never()).findGerenteById(any());
        verify(projetoMembroRepository, never()).save(any(ProjetoMembro.class));
    }

    @Test
    @DisplayName("Deve editar projeto alterando responsável")
    void deveEditarProjetoAlterandoResponsavel() {
        var novoGerenteId = "gerente-novo";
        var request = projetoRequest(PROJETO_ID, novoGerenteId);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        var gerenteAntigo = gerente();
        var novoGerente = membro(novoGerenteId, "Novo Gerente", EAtribuicao.GERENTE);
        var responsavelAtual = projetoMembro("pm-responsavel", projeto, gerenteAntigo, true,
            ESituacaoProjetoMembro.PARTICIPANTE);

        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(projetoMembroRepository.findByProjetoIdAndIsResponsavel(PROJETO_ID, true))
            .thenReturn(Optional.of(responsavelAtual));
        when(membroRepository.findGerenteById(novoGerenteId)).thenReturn(Optional.of(novoGerente));
        when(projetoMembroRepository.save(any(ProjetoMembro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(any(Projeto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = service.editar(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(PROJETO_ID);

        assertThat(responsavelAtual.getSituacao()).isEqualTo(ESituacaoProjetoMembro.EXCLUIDO);

        verify(projetoMembroRepository, times(2)).save(any(ProjetoMembro.class));
        verify(repository).save(projeto);
    }

    @Test
    @DisplayName("Deve editar projeto criando responsável quando não existir responsável atual")
    void deveEditarProjetoCriandoResponsavelQuandoNaoExistirResponsavelAtual() {
        var request = projetoRequest(PROJETO_ID, GERENTE_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        var gerente = gerente();

        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(projetoMembroRepository.findByProjetoIdAndIsResponsavel(PROJETO_ID, true))
            .thenReturn(Optional.empty());
        when(membroRepository.findGerenteById(GERENTE_ID)).thenReturn(Optional.of(gerente));
        when(projetoMembroRepository.save(any(ProjetoMembro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(any(Projeto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = service.editar(request);

        assertThat(resultado).isNotNull();
        verify(projetoMembroRepository).save(any(ProjetoMembro.class));
        verify(repository).save(projeto);
    }

    @Test
    @DisplayName("Deve lançar erro ao editar projeto cancelado")
    void deveLancarErroAoEditarProjetoCancelado() {
        var request = projetoRequest(PROJETO_ID, GERENTE_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.CANCELADO);

        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.editar(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Você não pode editar um projeto cancelado");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao editar projeto encerrado")
    void deveLancarErroAoEditarProjetoEncerrado() {
        var request = projetoRequest(PROJETO_ID, GERENTE_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.ENCERRADO);

        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.editar(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Você não pode editar um projeto encerrado");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve adicionar membro ao projeto")
    void deveAdicionarMembroAoProjeto() {
        var request = projetoMembroRequest(PROJETO_ID, FUNCIONARIO_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANDAMENTO);
        var funcionario = funcionario();

        when(membroRepository.findFuncionarioById(FUNCIONARIO_ID)).thenReturn(Optional.of(funcionario));
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(projetoMembroRepository.findByProjetoIdAndSituacaoNot(PROJETO_ID, ESituacaoProjetoMembro.EXCLUIDO))
            .thenReturn(List.of(projetoMembro("pm-1")));
        when(projetoMembroRepository.findByMembroIdAndProjetoSituacaoNotIn(eq(FUNCIONARIO_ID), anyList()))
            .thenReturn(List.of());
        when(projetoMembroRepository.save(any(ProjetoMembro.class)))
            .thenAnswer(invocation -> {
                var projetoMembro = invocation.<ProjetoMembro>getArgument(0);
                projetoMembro.setId(PROJETO_MEMBRO_ID);
                return projetoMembro;
            });

        var resultado = service.adicionarMembro(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getMembroId()).isEqualTo(FUNCIONARIO_ID);
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoProjetoMembro.PARTICIPANTE);

        verify(projetoMembroRepository).save(any(ProjetoMembro.class));
    }

    @Test
    @DisplayName("Deve lançar erro ao adicionar membro quando funcionário não existir")
    void deveLancarErroAoAdicionarMembroQuandoFuncionarioNaoExistir() {
        var request = projetoMembroRequest(PROJETO_ID, FUNCIONARIO_ID);

        when(membroRepository.findFuncionarioById(FUNCIONARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adicionarMembro(request))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Funcionário não encontrado");

        verify(repository, never()).findById(any());
        verify(projetoMembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao adicionar membro em projeto encerrado")
    void deveLancarErroAoAdicionarMembroEmProjetoEncerrado() {
        var request = projetoMembroRequest(PROJETO_ID, FUNCIONARIO_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.ENCERRADO);
        var funcionario = funcionario();

        when(membroRepository.findFuncionarioById(FUNCIONARIO_ID)).thenReturn(Optional.of(funcionario));
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.adicionarMembro(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Você não pode adicionar membros neste projeto");

        verify(projetoMembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao adicionar membro quando projeto já possuir 10 membros")
    void deveLancarErroAoAdicionarMembroQuandoProjetoJaPossuirDezMembros() {
        var request = projetoMembroRequest(PROJETO_ID, FUNCIONARIO_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANDAMENTO);
        var funcionario = funcionario();

        when(membroRepository.findFuncionarioById(FUNCIONARIO_ID)).thenReturn(Optional.of(funcionario));
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(projetoMembroRepository.findByProjetoIdAndSituacaoNot(PROJETO_ID, ESituacaoProjetoMembro.EXCLUIDO))
            .thenReturn(List.of(
                projetoMembro("pm-1"), projetoMembro("pm-2"), projetoMembro("pm-3"),
                projetoMembro("pm-4"), projetoMembro("pm-5"), projetoMembro("pm-6"),
                projetoMembro("pm-7"), projetoMembro("pm-8"), projetoMembro("pm-9"),
                projetoMembro("pm-10")
            ));

        assertThatThrownBy(() -> service.adicionarMembro(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("O projeto pode conter no máximo 10 membros");

        verify(projetoMembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao adicionar membro já alocado em 3 projetos")
    void deveLancarErroAoAdicionarMembroJaAlocadoEmTresProjetos() {
        var request = projetoMembroRequest(PROJETO_ID, FUNCIONARIO_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANDAMENTO);
        var funcionario = funcionario();

        when(membroRepository.findFuncionarioById(FUNCIONARIO_ID)).thenReturn(Optional.of(funcionario));
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(projetoMembroRepository.findByProjetoIdAndSituacaoNot(PROJETO_ID, ESituacaoProjetoMembro.EXCLUIDO))
            .thenReturn(List.of(projetoMembro("pm-1")));
        when(projetoMembroRepository.findByMembroIdAndProjetoSituacaoNotIn(eq(FUNCIONARIO_ID), anyList()))
            .thenReturn(List.of(projetoMembro("pm-1"), projetoMembro("pm-2"), projetoMembro("pm-3")));

        assertThatThrownBy(() -> service.adicionarMembro(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Este funcionário já está alocado em 3 projetos em andamento");

        verify(projetoMembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve remover membro do projeto")
    void deveRemoverMembroDoProjeto() {
        var request = projetoMembroRequest(PROJETO_ID, FUNCIONARIO_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANDAMENTO);
        var membroProjeto = projetoMembro(PROJETO_MEMBRO_ID, projeto, funcionario(), false,
            ESituacaoProjetoMembro.PARTICIPANTE);

        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(projetoMembroRepository.findByProjetoIdAndMembroIdAndSituacao(PROJETO_ID, FUNCIONARIO_ID,
            ESituacaoProjetoMembro.PARTICIPANTE)).thenReturn(Optional.of(membroProjeto));
        when(projetoMembroRepository.findByProjetoIdAndSituacaoNot(PROJETO_ID, ESituacaoProjetoMembro.EXCLUIDO))
            .thenReturn(List.of(membroProjeto, projetoMembro("pm-2")));
        when(projetoMembroRepository.save(any(ProjetoMembro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var resultado = service.removerMembro(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoProjetoMembro.EXCLUIDO);
        assertThat(membroProjeto.getDataExclusao()).isNotNull();

        verify(projetoMembroRepository).save(membroProjeto);
    }

    @Test
    @DisplayName("Deve lançar erro ao remover membro de projeto encerrado")
    void deveLancarErroAoRemoverMembroDeProjetoEncerrado() {
        var request = projetoMembroRequest(PROJETO_ID, FUNCIONARIO_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.ENCERRADO);
        var membroProjeto = projetoMembro(PROJETO_MEMBRO_ID);

        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(projetoMembroRepository.findByProjetoIdAndMembroIdAndSituacao(PROJETO_ID, FUNCIONARIO_ID,
            ESituacaoProjetoMembro.PARTICIPANTE)).thenReturn(Optional.of(membroProjeto));

        assertThatThrownBy(() -> service.removerMembro(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Você não pode remover membros deste projeto");

        verify(projetoMembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao remover último membro do projeto")
    void deveLancarErroAoRemoverUltimoMembroDoProjeto() {
        var request = projetoMembroRequest(PROJETO_ID, FUNCIONARIO_ID);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANDAMENTO);
        var membroProjeto = projetoMembro(PROJETO_MEMBRO_ID, projeto, funcionario(), false, ESituacaoProjetoMembro.PARTICIPANTE);

        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(projetoMembroRepository.findByProjetoIdAndMembroIdAndSituacao(PROJETO_ID, FUNCIONARIO_ID,
            ESituacaoProjetoMembro.PARTICIPANTE)).thenReturn(Optional.of(membroProjeto));
        when(projetoMembroRepository.findByProjetoIdAndSituacaoNot(PROJETO_ID, ESituacaoProjetoMembro.EXCLUIDO))
            .thenReturn(List.of(membroProjeto));

        assertThatThrownBy(() -> service.removerMembro(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("O projeto precisa ter no mínimo 1 membro");

        verify(projetoMembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve excluir membro de todos os projetos vinculados")
    void deveExcluirMembroDeTodosOsProjetosVinculados() {
        var projetoMembro1 = projetoMembro("pm-1");
        var projetoMembro2 = projetoMembro("pm-2");
        when(projetoMembroRepository.findByMembroIdAndSituacaoNot(FUNCIONARIO_ID, ESituacaoProjetoMembro.EXCLUIDO))
            .thenReturn(List.of(projetoMembro1, projetoMembro2));
        service.excluirMembro(FUNCIONARIO_ID);

        assertThat(projetoMembro1.getSituacao()).isEqualTo(ESituacaoProjetoMembro.EXCLUIDO);
        assertThat(projetoMembro2.getSituacao()).isEqualTo(ESituacaoProjetoMembro.EXCLUIDO);

        verify(projetoMembroRepository, times(2)).save(any(ProjetoMembro.class));
    }

    @Test
    @DisplayName("Não deve excluir membro de projetos quando não houver vínculos ativos")
    void naoDeveExcluirMembroDeProjetosQuandoNaoHouverVinculosAtivos() {
        when(projetoMembroRepository.findByMembroIdAndSituacaoNot(FUNCIONARIO_ID, ESituacaoProjetoMembro.EXCLUIDO))
            .thenReturn(List.of());
        service.excluirMembro(FUNCIONARIO_ID);

        verify(projetoMembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar situação do projeto na sequência correta")
    void deveAtualizarSituacaoDoProjetoNaSequenciaCorreta() {
        var request = atualizarSituacaoRequest(PROJETO_ID, ESituacaoProjeto.ANALISE_REALIZADA);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(repository.save(any(Projeto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var resultado = service.atualizarSituacao(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoProjeto.ANALISE_REALIZADA);

        verify(repository).save(projeto);
    }

    @Test
    @DisplayName("Deve lançar erro ao atualizar situação fora da sequência")
    void deveLancarErroAoAtualizarSituacaoForaDaSequencia() {
        var request = atualizarSituacaoRequest(PROJETO_ID, ESituacaoProjeto.INICIADO);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.atualizarSituacao(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("A situação não esta na sequência correta");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao atualizar projeto já encerrado")
    void deveLancarErroAoAtualizarProjetoJaEncerrado() {
        var request = atualizarSituacaoRequest(PROJETO_ID, ESituacaoProjeto.CANCELADO);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.ENCERRADO);
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.atualizarSituacao(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Este projeto já foi encerrado");
    }

    @Test
    @DisplayName("Deve lançar erro ao atualizar projeto cancelado")
    void deveLancarErroAoAtualizarProjetoCancelado() {
        var request = atualizarSituacaoRequest(PROJETO_ID, ESituacaoProjeto.EXCLUIDO);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.CANCELADO);
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.atualizarSituacao(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Este projeto foi cancelado");
    }

    @Test
    @DisplayName("Deve lançar erro ao atualizar projeto excluído")
    void deveLancarErroAoAtualizarProjetoExcluido() {
        var request = atualizarSituacaoRequest(PROJETO_ID, ESituacaoProjeto.CANCELADO);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EXCLUIDO);
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.atualizarSituacao(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Este projeto foi excluído");
    }

    @Test
    @DisplayName("Deve lançar erro ao excluir projeto iniciado via atualização de situação")
    void deveLancarErroAoExcluirProjetoIniciadoViaAtualizacaoDeSituacao() {
        var request = atualizarSituacaoRequest(PROJETO_ID, ESituacaoProjeto.EXCLUIDO);
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.INICIADO);
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.atualizarSituacao(request))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Este projeto não pode ser excluído");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cancelar projeto")
    void deveCancelarProjeto() {
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(repository.save(any(Projeto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var resultado = service.cancelar(PROJETO_ID);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoProjeto.CANCELADO);
        assertThat(projeto.getDataCancelamento()).isNotNull();

        verify(repository).save(projeto);
    }

    @Test
    @DisplayName("Deve lançar erro ao cancelar projeto encerrado")
    void deveLancarErroAoCancelarProjetoEncerrado() {
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.ENCERRADO);

        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.cancelar(PROJETO_ID))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Você não pode cancelar este projeto");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve excluir projeto")
    void deveExcluirProjeto() {
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        when(repository.save(any(Projeto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var resultado = service.excluir(PROJETO_ID);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getSituacao()).isEqualTo(ESituacaoProjeto.EXCLUIDO);
        assertThat(projeto.getDataExclusao()).isNotNull();

        verify(repository).save(projeto);
    }

    @Test
    @DisplayName("Deve lançar erro ao excluir projeto iniciado")
    void deveLancarErroAoExcluirProjetoIniciado() {
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.INICIADO);

        when(repository.findById(PROJETO_ID)).thenReturn(Optional.of(projeto));
        assertThatThrownBy(() -> service.excluir(PROJETO_ID))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Você não pode excluir este projeto");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve gerar relatório PDF do portfólio")
    void deveGerarRelatorioPdfDoPortfolio() {
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        projeto.setMembros(List.of());

        when(repository.findAllComplete()).thenReturn(List.of(projeto));
        when(filesUtils.processTemplate(eq("portfolio"), any()))
            .thenReturn("<html>portfolio</html>");
        when(filesUtils.htmlToPdfStream("<html>portfolio</html>")).thenReturn(new ByteArrayInputStream("pdf".getBytes()));
        service.gerarRelatorioPortfolioPdf(httpServletResponse);

        verify(repository).findAllComplete();
        verify(filesUtils).processTemplate(eq("portfolio"), any());
        verify(filesUtils).htmlToPdfStream("<html>portfolio</html>");
        verify(filesUtils).baixarArquivo(
            eq(httpServletResponse),
            any(ByteArrayInputStream.class),
            eq("portfolio.pdf"),
            eq("application/pdf")
        );
    }

    @Test
    @DisplayName("Deve gerar relatório Excel do portfólio")
    void deveGerarRelatorioExcelDoPortfolio() {
        var projeto = projeto(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        projeto.setMembros(List.of());
        when(repository.findAllComplete()).thenReturn(List.of(projeto));
        service.gerarRelatorioPortfolioExcel(httpServletResponse);

        verify(repository).findAllComplete();
        verify(filesUtils).baixarArquivo(
            eq(httpServletResponse),
            any(),
            eq("portfolio.xlsx"),
            eq("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
    }

    private ProjetoRequest projetoRequest(String id, String gerenteId) {
        var request = new ProjetoRequest();
        request.setId(id);
        request.setNome("PROJETO TESTE");
        request.setPrevisaoTermino(LocalDate.now().plusMonths(2));
        request.setOrcamentoTotal(new BigDecimal("10000.00"));
        request.setDescricao("Descrição do projeto");
        request.setGerenteId(gerenteId);

        return request;
    }

    private ProjetoMembroRequest projetoMembroRequest(String projetoId, String membroId) {
        var request = new ProjetoMembroRequest();
        request.setProjetoId(projetoId);
        request.setMembroId(membroId);

        return request;
    }

    private AtualizarSituacaoRequest atualizarSituacaoRequest(String projetoId, ESituacaoProjeto situacao) {
        var request = new AtualizarSituacaoRequest();
        request.setProjetoId(projetoId);
        request.setSituacao(situacao);

        return request;
    }

    private Projeto projeto(String id, String nome, ESituacaoProjeto situacao) {
        return ProjetoHelper.projeto(
            id,
            nome,
            LocalDateTime.of(2026, 6, 18, 10, 30),
            null,
            LocalDate.now().plusMonths(2),
            new BigDecimal("10000.00"),
            "DESCRIÇÃO DO PROJETO",
            situacao,
            ERiscoProjeto.BAIXO_RISCO);
    }

    private Membro gerente() {
        return membro(GERENTE_ID, "Gerente Teste", EAtribuicao.GERENTE);
    }

    private Membro funcionario() {
        return membro(FUNCIONARIO_ID, "Funcionário Teste", EAtribuicao.FUNCIONARIO);
    }

    private Membro membro(String id, String nome, EAtribuicao atribuicao) {
        return MembroHelper.membro(id, nome, atribuicao,
            LocalDateTime.of(2026, 6, 18, 10, 30), null, ESituacaoMembro.ATIVO);
    }

    private ProjetoMembro projetoMembro(String id) {
        return ProjetoHelper.projetoMembro(id, ProjetoHelper.projeto(PROJETO_ID, "PROJETO TESTE",
            ESituacaoProjeto.EM_ANDAMENTO), funcionario(), false, ESituacaoProjetoMembro.PARTICIPANTE);
    }

    private ProjetoMembro projetoMembro(String id, Projeto projeto, Membro membro, Boolean isResponsavel,
                                        ESituacaoProjetoMembro situacao) {
        return ProjetoHelper.projetoMembro(id, LocalDateTime.of(2026, 6, 18, 10, 30),
            ESituacaoProjetoMembro.EXCLUIDO.equals(situacao) ? LocalDateTime.of(2026, 6, 19, 10, 30) : null,
            projeto, membro, isResponsavel, situacao);
    }
}
