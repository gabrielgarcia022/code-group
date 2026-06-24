package br.com.codegroup.teste.modulos.membro.repository;

import br.com.codegroup.teste.modulos.membro.MembroHelper;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class MembroRepositoryTest {

    @Autowired
    private MembroRepository repository;

    @Test
    @DisplayName("Deve encontrar gerente por id")
    void deveEncontrarGerentePorId() {
        var gerente = MembroHelper.membro("membro-gerente", "GERENTE TESTE", EAtribuicao.GERENTE);
        var funcionario = MembroHelper.membro("membro-funcionario", "FUNCIONÁRIO TESTE", EAtribuicao.FUNCIONARIO);
        repository.saveAllAndFlush(List.of(gerente, funcionario));
        var resultado = repository.findGerenteById("membro-gerente");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo("membro-gerente");
        assertThat(resultado.get().getNome()).isEqualTo("GERENTE TESTE");
        assertThat(resultado.get().getAtribuicao()).isEqualTo(EAtribuicao.GERENTE);
    }

    @Test
    @DisplayName("Não deve encontrar gerente quando membro for funcionário")
    void naoDeveEncontrarGerenteQuandoMembroForFuncionario() {
        var funcionario = MembroHelper.membro("membro-funcionario", "FUNCIONÁRIO TESTE", EAtribuicao.FUNCIONARIO);
        repository.saveAndFlush(funcionario);
        var resultado = repository.findGerenteById("membro-funcionario");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar funcionário por id")
    void deveEncontrarFuncionarioPorId() {
        var gerente = MembroHelper.membro("membro-gerente", "GERENTE TESTE", EAtribuicao.GERENTE);
        var funcionario = MembroHelper.membro("membro-funcionario", "FUNCIONÁRIO TESTE", EAtribuicao.FUNCIONARIO);
        repository.saveAllAndFlush(List.of(gerente, funcionario));
        var resultado = repository.findFuncionarioById("membro-funcionario");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo("membro-funcionario");
        assertThat(resultado.get().getNome()).isEqualTo("FUNCIONÁRIO TESTE");
        assertThat(resultado.get().getAtribuicao()).isEqualTo(EAtribuicao.FUNCIONARIO);
    }

    @Test
    @DisplayName("Não deve encontrar funcionário quando membro for gerente")
    void naoDeveEncontrarFuncionarioQuandoMembroForGerente() {
        var gerente = MembroHelper.membro("membro-gerente", "GERENTE TESTE", EAtribuicao.GERENTE);
        repository.saveAndFlush(gerente);
        var resultado = repository.findFuncionarioById("membro-gerente");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar vazio quando membro não existir")
    void deveRetornarVazioQuandoMembroNaoExistir() {
        var resultadoGerente = repository.findGerenteById("id-inexistente");
        var resultadoFuncionario = repository.findFuncionarioById("id-inexistente");

        assertThat(resultadoGerente).isEmpty();
        assertThat(resultadoFuncionario).isEmpty();
    }

    @Test
    @DisplayName("Deve gerar id ao salvar membro sem id")
    void deveGerarIdAoSalvarMembroSemId() {
        var membro = MembroHelper.membro(null, "MEMBRO SEM ID", EAtribuicao.FUNCIONARIO, LocalDateTime.now(),
            null, ESituacaoMembro.ATIVO);
        var membroSalvo = repository.saveAndFlush(membro);

        assertThat(membroSalvo.getId()).isNotNull();
        assertThat(membroSalvo.getId()).isNotBlank();
    }
}
