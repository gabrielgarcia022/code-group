package br.com.codegroup.teste.modulos.comum.utils;

import java.text.Normalizer;
import java.util.Locale;

public class StringUtils {

    public static String normalizar(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase(Locale.ROOT);
    }
}
