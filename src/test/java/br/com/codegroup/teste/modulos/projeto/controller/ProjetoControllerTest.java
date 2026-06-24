package br.com.codegroup.teste.modulos.projeto.controller;

import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import br.com.codegroup.teste.modulos.projeto.ProjetoHelper;
import br.com.codegroup.teste.modulos.projeto.dto.AtualizarSituacaoRequest;
import br.com.codegroup.teste.modulos.projeto.dto.ProjetoMembroRequest;
import br.com.codegroup.teste.modulos.projeto.dto.ProjetoRequest;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.filtros.ProjetoFiltros;
import br.com.codegroup.teste.modulos.projeto.service.ProjetoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjetoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProjetoControllerTest {

    private static final String BASE_URL = "/api/projeto";
    private static final String PROJETO_ID = "projeto-123";
    private static final String MEMBRO_ID = IdUtils.generateId();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ProjetoService service;

    @Test
    @DisplayName("Deve buscar projetos paginados")
    void deveBuscarProjetosPaginados() throws Exception {
        var response = ProjetoHelper.projetoResponse(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        var page = new PageImpl<>(List.of(response), new PageRequest(), 1);
        when(service.getAll(any(ProjetoFiltros.class), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .param("nome", "PROJETO")
                .param("situacao", "EM_ANALISE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].id").value(PROJETO_ID))
            .andExpect(jsonPath("$.content[0].nome").value("PROJETO TESTE"))
            .andExpect(jsonPath("$.content[0].situacao").value("EM_ANALISE"))
            .andExpect(jsonPath("$.content[0].risco").value("BAIXO RISCO"));

        verify(service).getAll(any(ProjetoFiltros.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("Deve detalhar projeto")
    void deveDetalharProjeto() throws Exception {
        var response = ProjetoHelper.projetoDetalharResponse(PROJETO_ID, MEMBRO_ID);
        when(service.detalhar(PROJETO_ID)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{projetoId}", PROJETO_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(PROJETO_ID))
            .andExpect(jsonPath("$.nome").value("PROJETO TESTE"))
            .andExpect(jsonPath("$.situacao").value("EM_ANALISE"))
            .andExpect(jsonPath("$.membros", hasSize(1)))
            .andExpect(jsonPath("$.membros[0].membroId").value(MEMBRO_ID))
            .andExpect(jsonPath("$.membros[0].membroNome").value("GABRIEL GARCIA"))
            .andExpect(jsonPath("$.membros[0].situacao").value("PARTICIPANTE"))
            .andExpect(jsonPath("$.membrosAnteriores", hasSize(1)))
            .andExpect(jsonPath("$.membrosAnteriores[0].situacao").value("EXCLUIDO"));

        verify(service).detalhar(PROJETO_ID);
    }

    @Test
    @DisplayName("Deve salvar projeto")
    void deveSalvarProjeto() throws Exception {
        var request = ProjetoHelper.projetoRequest(null);
        var response = ProjetoHelper.projetoResponse(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EM_ANALISE);
        when(service.salvar(any(ProjetoRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(PROJETO_ID))
            .andExpect(jsonPath("$.nome").value("PROJETO TESTE"))
            .andExpect(jsonPath("$.situacao").value("EM_ANALISE"))
            .andExpect(jsonPath("$.risco").value("BAIXO RISCO"));

        verify(service).salvar(any(ProjetoRequest.class));
    }

    @Test
    @DisplayName("Deve retornar bad request ao salvar projeto sem nome")
    void deveRetornarBadRequestAoSalvarProjetoSemNome() throws Exception {
        var request = ProjetoHelper.projetoRequest(null);
        request.setNome("");

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao salvar projeto sem previsão de término")
    void deveRetornarBadRequestAoSalvarProjetoSemPrevisaoTermino() throws Exception {
        var request = ProjetoHelper.projetoRequest(null);
        request.setPrevisaoTermino(null);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao salvar projeto sem orçamento total")
    void deveRetornarBadRequestAoSalvarProjetoSemOrcamentoTotal() throws Exception {
        var request = ProjetoHelper.projetoRequest(null);
        request.setOrcamentoTotal(null);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao salvar projeto sem descrição")
    void deveRetornarBadRequestAoSalvarProjetoSemDescricao() throws Exception {
        var request = ProjetoHelper.projetoRequest(null);
        request.setDescricao(" ");

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao salvar projeto sem gerente")
    void deveRetornarBadRequestAoSalvarProjetoSemGerente() throws Exception {
        var request = ProjetoHelper.projetoRequest(null);
        request.setGerenteId(null);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve editar projeto")
    void deveEditarProjeto() throws Exception {
        var request = ProjetoHelper.projetoRequest(PROJETO_ID);
        request.setNome("PROJETO ATUALIZADO");
        var response = ProjetoHelper.projetoResponse(PROJETO_ID, "PROJETO ATUALIZADO", ESituacaoProjeto.EM_ANALISE);
        when(service.editar(any(ProjetoRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(PROJETO_ID))
            .andExpect(jsonPath("$.nome").value("PROJETO ATUALIZADO"))
            .andExpect(jsonPath("$.situacao").value("EM_ANALISE"));

        verify(service).editar(any(ProjetoRequest.class));
    }

    @Test
    @DisplayName("Deve retornar bad request ao editar projeto sem nome")
    void deveRetornarBadRequestAoEditarProjetoSemNome() throws Exception {
        var request = ProjetoHelper.projetoRequest(PROJETO_ID);
        request.setNome("");

        mockMvc.perform(put(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve atualizar situação do projeto")
    void deveAtualizarSituacaoDoProjeto() throws Exception {
        var request = ProjetoHelper.atualizarSituacaoRequest(PROJETO_ID, ESituacaoProjeto.INICIADO);
        var response = ProjetoHelper.projetoResponse(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.INICIADO);

        when(service.atualizarSituacao(any(AtualizarSituacaoRequest.class)))
            .thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/atualizar-situacao")
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(PROJETO_ID))
            .andExpect(jsonPath("$.situacao").value("INICIADO"));

        verify(service).atualizarSituacao(any(AtualizarSituacaoRequest.class));
    }

    @Test
    @DisplayName("Deve retornar bad request ao atualizar situação sem projetoId")
    void deveRetornarBadRequestAoAtualizarSituacaoSemProjetoId() throws Exception {
        var request = ProjetoHelper.atualizarSituacaoRequest(null, ESituacaoProjeto.INICIADO);

        mockMvc.perform(put(BASE_URL + "/atualizar-situacao")
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao atualizar situação sem situação")
    void deveRetornarBadRequestAoAtualizarSituacaoSemSituacao() throws Exception {
        var request = ProjetoHelper.atualizarSituacaoRequest(PROJETO_ID, null);

        mockMvc.perform(put(BASE_URL + "/atualizar-situacao")
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve cancelar projeto")
    void deveCancelarProjeto() throws Exception {
        var response = ProjetoHelper.projetoResponse(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.CANCELADO);

        when(service.cancelar(PROJETO_ID)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/cancelar/{projetoId}", PROJETO_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(PROJETO_ID))
            .andExpect(jsonPath("$.situacao").value("CANCELADO"));

        verify(service).cancelar(PROJETO_ID);
    }

    @Test
    @DisplayName("Deve excluir projeto")
    void deveExcluirProjeto() throws Exception {
        var response = ProjetoHelper.projetoResponse(PROJETO_ID, "PROJETO TESTE", ESituacaoProjeto.EXCLUIDO);

        when(service.excluir(PROJETO_ID)).thenReturn(response);

        mockMvc.perform(delete(BASE_URL + "/{projetoId}", PROJETO_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(PROJETO_ID))
            .andExpect(jsonPath("$.situacao").value("EXCLUIDO"));

        verify(service).excluir(PROJETO_ID);
    }

    @Test
    @DisplayName("Deve adicionar membro ao projeto")
    void deveAdicionarMembroAoProjeto() throws Exception {
        var request = ProjetoHelper.projetoMembroRequest(PROJETO_ID, MEMBRO_ID);
        var response = ProjetoHelper.projetoMembroResponse(MEMBRO_ID, ESituacaoProjetoMembro.PARTICIPANTE);

        when(service.adicionarMembro(any(ProjetoMembroRequest.class)))
            .thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/adicionar-membro")
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.membroId").value(MEMBRO_ID))
            .andExpect(jsonPath("$.membroNome").value("GABRIEL GARCIA"))
            .andExpect(jsonPath("$.situacao").value("PARTICIPANTE"));

        verify(service).adicionarMembro(any(ProjetoMembroRequest.class));
    }

    @Test
    @DisplayName("Deve retornar bad request ao adicionar membro sem projetoId")
    void deveRetornarBadRequestAoAdicionarMembroSemProjetoId() throws Exception {
        var request = ProjetoHelper.projetoMembroRequest(null, MEMBRO_ID);

        mockMvc.perform(put(BASE_URL + "/adicionar-membro")
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao adicionar membro sem membroId")
    void deveRetornarBadRequestAoAdicionarMembroSemMembroId() throws Exception {
        var request = ProjetoHelper.projetoMembroRequest(PROJETO_ID, "");

        mockMvc.perform(put(BASE_URL + "/adicionar-membro")
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve remover membro do projeto")
    void deveRemoverMembroDoProjeto() throws Exception {
        var request = ProjetoHelper.projetoMembroRequest(PROJETO_ID, MEMBRO_ID);
        var response = ProjetoHelper.projetoMembroResponse(MEMBRO_ID, ESituacaoProjetoMembro.EXCLUIDO);

        when(service.removerMembro(any(ProjetoMembroRequest.class)))
            .thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/remover-membro")
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.membroId").value(MEMBRO_ID))
            .andExpect(jsonPath("$.situacao").value("EXCLUIDO"));

        verify(service).removerMembro(any(ProjetoMembroRequest.class));
    }

    @Test
    @DisplayName("Deve retornar bad request ao remover membro sem projetoId")
    void deveRetornarBadRequestAoRemoverMembroSemProjetoId() throws Exception {
        var request = ProjetoHelper.projetoMembroRequest(" ", MEMBRO_ID);

        mockMvc.perform(put(BASE_URL + "/remover-membro")
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar bad request ao remover membro sem membroId")
    void deveRetornarBadRequestAoRemoverMembroSemMembroId() throws Exception {
        var request = ProjetoHelper.projetoMembroRequest(PROJETO_ID, null);

        mockMvc.perform(put(BASE_URL + "/remover-membro")
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve gerar relatório PDF")
    void deveGerarRelatorioPdf() throws Exception {
        doNothing().when(service).gerarRelatorioPortfolioPdf(any(HttpServletResponse.class));

        mockMvc.perform(get(BASE_URL + "/relatorio/pdf"))
            .andExpect(status().isOk());

        verify(service).gerarRelatorioPortfolioPdf(any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("Deve gerar relatório Excel")
    void deveGerarRelatorioExcel() throws Exception {
        doNothing().when(service).gerarRelatorioPortfolioExcel(any(HttpServletResponse.class));

        mockMvc.perform(get(BASE_URL + "/relatorio/excel"))
            .andExpect(status().isOk());

        verify(service).gerarRelatorioPortfolioExcel(any(HttpServletResponse.class));
    }
}
