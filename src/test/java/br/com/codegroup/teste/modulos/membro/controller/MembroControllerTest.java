package br.com.codegroup.teste.modulos.membro.controller;

import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.membro.MembroHelper;
import br.com.codegroup.teste.modulos.membro.dto.MembroRequest;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import br.com.codegroup.teste.modulos.membro.filtros.MembroFiltros;
import br.com.codegroup.teste.modulos.membro.service.MembroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MembroController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MembroControllerTest {

    private static final String BASE_URL = "/api/membro";
    private static final String MEMBRO_ID = "membro-123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private MembroService service;

    @Test
    @DisplayName("Deve buscar membros paginados")
    void deveBuscarMembrosPaginados() throws Exception {
        var response = MembroHelper.membroResponse(MEMBRO_ID, "GABRIEL GARCIA", EAtribuicao.GERENTE, ESituacaoMembro.ATIVO);
        var page = new PageImpl<>(List.of(response), new PageRequest(), 1);

        when(service.getAll(any(MembroFiltros.class), any(PageRequest.class)))
            .thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .param("nome", "Gabriel")
                .param("atribuicao", "GERENTE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].id").value(MEMBRO_ID))
            .andExpect(jsonPath("$.content[0].nome").value("GABRIEL GARCIA"))
            .andExpect(jsonPath("$.content[0].atribuicao").value("GERENTE"))
            .andExpect(jsonPath("$.content[0].situacao").value("ATIVO"));

        verify(service).getAll(any(MembroFiltros.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve detalhar membro")
    void deveDetalharMembro() throws Exception {
        var response = MembroHelper.membroResponse(MEMBRO_ID, "GABRIEL GARCIA", EAtribuicao.GERENTE,
            ESituacaoMembro.ATIVO);

        when(service.detalhar(MEMBRO_ID)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{membroId}", MEMBRO_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(MEMBRO_ID))
            .andExpect(jsonPath("$.nome").value("GABRIEL GARCIA"))
            .andExpect(jsonPath("$.atribuicao").value("GERENTE"))
            .andExpect(jsonPath("$.situacao").value("ATIVO"));

        verify(service).detalhar(MEMBRO_ID);
    }

    @Test
    @DisplayName("Deve salvar membro")
    void deveSalvarMembro() throws Exception {
        var request = MembroHelper.membroRequest(null, "GABRIEL GARCIA", EAtribuicao.GERENTE);
        var response = MembroHelper.membroResponse(MEMBRO_ID, "GABRIEL GARCIA", EAtribuicao.GERENTE, ESituacaoMembro.ATIVO);

        when(service.salvar(any(MembroRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(MEMBRO_ID))
            .andExpect(jsonPath("$.nome").value("GABRIEL GARCIA"))
            .andExpect(jsonPath("$.atribuicao").value("GERENTE"))
            .andExpect(jsonPath("$.situacao").value("ATIVO"));

        verify(service).salvar(any(MembroRequest.class));
    }

    @Test
    @DisplayName("Deve retornar bad request ao salvar membro sem nome")
    void deveRetornarBadRequestAoSalvarMembroSemNome() throws Exception {
        var request = MembroHelper.membroRequest(null, "", EAtribuicao.GERENTE);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve editar membro")
    void deveEditarMembro() throws Exception {
        var request = MembroHelper.membroRequest(MEMBRO_ID, "GABRIEL ATUALIZADO", EAtribuicao.FUNCIONARIO);
        var response = MembroHelper.membroResponse(MEMBRO_ID, "GABRIEL ATUALIZADO", EAtribuicao.FUNCIONARIO,
            ESituacaoMembro.ATIVO);

        when(service.editar(any(MembroRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(MEMBRO_ID))
            .andExpect(jsonPath("$.nome").value("GABRIEL ATUALIZADO"))
            .andExpect(jsonPath("$.atribuicao").value("FUNCIONARIO"))
            .andExpect(jsonPath("$.situacao").value("ATIVO"));

        verify(service).editar(any(MembroRequest.class));
    }

    @Test
    @DisplayName("Deve retornar bad request ao editar membro sem nome")
    void deveRetornarBadRequestAoEditarMembroSemNome() throws Exception {
        var request = MembroHelper.membroRequest(MEMBRO_ID, " ", EAtribuicao.FUNCIONARIO);

        mockMvc.perform(put(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve excluir membro")
    void deveExcluirMembro() throws Exception {
        var response = MembroHelper.membroResponse(MEMBRO_ID, "GABRIEL GARCIA", EAtribuicao.FUNCIONARIO,
            ESituacaoMembro.EXCLUIDO);
        when(service.excluir(MEMBRO_ID)).thenReturn(response);

        mockMvc.perform(delete(BASE_URL + "/{membroId}", MEMBRO_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(MEMBRO_ID))
            .andExpect(jsonPath("$.nome").value("GABRIEL GARCIA"))
            .andExpect(jsonPath("$.situacao").value("EXCLUIDO"));

        verify(service).excluir(MEMBRO_ID);
    }

    @Test
    @DisplayName("Deve reativar membro")
    void deveReativarMembro() throws Exception {
        var response = MembroHelper.membroResponse(MEMBRO_ID, "GABRIEL GARCIA", EAtribuicao.FUNCIONARIO, ESituacaoMembro.ATIVO);
        when(service.reativar(MEMBRO_ID)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/reativar/{membroId}", MEMBRO_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(MEMBRO_ID))
            .andExpect(jsonPath("$.nome").value("GABRIEL GARCIA"))
            .andExpect(jsonPath("$.situacao").value("ATIVO"));

        verify(service).reativar(MEMBRO_ID);
    }
}
