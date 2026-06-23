package br.com.codegroup.teste.modulos.membro.dto;

import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroResponse {

    private String id;
    private String nome;
    private EAtribuicao atribuicao;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataExclusao;
    private ESituacaoMembro situacao;

    public static MembroResponse of(Membro membro) {
        return MembroResponse.builder()
            .id(membro.getId())
            .nome(membro.getNome())
            .atribuicao(membro.getAtribuicao())
            .dataCadastro(membro.getDataCadastro())
            .build();
    }
}
