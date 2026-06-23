package br.com.codegroup.teste.modulos.membro.service;

import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.comum.exception.NotFoundException;
import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
import br.com.codegroup.teste.modulos.membro.dto.MembroRequest;
import br.com.codegroup.teste.modulos.membro.dto.MembroResponse;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import br.com.codegroup.teste.modulos.membro.filtros.MembroFiltros;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.membro.repository.MembroRepository;
import br.com.codegroup.teste.modulos.projeto.service.ProjetoService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembroService {

    private final ProjetoService projetoService;
    private final MembroRepository repository;

    public Page<MembroResponse> getAll(MembroFiltros filtros, PageRequest pageRequest) {
        return repository.findAll(filtros.toPredicate().build(), pageRequest).map(MembroResponse::of);
    }

    public MembroResponse detalhar(String membroId) {
        var membro = getById(membroId);

        return MembroResponse.of(membro);
    }

    @Transactional
    public MembroResponse salvar(MembroRequest request) {
        var membro = Membro.of(request);

        return MembroResponse.of(repository.save(membro));
    }

    @Transactional
    public MembroResponse editar(MembroRequest request) {
        var membro = getById(request.getId());
        validarEdicao(membro);
        membro.merge(request);

        return MembroResponse.of(repository.save(membro));
    }

    public void validarEdicao(Membro membro) {
        if (Objects.equals(membro.getSituacao(), ESituacaoMembro.EXCLUIDO)) {
            throw new ValidacaoException("Você não pode editar um membro excluido");
        }
    }

    @Transactional
    public MembroResponse excluir(String membroId) {
        var membro = getById(membroId);
        validarExclusao(membro);
        membro.excluir();
        projetoService.excluirMembro(membroId);

        return MembroResponse.of(repository.save(membro));
    }

    private void validarExclusao(Membro membro) {
        if (Objects.equals(membro.getSituacao(), ESituacaoMembro.EXCLUIDO)) {
            throw new ValidacaoException("Este membro já está excluído");
        }
    }

    @Transactional
    public MembroResponse reativar(String membroId) {
        var membro = getById(membroId);
        validarReativacao(membro);
        membro.reativar();

        return MembroResponse.of(repository.save(membro));
    }

    private void validarReativacao(Membro membro) {
        if (Objects.equals(membro.getSituacao(), ESituacaoMembro.ATIVO)) {
            throw new ValidacaoException("Este membro já está ativo");
        }
    }

    public Membro getById(String membroId) {
        return repository.findById(membroId)
            .orElseThrow(() -> new NotFoundException("Membro não encontrado"));
    }
}
