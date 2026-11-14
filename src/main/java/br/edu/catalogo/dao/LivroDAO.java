package br.edu.catalogo.dao;

import br.edu.catalogo.modelo.Livro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) da entidade {@link Livro}.
 *
 * <p>Toda a conversa com a tabela {@code livros} fica concentrada aqui. Os Servlets
 * e as regras de negócio não escrevem SQL: só chamam estes métodos. Isso evita
 * código repetido e deixa o acesso ao banco em um único lugar (Situação-Problema 3).</p>
 *
 * <p><b>Segurança (Situação-Problema 1):</b> todos os comandos usam
 * {@link PreparedStatement} com parâmetros {@code ?}. Os valores digitados pelo
 * usuário NUNCA são concatenados no texto do SQL, o que impede SQL Injection.</p>
 */
public class LivroDAO {

    /** Colunas lidas em todas as consultas (evita repetir o texto). */
    private static final String COLUNAS = "id, titulo, autor, ano, genero, detalhes, capa_url";

    /** Cria o DAO. Ele não guarda estado: cada método abre e fecha a sua própria conexão. */
    public LivroDAO() {
    }

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    /**
     * Insere um livro novo. Depois de inserir, o {@code id} gerado pelo banco
     * é colocado no próprio objeto recebido.
     *
     * @param livro livro a gravar (o id é preenchido ao final)
     * @throws SQLException se o banco recusar o comando ou estiver indisponível
     */
    public void inserir(Livro livro) throws SQLException {
        String sql = "INSERT INTO livros (titulo, autor, ano, genero, detalhes, capa_url) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherParametros(comando, livro);
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    livro.setId(chaves.getInt(1));
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------

    /**
     * Devolve todos os livros, em ordem alfabética de título.
     *
     * @return lista com todos os livros (vazia se não houver nenhum)
     * @throws SQLException se houver problema ao consultar o banco
     */
    public List<Livro> listarTodos() throws SQLException {
        return buscar(null, null);
    }

    /**
     * Busca um livro pelo id.
     *
     * @param id identificador do livro
     * @return o livro, ou {@code null} se não existir
     * @throws SQLException se houver problema ao consultar o banco
     */
    public Livro buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM livros WHERE id = ?";

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    /**
     * Busca simples por título OU autor, com filtro opcional por gênero.
     *
     * <p>O SQL é montado só com trechos fixos escritos aqui (nunca com texto do usuário).
     * O que o usuário digitou entra apenas como parâmetro {@code ?}.</p>
     *
     * @param termo  texto a procurar em título/autor (pode ser {@code null} ou vazio)
     * @param genero gênero exato (pode ser {@code null} ou vazio = todos)
     * @return livros encontrados, em ordem alfabética de título
     * @throws SQLException se houver problema ao consultar o banco
     */
    public List<Livro> buscar(String termo, String genero) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUNAS + " FROM livros WHERE 1 = 1");
        List<String> parametros = new ArrayList<>();

        if (termo != null && !termo.isBlank()) {
            sql.append(" AND (titulo LIKE ? OR autor LIKE ?)");
            String padrao = "%" + escaparCuringasLike(termo.trim()) + "%";
            parametros.add(padrao);
            parametros.add(padrao);
        }
        if (genero != null && !genero.isBlank()) {
            sql.append(" AND genero = ?");
            parametros.add(genero.trim());
        }
        sql.append(" ORDER BY titulo");

        List<Livro> livros = new ArrayList<>();
        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                comando.setString(i + 1, parametros.get(i));
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    livros.add(mapear(resultado));
                }
            }
        }
        return livros;
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    /**
     * Atualiza todos os campos de um livro existente (identificado pelo id do objeto).
     *
     * @param livro livro com os novos dados e o id do registro a alterar
     * @return {@code true} se algum registro foi alterado
     * @throws SQLException se o banco recusar o comando ou estiver indisponível
     */
    public boolean atualizar(Livro livro) throws SQLException {
        String sql = "UPDATE livros SET titulo = ?, autor = ?, ano = ?, genero = ?, "
                   + "detalhes = ?, capa_url = ? WHERE id = ?";

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            preencherParametros(comando, livro);
            comando.setInt(7, livro.getId());
            return comando.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    /**
     * Exclui o livro com o id informado.
     *
     * @param id identificador do livro a excluir
     * @return {@code true} se algum registro foi removido
     * @throws SQLException se o banco recusar o comando ou estiver indisponível
     */
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM livros WHERE id = ?";

        try (Connection conexao = ConexaoBD.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------
    // Métodos auxiliares (privados: só o DAO precisa deles)
    // ------------------------------------------------------------------

    /**
     * Preenche os 6 primeiros parâmetros, iguais no INSERT e no UPDATE.
     *
     * @param comando comando já preparado, com os 6 primeiros {@code ?} a preencher
     * @param livro   livro que fornece os valores
     * @throws SQLException se algum valor não puder ser definido no comando
     */
    private void preencherParametros(PreparedStatement comando, Livro livro) throws SQLException {
        comando.setString(1, livro.getTitulo());
        comando.setString(2, livro.getAutor());
        comando.setInt(3, livro.getAno());
        comando.setString(4, livro.getGenero());
        comando.setString(5, vazioParaNull(livro.getDetalhes()));
        comando.setString(6, vazioParaNull(livro.getCapaUrl()));
    }

    /**
     * Converte a linha atual do resultado em um objeto {@link Livro}.
     *
     * @param resultado resultado da consulta, posicionado em uma linha
     * @return o livro correspondente à linha
     * @throws SQLException se alguma coluna não puder ser lida
     */
    private Livro mapear(ResultSet resultado) throws SQLException {
        return new Livro(
                resultado.getInt("id"),
                resultado.getString("titulo"),
                resultado.getString("autor"),
                resultado.getInt("ano"),
                resultado.getString("genero"),
                resultado.getString("detalhes"),
                resultado.getString("capa_url"));
    }

    /**
     * Guarda NULL no banco em vez de texto vazio.
     *
     * @param texto texto a conferir
     * @return o texto sem espaços nas pontas, ou {@code null} se for nulo ou vazio
     */
    private String vazioParaNull(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    /**
     * Se o usuário digitar % ou _ na busca, eles seriam tratados como curingas do LIKE.
     * Aqui eles viram texto comum (a barra invertida é o caractere de escape padrão do MySQL).
     *
     * @param texto texto digitado pelo usuário
     * @return o texto com {@code \}, {@code %} e {@code _} protegidos por barra invertida
     */
    private String escaparCuringasLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
