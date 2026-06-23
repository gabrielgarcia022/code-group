package br.com.codegroup.teste.modulos.projeto.controller;

import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.projeto.dto.*;
import br.com.codegroup.teste.modulos.projeto.filtros.ProjetoFiltros;
import br.com.codegroup.teste.modulos.projeto.service.ProjetoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/projeto")
public class ProjetoController {

    private final ProjetoService service;

    @GetMapping
    public Page<ProjetoResponse> getAll(ProjetoFiltros filtros, PageRequest pageRequest) {
        return service.getAll(filtros, pageRequest);
    }

    @GetMapping("{projetoId}")
    public ProjetoDetalharResponse detalhar(@PathVariable String projetoId) {
        return service.detalhar(projetoId);
    }

    @PostMapping
    public ProjetoResponse salvar(@RequestBody @Valid ProjetoRequest request) {
        return service.salvar(request);
    }

    @PutMapping
    public ProjetoResponse editar(@RequestBody @Valid ProjetoRequest request) {
        return service.editar(request);
    }

    @PutMapping("atualizar-situacao")
    public ProjetoResponse atualizarSituacao(@RequestBody @Valid AtualizarSituacaoRequest request) {
        return service.atualizarSituacao(request);
    }

    @PutMapping("cancelar/{projetoId}")
    public ProjetoResponse cancelar(@PathVariable String projetoId) {
        return service.cancelar(projetoId);
    }

    @DeleteMapping("{projetoId}")
    public ProjetoResponse excluir(@PathVariable String projetoId) {
        return service.excluir(projetoId);
    }

    @PutMapping("adicionar-membro")
    public ProjetoMembroResponse adicionarMembro(@RequestBody @Valid ProjetoMembroRequest request) {
        return service.adicionarMembro(request);
    }

    @PutMapping("remover-membro")
    public ProjetoMembroResponse removerMembro(@RequestBody @Valid ProjetoMembroRequest request) {
        return service.removerMembro(request);
    }

    @GetMapping("relatorio/pdf")
    public void gerarRelatorioPortfolioPdf(HttpServletResponse response) {
        // TODO
    }

    @GetMapping("relatorio/excel")
    public void gerarRelatorioPortfolioExcel(HttpServletResponse response) {
        // TODO
    }
}
