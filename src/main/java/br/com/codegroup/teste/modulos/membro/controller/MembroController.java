package br.com.codegroup.teste.modulos.membro.controller;

import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.membro.dto.MembroRequest;
import br.com.codegroup.teste.modulos.membro.dto.MembroResponse;
import br.com.codegroup.teste.modulos.membro.filtros.MembroFiltros;
import br.com.codegroup.teste.modulos.membro.service.MembroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/membro")
public class MembroController {

    private final MembroService service;

    @GetMapping
    public Page<MembroResponse> getAll(MembroFiltros filtros, PageRequest pageRequest) {
        return service.getAll(filtros, pageRequest);
    }

    @GetMapping("{membroId}")
    public MembroResponse detalhar(@PathVariable String membroId) {
        return service.detalhar(membroId);
    }

    @PostMapping
    public MembroResponse salvar(@RequestBody @Valid MembroRequest request) {
        return service.salvar(request);
    }

    @PutMapping
    public MembroResponse editar(@RequestBody @Valid MembroRequest request) {
        return service.editar(request);
    }

    @DeleteMapping("{membroId}")
    public MembroResponse excluir(@PathVariable String membroId) {
        return service.excluir(membroId);
    }

    @PutMapping("reativar/{membroId}")
    public MembroResponse reativar(@PathVariable String membroId) {
        return service.reativar(membroId);
    }
}
