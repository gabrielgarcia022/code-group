package br.com.codegroup.teste.modulos.membro.predicate;

import br.com.codegroup.teste.config.PredicateBase;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;

import static br.com.codegroup.teste.modulos.membro.model.QMembro.membro;

public class MembroPredicate extends PredicateBase {

    public MembroPredicate excetoExcluidos() {
        builder.and(membro.situacao.ne(ESituacaoMembro.EXCLUIDO));

        return this;
    }
}
