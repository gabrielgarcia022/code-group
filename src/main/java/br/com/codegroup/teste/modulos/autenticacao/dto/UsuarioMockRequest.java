package br.com.codegroup.teste.modulos.autenticacao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioMockRequest {


    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;
    @NotBlank(message = "Senha é obrigatória")
    @Min(value = 6, message = "Deve conter no mínimo 6 caracteres")
    private String senha;
}
