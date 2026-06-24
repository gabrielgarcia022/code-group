package br.com.codegroup.teste.modulos.comum.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class IdUtilsTest {

    @Test
    @DisplayName("Deve gerar id não nulo e não vazio")
    void deveGerarIdNaoNuloENaoVazio() {
        var id = IdUtils.generateId();

        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    @DisplayName("Deve gerar id com tamanho esperado")
    void deveGerarIdComTamanhoEsperado() {
        var id = IdUtils.generateId();

        assertEquals(28, id.length());
    }

    @Test
    @DisplayName("Deve gerar id no formato esperado")
    void deveGerarIdNoFormatoEsperado() {
        var id = IdUtils.generateId();

        assertTrue(id.matches("^[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{18}$"));
    }

    @Test
    @DisplayName("Deve gerar id em letras minúsculas")
    void deveGerarIdEmLetrasMinusculas() {
        var id = IdUtils.generateId();

        assertEquals(id.toLowerCase(), id);
    }

    @Test
    @DisplayName("Deve gerar ids diferentes")
    void deveGerarIdsDiferentes() {
        var primeiroId = IdUtils.generateId();
        var segundoId = IdUtils.generateId();

        assertNotEquals(primeiroId, segundoId);
    }

    @Test
    @DisplayName("Deve gerar vários ids únicos")
    void deveGerarVariosIdsUnicos() {
        var ids = new HashSet<String>();

        for (var i = 0; i < 100; i++) {
            ids.add(IdUtils.generateId());
        }

        assertEquals(100, ids.size());
    }

    @Test
    @DisplayName("Deve conter hífens nas posições esperadas")
    void deveConterHifensNasPosicoesEsperadas() {
        var id = IdUtils.generateId();

        assertEquals('-', id.charAt(4));
        assertEquals('-', id.charAt(9));
    }
}
