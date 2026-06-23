package br.com.codegroup.teste.modulos.projeto.service;

import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.comum.exception.NotFoundException;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.membro.repository.MembroRepository;
import br.com.codegroup.teste.modulos.projeto.dto.*;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.filtros.ProjetoFiltros;
import br.com.codegroup.teste.modulos.projeto.model.Projeto;
import br.com.codegroup.teste.modulos.projeto.model.ProjetoMembro;
import br.com.codegroup.teste.modulos.projeto.repository.ProjetoMembroRepository;
import br.com.codegroup.teste.modulos.projeto.repository.ProjetoRepository;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ListUtils;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

import static br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto.*;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoMembroRepository projetoMembroRepository;
    private final MembroRepository membroRepository;
    private final ProjetoRepository repository;

    public Page<ProjetoResponse> getAll(ProjetoFiltros filtros, PageRequest pageRequest) {
        return repository.findAll(filtros.toPredicate().build(), pageRequest).map(ProjetoResponse::of);
    }

    @Transactional
    public ProjetoResponse salvar(ProjetoRequest request) {
        var projeto = Projeto.of(request);
        var gerente = getGerenteById(request.getGerenteId());
        var projetoMembro = ProjetoMembro.of(gerente, projeto);
        projetoMembro.setIsResponsavel(true);
        projetoMembroRepository.save(projetoMembro);

        return ProjetoResponse.of(repository.save(projeto));
    }

    @Transactional
    public ProjetoResponse editar(ProjetoRequest request) {
        var projeto = getById(request.getId());
        validarEdicao(projeto);
        projeto.merge(request);
        atualizarResponsavel(request);

        return ProjetoResponse.of(repository.save(projeto));
    }

    @Transactional
    private void atualizarResponsavel(ProjetoRequest request) {
        var responsavel = projetoMembroRepository.findByProjetoIdAndIsResponsavel(request.getId(), true).orElse(null);

        if (Objects.nonNull(responsavel)) {
            if (!Objects.equals(responsavel.getMembro().getId(), request.getGerenteId())) {
                var gerente = getGerenteById(request.getGerenteId());
                responsavel.setSituacao(ESituacaoProjetoMembro.EXCLUIDO);
                projetoMembroRepository.save(responsavel);
                var novoResponsavel = ProjetoMembro.of(gerente, new Projeto(request.getId()));
                novoResponsavel.setIsResponsavel(true);
                projetoMembroRepository.save(novoResponsavel);
            }
        } else {
            var gerente = getGerenteById(request.getGerenteId());
            var novoResponsavel = ProjetoMembro.of(gerente, new Projeto(request.getId()));
            novoResponsavel.setIsResponsavel(true);
            projetoMembroRepository.save(novoResponsavel);
        }
    }

    private void validarEdicao(Projeto projeto) {
        if (Objects.equals(projeto.getSituacao(), ESituacaoProjeto.CANCELADO)) {
            throw new ValidationException("Você não pode editar um projeto cancelado");
        } else if (Objects.equals(projeto.getSituacao(), ESituacaoProjeto.ENCERRADO)) {
            throw new ValidationException("Você não pode editar um projeto encerrado");
        }
    }

    public ProjetoMembroResponse adicionarMembro(ProjetoMembroRequest request) {
        var membro = getFuncionarioById(request.getMembroId());
        var projeto = getById(request.getProjetoId());
        validarAdicaoMembro(projeto, membro);
        var projetoMembro = ProjetoMembro.of(membro, projeto);

        return ProjetoMembroResponse.of(projetoMembroRepository.save(projetoMembro));
    }

    @SuppressWarnings({"checkstyle:MagicNumber"})
    private void validarAdicaoMembro(Projeto projeto, Membro membro) {
        if (List.of(ENCERRADO, EXCLUIDO, CANCELADO).contains(projeto.getSituacao())) {
            throw new ValidationException("Você não pode adicionar membros neste projeto");
        }

        var membrosProjeto = projetoMembroRepository.findByProjetoIdAndSituacaoNot(projeto.getId(),
            ESituacaoProjetoMembro.EXCLUIDO);
        if (membrosProjeto.size() >= 10) {
            throw new ValidationException("O projeto pode conter no máximo 10 membros");
        }

        var projetosMembro = projetoMembroRepository.findByMembroIdAndProjetoSituacaoNotIn(membro.getId(),
            List.of(ESituacaoProjeto.CANCELADO, ESituacaoProjeto.ENCERRADO));
        if (projetosMembro.size() >= 3) {
            throw new ValidationException("Este funcionário já está alocado em 3 projetos em andamento");
        }
    }

    public ProjetoMembroResponse removerMembro(ProjetoMembroRequest request) {
        var projeto = getById(request.getProjetoId());
        var membroProjeto = getMembroProjeto(request.getProjetoId(), request.getMembroId());
        validarExclusaoMembro(projeto);
        membroProjeto.excluir();

        return ProjetoMembroResponse.of(projetoMembroRepository.save(membroProjeto));
    }

    @SuppressWarnings({"checkstyle:MagicNumber"})
    private void validarExclusaoMembro(Projeto projeto) {
        if (List.of(ENCERRADO, CANCELADO, EXCLUIDO).contains(projeto.getSituacao())) {
            throw new ValidationException("Você não pode remover membros deste projeto");
        }

        var membrosProjeto = projetoMembroRepository.findByProjetoIdAndSituacaoNot(projeto.getId(),
            ESituacaoProjetoMembro.EXCLUIDO);

        if (!ListUtils.isEmpty(membrosProjeto) && membrosProjeto.size() == 1) {
            throw new ValidationException("O projeto precisa ter no mínimo 1 membro");
        }
    }

    public void excluirMembro(String membroId) {
        var projetosMembro = projetoMembroRepository.findByMembroIdAndSituacaoNot(membroId, ESituacaoProjetoMembro.EXCLUIDO);

        if (!ListUtils.isEmpty(projetosMembro)) {
            projetosMembro.forEach(projetoMembro -> {
                projetoMembro.excluir();
                projetoMembroRepository.save(projetoMembro);
            });
        }
    }

    public ProjetoResponse atualizarSituacao(AtualizarSituacaoRequest request) {
        var projeto = getById(request.getProjetoId());
        validarAtualizacaoSituacao(projeto, request.getSituacao());
        projeto.atualizarSituacao(request.getSituacao());

        return ProjetoResponse.of(repository.save(projeto));
    }

    private void validarAtualizacaoSituacao(Projeto projeto, ESituacaoProjeto situacao) {
        if (Objects.equals(projeto.getSituacao(), ESituacaoProjeto.ENCERRADO)) {
            throw new ValidationException("Este projeto já foi encerrado");
        } else if (Objects.equals(projeto.getSituacao(), ESituacaoProjeto.CANCELADO)) {
            throw new ValidationException("Este projeto foi cancelado");
        } else if (Objects.equals(projeto.getSituacao(), ESituacaoProjeto.EXCLUIDO)) {
            throw new ValidationException("Este projeto foi excluído");
        } else if (!Objects.equals(projeto.getSituacao(), situacao.getSituacaoAnterior())) {
            throw new ValidationException("A situação não esta na sequência correta");
        }
    }

    public ProjetoResponse cancelar(String projetoId) {
        var projeto = getById(projetoId);
        validarCancelamento(projeto);
        projeto.cancelar();

        return ProjetoResponse.of(repository.save(projeto));
    }

    private void validarCancelamento(Projeto projeto) {
        if (List.of(ENCERRADO, CANCELADO).contains(projeto.getSituacao())) {
            throw new ValidationException("Você não pode cancelar este projeto");
        }
    }

    public ProjetoResponse excluir(String projetoId) {
        var projeto = getById(projetoId);
        validarExclusao(projeto);
        projeto.excluir();

        return ProjetoResponse.of(repository.save(projeto));
    }

    private void validarExclusao(Projeto projeto) {
        if (List.of(INICIADO, EM_ANDAMENTO, ENCERRADO).contains(projeto.getSituacao())) {
            throw new ValidationException("Você não pode excluir este projeto");
        }
    }

    private Projeto getById(String projetoId) {
        return repository.findById(projetoId)
            .orElseThrow(() -> new NotFoundException("Projeto não encontrado"));
    }

    private Membro getGerenteById(String membroId) {
        return membroRepository.findGerenteById(membroId)
            .orElseThrow(() -> new NotFoundException("Gerente não encontrado"));
    }

    private Membro getFuncionarioById(String membroId) {
        return membroRepository.findGerenteById(membroId)
            .orElseThrow(() -> new NotFoundException("Funcionário não encontrado"));
    }

    private ProjetoMembro getMembroProjeto(String projetoId, String membroId) {
        return projetoMembroRepository.findByProjetoIdAndMembroIdAndSituacao(projetoId, membroId,
            ESituacaoProjetoMembro.PARTICIPANTE).orElseThrow(() -> new NotFoundException("Membro do projeto não encontrado"));
    }
}
