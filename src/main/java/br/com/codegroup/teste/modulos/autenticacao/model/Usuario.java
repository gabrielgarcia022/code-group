package br.com.codegroup.teste.modulos.autenticacao.model;

import br.com.codegroup.teste.modulos.autenticacao.dto.UsuarioMockRequest;
import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import jakarta.persistence.*;
import java.util.Optional;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NOME")
    private String nome;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "SENHA", nullable = false)
    private String senha;

    @PrePersist
    public void generateId() {
        this.id = Optional.ofNullable(this.id).orElse(IdUtils.generateId());
    }

    public static Usuario of(UsuarioMockRequest request, String senha) {
        return Usuario.builder()
            .nome(request.getNome())
            .email(request.getEmail())
            .senha(senha)
            .build();
    }
}
