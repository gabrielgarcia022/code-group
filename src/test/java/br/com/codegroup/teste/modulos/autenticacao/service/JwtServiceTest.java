package br.com.codegroup.teste.modulos.autenticacao.service;

import br.com.codegroup.teste.modulos.autenticacao.AutenticacaoHelper;
import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private static final String USUARIO_ID = IdUtils.generateId();
    private static final String NOME = "GABRIEL GARCIA";
    private static final String EMAIL = "gabriel@email.com";
    private static final String TOKEN = "token-jwt-teste";

    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private JwtDecoder jwtDecoder;
    private JwtService service;

    @BeforeEach
    void setUp() {
        service = new JwtService(jwtEncoder, jwtDecoder);

        ReflectionTestUtils.setField(service, "expirationMinutes", 60L);
    }

    @Test
    @DisplayName("Deve gerar token JWT")
    void deveGerarTokenJwt() {
        var usuario = AutenticacaoHelper.usuario(USUARIO_ID, NOME, EMAIL, "123456");
        var jwtGerado = AutenticacaoHelper.jwtValido(TOKEN, EMAIL, USUARIO_ID, NOME);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwtGerado);
        var resultado = service.gerarToken(usuario);
        assertThat(resultado).isEqualTo(TOKEN);
        var captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        var parametros = captor.getValue();
        var claims = parametros.getClaims();
        var headers = parametros.getJwsHeader();
        var issuer = claims.getClaim("iss");
        var usuarioId = claims.getClaim("usuarioId");
        var nome = claims.getClaim("nome");

        assertThat(headers.getAlgorithm()).isEqualTo(MacAlgorithm.HS256);
        assertThat(issuer).isEqualTo("codegroup-teste-api");
        assertThat(claims.getSubject()).isEqualTo(EMAIL);
        assertThat(usuarioId).isEqualTo(USUARIO_ID);
        assertThat(nome).isEqualTo(NOME);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiresAt()).isNotNull();
        assertThat(claims.getExpiresAt()).isAfter(claims.getIssuedAt());
        assertThat(ChronoUnit.MINUTES.between(claims.getIssuedAt(), claims.getExpiresAt())).isEqualTo(60);
    }

    @Test
    @DisplayName("Deve extrair usuarioId do token")
    void deveExtrairUsuarioIdDoToken() {
        var jwt = AutenticacaoHelper.jwtValido(TOKEN, EMAIL, USUARIO_ID, NOME);

        when(jwtDecoder.decode(TOKEN)).thenReturn(jwt);
        var resultado = service.extrairUsuarioId(TOKEN);
        assertThat(resultado).isEqualTo(USUARIO_ID);
        verify(jwtDecoder).decode(TOKEN);
    }

    @Test
    @DisplayName("Deve retornar true quando token for válido")
    void deveRetornarTrueQuandoTokenForValido() {
        var jwt = AutenticacaoHelper.jwtValido(TOKEN, EMAIL, USUARIO_ID, NOME);

        when(jwtDecoder.decode(TOKEN)).thenReturn(jwt);
        var resultado = service.tokenValido(TOKEN);
        assertThat(resultado).isTrue();
        verify(jwtDecoder).decode(TOKEN);
    }

    @Test
    @DisplayName("Deve retornar false quando token tiver usuarioId null")
    void deveRetornarFalseQuandoTokenTiverUsuarioIdNull() {
        var jwt = AutenticacaoHelper.jwtValido(TOKEN, EMAIL, null, NOME);

        when(jwtDecoder.decode(TOKEN)).thenReturn(jwt);
        var resultado = service.tokenValido(TOKEN);
        assertThat(resultado).isFalse();
        verify(jwtDecoder).decode(TOKEN);
    }

    @Test
    @DisplayName("Deve retornar false quando token não tiver data de expiração")
    void deveRetornarFalseQuandoTokenNaoTiverDataExpiracao() {
        var jwt = Jwt.withTokenValue(TOKEN)
            .header("alg", "HS256")
            .subject(EMAIL)
            .claim("usuarioId", USUARIO_ID)
            .issuedAt(Instant.now())
            .build();

        when(jwtDecoder.decode(TOKEN)).thenReturn(jwt);
        var resultado = service.tokenValido(TOKEN);
        assertThat(resultado).isFalse();
        verify(jwtDecoder).decode(TOKEN);
    }

    @Test
    @DisplayName("Deve retornar false quando token estiver expirado")
    void deveRetornarFalseQuandoTokenEstiverExpirado() {
        var jwt = Jwt.withTokenValue(TOKEN)
            .header("alg", "HS256")
            .subject(EMAIL)
            .claim("usuarioId", USUARIO_ID)
            .issuedAt(Instant.now().minus(2, ChronoUnit.HOURS))
            .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .build();

        when(jwtDecoder.decode(TOKEN)).thenReturn(jwt);
        var resultado = service.tokenValido(TOKEN);
        assertThat(resultado).isFalse();
        verify(jwtDecoder).decode(TOKEN);
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando decoder lançar erro ao validar token")
    void deveLancarValidacaoExceptionQuandoDecoderLancarErroAoValidarToken() {
        when(jwtDecoder.decode(TOKEN)).thenThrow(new RuntimeException("token inválido"));

        assertThatThrownBy(() -> service.tokenValido(TOKEN))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Token inválido");

        verify(jwtDecoder).decode(TOKEN);
    }
}
