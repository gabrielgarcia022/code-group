package br.com.codegroup.teste.modulos.projeto.dto;

import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoResponse {

    private String id;

    public static ProjetoResponse of(Projeto projeto) {
        return ProjetoResponse.builder()
            .id(projeto.getId())
            .build();
    }
}
