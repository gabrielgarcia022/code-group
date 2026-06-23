package br.com.codegroup.teste.modulos.membro.repository;

import br.com.codegroup.teste.modulos.membro.model.Membro;
import java.util.Optional;

public interface MembroRepositoryCustom {

    Optional<Membro> findGerenteById(String membroId);

    Optional<Membro> findFuncionarioById(String membroId);
}
