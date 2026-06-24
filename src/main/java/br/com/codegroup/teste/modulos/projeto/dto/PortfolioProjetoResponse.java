package br.com.codegroup.teste.modulos.projeto.dto;

import br.com.codegroup.teste.modulos.comum.utils.DateUtils;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import br.com.codegroup.teste.modulos.projeto.model.ProjetoMembro;
import org.thymeleaf.util.ListUtils;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static br.com.codegroup.teste.modulos.comum.utils.StringUtils.adicionarMascaraValor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioProjetoResponse {

    private String situacao;
    private Integer quantidade;
    private String valor;
    private Long membros;
    private String duracao;

    @SuppressWarnings({"checkstyle:MethodLength"})
    public static PortfolioProjetoResponse of(List<Projeto> projetos, ESituacaoProjeto situacao) {
        if (!ListUtils.isEmpty(projetos)) {
            var membros = projetos.stream()
                .flatMap(projeto -> Optional
                    .ofNullable(projeto.getMembros())
                    .orElse(List.of())
                    .stream())
                .filter(membro -> !Objects.equals(ESituacaoProjetoMembro.EXCLUIDO, membro.getSituacao()))
                .map(ProjetoMembro::getMembro)
                .map(Membro::getId)
                .distinct()
                .count();

            var duracao = Objects.equals(situacao, ESituacaoProjeto.ENCERRADO)
                ? projetos.stream()
                  .mapToLong(projeto -> ChronoUnit.DAYS.between(projeto.getDataInicio(), projeto.getDataRealTermino()))
                  .average()
                  .stream()
                  .mapToLong(Math::round)
                  .boxed()
                  .findFirst()
                  .map(DateUtils::getDuracaoFromDias)
                  .orElse(null)
                : null;

            return PortfolioProjetoResponse.builder()
                .situacao(situacao.getDescricao())
                .quantidade(projetos.size())
                .valor(adicionarMascaraValor(projetos.stream()
                    .map(Projeto::getOrcamentoTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .membros(membros)
                .duracao(duracao)
                .build();
        }

        return base(situacao);
    }

    private static PortfolioProjetoResponse base(ESituacaoProjeto situacao) {
        return PortfolioProjetoResponse.builder()
            .situacao(situacao.getDescricao())
            .quantidade(0)
            .valor("0,00")
            .membros(0L)
            .duracao(Objects.equals(situacao, ESituacaoProjeto.ENCERRADO) ? "SEM DADOS" : null)
            .build();
    }
}
