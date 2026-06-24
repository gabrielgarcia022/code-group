package br.com.codegroup.teste.modulos.autenticacao.model;

import br.com.codegroup.teste.modulos.autenticacao.AutenticacaoHelper;
import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UsuarioTest {

    private static final String USUARIO_ID = IdUtils.generateId();
    private static final String NOME = "GABRIEL GARCIA";
    private static final String EMAIL = "gabriel@email.com";
    private static final String SENHA = "senha-criptografada";

    @Test
    @DisplayName("Deve gerar id quando id for null")
    void deveGerarIdQuandoIdForNull() {
        var usuario = AutenticacaoHelper.usuario(null, NOME, EMAIL, SENHA);
        usuario.generateId();

        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.getId()).isNotBlank();
    }

    @Test
    @DisplayName("Deve manter id existente ao gerar id")
    void deveManterIdExistenteAoGerarId() {
        var usuario = AutenticacaoHelper.usuario(USUARIO_ID, NOME, EMAIL, SENHA);
        usuario.generateId();

        assertThat(usuario.getId()).isEqualTo(USUARIO_ID);
    }

    @Test
    @DisplayName("Deve criar usuário a partir do request")
    void deveCriarUsuarioAPartirDoRequest() {
        var request = AutenticacaoHelper.usuarioMockRequest(NOME, EMAIL, SENHA);
        var usuario = Usuario.of(request, SENHA);

        assertThat(usuario).isNotNull();
        assertThat(usuario.getId()).isNull();
        assertThat(usuario.getNome()).isEqualTo(NOME);
        assertThat(usuario.getEmail()).isEqualTo(EMAIL);
        assertThat(usuario.getSenha()).isEqualTo(SENHA);
    }

    @Test
    @DisplayName("Deve criar usuário usando builder")
    void deveCriarUsuarioUsandoBuilder() {
        var usuario = Usuario.builder()
            .id(USUARIO_ID)
            .nome(NOME)
            .email(EMAIL)
            .senha(SENHA)
            .build();

        assertThat(usuario.getId()).isEqualTo(USUARIO_ID);
        assertThat(usuario.getNome()).isEqualTo(NOME);
        assertThat(usuario.getEmail()).isEqualTo(EMAIL);
        assertThat(usuario.getSenha()).isEqualTo(SENHA);
    }
}
