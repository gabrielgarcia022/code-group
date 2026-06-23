package br.com.codegroup.teste.modulos.membro.dto;

import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MembroRequest {

    private String id;
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    private EAtribuicao atribuicao;
}
