package br.com.codegroup.teste.modulos.projeto.repository;

import br.com.codegroup.teste.modulos.membro.MembroHelper;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.projeto.ProjetoHelper;
import br.com.codegroup.teste.modulos.projeto.enums.ERiscoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ProjetoRepositoryTest {

    @Autowired
    private ProjetoRepository repository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve buscar projeto completo por id")
    void deveBuscarProjetoCompletoPorId() {
        var projeto = ProjetoHelper.projeto("projeto-1", "PROJETO TESTE");
        var membro = MembroHelper.membro("membro-1", "GARBRIEL GARCIA", EAtribuicao.GERENTE);
        entityManager.persist(projeto);
        entityManager.persist(membro);
        entityManager.persist(ProjetoHelper.projetoMembro("projeto-membro-1", projeto, membro));
        entityManager.flush();
        entityManager.clear();
        var resultado = repository.findCompleteById("projeto-1");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo("projeto-1");
        assertThat(resultado.get().getNome()).isEqualTo("PROJETO TESTE");
        assertThat(resultado.get().getMembros()).hasSize(1);
        assertThat(resultado.get().getMembros().getFirst().getMembro().getId()).isEqualTo("membro-1");
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar projeto completo inexistente")
    void deveRetornarVazioAoBuscarProjetoCompletoInexistente() {
        var resultado = repository.findCompleteById("projeto-inexistente");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve carregar membros e dados do membro ao buscar projeto completo por id")
    void deveCarregarMembrosEDadosDoMembroAoBuscarProjetoCompletoPorId() {
        var projeto = ProjetoHelper.projeto("projeto-1", "PROJETO TESTE");
        var membro = MembroHelper.membro("membro-1", "GABRIEL GARCIA", EAtribuicao.FUNCIONARIO);
        entityManager.persist(projeto);
        entityManager.persist(membro);
        entityManager.persist(ProjetoHelper.projetoMembro("projeto-membro-1", projeto, membro));
        entityManager.flush();
        entityManager.clear();
        var resultado = repository.findCompleteById("projeto-1");

        assertThat(resultado).isPresent();
        var projetoEncontrado = resultado.get();
        assertThat(Hibernate.isInitialized(projetoEncontrado.getMembros())).isTrue();
        assertThat(projetoEncontrado.getMembros()).hasSize(1);
        var projetoMembro = projetoEncontrado.getMembros().getFirst();
        assertThat(Hibernate.isInitialized(projetoMembro.getMembro())).isTrue();
        assertThat(projetoMembro.getMembro().getNome()).isEqualTo("GABRIEL GARCIA");
    }

    @Test
    @DisplayName("Deve listar todos os projetos completos")
    void deveListarTodosOsProjetosCompletos() {
        var projeto1 = ProjetoHelper.projeto("projeto-1", "PROJETO 1");
        var projeto2 = ProjetoHelper.projeto("projeto-2", "PROJETO 2");
        var membro1 = MembroHelper.membro("membro-1", "GERENTE", EAtribuicao.GERENTE);
        var membro2 = MembroHelper.membro("membro-2", "FUNCIONÁRIO", EAtribuicao.FUNCIONARIO);
        entityManager.persist(projeto1);
        entityManager.persist(projeto2);
        entityManager.persist(membro1);
        entityManager.persist(membro2);
        entityManager.persist(ProjetoHelper.projetoMembro("projeto-membro-1", projeto1, membro1));
        entityManager.persist(ProjetoHelper.projetoMembro("projeto-membro-2", projeto2, membro2));
        entityManager.flush();
        entityManager.clear();
        var resultado = repository.findAllComplete();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Projeto::getId).containsExactlyInAnyOrder("projeto-1", "projeto-2");
        assertThat(resultado).allSatisfy(projeto -> {
            assertThat(Hibernate.isInitialized(projeto.getMembros())).isTrue();
            assertThat(projeto.getMembros()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("Deve listar projeto apenas uma vez quando possuir vários membros")
    void deveListarProjetoApenasUmaVezQuandoPossuirVariosMembros() {
        var projeto = ProjetoHelper.projeto("projeto-1", "PROJETO COM MEMBROS");
        var gerente = MembroHelper.membro("membro-1", "GERENTE", EAtribuicao.GERENTE);
        var funcionario = MembroHelper.membro("membro-2", "FUNCIONÁRIO", EAtribuicao.FUNCIONARIO);
        entityManager.persist(projeto);
        entityManager.persist(gerente);
        entityManager.persist(funcionario);
        entityManager.persist(ProjetoHelper.projetoMembro("projeto-membro-1", projeto, gerente));
        entityManager.persist(ProjetoHelper.projetoMembro("projeto-membro-2", projeto, funcionario));
        entityManager.flush();
        entityManager.clear();
        var resultado = repository.findAllComplete();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo("projeto-1");
        assertThat(resultado.getFirst().getMembros()).hasSize(2);
    }

    @Test
    @DisplayName("Deve gerar id ao salvar projeto sem id")
    void deveGerarIdAoSalvarProjetoSemId() {
        var projeto = ProjetoHelper.projeto(
            null,
            "PROJETO SEM ID",
            LocalDateTime.now(),
            LocalDate.now(),
            LocalDate.now().plusMonths(1),
            new BigDecimal("10000.00"),
            "DESCRIÇÃO DO PROJETO",
            ESituacaoProjeto.EM_ANALISE,
            ERiscoProjeto.BAIXO_RISCO);

        var projetoSalvo = repository.saveAndFlush(projeto);

        assertThat(projetoSalvo.getId()).isNotNull();
        assertThat(projetoSalvo.getId()).isNotBlank();
    }
}
