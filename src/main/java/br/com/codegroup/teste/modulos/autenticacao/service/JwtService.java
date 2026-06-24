package br.com.codegroup.teste.modulos.autenticacao.service;

import br.com.codegroup.teste.modulos.autenticacao.model.Usuario;
import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${security.jwt.expiration-minutes}")
    private Long expirationMinutes;

    public String gerarToken(Usuario usuario) {
        var agora = Instant.now();
        var expiraEm = agora.plus(expirationMinutes, ChronoUnit.MINUTES);

        var claims = JwtClaimsSet.builder()
            .issuer("codegroup-teste-api")
            .issuedAt(agora)
            .expiresAt(expiraEm)
            .subject(usuario.getEmail())
            .claim("usuarioId", usuario.getId())
            .claim("nome", usuario.getNome())
            .build();

        var header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String extrairUsuarioId(String token) {
        var jwt = jwtDecoder.decode(token);

        return jwt.getClaimAsString("usuarioId");
    }

    public Boolean tokenValido(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            var usuarioId = jwt.getClaimAsString("usuarioId");
            var expiraEm = jwt.getExpiresAt();

            return !StringUtils.isEmpty(usuarioId) && Objects.nonNull(expiraEm) && Instant.now().isBefore(expiraEm);
        } catch (Exception ex) {
            throw new ValidacaoException("Token inválido");
        }
    }
}
