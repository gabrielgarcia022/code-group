package br.com.codegroup.teste.modulos.membro.repository;

import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

import static br.com.codegroup.teste.modulos.membro.model.QMembro.membro;

public class MembroRepositoryImpl implements MembroRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Membro> findGerenteById(String membroId) {
        return findMembroByIdAndAtribuicao(membroId, EAtribuicao.GERENTE);
    }

    @Override
    public Optional<Membro> findFuncionarioById(String membroId) {
        return findMembroByIdAndAtribuicao(membroId, EAtribuicao.FUNCIONARIO);
    }

    public Optional<Membro> findMembroByIdAndAtribuicao(String membroId, EAtribuicao atribuicao) {
        return Optional.of(new JPAQueryFactory(entityManager)
            .selectFrom(membro)
            .where(membro.id.eq(membroId), membro.atribuicao.eq(atribuicao))
            .fetchFirst());
    }
}
