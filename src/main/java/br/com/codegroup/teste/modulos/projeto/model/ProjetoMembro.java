package br.com.codegroup.teste.modulos.projeto.model;

import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import br.com.codegroup.teste.modulos.projeto.enums.ESituacaoProjetoMembro;
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
@Table(name = "PROJETO_MEMBRO", indexes = {
    @Index(name = "PROJETO_INDEX", columnList = "FK_PROJETO")
})
public class ProjetoMembro {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "DATA_CADASTRO")
    private LocalDateTime dataCadastro;

    @JoinColumn(name = "FK_PROJETO", referencedColumnName = "ID",
        foreignKey = @ForeignKey(name = "FK_PROJETO"))
    @ManyToOne(fetch = FetchType.LAZY)
    private Projeto projeto;

    @JoinColumn(name = "FK_MEMBRO", referencedColumnName = "ID",
        foreignKey = @ForeignKey(name = "FK_MEMBRO"))
    @ManyToOne(fetch = FetchType.LAZY)
    private Membro membro;

    @Column(name = "IS_RESPONSAVEL")
    private Boolean isResponsavel;

    @Column(name = "DATA_EXCLUSAO")
    private LocalDateTime dataExclusao;

    @Column(name = "SITUACAO")
    @Enumerated(EnumType.STRING)
    private ESituacaoProjetoMembro situacao;

    @PrePersist
    public void generateId() {
        this.id = Optional.ofNullable(this.id).orElse(IdUtils.generateId());
    }

    public static ProjetoMembro of(Membro membro, Projeto projeto) {
        return ProjetoMembro.builder()
            .dataCadastro(LocalDateTime.now())
            .projeto(projeto)
            .membro(membro)
            .situacao(ESituacaoProjetoMembro.PARTICIPANTE)
            .build();
    }

    public void excluir() {
        this.situacao = ESituacaoProjetoMembro.EXCLUIDO;
        this.dataExclusao = LocalDateTime.now();
    }
}
