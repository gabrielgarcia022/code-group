package br.com.codegroup.teste.modulos.projeto.service;

import br.com.codegroup.teste.config.FilesUtils;
import br.com.codegroup.teste.config.PageRequest;
import br.com.codegroup.teste.modulos.comum.exception.NotFoundException;
import br.com.codegroup.teste.modulos.comum.exception.ValidacaoException;
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
import jakarta.servlet.http.HttpServletResponse;
import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ListUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import static br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto.*;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoMembroRepository projetoMembroRepository;
    private final MembroRepository membroRepository;
    private final ProjetoRepository repository;
    private final FilesUtils filesUtils;

    public Page<ProjetoResponse> getAll(ProjetoFiltros filtros, PageRequest pageRequest) {
        return repository.findAll(filtros.toPredicate().build(), pageRequest).map(ProjetoResponse::of);
    }

    public ProjetoDetalharResponse detalhar(String projetoId) {
        var projeto = getCompleteById(projetoId);

        return ProjetoDetalharResponse.of(projeto);
    }

    @Transactional
    public ProjetoResponse salvar(ProjetoRequest request) {
        var projeto = Projeto.of(request);
        var gerente = getGerenteById(request.getGerenteId());
        validarCadastro(gerente);
        var projetoMembro = ProjetoMembro.of(gerente, projeto);
        projetoMembro.setIsResponsavel(true);
        projetoMembroRepository.save(projetoMembro);

        return ProjetoResponse.of(repository.save(projeto));
    }

    @SuppressWarnings({"checkstyle:MagicNumber"})
    private void validarCadastro(Membro gerente) {
        var projetos = projetoMembroRepository.findByMembroIdAndProjetoSituacaoNotIn(gerente.getId(),
            List.of(ENCERRADO, CANCELADO, EXCLUIDO));

        if (projetos.size() >= 3) {
            throw new ValidacaoException("Este gerente já esta alocado em 3 projetos em andamento");
        }
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
            throw new ValidacaoException("Você não pode editar um projeto cancelado");
        } else if (Objects.equals(projeto.getSituacao(), ESituacaoProjeto.ENCERRADO)) {
            throw new ValidacaoException("Você não pode editar um projeto encerrado");
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
            throw new ValidacaoException("Você não pode adicionar membros neste projeto");
        }

        var membrosProjeto = projetoMembroRepository.findByProjetoIdAndSituacaoNot(projeto.getId(),
            ESituacaoProjetoMembro.EXCLUIDO);
        if (membrosProjeto.size() >= 10) {
            throw new ValidacaoException("O projeto pode conter no máximo 10 membros");
        }

        var projetosMembro = projetoMembroRepository.findByMembroIdAndProjetoSituacaoNotIn(membro.getId(),
            List.of(ESituacaoProjeto.CANCELADO, ESituacaoProjeto.ENCERRADO));
        if (projetosMembro.size() >= 3) {
            throw new ValidacaoException("Este funcionário já está alocado em 3 projetos em andamento");
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
            throw new ValidacaoException("Você não pode remover membros deste projeto");
        }

        var membrosProjeto = projetoMembroRepository.findByProjetoIdAndSituacaoNot(projeto.getId(),
            ESituacaoProjetoMembro.EXCLUIDO);

        if (!ListUtils.isEmpty(membrosProjeto) && membrosProjeto.size() == 1) {
            throw new ValidacaoException("O projeto precisa ter no mínimo 1 membro");
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
            throw new ValidacaoException("Este projeto já foi encerrado");
        } else if (Objects.equals(projeto.getSituacao(), ESituacaoProjeto.CANCELADO)) {
            throw new ValidacaoException("Este projeto foi cancelado");
        } else if (Objects.equals(projeto.getSituacao(), ESituacaoProjeto.EXCLUIDO)) {
            throw new ValidacaoException("Este projeto foi excluído");
        } else {
            if (!Objects.equals(situacao, CANCELADO) && !Objects.equals(situacao, EXCLUIDO)) {
                if (!Objects.equals(projeto.getSituacao(), situacao.getSituacaoAnterior())) {
                    throw new ValidacaoException("A situação não esta na sequência correta");
                }
            } else if (Objects.equals(situacao, EXCLUIDO)) {
                if (List.of(INICIADO, EM_ANDAMENTO).contains(projeto.getSituacao())) {
                    throw new ValidacaoException("Este projeto não pode ser excluído");
                }
            }
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
            throw new ValidacaoException("Você não pode cancelar este projeto");
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
            throw new ValidacaoException("Você não pode excluir este projeto");
        }
    }

    private Projeto getById(String projetoId) {
        return repository.findById(projetoId)
            .orElseThrow(() -> new NotFoundException("Projeto não encontrado"));
    }

    private Projeto getCompleteById(String projetoId) {
        return repository.findCompleteById(projetoId)
            .orElseThrow(() -> new NotFoundException("Projeto não encontrado"));
    }

    private Membro getGerenteById(String membroId) {
        return membroRepository.findGerenteById(membroId)
            .orElseThrow(() -> new NotFoundException("Gerente não encontrado"));
    }

    private Membro getFuncionarioById(String membroId) {
        return membroRepository.findFuncionarioById(membroId)
            .orElseThrow(() -> new NotFoundException("Funcionário não encontrado"));
    }

    private ProjetoMembro getMembroProjeto(String projetoId, String membroId) {
        return projetoMembroRepository.findByProjetoIdAndMembroIdAndSituacao(projetoId, membroId,
            ESituacaoProjetoMembro.PARTICIPANTE).orElseThrow(() -> new NotFoundException("Membro do projeto não encontrado"));
    }

    private PortfolioRelatorioResponse getPortfolio() {
        return PortfolioRelatorioResponse.of(repository.findAllComplete());
    }

    @SneakyThrows
    public void gerarRelatorioPortfolioPdf(HttpServletResponse response) {
        var portfolio = getPortfolio();
        var html = getHtmlPortfolio(portfolio);
        var pdf = filesUtils.htmlToPdfStream(html);

        filesUtils.baixarArquivo(response, pdf, "portfolio.pdf", "application/pdf");
    }

    private String getHtmlPortfolio(PortfolioRelatorioResponse portfolio) {
        var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var obj = new HashMap<String, Object>();

        obj.put("data", formatter.format(LocalDate.now()));
        obj.put("projetos", portfolio.getProjetosSituacao());

        return filesUtils.processTemplate("portfolio", obj);
    }

    @SneakyThrows
    public void gerarRelatorioPortfolioExcel(HttpServletResponse response) {
        var portfolio = getPortfolio();
        var planilha = gerarPlanilhaPortfolio(portfolio);

        filesUtils.baixarArquivo(response,
            new FileInputStream(planilha),
            "portfolio.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @SneakyThrows
    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:MethodLength"})
    private File gerarPlanilhaPortfolio(PortfolioRelatorioResponse portfolio) {
        var arquivo = Files.createTempFile("portfolio", ".xlsx").toFile();
        try (var fos = new FileOutputStream(arquivo)) {
            var livro = new Workbook(fos, "portfolio", "1.0");
            var planilha = livro.newWorksheet("Portfolio");

            planilha.width(0, 40);
            planilha.width(1, 20);
            planilha.width(2, 20);
            planilha.width(3, 20);
            planilha.width(4, 40);

            planilha.value(0, 0, "SITUAÇÃO");
            planilha.value(0, 1, "QUANTIDADE");
            planilha.value(0, 2, "VALOR");
            planilha.value(0, 3, "MEMBROS");
            planilha.value(0, 4, "DURAÇÃO");
            planilha.range(0, 0, 0, 4).style()
                .horizontalAlignment("center").verticalAlignment("center")
                .fillColor(Color.GRAY9).fontColor(Color.WHITE).set();

            var row = new AtomicInteger(1);
            portfolio.getProjetosSituacao().forEach(projeto -> {
                planilha.value(row.get(), 0, projeto.getSituacao());
                planilha.value(row.get(), 1, projeto.getQuantidade());
                planilha.value(row.get(), 2, projeto.getValor());
                planilha.value(row.get(), 3, projeto.getMembros());
                planilha.value(row.get(), 4, projeto.getDuracao());

                row.set(row.get() + 1);
            });

            planilha.range(1, 1, row.get(), 1).style().horizontalAlignment("center").set();
            planilha.range(1, 3, row.get(), 3).style().horizontalAlignment("center").set();

            livro.finish();

            return arquivo;
        } catch (Exception ex) {
            throw new ValidacaoException("Erro ao gerar planilha");
        }
    }
}
