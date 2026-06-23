package br.com.codegroup.teste.modulos.projeto.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ProjetoRequest {

    private String id;
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    @NotNull(message = "Previsão de término é obrigatório")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate previsaoTermino;
    @NotNull(message = "Orçamento total é obrigatório")
    private BigDecimal orcamentoTotal;
    @NotBlank(message = "Descrição é obrigatório")
    private String descricao;
    @NotBlank(message = "Gerente é obrigatório")
    private String gerenteId;
}
