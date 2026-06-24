package br.com.codegroup.teste.modulos.membro;

import br.com.codegroup.teste.modulos.membro.dto.MembroRequest;
import br.com.codegroup.teste.modulos.membro.dto.MembroResponse;
import br.com.codegroup.teste.modulos.membro.enums.EAtribuicao;
import br.com.codegroup.teste.modulos.membro.enums.ESituacaoMembro;
import br.com.codegroup.teste.modulos.membro.model.Membro;
import java.time.LocalDateTime;

@SuppressWarnings({"checkstyle:ParameterNumber"})
public class MembroHelper {

    public static Membro membro(String id, String nome, EAtribuicao atribuicao, LocalDateTime dataCadastro,
                                LocalDateTime dataExclusao, ESituacaoMembro situacao) {
        return Membro.builder()
            .id(id)
            .nome(nome)
            .atribuicao(atribuicao)
            .dataCadastro(dataCadastro)
            .dataExclusao(dataExclusao)
            .situacao(situacao)
            .build();
    }

    public static Membro membro(String id, String nome, EAtribuicao atribuicao) {
        return Membro.builder()
            .id(id)
            .nome(nome)
            .atribuicao(atribuicao)
            .build();
    }

    public static MembroResponse membroResponse(String id, String nome, EAtribuicao atribuicao, ESituacaoMembro situacao) {
        return MembroResponse.builder()
            .id(id)
            .nome(nome)
            .atribuicao(atribuicao)
            .dataCadastro(LocalDateTime.of(2026, 6, 18, 10, 30))
            .dataExclusao(ESituacaoMembro.EXCLUIDO.equals(situacao)
                ? LocalDateTime.of(2026, 6, 19, 10, 30)
                : null)
            .situacao(situacao)
            .build();
    }

    public static MembroRequest membroRequest(String id, String nome, EAtribuicao atribuicao) {
        var request = new MembroRequest();
        request.setId(id);
        request.setNome(nome);
        request.setAtribuicao(atribuicao);

        return request;
    }
}
