package br.edu.catalogo.service;

import br.edu.catalogo.dao.LivroDAO;
import br.edu.catalogo.modelo.Livro;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de serviço: concentra as <b>regras de negócio e as validações</b>.
 *
 * <p>O Servlet cuida só de HTTP (receber e responder), o DAO cuida só do banco, e esta
 * classe fica no meio decidindo se os dados são aceitáveis. Assim cada parte tem uma
 * responsabilidade e esta classe pode ser testada sem Tomcat e sem banco
 * (veja {@code LivroServiceTest}).</p>
 */
public class LivroService {

    /** Gêneros aceitos. A tela usa esta mesma lista, e o serviço rejeita qualquer outro valor. */
    public static final List<String> GENEROS = List.of(
            "Romance", "Ficção", "Fantasia", "Aventura", "Drama",
            "Terror", "Biografia", "Técnico", "Outro");

    /** Menor ano de publicação aceito. */
    public static final int ANO_MINIMO = 1;

    /** Maior ano de publicação aceito (igual ao CHECK da tabela no banco). */
    public static final int ANO_MAXIMO = 2100;

    /** Tamanho máximo do título, em caracteres (igual à coluna do banco). */
    public static final int MAX_TITULO = 150;

    /** Tamanho máximo do nome do autor, em caracteres (igual à coluna do banco). */
    public static final int MAX_AUTOR = 100;

    /** Tamanho máximo da sinopse, em caracteres. */
    public static final int MAX_DETALHES = 2000;

    /** Tamanho máximo do endereço da capa, em caracteres (igual à coluna do banco). */
    public static final int MAX_CAPA_URL = 500;

    /** Acesso ao banco de dados. */
    private final LivroDAO dao;

    /**
     * Cria o serviço. O DAO é recebido pelo construtor, o que facilita trocá-lo em testes.
     *
     * @param dao objeto de acesso aos dados dos livros
     */
    public LivroService(LivroDAO dao) {
        this.dao = dao;
    }

    // ------------------------------------------------------------------
    // Operações usadas pelo Servlet
    // ------------------------------------------------------------------

    /**
     * Lista os livros, com busca (título ou autor) e filtro de gênero opcionais.
     *
     * @param termo  texto a procurar em título e autor (pode ser {@code null})
     * @param genero gênero exato a filtrar (pode ser {@code null})
     * @return livros encontrados, em ordem alfabética de título
     * @throws SQLException se houver problema no banco de dados
     */
    public List<Livro> listar(String termo, String genero) throws SQLException {
        return dao.buscar(termo, genero);
    }

    /**
     * Busca um livro pelo id.
     *
     * @param id identificador do livro
     * @return o livro, ou {@code null} se não existir
     * @throws SQLException se houver problema no banco de dados
     */
    public Livro buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    /**
     * Valida e grava o livro: insere se ele ainda não tem id, atualiza se já tem.
     *
     * @param livro livro a gravar (os textos são aparados antes da validação)
     * @throws ValidacaoException se algum dado for inválido (nada é gravado)
     * @throws SQLException       se houver problema no banco de dados
     */
    public void salvar(Livro livro) throws ValidacaoException, SQLException {
        normalizar(livro);

        List<String> erros = validar(livro);
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        if (livro.getId() == 0) {
            dao.inserir(livro);
        } else if (!dao.atualizar(livro)) {
            throw new ValidacaoException("Livro não encontrado. Ele pode ter sido excluído.");
        }
    }

    /**
     * Exclui um livro.
     *
     * @param id identificador do livro a excluir
     * @return {@code true} se o livro existia e foi excluído
     * @throws SQLException se houver problema no banco de dados
     */
    public boolean excluir(int id) throws SQLException {
        return dao.excluir(id);
    }

    // ------------------------------------------------------------------
    // Validação
    // ------------------------------------------------------------------

    /**
     * Confere todas as regras e devolve a lista de problemas (vazia = tudo certo).
     * Não acessa o banco e não altera o livro.
     *
     * @param livro livro a validar
     * @return mensagens de erro, prontas para exibir ao usuário (lista vazia se o livro é válido)
     */
    public List<String> validar(Livro livro) {
        List<String> erros = new ArrayList<>();

        String titulo = livro.getTitulo();
        if (vazio(titulo)) {
            erros.add("O título é obrigatório.");
        } else if (titulo.trim().length() > MAX_TITULO) {
            erros.add("O título deve ter no máximo " + MAX_TITULO + " caracteres.");
        }

        String autor = livro.getAutor();
        if (vazio(autor)) {
            erros.add("O autor é obrigatório.");
        } else if (autor.trim().length() > MAX_AUTOR) {
            erros.add("O autor deve ter no máximo " + MAX_AUTOR + " caracteres.");
        }

        int ano = livro.getAno();
        if (ano < ANO_MINIMO || ano > ANO_MAXIMO) {
            erros.add("O ano deve ser um número inteiro entre " + ANO_MINIMO + " e " + ANO_MAXIMO + ".");
        }

        String genero = livro.getGenero();
        if (genero == null || !GENEROS.contains(genero.trim())) {
            erros.add("Selecione um gênero válido.");
        }

        String detalhes = livro.getDetalhes();
        if (detalhes != null && detalhes.trim().length() > MAX_DETALHES) {
            erros.add("A sinopse deve ter no máximo " + MAX_DETALHES + " caracteres.");
        }

        String capa = livro.getCapaUrl();
        if (!vazio(capa) && !urlValida(capa.trim())) {
            erros.add("O link da capa deve ser um endereço http:// ou https:// válido (máximo "
                    + MAX_CAPA_URL + " caracteres).");
        }

        return erros;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /**
     * Remove espaços sobrando no começo e no fim dos textos do livro.
     *
     * @param livro livro cujos textos serão aparados (o próprio objeto é alterado)
     */
    private void normalizar(Livro livro) {
        livro.setTitulo(aparar(livro.getTitulo()));
        livro.setAutor(aparar(livro.getAutor()));
        livro.setGenero(aparar(livro.getGenero()));
        livro.setDetalhes(aparar(livro.getDetalhes()));
        livro.setCapaUrl(aparar(livro.getCapaUrl()));
    }

    /**
     * Apara um texto, aceitando {@code null}.
     *
     * @param texto texto a aparar
     * @return o texto sem espaços nas pontas, ou {@code null} se o texto for nulo
     */
    private String aparar(String texto) {
        return texto == null ? null : texto.trim();
    }

    /**
     * Informa se um texto está ausente ou só tem espaços.
     *
     * @param texto texto a conferir
     * @return {@code true} se o texto for nulo ou estiver em branco
     */
    private boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    /**
     * Só aceita http/https com host. Isso também barra esquemas perigosos como {@code javascript:}.
     *
     * @param url endereço a conferir (já sem espaços nas pontas)
     * @return {@code true} se for um endereço http ou https válido, dentro do limite de tamanho
     */
    private boolean urlValida(String url) {
        if (url.length() > MAX_CAPA_URL) {
            return false;
        }
        try {
            URI uri = new URI(url);
            String esquema = uri.getScheme();
            return ("http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema))
                    && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
