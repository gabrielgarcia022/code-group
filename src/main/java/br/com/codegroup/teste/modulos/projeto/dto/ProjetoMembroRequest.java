package br.com.codegroup.teste.modulos.projeto.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjetoMembroRequest {

    @NotBlank(message = "O id do projeto é obrigatório")
    private String projetoId;
    @NotBlank(message = "O id do membro é obrigatório")
    private String membroId;
}
