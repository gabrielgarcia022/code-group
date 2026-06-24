package br.com.codegroup.teste.modulos.autenticacao.controller;

import br.com.codegroup.teste.modulos.autenticacao.dto.LoginRequest;
import br.com.codegroup.teste.modulos.autenticacao.dto.UsuarioMockRequest;
import br.com.codegroup.teste.modulos.autenticacao.service.AutenticacaoService;
import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static br.com.codegroup.teste.modulos.autenticacao.AutenticacaoHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutenticacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AutenticacaoControllerTest {

    private static final String BASE_URL = "/api/autenticacao";
    private static final String USUARIO_ID = IdUtils.generateId();
    private static final String NOME = "GABRIEL GARCIA";
    private static final String EMAIL = "gabriel@email.com";
    private static final String SENHA = "123456";
    private static final String TOKEN = "token-jwt-teste";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AutenticacaoService service;

    @Test
    @DisplayName("Deve realizar login")
    void deveRealizarLogin() throws Exception {
        var request = loginRequest(EMAIL, SENHA);
        when(service.login(any(LoginRequest.class))).thenReturn(autenticacaoResponse(USUARIO_ID, NOME, TOKEN));

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(USUARIO_ID))
            .andExpect(jsonPath("$.nome").value(NOME))
            .andExpect(jsonPath("$.token").value(TOKEN));

        verify(service).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Deve retornar bad request ao realizar login sem e-mail")
    void deveRetornarBadRequestAoRealizarLoginSemEmail() throws Exception {
        var request = loginRequest("", SENHA);

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao realizar login com e-mail inválido")
    void deveRetornarBadRequestAoRealizarLoginComEmailInvalido() throws Exception {
        var request = loginRequest("email-invalido", SENHA);

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao realizar login sem senha")
    void deveRetornarBadRequestAoRealizarLoginSemSenha() throws Exception {
        var request = loginRequest(EMAIL, "");

        mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve validar token")
    void deveValidarToken() throws Exception {
        when(service.checkToken()).thenReturn(usuarioAutenticado(USUARIO_ID, NOME, EMAIL));

        mockMvc.perform(get(BASE_URL + "/check-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(USUARIO_ID))
            .andExpect(jsonPath("$.nome").value(NOME))
            .andExpect(jsonPath("$.email").value(EMAIL));

        verify(service).checkToken();
    }

    @Test
    @DisplayName("Deve cadastrar usuário mock")
    void deveCadastrarUsuarioMock() throws Exception {
        var request = usuarioMockRequest(NOME, EMAIL, SENHA);
        when(service.salvarUsuario(any(UsuarioMockRequest.class))).thenReturn(usuarioMockResponse(NOME, EMAIL, SENHA));

        mockMvc.perform(post(BASE_URL + "/mock/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value(NOME))
            .andExpect(jsonPath("$.email").value(EMAIL))
            .andExpect(jsonPath("$.senha").value(SENHA));

        verify(service).salvarUsuario(any(UsuarioMockRequest.class));
    }

    @Test
    @DisplayName("Deve retornar bad request ao cadastrar usuário mock sem nome")
    void deveRetornarBadRequestAoCadastrarUsuarioMockSemNome() throws Exception {
        var request = usuarioMockRequest("", EMAIL, SENHA);

        mockMvc.perform(post(BASE_URL + "/mock/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao cadastrar usuário mock sem e-mail")
    void deveRetornarBadRequestAoCadastrarUsuarioMockSemEmail() throws Exception {
        var request = usuarioMockRequest(NOME, "", SENHA);

        mockMvc.perform(post(BASE_URL + "/mock/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao cadastrar usuário mock com e-mail inválido")
    void deveRetornarBadRequestAoCadastrarUsuarioMockComEmailInvalido() throws Exception {
        var request = usuarioMockRequest(NOME, "email-invalido", SENHA);

        mockMvc.perform(post(BASE_URL + "/mock/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao cadastrar usuário mock sem senha")
    void deveRetornarBadRequestAoCadastrarUsuarioMockSemSenha() throws Exception {
        var request = usuarioMockRequest(NOME, EMAIL, "");

        mockMvc.perform(post(BASE_URL + "/mock/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
