package br.com.codegroup.teste.modulos.autenticacao.service;

import br.com.codegroup.teste.modulos.autenticacao.AutenticacaoHelper;
import br.com.codegroup.teste.modulos.autenticacao.model.Usuario;
import br.com.codegroup.teste.modulos.autenticacao.repository.UsuarioRepository;
import br.com.codegroup.teste.modulos.comum.exception.NaoAutorizadoException;
import br.com.codegroup.teste.modulos.comum.exception.NotFoundException;
import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AutenticacaoServiceTest {

    private static final String USUARIO_ID = IdUtils.generateId();
    private static final String NOME = "GABRIEL GARCIA";
    private static final String EMAIL = "gabriel@email.com";
    private static final String SENHA = "123456";
    private static final String SENHA_CRIPTOGRAFADA = "senha-criptografada";
    private static final String TOKEN = "token-jwt-teste";
    private static final String AUTHORIZATION_HEADER = "Bearer " + TOKEN;

    @Mock
    private UsuarioRepository repository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtService jwtService;
    private AutenticacaoService service;

    @BeforeEach
    void setUp() {
        service = new AutenticacaoService(repository, request, encoder, jwtService);
    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void deveRealizarLoginComSucesso() {
        var loginRequest = AutenticacaoHelper.loginRequest(EMAIL, SENHA);
        var usuario = AutenticacaoHelper.usuario(USUARIO_ID, NOME, EMAIL, SENHA_CRIPTOGRAFADA);

        when(repository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(usuario));
        when(encoder.matches(SENHA, SENHA_CRIPTOGRAFADA)).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn(TOKEN);

        var resultado = service.login(loginRequest);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(USUARIO_ID);
        assertThat(resultado.getNome()).isEqualTo(NOME);
        assertThat(resultado.getToken()).isEqualTo(TOKEN);
        verify(repository).findByEmailIgnoreCase(EMAIL);
        verify(encoder).matches(SENHA, SENHA_CRIPTOGRAFADA);
        verify(jwtService).gerarToken(usuario);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando usuário não existir no login")
    void deveLancarNotFoundExceptionQuandoUsuarioNaoExistirNoLogin() {
        var loginRequest = AutenticacaoHelper.loginRequest(EMAIL, SENHA);
        when(repository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(loginRequest))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Usuário ou senha inválidos");

        verify(repository).findByEmailIgnoreCase(EMAIL);
        verify(encoder, never()).matches(any(), any());
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("Deve lançar ValidacaoException quando senha for inválida")
    void deveLancarValidacaoExceptionQuandoSenhaForInvalida() {
        var loginRequest = AutenticacaoHelper.loginRequest(EMAIL, SENHA);
        var usuario = AutenticacaoHelper.usuario(USUARIO_ID, NOME, EMAIL, SENHA_CRIPTOGRAFADA);
        when(repository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(usuario));
        when(encoder.matches(SENHA, SENHA_CRIPTOGRAFADA)).thenReturn(false);

        assertThatThrownBy(() -> service.login(loginRequest))
            .isInstanceOf(ValidacaoException.class)
            .hasMessage("Usuário ou senha inválidos");

        verify(repository).findByEmailIgnoreCase(EMAIL);
        verify(encoder).matches(SENHA, SENHA_CRIPTOGRAFADA);
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    @DisplayName("Deve validar token e retornar usuário autenticado")
    void deveValidarTokenERetornarUsuarioAutenticado() {
        var usuario = AutenticacaoHelper.usuario(USUARIO_ID, NOME, EMAIL, SENHA);
        when(request.getHeader("Authorization")).thenReturn(AUTHORIZATION_HEADER);
        when(jwtService.tokenValido(TOKEN)).thenReturn(true);
        when(jwtService.extrairUsuarioId(TOKEN)).thenReturn(USUARIO_ID);
        when(repository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        var resultado = service.checkToken();
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(USUARIO_ID);
        assertThat(resultado.getEmail()).isEqualTo(EMAIL);

        verify(request).getHeader("Authorization");
        verify(jwtService).tokenValido(TOKEN);
        verify(jwtService).extrairUsuarioId(TOKEN);
        verify(repository).findById(USUARIO_ID);
    }

    @Test
    @DisplayName("Deve lançar NaoAutorizadoException quando header Authorization não existir")
    void deveLancarNaoAutorizadoExceptionQuandoHeaderAuthorizationNaoExistir() {
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThatThrownBy(() -> service.checkToken())
            .isInstanceOf(NaoAutorizadoException.class)
            .hasMessage("Você não está autenticado");

        verify(request).getHeader("Authorization");
        verify(jwtService).tokenValido(null);
        verify(jwtService, never()).extrairUsuarioId(any());
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar NaoAutorizadoException quando token for inválido")
    void deveLancarNaoAutorizadoExceptionQuandoTokenForInvalido() {
        when(request.getHeader("Authorization")).thenReturn(AUTHORIZATION_HEADER);
        when(jwtService.tokenValido(TOKEN)).thenReturn(false);

        assertThatThrownBy(() -> service.checkToken())
            .isInstanceOf(NaoAutorizadoException.class)
            .hasMessage("Você não está autenticado");

        verify(request).getHeader("Authorization");
        verify(jwtService).tokenValido(TOKEN);
        verify(jwtService, never()).extrairUsuarioId(any());
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar NaoAutorizadoException quando JwtService lançar erro")
    void deveLancarNaoAutorizadoExceptionQuandoJwtServiceLancarErro() {
        when(request.getHeader("Authorization")).thenReturn(AUTHORIZATION_HEADER);
        when(jwtService.tokenValido(TOKEN)).thenThrow(new ValidacaoException("Token inválido"));

        assertThatThrownBy(() -> service.checkToken())
            .isInstanceOf(NaoAutorizadoException.class)
            .hasMessage("Você não está autenticado");

        verify(request).getHeader("Authorization");
        verify(jwtService).tokenValido(TOKEN);
        verify(jwtService, never()).extrairUsuarioId(any());
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar NaoAutorizadoException quando usuário do token não existir")
    void deveLancarNaoAutorizadoExceptionQuandoUsuarioDoTokenNaoExistir() {
        when(request.getHeader("Authorization")).thenReturn(AUTHORIZATION_HEADER);
        when(jwtService.tokenValido(TOKEN)).thenReturn(true);
        when(jwtService.extrairUsuarioId(TOKEN)).thenReturn(USUARIO_ID);
        when(repository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkToken())
            .isInstanceOf(NaoAutorizadoException.class)
            .hasMessage("Você não está autenticado");

        verify(request).getHeader("Authorization");
        verify(jwtService).tokenValido(TOKEN);
        verify(jwtService).extrairUsuarioId(TOKEN);
        verify(repository).findById(USUARIO_ID);
    }

    @Test
    @DisplayName("Deve lançar NaoAutorizadoException quando Authorization tiver formato inválido")
    void deveLancarNaoAutorizadoExceptionQuandoAuthorizationTiverFormatoInvalido() {
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        assertThatThrownBy(() -> service.checkToken())
            .isInstanceOf(NaoAutorizadoException.class)
            .hasMessage("Você não está autenticado");

        verify(request).getHeader("Authorization");
        verify(jwtService, never()).tokenValido(any());
        verify(jwtService, never()).extrairUsuarioId(any());
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve salvar usuário mock")
    void deveSalvarUsuarioMock() {
        var usuarioMockRequest = AutenticacaoHelper.usuarioMockRequest(NOME, EMAIL, SENHA);
        when(encoder.encode(SENHA)).thenReturn(SENHA_CRIPTOGRAFADA);
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> {
            var usuario = invocation.<Usuario>getArgument(0);
            usuario.setId(USUARIO_ID);
            return usuario;
        });

        var resultado = service.salvarUsuario(usuarioMockRequest);
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo(NOME);
        assertThat(resultado.getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.getSenha()).isEqualTo(SENHA);

        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repository).save(captor.capture());
        var usuarioSalvo = captor.getValue();

        assertThat(usuarioSalvo.getNome()).isEqualTo(NOME);
        assertThat(usuarioSalvo.getEmail()).isEqualTo(EMAIL);
        assertThat(usuarioSalvo.getSenha()).isEqualTo(SENHA_CRIPTOGRAFADA);
        verify(encoder).encode(SENHA);
    }
}
