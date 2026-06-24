package br.com.codegroup.teste.modulos.autenticacao;

import br.com.codegroup.teste.modulos.autenticacao.dto.*;
import br.com.codegroup.teste.modulos.autenticacao.model.Usuario;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AutenticacaoHelper {

    public static LoginRequest loginRequest(String email, String senha) {
        var request = new LoginRequest();
        request.setEmail(email);
        request.setSenha(senha);

        return request;
    }

    public static UsuarioMockRequest usuarioMockRequest(String nome, String email, String senha) {
        var request = new UsuarioMockRequest();
        request.setNome(nome);
        request.setEmail(email);
        request.setSenha(senha);

        return request;
    }

    public static UsuarioMockResponse usuarioMockResponse(String nome, String email, String senha) {
        return UsuarioMockResponse.builder()
            .nome(nome)
            .email(email)
            .senha(senha)
            .build();
    }

    public static UsuarioAutenticado usuarioAutenticado(String usuarioId, String nome, String email) {
        return UsuarioAutenticado.builder()
            .id(usuarioId)
            .nome(nome)
            .email(email)
            .build();
    }

    public static AutenticacaoResponse autenticacaoResponse(String usuarioId, String nome, String token) {
        return AutenticacaoResponse.builder()
            .id(usuarioId)
            .nome(nome)
            .token(token)
            .build();
    }

    public static Usuario usuario(String usuarioId, String nome, String email, String senha) {
        return Usuario.builder()
            .id(usuarioId)
            .nome(nome)
            .email(email)
            .senha(senha)
            .build();
    }

    public static Jwt jwtValido(String token, String email, String usuarioId, String nome) {
        return Jwt.withTokenValue(token)
            .header("alg", "HS256")
            .subject(email)
            .claim("usuarioId", usuarioId)
            .claim("nome", nome)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plus(60, ChronoUnit.MINUTES))
            .build();
    }
}
