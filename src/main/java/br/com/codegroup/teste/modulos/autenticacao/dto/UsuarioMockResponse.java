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
public class UsuarioMockResponse {

    private String nome;
    private String email;
    private String senha;

    public static UsuarioMockResponse of(Usuario usuario, String senha) {
        return UsuarioMockResponse.builder()
            .nome(usuario.getNome())
            .email(usuario.getEmail())
            .senha(senha)
            .build();
    }
}
