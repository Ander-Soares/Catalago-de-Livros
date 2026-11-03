package br.edu.catalogo.dao;

import br.edu.catalogo.modelo.Livro;

import java.sql.SQLException;
import java.util.List;

/**
 * Exercita o LivroDAO no console: lista, busca, tenta um SQL Injection, insere,
 * edita e exclui. No final o banco volta ao estado original (5 livros).
 *
 * No IntelliJ: triângulo verde ao lado do "main" > Run.
 * (Não é JUnit de propósito: para não quebrar o "mvn package" com o MySQL desligado.)
 */
public class VerificaDAO {

    public static void main(String[] args) {
        LivroDAO dao = new LivroDAO();

        try {
            System.out.println("== 1) listarTodos ==");
            imprimir(dao.listarTodos());

            System.out.println("\n== 2) buscar por 'orwell' (autor) ==");
            imprimir(dao.buscar("orwell", null));

            System.out.println("\n== 3) buscar por 'o' no gênero Fantasia ==");
            imprimir(dao.buscar("o", "Fantasia"));

            System.out.println("\n== 4) TENTATIVA DE SQL INJECTION: ' OR '1'='1 ==");
            List<Livro> ataque = dao.buscar("' OR '1'='1", null);
            imprimir(ataque);
            System.out.println(ataque.isEmpty()
                    ? "-> Bloqueado: o texto foi tratado como dado, não como SQL."
                    : "-> ATENÇÃO: algo está errado!");

            System.out.println("\n== 5) inserir ==");
            Livro novo = new Livro("Livro de Teste", "Autor Teste", 2024, "Outro", "Só para testar.", null);
            dao.inserir(novo);
            System.out.println("Inserido com id " + novo.getId());

            System.out.println("\n== 6) atualizar ==");
            novo.setTitulo("Livro de Teste (editado)");
            System.out.println("Atualizou? " + dao.atualizar(novo));
            System.out.println(dao.buscarPorId(novo.getId()));

            System.out.println("\n== 7) excluir ==");
            System.out.println("Excluiu? " + dao.excluir(novo.getId()));
            System.out.println("Ainda existe? " + (dao.buscarPorId(novo.getId()) != null));

            System.out.println("\nTudo certo. Total final: " + dao.listarTodos().size() + " livros.");

        } catch (SQLException e) {
            System.out.println("FALHA: " + e.getMessage());
        }
    }

    private static void imprimir(List<Livro> livros) {
        if (livros.isEmpty()) {
            System.out.println("(nenhum resultado)");
        }
        for (Livro livro : livros) {
            System.out.println(livro);
        }
    }
}
