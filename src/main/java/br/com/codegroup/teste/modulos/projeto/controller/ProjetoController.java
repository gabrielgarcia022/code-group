package br.com.codegroup.teste.modulos.projeto.controller;

import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.projeto.dto.*;
import br.com.codegroup.teste.modulos.projeto.filtros.ProjetoFiltros;
import br.com.codegroup.teste.modulos.projeto.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/projeto")
@Tag(name = "Projetos", description = "URLs para gerenciamento de projetos")
@SecurityRequirement(name = "bearerAuth")
public class ProjetoController {

    private final ProjetoService service;

    @GetMapping
    @Operation(summary = "Buscar projetos", description = "Retorna todos os projetos")
    public Page<ProjetoResponse> getAll(@ParameterObject ProjetoFiltros filtros, @ParameterObject PageRequest pageRequest) {
        return service.getAll(filtros, pageRequest);
    }

    @GetMapping("{projetoId}")
    @Operation(summary = "Detalhar projeto", description = "Exibe detalhes de um projeto específico")
    public ProjetoDetalharResponse detalhar(@PathVariable String projetoId) {
        return service.detalhar(projetoId);
    }

    @PostMapping
    @Operation(summary = "Salvar projeto", description = "Salva um novo projeto")
    public ProjetoResponse salvar(@RequestBody @Valid ProjetoRequest request) {
        return service.salvar(request);
    }

    @PutMapping
    @Operation(summary = "Editar projeto", description = "Edita um projeto existente")
    public ProjetoResponse editar(@RequestBody @Valid ProjetoRequest request) {
        return service.editar(request);
    }

    @PutMapping("atualizar-situacao")
    @Operation(summary = "Atualizar situação", description = "Atualiza a situação de um projeto existente")
    public ProjetoResponse atualizarSituacao(@RequestBody @Valid AtualizarSituacaoRequest request) {
        return service.atualizarSituacao(request);
    }

    @PutMapping("cancelar/{projetoId}")
    @Operation(summary = "Cancelar projeto", description = "Cancela um projeto existente")
    public ProjetoResponse cancelar(@PathVariable String projetoId) {
        return service.cancelar(projetoId);
    }

    @DeleteMapping("{projetoId}")
    @Operation(summary = "Excluir projeto", description = "Excluí um projeto existente")
    public ProjetoResponse excluir(@PathVariable String projetoId) {
        return service.excluir(projetoId);
    }

    @PutMapping("adicionar-membro")
    @Operation(summary = "Adicionar membro", description = "Adiciona um membro a um projeto em andamento")
    public ProjetoMembroResponse adicionarMembro(@RequestBody @Valid ProjetoMembroRequest request) {
        return service.adicionarMembro(request);
    }

    @PutMapping("remover-membro")
    @Operation(summary = "Remover membro", description = "Remove um membro de um projeto em andamento")
    public ProjetoMembroResponse removerMembro(@RequestBody @Valid ProjetoMembroRequest request) {
        return service.removerMembro(request);
    }

    @GetMapping("relatorio/pdf")
    @Operation(summary = "Gerar relatório PDF", description = "Gera relatório do portfólio de projetos em pdf")
    public void gerarRelatorioPortfolioPdf(HttpServletResponse response) {
        service.gerarRelatorioPortfolioPdf(response);
    }

    @GetMapping("relatorio/excel")
    @Operation(summary = "Gerar relatório EXCEL", description = "Gera relatório do portfólio de projetos em excel")
    public void gerarRelatorioPortfolioExcel(HttpServletResponse response) {
        service.gerarRelatorioPortfolioExcel(response);
    }
}
