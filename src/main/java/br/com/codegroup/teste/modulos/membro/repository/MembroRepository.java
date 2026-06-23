package br.com.codegroup.teste.modulos.membro.repository;

import br.com.codegroup.teste.modulos.membro.model.Membro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MembroRepository extends JpaRepository<Membro, String>,
    QuerydslPredicateExecutor<Membro>, MembroRepositoryCustom {
}
