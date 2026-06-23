package br.com.codegroup.teste.modulos.projeto.predicate;

import br.com.codegroup.teste.config.PredicateBase;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;

import static br.com.codegroup.teste.modulos.projeto.model.QProjeto.projeto;

public class ProjetoPredicate extends PredicateBase {

    public ProjetoPredicate comNome(String nome) {
        if (!isEmpty(nome)) {
            builder.and(projeto.nome.like("%" + nome + "%"));
        }

        return this;
    }

    public ProjetoPredicate comSituacao(ESituacaoProjeto situacao) {
        if (!isEmpty(situacao)) {
            builder.and(projeto.situacao.eq(situacao));
        }

        return this;
    }
}
