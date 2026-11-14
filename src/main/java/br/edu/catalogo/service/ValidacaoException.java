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

    /** Identificador de versão da classe (exigido para exceções serializáveis). */
    private static final long serialVersionUID = 1L;

    /** Mensagens de erro de validação, prontas para exibir ao usuário. */
    private final transient List<String> erros;

    /**
     * Cria a exceção com várias mensagens de erro.
     *
     * @param erros mensagens de erro de validação (não pode ser {@code null})
     */
    public ValidacaoException(List<String> erros) {
        super(String.join(" | ", erros));
        this.erros = List.copyOf(erros);
    }

    /**
     * Cria a exceção com uma única mensagem de erro.
     *
     * @param erro mensagem de erro de validação
     */
    public ValidacaoException(String erro) {
        this(List.of(erro));
    }

    /**
     * Devolve as mensagens de erro, prontas para exibir ao usuário.
     *
     * @return lista imutável de mensagens de erro
     */
    public List<String> getErros() {
        return erros;
    }
}
