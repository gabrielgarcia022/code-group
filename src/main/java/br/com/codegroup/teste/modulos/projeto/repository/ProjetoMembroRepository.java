package br.com.codegroup.teste.modulos.projeto.repository;

import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.model.ProjetoMembro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjetoMembroRepository extends JpaRepository<ProjetoMembro, String> {

    Optional<ProjetoMembro> findByProjetoIdAndIsResponsavel(String projetoId, Boolean isResponsavel);

    Optional<ProjetoMembro> findByProjetoIdAndMembroIdAndSituacao(String projetoId, String membroId,
                                                                  ESituacaoProjetoMembro situacao);

    List<ProjetoMembro> findByMembroIdAndSituacaoNot(String membroId, ESituacaoProjetoMembro situacao);

    List<ProjetoMembro> findByProjetoIdAndSituacaoNot(String projetoId, ESituacaoProjetoMembro situacao);

    List<ProjetoMembro> findByMembroIdAndProjetoSituacaoNotIn(String membroId, List<ESituacaoProjeto> situacoes);
}
