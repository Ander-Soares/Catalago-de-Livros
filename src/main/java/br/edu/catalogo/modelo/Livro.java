package br.edu.catalogo.modelo;

/**
 * Entidade que representa um livro do catálogo.
 *
 * <p>Conceitos de POO aplicados aqui:</p>
 * <ul>
 *   <li><b>Classe e objeto:</b> cada instância de {@code Livro} é um livro do catálogo.</li>
 *   <li><b>Atributos:</b> id, título, autor, ano, gênero, detalhes e capa.</li>
 *   <li><b>Encapsulamento:</b> atributos {@code private}, acessados só por getters e setters.</li>
 *   <li><b>Construtores:</b> vazio, para livro novo (sem id) e completo (vindo do banco).</li>
 * </ul>
 */
public class Livro {

    // ---------- Atributos (privados: encapsulamento) ----------

    /** Identificador único do livro, gerado pelo banco (0 enquanto o livro ainda não foi gravado). */
    private int id;

    /** Título do livro (obrigatório). */
    private String titulo;

    /** Nome do autor (obrigatório). */
    private String autor;

    /** Ano de publicação. */
    private int ano;

    /** Gênero literário, escolhido entre os gêneros aceitos pelo sistema. */
    private String genero;

    /** Sinopse ou detalhes do livro (opcional). */
    private String detalhes;

    /** Endereço http/https da imagem da capa (opcional). */
    private String capaUrl;

    // ---------- Construtores ----------

    /** Construtor vazio: útil para montar o objeto aos poucos (ex.: a partir do formulário). */
    public Livro() {
    }

    /**
     * Construtor para um livro NOVO, ainda sem id (o banco gera o id ao inserir).
     *
     * @param titulo   título do livro
     * @param autor    nome do autor
     * @param ano      ano de publicação
     * @param genero   gênero literário
     * @param detalhes sinopse ou detalhes (pode ser {@code null})
     * @param capaUrl  endereço da imagem da capa (pode ser {@code null})
     */
    public Livro(String titulo, String autor, int ano, String genero, String detalhes, String capaUrl) {
        this.titulo = titulo;
        this.autor = autor;
        this.ano = ano;
        this.genero = genero;
        this.detalhes = detalhes;
        this.capaUrl = capaUrl;
    }

    /**
     * Construtor completo: usado quando o livro já existe no banco e traz o seu id.
     *
     * @param id       identificador do livro no banco
     * @param titulo   título do livro
     * @param autor    nome do autor
     * @param ano      ano de publicação
     * @param genero   gênero literário
     * @param detalhes sinopse ou detalhes (pode ser {@code null})
     * @param capaUrl  endereço da imagem da capa (pode ser {@code null})
     */
    public Livro(int id, String titulo, String autor, int ano, String genero, String detalhes, String capaUrl) {
        this(titulo, autor, ano, genero, detalhes, capaUrl);
        this.id = id;
    }

    // ---------- Getters e setters ----------

    /**
     * Devolve o identificador do livro.
     *
     * @return o id (0 se o livro ainda não foi gravado)
     */
    public int getId() {
        return id;
    }

    /**
     * Define o identificador do livro.
     *
     * @param id novo id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Devolve o título.
     *
     * @return o título do livro
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Define o título.
     *
     * @param titulo novo título
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Devolve o autor.
     *
     * @return o nome do autor
     */
    public String getAutor() {
        return autor;
    }

    /**
     * Define o autor.
     *
     * @param autor novo nome de autor
     */
    public void setAutor(String autor) {
        this.autor = autor;
    }

    /**
     * Devolve o ano de publicação.
     *
     * @return o ano de publicação
     */
    public int getAno() {
        return ano;
    }

    /**
     * Define o ano de publicação.
     *
     * @param ano novo ano de publicação
     */
    public void setAno(int ano) {
        this.ano = ano;
    }

    /**
     * Devolve o gênero.
     *
     * @return o gênero literário
     */
    public String getGenero() {
        return genero;
    }

    /**
     * Define o gênero.
     *
     * @param genero novo gênero literário
     */
    public void setGenero(String genero) {
        this.genero = genero;
    }

    /**
     * Devolve a sinopse.
     *
     * @return a sinopse ou detalhes (pode ser {@code null})
     */
    public String getDetalhes() {
        return detalhes;
    }

    /**
     * Define a sinopse.
     *
     * @param detalhes nova sinopse ou detalhes
     */
    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    /**
     * Devolve o endereço da capa. O nome do getter segue o atributo, e é assim que as
     * JSPs o acessam, com {@code ${livro.capaUrl}}.
     *
     * @return o endereço da imagem da capa (pode ser {@code null})
     */
    public String getCapaUrl() {
        return capaUrl;
    }

    /**
     * Define o endereço da capa.
     *
     * @param capaUrl novo endereço da imagem
     */
    public void setCapaUrl(String capaUrl) {
        this.capaUrl = capaUrl;
    }

    /**
     * Representação em texto, útil para depuração e para os verificadores no console.
     *
     * @return texto com id, título, autor, ano e gênero
     */
    @Override
    public String toString() {
        return "Livro{id=" + id + ", titulo='" + titulo + "', autor='" + autor
                + "', ano=" + ano + ", genero='" + genero + "'}";
    }
}
