package br.com.codegroup.teste.modulos.projeto.dto;

import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import org.thymeleaf.util.ListUtils;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioRelatorioResponse {

    private List<PortfolioProjetoResponse> projetosSituacao;

    public static PortfolioRelatorioResponse of(List<Projeto> projetos) {
        return PortfolioRelatorioResponse.builder()
            .projetosSituacao(Stream.of(ESituacaoProjeto.values())
                .map(situacao -> PortfolioProjetoResponse.of(getProjetosBySituacao(projetos, situacao), situacao))
                .toList())
            .build();
    }

    private static List<Projeto> getProjetosBySituacao(List<Projeto> projetos, ESituacaoProjeto situacao) {
        return !ListUtils.isEmpty(projetos)
            ? projetos.stream().filter(projeto -> Objects.equals(projeto.getSituacao(), situacao)).toList()
            : List.of();
    }
}
