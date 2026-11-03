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
    private int id;
    private String titulo;
    private String autor;
    private int ano;
    private String genero;
    private String detalhes;
    private String capaUrl;

    // ---------- Construtores ----------

    /** Construtor vazio: útil para montar o objeto aos poucos (ex.: a partir do formulário). */
    public Livro() {
    }

    /**
     * Construtor para um livro NOVO, ainda sem id (o banco gera o id ao inserir).
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
     */
    public Livro(int id, String titulo, String autor, int ano, String genero, String detalhes, String capaUrl) {
        this(titulo, autor, ano, genero, detalhes, capaUrl);
        this.id = id;
    }

    // ---------- Getters e setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    /** O nome do getter segue o atributo: {@code ${livro.capaUrl}} nas JSPs. */
    public String getCapaUrl() {
        return capaUrl;
    }

    public void setCapaUrl(String capaUrl) {
        this.capaUrl = capaUrl;
    }

    @Override
    public String toString() {
        return "Livro{id=" + id + ", titulo='" + titulo + "', autor='" + autor
                + "', ano=" + ano + ", genero='" + genero + "'}";
    }
}
