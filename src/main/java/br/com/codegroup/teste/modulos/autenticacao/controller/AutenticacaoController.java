package br.com.codegroup.teste.modulos.autenticacao.controller;

import br.com.codegroup.teste.modulos.autenticacao.dto.*;
import br.com.codegroup.teste.modulos.autenticacao.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/autenticacao")
@Tag(name = "Autenticação", description = "URLs para autenticação do usuário")
public class AutenticacaoController {

    private final AutenticacaoService service;

    @PostMapping("login")
    @Operation(summary = "Relizar login", description = "Reliza o login do usuário")
    public AutenticacaoResponse login(@RequestBody @Valid LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("check-token")
    @Operation(summary = "Validar token", description = "Valida o token de autenticação do usuário")
    public UsuarioAutenticado checkToken() {
        return service.checkToken();
    }

    @PostMapping("mock/cadastrar")
    @Operation(summary = "Cadastrar usuário", description = "Cadastra um novo usuário no sistema")
    public UsuarioMockResponse salvarUsuario(@RequestBody @Valid UsuarioMockRequest request) {
        return service.salvarUsuario(request);
    }
}
