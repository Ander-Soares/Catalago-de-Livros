package br.edu.catalogo.service;

import java.util.List;

/**
 * Exceção lançada quando os dados de um livro não passam nas validações.
 *
 * <p>É uma exceção <b>checada</b> de propósito: quem chama o serviço é obrigado a
 * tratá-la (o Servlet mostra as mensagens de volta no formulário). Ela carrega uma
 * lista, para o usuário ver todos os problemas de uma vez, e não um de cada vez.</p>
 */
public class ValidacaoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final transient List<String> erros;

    public ValidacaoException(List<String> erros) {
        super(String.join(" | ", erros));
        this.erros = List.copyOf(erros);
    }

    public ValidacaoException(String erro) {
        this(List.of(erro));
    }

    /** Mensagens prontas para exibir ao usuário. */
    public List<String> getErros() {
        return erros;
    }
}
