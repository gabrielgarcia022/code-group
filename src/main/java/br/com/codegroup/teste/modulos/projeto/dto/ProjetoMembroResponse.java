package br.com.codegroup.teste.modulos.projeto.dto;

import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import br.com.codegroup.teste.modulos.projeto.model.ProjetoMembro;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoMembroResponse {

    private String projetoId;
    private String membroId;
    private String membroNome;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataExclusao;
    private Boolean isResponsavel;
    private ESituacaoProjetoMembro situacao;

    public static ProjetoMembroResponse of(ProjetoMembro projetoMembro) {
        return ProjetoMembroResponse.builder()
            .projetoId(Optional.ofNullable(projetoMembro.getProjeto()).map(Projeto::getId).orElse(null))
            .membroId(Optional.ofNullable(projetoMembro.getMembro()).map(Membro::getId).orElse(null))
            .membroNome(Optional.ofNullable(projetoMembro.getMembro()).map(Membro::getNome).orElse(null))
            .dataCadastro(projetoMembro.getDataCadastro())
            .dataExclusao(projetoMembro.getDataExclusao())
            .isResponsavel(projetoMembro.getIsResponsavel())
            .situacao(projetoMembro.getSituacao())
            .build();
    }
}
