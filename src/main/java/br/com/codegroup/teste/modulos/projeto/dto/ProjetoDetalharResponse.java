package br.com.codegroup.teste.modulos.projeto.dto;

import br.com.codegroup.teste.modulos.projeto.enums.ERiscoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoDetalharResponse {

    private String id;
    private String nome;
    private String descricao;
    private LocalDateTime dataCadastro;
    private LocalDate dataInicio;
    private LocalDate previsaoTermino;
    private LocalDate dataRealTermino;
    private LocalDateTime dataExclusao;
    private LocalDateTime dataCancelamento;
    private BigDecimal orcamentoTotal;
    private ESituacaoProjeto situacao;
    private String risco;

    private List<ProjetoMembroResponse> membros;
    private List<ProjetoMembroResponse> membrosAnteriores;

    public static ProjetoDetalharResponse of(Projeto projeto) {
        return ProjetoDetalharResponse.builder()
            .id(projeto.getId())
            .nome(projeto.getNome())
            .descricao(projeto.getDescricao())
            .dataCadastro(projeto.getDataCadastro())
            .dataInicio(projeto.getDataInicio())
            .previsaoTermino(projeto.getPrevisaoTermino())
            .dataRealTermino(projeto.getDataRealTermino())
            .dataExclusao(projeto.getDataExclusao())
            .dataCancelamento(projeto.getDataCancelamento())
            .orcamentoTotal(projeto.getOrcamentoTotal())
            .situacao(projeto.getSituacao())
            .risco(Optional.ofNullable(projeto.getRisco()).map(ERiscoProjeto::getDescricao).orElse(null))
            .membros(ProjetoMembroResponse.ofMembrosAtivos(projeto.getMembros()))
            .membrosAnteriores(ProjetoMembroResponse.ofMembrosAnteriores(projeto.getMembros()))
            .build();
    }
}
