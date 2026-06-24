package br.com.codegroup.teste.modulos.membro.filtros;

import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.predicate.MembroPredicate;
import lombok.Data;

@Data
public class MembroFiltros {

    private String nome;
    private EAtribuicao atribuicao;

    public MembroPredicate toPredicate() {
        return new MembroPredicate()
            .excetoExcluidos();
    }
}
