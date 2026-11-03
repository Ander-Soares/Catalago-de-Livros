package br.edu.catalogo.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Programinha para testar a conexão com o banco ANTES de ligar o resto.
 * No IntelliJ: clique no triângulo verde ao lado do "main" e escolha "Run".
 * (Não é um teste JUnit: de propósito, para não quebrar o "mvn package" se o MySQL estiver desligado.)
 */
public class VerificaConexao {

    public static void main(String[] args) {
        try (Connection conexao = ConexaoBD.obterConexao();
             Statement comando = conexao.createStatement();
             ResultSet resultado = comando.executeQuery("SELECT COUNT(*) FROM livros")) {

            resultado.next();
            System.out.println("Conexão OK! Livros na tabela: " + resultado.getInt(1));

        } catch (SQLException e) {
            System.out.println("FALHA ao conectar: " + e.getMessage());
        }
    }
}
