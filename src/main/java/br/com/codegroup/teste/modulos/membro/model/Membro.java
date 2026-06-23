package br.com.codegroup.teste.modulos.membro.model;

import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import br.com.codegroup.teste.modulos.membro.dto.MembroRequest;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "MEMBRO")
public class Membro {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NOME")
    private String nome;

    @Column(name = "ATRIBUICAO")
    @Enumerated(EnumType.STRING)
    private EAtribuicao atribuicao;

    @Column(name = "DATA_CADASTRO")
    private LocalDateTime dataCadastro;

    @Column(name = "DATA_EXCLUSAO")
    private LocalDateTime dataExclusao;

    @Column(name = "SITUACAO")
    @Enumerated(EnumType.STRING)
    private ESituacaoMembro situacao;

    @PrePersist
    public void generateId() {
        this.id = Optional.ofNullable(this.id).orElse(IdUtils.generateId());
    }

    public Membro(String id) {
        this.id = id;
    }

    public static Membro of(MembroRequest request) {
        return Membro.builder()
            .nome(request.getNome())
            .atribuicao(request.getAtribuicao())
            .dataCadastro(LocalDateTime.now())
            .situacao(ESituacaoMembro.ATIVO)
            .build();
    }

    public void merge(MembroRequest request) {
        this.nome = request.getNome();
        this.atribuicao = request.getAtribuicao();
    }

    public void excluir() {
        this.situacao = ESituacaoMembro.EXCLUIDO;
        this.dataExclusao = LocalDateTime.now();
    }

    public void reativar() {
        this.situacao = ESituacaoMembro.ATIVO;
        this.dataExclusao = null;
    }
}
