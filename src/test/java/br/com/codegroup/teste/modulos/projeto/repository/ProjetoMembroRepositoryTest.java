package br.com.codegroup.teste.modulos.projeto.repository;

import br.com.codegroup.teste.modulos.membro.MembroHelper;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.projeto.ProjetoHelper;
import br.com.codegroup.teste.modulos.projeto.enums.ERiscoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import br.com.codegroup.teste.modulos.projeto.model.ProjetoMembro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ProjetoMembroRepositoryTest {

    @Autowired
    private ProjetoMembroRepository repository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve buscar responsável pelo projeto")
    void deveBuscarResponsavelPeloProjeto() {
        var projeto = persistirProjeto("projeto-1", "Projeto 1", ESituacaoProjeto.EM_ANALISE);
        var gerente = persistirMembro("membro-1", "Gerente", EAtribuicao.GERENTE);
        var funcionario = persistirMembro("membro-2", "Funcionário", EAtribuicao.FUNCIONARIO);
        persistirProjetoMembro("pm-1", projeto, gerente, true, ESituacaoProjetoMembro.PARTICIPANTE);
        persistirProjetoMembro("pm-2", projeto, funcionario, false, ESituacaoProjetoMembro.PARTICIPANTE);
        entityManager.flush();
        entityManager.clear();
        var resultado = repository.findByProjetoIdAndIsResponsavel("projeto-1", true);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo("pm-1");
        assertThat(resultado.get().getIsResponsavel()).isTrue();
        assertThat(resultado.get().getProjeto().getId()).isEqualTo("projeto-1");
        assertThat(resultado.get().getMembro().getId()).isEqualTo("membro-1");
    }

    @Test
    @DisplayName("Deve retornar vazio quando projeto não possuir responsável")
    void deveRetornarVazioQuandoProjetoNaoPossuirResponsavel() {
        var projeto = persistirProjeto("projeto-1", "Projeto 1", ESituacaoProjeto.EM_ANALISE);
        var membro = persistirMembro("membro-1", "Funcionário", EAtribuicao.FUNCIONARIO);
        persistirProjetoMembro("pm-1", projeto, membro, false, ESituacaoProjetoMembro.PARTICIPANTE);
        entityManager.flush();
        entityManager.clear();
        var resultado = repository.findByProjetoIdAndIsResponsavel("projeto-1", true);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar vínculo por projeto, membro e situação")
    void deveBuscarVinculoPorProjetoMembroESituacao() {
        var projeto = persistirProjeto("projeto-1", "Projeto 1", ESituacaoProjeto.EM_ANALISE);
        var membro = persistirMembro("membro-1", "Gabriel", EAtribuicao.FUNCIONARIO);
        persistirProjetoMembro("pm-1", projeto, membro, false, ESituacaoProjetoMembro.PARTICIPANTE);
        entityManager.flush();
        entityManager.clear();
        var resultado = repository.findByProjetoIdAndMembroIdAndSituacao("projeto-1", "membro-1",
            ESituacaoProjetoMembro.PARTICIPANTE);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo("pm-1");
        assertThat(resultado.get().getProjeto().getId()).isEqualTo("projeto-1");
        assertThat(resultado.get().getMembro().getId()).isEqualTo("membro-1");
        assertThat(resultado.get().getSituacao()).isEqualTo(ESituacaoProjetoMembro.PARTICIPANTE);
    }

    @Test
    @DisplayName("Deve retornar vazio quando situação do vínculo for diferente")
    void deveRetornarVazioQuandoSituacaoDoVinculoForDiferente() {
        var projeto = persistirProjeto("projeto-1", "Projeto 1", ESituacaoProjeto.EM_ANALISE);
        var membro = persistirMembro("membro-1", "Gabriel", EAtribuicao.FUNCIONARIO);
        persistirProjetoMembro("pm-1", projeto, membro, false, ESituacaoProjetoMembro.EXCLUIDO);
        entityManager.flush();
        entityManager.clear();
        var resultado = repository.findByProjetoIdAndMembroIdAndSituacao("projeto-1", "membro-1",
            ESituacaoProjetoMembro.PARTICIPANTE);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar vínculos do membro com situação diferente da informada")
    void deveBuscarVinculosDoMembroComSituacaoDiferenteDaInformada() {
        var projeto1 = persistirProjeto("projeto-1", "Projeto 1", ESituacaoProjeto.EM_ANALISE);
        var projeto2 = persistirProjeto("projeto-2", "Projeto 2", ESituacaoProjeto.EM_ANALISE);
        var projeto3 = persistirProjeto("projeto-3", "Projeto 3", ESituacaoProjeto.EM_ANALISE);
        var membro = persistirMembro("membro-1", "Gabriel", EAtribuicao.FUNCIONARIO);
        persistirProjetoMembro("pm-1", projeto1, membro, false, ESituacaoProjetoMembro.PARTICIPANTE);
        persistirProjetoMembro("pm-2", projeto2, membro, false, ESituacaoProjetoMembro.PARTICIPANTE);
        persistirProjetoMembro("pm-3", projeto3, membro, false, ESituacaoProjetoMembro.EXCLUIDO);

        entityManager.flush();
        entityManager.clear();

        var resultado = repository.findByMembroIdAndSituacaoNot("membro-1", ESituacaoProjetoMembro.EXCLUIDO);

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
            .extracting(ProjetoMembro::getId)
            .containsExactlyInAnyOrder("pm-1", "pm-2");

        assertThat(resultado).allSatisfy(projetoMembro ->
            assertThat(projetoMembro.getSituacao()).isNotEqualTo(ESituacaoProjetoMembro.EXCLUIDO));
    }

    @Test
    @DisplayName("Deve buscar vínculos do projeto com situação diferente da informada")
    void deveBuscarVinculosDoProjetoComSituacaoDiferenteDaInformada() {
        var projeto = persistirProjeto("projeto-1", "Projeto 1", ESituacaoProjeto.EM_ANALISE);
        var membro1 = persistirMembro("membro-1", "Gerente", EAtribuicao.GERENTE);
        var membro2 = persistirMembro("membro-2", "Funcionário 1", EAtribuicao.FUNCIONARIO);
        var membro3 = persistirMembro("membro-3", "Funcionário 2", EAtribuicao.FUNCIONARIO);
        persistirProjetoMembro("pm-1", projeto, membro1, true, ESituacaoProjetoMembro.PARTICIPANTE);
        persistirProjetoMembro("pm-2", projeto, membro2, false, ESituacaoProjetoMembro.PARTICIPANTE);
        persistirProjetoMembro("pm-3", projeto, membro3, false, ESituacaoProjetoMembro.EXCLUIDO);
        entityManager.flush();
        entityManager.clear();
        var resultado = repository.findByProjetoIdAndSituacaoNot("projeto-1", ESituacaoProjetoMembro.EXCLUIDO);

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
            .extracting(ProjetoMembro::getId)
            .containsExactlyInAnyOrder("pm-1", "pm-2");

        assertThat(resultado).allSatisfy(projetoMembro ->
            assertThat(projetoMembro.getSituacao()).isNotEqualTo(ESituacaoProjetoMembro.EXCLUIDO));
    }

    @Test
    @DisplayName("Deve buscar vínculos do membro ignorando projetos com situações informadas")
    void deveBuscarVinculosDoMembroIgnorandoProjetosComSituacoesInformadas() {
        var projetoAtivo = persistirProjeto("projeto-1", "Projeto Ativo", ESituacaoProjeto.EM_ANALISE);
        var projetoExcluido = persistirProjeto("projeto-2", "Projeto Excluído", ESituacaoProjeto.EXCLUIDO);
        var projetoCancelado = persistirProjeto("projeto-3", "Projeto Cancelado", ESituacaoProjeto.CANCELADO);
        var membro = persistirMembro("membro-1", "Gabriel", EAtribuicao.FUNCIONARIO);
        persistirProjetoMembro("pm-1", projetoAtivo, membro, false, ESituacaoProjetoMembro.PARTICIPANTE);
        persistirProjetoMembro("pm-2", projetoExcluido, membro, false, ESituacaoProjetoMembro.PARTICIPANTE);
        persistirProjetoMembro("pm-3", projetoCancelado, membro, false, ESituacaoProjetoMembro.PARTICIPANTE);

        entityManager.flush();
        entityManager.clear();

        var resultado = repository.findByMembroIdAndProjetoSituacaoNotIn("membro-1",
            List.of(ESituacaoProjeto.EXCLUIDO, ESituacaoProjeto.CANCELADO));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo("pm-1");
        assertThat(resultado.get(0).getProjeto().getSituacao()).isEqualTo(ESituacaoProjeto.EM_ANALISE);
    }

    @Test
    @DisplayName("Deve gerar id ao salvar projeto membro sem id")
    void deveGerarIdAoSalvarProjetoMembroSemId() {
        var projeto = persistirProjeto("projeto-1", "Projeto 1", ESituacaoProjeto.EM_ANALISE);
        var membro = persistirMembro("membro-1", "Gabriel", EAtribuicao.FUNCIONARIO);

        var projetoMembro = ProjetoHelper.projetoMembro(null, LocalDateTime.now(), null, projeto, membro,
            false, ESituacaoProjetoMembro.PARTICIPANTE);
        var salvo = repository.saveAndFlush(projetoMembro);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getId()).isNotBlank();
    }

    private Projeto persistirProjeto(String id, String nome, ESituacaoProjeto situacao) {
        var projeto = ProjetoHelper.projeto(id, nome, LocalDateTime.now(), LocalDate.now(), LocalDate.now().plusMonths(1),
            new BigDecimal("10000.00"), "DESCRIÇAO DO PROJETO", situacao, ERiscoProjeto.BAIXO_RISCO);
        entityManager.persist(projeto);

        return projeto;
    }

    private Membro persistirMembro(String id, String nome, EAtribuicao atribuicao) {
        var membro = MembroHelper.membro(id, nome, atribuicao, LocalDateTime.now(), null, ESituacaoMembro.ATIVO);
        entityManager.persist(membro);

        return membro;
    }

    private ProjetoMembro persistirProjetoMembro(String id, Projeto projeto, Membro membro, Boolean isResponsavel,
                                                 ESituacaoProjetoMembro situacao) {
        var projetoMembro = ProjetoHelper.projetoMembro(id, LocalDateTime.now(),
            ESituacaoProjetoMembro.EXCLUIDO.equals(situacao) ? LocalDateTime.now() : null, projeto,
            membro, isResponsavel, situacao);
        entityManager.persist(projetoMembro);

        return projetoMembro;
    }
}
