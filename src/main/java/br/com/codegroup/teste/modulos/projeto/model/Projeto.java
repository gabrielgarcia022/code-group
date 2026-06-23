package br.com.codegroup.teste.modulos.projeto.model;

import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import br.com.codegroup.teste.modulos.projeto.dto.ProjetoRequest;
import br.com.codegroup.teste.modulos.projeto.enums.ERiscoProjeto;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjeto;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.*;

import static br.com.codegroup.teste.modulos.projeto.enums.ERiscoProjeto.analisarRisco;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PROJETO")
public class Projeto {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NOME")
    private String nome;

    @Column(name = "DATA_CADASTRO")
    private LocalDateTime dataCadastro;

    @Column(name = "DATA_INICIO")
    private LocalDate dataInicio;

    @Column(name = "PREVISAO_TERMINO")
    private LocalDate previsaoTermino;

    @Column(name = "DATA_REAL_TERMINO")
    private LocalDate dataRealTermino;

    @Column(name = "DATA_EXCLUSAO")
    private LocalDateTime dataExclusao;

    @Column(name = "DATA_CANCELAMENTO")
    private LocalDateTime dataCancelamento;

    @Column(name = "ORCAMENTO_TOTAL")
    private BigDecimal orcamentoTotal;

    @Column(name = "DESCRICAO", length = 10000)
    private String descricao;

    @OneToMany(mappedBy = "projeto")
    private List<ProjetoMembro> membros;

    @Column(name = "SITUACAO")
    @Enumerated(EnumType.STRING)
    private ESituacaoProjeto situacao;

    @Column(name = "RISCO")
    @Enumerated(EnumType.STRING)
    private ERiscoProjeto risco;

    @PrePersist
    public void generateId() {
        this.id = Optional.ofNullable(this.id).orElse(IdUtils.generateId());
    }

    public Projeto(String id) {
        this.id = id;
    }

    public static Projeto of(ProjetoRequest request) {
        return Projeto.builder()
            .nome(request.getNome())
            .previsaoTermino(request.getPrevisaoTermino())
            .orcamentoTotal(request.getOrcamentoTotal())
            .descricao(request.getDescricao())
            .situacao(ESituacaoProjeto.EM_ANALISE)
            .risco(analisarRisco(request.getOrcamentoTotal(), null, request.getPrevisaoTermino()))
            .dataCadastro(LocalDateTime.now())
            .build();
    }

    public void merge(ProjetoRequest request) {
        this.nome = request.getNome();
        this.previsaoTermino = request.getPrevisaoTermino();
        this.orcamentoTotal = request.getOrcamentoTotal();
        this.descricao = request.getDescricao();
        this.risco = analisarRisco(request.getOrcamentoTotal(), this.dataInicio, request.getPrevisaoTermino());
    }

    public void atualizarSituacao(ESituacaoProjeto situacao) {
        if (Objects.equals(situacao, ESituacaoProjeto.INICIADO)) {
            iniciar();
        } else if (Objects.equals(situacao, ESituacaoProjeto.ENCERRADO)) {
            encerrar();
        } else if (Objects.equals(situacao, ESituacaoProjeto.CANCELADO)) {
            cancelar();
        } else if (Objects.equals(situacao, ESituacaoProjeto.EXCLUIDO)) {
            excluir();
        }

        this.situacao = situacao;
    }

    public void iniciar() {
        var data = LocalDate.now();

        this.dataInicio = data;
        this.situacao = ESituacaoProjeto.INICIADO;
        this.risco = analisarRisco(this.orcamentoTotal, data, this.previsaoTermino);
    }

    public void encerrar() {
        this.dataRealTermino = LocalDate.now();
        this.situacao = ESituacaoProjeto.ENCERRADO;
    }

    public void cancelar() {
        this.situacao = ESituacaoProjeto.CANCELADO;
        this.dataCancelamento = LocalDateTime.now();
    }

    public void excluir() {
        this.situacao = ESituacaoProjeto.EXCLUIDO;
        this.dataExclusao = LocalDateTime.now();
    }
}
