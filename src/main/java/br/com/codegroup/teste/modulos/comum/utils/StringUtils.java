package br.com.codegroup.teste.modulos.comum.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import static org.thymeleaf.util.StringUtils.isEmpty;

public class StringUtils {

    public static String normalizar(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase(Locale.ROOT);
    }

    public static String adicionarMascaraValor(BigDecimal valor) {
        if (Objects.nonNull(valor)) {
            var formatter = NumberFormat.getNumberInstance(Locale.of("pt", "BR"));
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
            formatter.setRoundingMode(RoundingMode.HALF_UP);

            return formatter.format(valor);
        }

        return "0,00";
    }

    @SuppressWarnings({"checkstyle:MethodLength"})
    public static String tratarNomeDownload(String nome) {
        if (!isEmpty(nome)) {
            if (nome.chars().filter(ch -> ch == '.').count() > 1) {
                var lastIndex = nome.lastIndexOf(".");
                return Normalizer.normalize(nome.substring(0, lastIndex).chars()
                        .filter(ch -> ch != '.')
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString()
                        .toLowerCase()
                        .replaceAll(" ", "-")
                        .concat(".")
                        .concat(nome.substring(lastIndex + 1)), Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            } else {
                return Normalizer.normalize(nome.toLowerCase().replaceAll(" ", "-"), Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            }
        } else {
            return nome;
        }
    }
}
