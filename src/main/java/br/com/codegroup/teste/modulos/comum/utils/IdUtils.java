package br.com.codegroup.teste.modulos.comum.utils;

import com.github.f4b6a3.ulid.UlidCreator;

public class IdUtils {

    @SuppressWarnings({"checkstyle:MagicNumber"})
    public static synchronized String generateId() {
        var ulid = UlidCreator.getUlid()
            .toString();
        return ulid.substring(0, 4)
            .concat("-")
            .concat(ulid.substring(4, 8))
            .concat("-")
            .concat(ulid.substring(8))
            .toLowerCase();
    }
}
