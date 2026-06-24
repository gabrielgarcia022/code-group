package br.com.codegroup.teste.modulos.autenticacao.service;

import br.com.codegroup.teste.modulos.autenticacao.dto.*;
import br.com.codegroup.teste.modulos.autenticacao.model.Usuario;
import br.com.codegroup.teste.modulos.autenticacao.repository.UsuarioRepository;
import br.com.codegroup.teste.modulos.comum.exception.NaoAutorizadoException;
import br.com.codegroup.teste.modulos.comum.exception.NotFoundException;
import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository repository;
    private final HttpServletRequest request;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AutenticacaoResponse login(LoginRequest request) {
        var usuario = repository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new NotFoundException("Usuário ou senha inválidos"));

        if (!encoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new ValidacaoException("Usuário ou senha inválidos");
        }

        var token = jwtService.gerarToken(usuario);

        return AutenticacaoResponse.of(usuario, token);
    }

    public UsuarioAutenticado checkToken() {
        try {
            var token = getTokenHeader();
            if (jwtService.tokenValido(token)) {
                var usuarioId = jwtService.extrairUsuarioId(token);
                var usuario = repository.findById(usuarioId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

                return UsuarioAutenticado.of(usuario);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new NaoAutorizadoException("Você não está autenticado");
        }
    }

    public UsuarioMockResponse salvarUsuario(@Valid UsuarioMockRequest request) {
        var senha = encoder.encode(request.getSenha());
        var usuario = Usuario.of(request, senha);

        return UsuarioMockResponse.of(repository.save(usuario), request.getSenha());
    }

    @SuppressWarnings({"checkstyle:MagicNumber"})
    private String getTokenHeader() {
        final var authHeader = request.getHeader("Authorization");
        if (!StringUtils.isEmpty(authHeader)) {
            return authHeader.substring(7);
        }
        return null;
    }
}
