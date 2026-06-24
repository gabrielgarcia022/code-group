package br.com.codegroup.teste.modulos.autenticacao.dto;

import br.com.codegroup.teste.modulos.autenticacao.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutenticacaoResponse {

    private String id;
    private String nome;
    private String token;

    public static AutenticacaoResponse of(Usuario usuario, String token) {
        return AutenticacaoResponse.builder()
            .id(usuario.getId())
            .nome(usuario.getNome())
            .token(token)
            .build();
    }
}
