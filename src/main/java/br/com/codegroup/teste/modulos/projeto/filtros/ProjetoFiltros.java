package br.com.codegroup.teste.modulos.projeto.filtros;

import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.predicate.ProjetoPredicate;
import lombok.Data;

@Data
public class ProjetoFiltros {

    private String nome;
    private ESituacaoProjeto situacao;

    public ProjetoPredicate toPredicate() {
        return new ProjetoPredicate()
            .comNome(nome)
            .comSituacao(situacao)
            .excetoExcluidos();
    }
}
