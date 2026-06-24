package br.com.codegroup.teste.modulos.autenticacao.controller;

import br.com.codegroup.teste.modulos.autenticacao.dto.*;
import br.com.codegroup.teste.modulos.autenticacao.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService service;

    @PostMapping("login")
    public AutenticacaoResponse login(@RequestBody @Valid LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("check-token")
    public UsuarioAutenticado checkToken() {
        return service.checkToken();
    }

    @PostMapping("mock/cadastrar")
    public UsuarioMockResponse salvarUsuario(@RequestBody @Valid UsuarioMockRequest request) {
        return service.salvarUsuario(request);
    }
}
