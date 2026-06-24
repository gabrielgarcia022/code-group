package br.com.codegroup.teste.modulos.autenticacao.repository;

import br.com.codegroup.teste.modulos.autenticacao.AutenticacaoHelper;
import br.com.codegroup.teste.modulos.comum.utils.IdUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UsuarioRepositoryTest {

    private static final String USUARIO_ID = IdUtils.generateId();
    private static final String NOME = "GABRIEL GARCIA";
    private static final String EMAIL = "gabriel@email.com";
    private static final String SENHA = "senha-criptografada";

    @Autowired
    private UsuarioRepository repository;

    @Test
    @DisplayName("Deve buscar usuário por e-mail")
    void deveBuscarUsuarioPorEmail() {
        var usuario = AutenticacaoHelper.usuario(USUARIO_ID, NOME, EMAIL, SENHA);
        repository.saveAndFlush(usuario);
        var resultado = repository.findByEmailIgnoreCase(EMAIL);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(USUARIO_ID);
        assertThat(resultado.get().getNome()).isEqualTo(NOME);
        assertThat(resultado.get().getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.get().getSenha()).isEqualTo(SENHA);
    }

    @Test
    @DisplayName("Deve buscar usuário por e-mail ignorando maiúsculas e minúsculas")
    void deveBuscarUsuarioPorEmailIgnorandoMaiusculasEMinusculas() {
        var usuario = AutenticacaoHelper.usuario(USUARIO_ID, NOME, EMAIL, SENHA);
        repository.saveAndFlush(usuario);
        var resultado = repository.findByEmailIgnoreCase("GABRIEL@EMAIL.COM");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(USUARIO_ID);
        assertThat(resultado.get().getEmail()).isEqualTo("gabriel@email.com");
    }

    @Test
    @DisplayName("Deve retornar vazio quando e-mail não existir")
    void deveRetornarVazioQuandoEmailNaoExistir() {
        var resultado = repository.findByEmailIgnoreCase("naoexiste@email.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve salvar usuário gerando id automaticamente")
    void deveSalvarUsuarioGerandoIdAutomaticamente() {
        var usuario = AutenticacaoHelper.usuario(USUARIO_ID, NOME, EMAIL, SENHA);
        var resultado = repository.saveAndFlush(usuario);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getId()).isNotBlank();
        assertThat(resultado.getNome()).isEqualTo(NOME);
        assertThat(resultado.getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.getSenha()).isEqualTo(SENHA);
    }

    @Test
    @DisplayName("Deve manter id informado ao salvar usuário")
    void deveManterIdInformadoAoSalvarUsuario() {
        var usuario = AutenticacaoHelper.usuario(USUARIO_ID, NOME, EMAIL, SENHA);
        var resultado = repository.saveAndFlush(usuario);

        assertThat(resultado.getId()).isEqualTo(USUARIO_ID);
    }
}
