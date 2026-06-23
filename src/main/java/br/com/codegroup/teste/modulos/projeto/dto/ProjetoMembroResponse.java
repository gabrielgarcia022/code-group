package br.com.codegroup.teste.modulos.projeto.dto;

import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.model.ProjetoMembro;
import org.thymeleaf.util.ListUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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

    private String membroId;
    private String membroNome;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataExclusao;
    private Boolean isResponsavel;
    private ESituacaoProjetoMembro situacao;

    public static ProjetoMembroResponse of(ProjetoMembro projetoMembro) {
        return ProjetoMembroResponse.builder()
            .membroId(Optional.ofNullable(projetoMembro.getMembro()).map(Membro::getId).orElse(null))
            .membroNome(Optional.ofNullable(projetoMembro.getMembro()).map(Membro::getNome).orElse(null))
            .dataCadastro(projetoMembro.getDataCadastro())
            .dataExclusao(projetoMembro.getDataExclusao())
            .isResponsavel(projetoMembro.getIsResponsavel())
            .situacao(projetoMembro.getSituacao())
            .build();
    }

    public static List<ProjetoMembroResponse> ofMembrosAtivos(List<ProjetoMembro> membros) {
        return !ListUtils.isEmpty(membros)
            ? membros.stream()
              .filter(membro -> Objects.equals(membro.getSituacao(), ESituacaoProjetoMembro.PARTICIPANTE))
              .map(ProjetoMembroResponse::of)
              .toList()
            : null;
    }

    public static List<ProjetoMembroResponse> ofMembrosAnteriores(List<ProjetoMembro> membros) {
        return !ListUtils.isEmpty(membros)
            ? membros.stream()
              .filter(membro -> Objects.equals(membro.getSituacao(), ESituacaoProjetoMembro.EXCLUIDO))
              .map(ProjetoMembroResponse::of)
              .toList()
            : null;
    }
}
