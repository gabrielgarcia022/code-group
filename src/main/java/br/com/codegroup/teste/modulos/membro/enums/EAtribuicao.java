package br.com.codegroup.teste.modulos.membro.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.thymeleaf.util.StringUtils;
import java.text.Normalizer;
import java.util.Locale;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

    private static String normalizar(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase(Locale.ROOT);
    }
}
