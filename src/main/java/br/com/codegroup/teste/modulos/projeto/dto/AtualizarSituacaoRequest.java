package br.com.codegroup.teste.modulos.projeto.dto;

import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarSituacaoRequest {

    @NotBlank(message = "O id do projeto é obrigatório")
    private String projetoId;
    @NotNull(message = "A situação é obrigatória")
    private ESituacaoProjeto situacao;
}
