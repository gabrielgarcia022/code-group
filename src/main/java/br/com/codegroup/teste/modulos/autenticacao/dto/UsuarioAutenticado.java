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
public class UsuarioAutenticado {

    private String id;
    private String nome;
    private String email;

    public static UsuarioAutenticado of(Usuario usuario) {
        return UsuarioAutenticado.builder()
            .id(usuario.getId())
            .email(usuario.getEmail())
            .build();
    }
}
