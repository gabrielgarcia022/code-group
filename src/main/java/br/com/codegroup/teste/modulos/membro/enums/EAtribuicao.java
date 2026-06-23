package br.com.codegroup.teste.modulos.membro.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.thymeleaf.util.StringUtils;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static br.com.codegroup.teste.modulos.comum.utils.StringUtils.normalizar;

@Getter
@AllArgsConstructor
public enum EAtribuicao {

    FUNCIONARIO("Funcionário"),
    GERENTE("Gerente"),
    OUTRO("Outro");

    private final String descricao;

    @JsonCreator
    public static EAtribuicao from(String value) {
        if (!StringUtils.isEmpty(value)) {
            var valueNormalizado = normalizar(value);

            return Stream.of(values()).filter(atribuicao -> normalizar(atribuicao.name()).equals(valueNormalizado)
                    || normalizar(atribuicao.getDescricao()).equals(valueNormalizado))
                .findFirst()
                .orElse(OUTRO);
        }

        return OUTRO;
    }
}
