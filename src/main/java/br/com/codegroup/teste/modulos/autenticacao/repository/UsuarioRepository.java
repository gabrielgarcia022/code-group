package br.com.codegroup.teste.modulos.autenticacao.repository;

import br.com.codegroup.teste.modulos.autenticacao.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmailIgnoreCase(String email);
}
