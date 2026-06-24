package br.com.codegroup.teste.modulos.projeto.repository;

import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import java.util.List;
import java.util.Optional;

public interface ProjetoRepository extends JpaRepository<Projeto, String>, QuerydslPredicateExecutor<Projeto> {

    @EntityGraph(attributePaths = {"membros", "membros.membro"})
    Optional<Projeto> findCompleteById(String projetoId);

    @EntityGraph(attributePaths = {"membros", "membros.membro"})
    @Query("select distinct p from Projeto p")
    List<Projeto> findAllComplete();
}
