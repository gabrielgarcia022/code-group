package br.com.codegroup.teste.modulos.membro.controller;

import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.membro.dto.MembroRequest;
import br.com.codegroup.teste.modulos.membro.dto.MembroResponse;
import br.com.codegroup.teste.modulos.membro.filtros.MembroFiltros;
import br.com.codegroup.teste.modulos.membro.service.MembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/membro")
@Tag(name = "Membros", description = "URLs para gerenciamento de membros")
@SecurityRequirement(name = "bearerAuth")
public class MembroController {

    private final MembroService service;

    @GetMapping
    @Operation(summary = "Buscar membros", description = "Retorna todos os membros")
    public Page<MembroResponse> getAll(@ParameterObject MembroFiltros filtros, @ParameterObject PageRequest pageRequest) {
        return service.getAll(filtros, pageRequest);
    }

    @GetMapping("{membroId}")
    @Operation(summary = "Detalhar membro", description = "Exibe detalhes de um membro específico")
    public MembroResponse detalhar(@PathVariable String membroId) {
        return service.detalhar(membroId);
    }

    @PostMapping
    @Operation(summary = "Salvar membro", description = "Cadastra um novo membro")
    public MembroResponse salvar(@RequestBody @Valid MembroRequest request) {
        return service.salvar(request);
    }

    @PutMapping
    @Operation(summary = "Editar membro", description = "Edita um membro existente")
    public MembroResponse editar(@RequestBody @Valid MembroRequest request) {
        return service.editar(request);
    }

    @DeleteMapping("{membroId}")
    @Operation(summary = "Excluir membro", description = "Exclui um membro existente")
    public MembroResponse excluir(@PathVariable String membroId) {
        return service.excluir(membroId);
    }

    @PutMapping("reativar/{membroId}")
    @Operation(summary = "Reativar membro", description = "Reativa um membro excluído")
    public MembroResponse reativar(@PathVariable String membroId) {
        return service.reativar(membroId);
    }
}
