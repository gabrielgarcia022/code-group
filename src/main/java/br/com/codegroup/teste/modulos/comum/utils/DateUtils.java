package br.com.codegroup.teste.modulos.comum.utils;

import java.util.Objects;

public class DateUtils {

    @SuppressWarnings({"checkstyle:MagicNumber"})
    public static String getDuracaoFromDias(Long totalDias) {
        if (Objects.nonNull(totalDias)) {
            if (totalDias < 0) {
                throw new IllegalArgumentException("A quantidade de dias não pode ser negativa");
            }

            if (totalDias == 0) {
                return "0 dias";
            }

            var meses = totalDias / 30;
            var dias = totalDias % 30;

            if (meses == 0) {
                return formatarParte(dias, "dia", "dias");
            }

            if (dias == 0) {
                return formatarParte(meses, "mês", "meses");
            }

            return formatarParte(meses, "mês", "meses")
                + " e "
                + formatarParte(dias, "dia", "dias");
        }

        return null;
    }

    private static String formatarParte(Long valor, String singular, String plural) {
        return valor + " " + (valor == 1 ? singular : plural);
    }
}
