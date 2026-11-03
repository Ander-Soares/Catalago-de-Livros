package br.edu.catalogo.service;

import br.edu.catalogo.dao.LivroDAO;
import br.edu.catalogo.modelo.Livro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários das validações. Não usam banco de dados nem Tomcat:
 * o LivroDAO é criado mas nunca chega a ser chamado, porque a validação barra antes.
 */
class LivroServiceTest {

    private LivroService service;

    @BeforeEach
    void preparar() {
        service = new LivroService(new LivroDAO());
    }

    /** Livro válido, usado como base: cada teste estraga só um campo. */
    private Livro livroValido() {
        return new Livro("Dom Casmurro", "Machado de Assis", 1899, "Romance",
                "Clássico da literatura brasileira.", "https://exemplo.com/capa.jpg");
    }

    @Test
    void livroValido_naoTemErros() {
        assertTrue(service.validar(livroValido()).isEmpty());
    }

    @Test
    void capaECampoOpcional() {
        Livro livro = livroValido();
        livro.setCapaUrl("");
        livro.setDetalhes(null);
        assertTrue(service.validar(livro).isEmpty());
    }

    @Test
    void tituloVazio_geraErro() {
        Livro livro = livroValido();
        livro.setTitulo("   ");
        List<String> erros = service.validar(livro);
        assertEquals(1, erros.size());
        assertTrue(erros.get(0).contains("título"));
    }

    @Test
    void tituloMuitoLongo_geraErro() {
        Livro livro = livroValido();
        livro.setTitulo("x".repeat(LivroService.MAX_TITULO + 1));
        assertFalse(service.validar(livro).isEmpty());
    }

    @Test
    void autorNulo_geraErro() {
        Livro livro = livroValido();
        livro.setAutor(null);
        assertFalse(service.validar(livro).isEmpty());
    }

    @Test
    void anoForaDoIntervalo_geraErro() {
        for (int anoInvalido : new int[]{0, -5, LivroService.ANO_MAXIMO + 1}) {
            Livro livro = livroValido();
            livro.setAno(anoInvalido);
            assertFalse(service.validar(livro).isEmpty(), "ano " + anoInvalido + " deveria ser rejeitado");
        }
    }

    @Test
    void generoForaDaLista_geraErro() {
        Livro livro = livroValido();
        livro.setGenero("Inventado");
        assertFalse(service.validar(livro).isEmpty());
    }

    @Test
    void capaComEsquemaPerigoso_geraErro() {
        Livro livro = livroValido();
        livro.setCapaUrl("javascript:alert(1)");
        assertFalse(service.validar(livro).isEmpty());
    }

    @Test
    void capaSemHost_geraErro() {
        Livro livro = livroValido();
        livro.setCapaUrl("http://");
        assertFalse(service.validar(livro).isEmpty());
    }

    @Test
    void varioProblemasJuntos_voltamTodosDeUmaVez() {
        Livro livro = new Livro();   // tudo vazio, ano 0
        assertEquals(4, service.validar(livro).size());   // título, autor, ano, gênero
    }

    @Test
    void salvarComDadosInvalidos_lancaExcecaoSemTocarNoBanco() {
        Livro livro = livroValido();
        livro.setTitulo("");
        ValidacaoException e = assertThrows(ValidacaoException.class, () -> service.salvar(livro));
        assertFalse(e.getErros().isEmpty());
    }

    @Test
    void textoComAspasEHtml_naoEBarradoPelaValidacao() {
        // A defesa contra SQL Injection é o PreparedStatement e contra XSS é o c:out;
        // a validação aceita o texto como dado comum.
        Livro livro = livroValido();
        livro.setTitulo("O'Reilly <b>\"teste\"</b>; DROP TABLE livros;--");
        assertTrue(service.validar(livro).isEmpty());
    }
}
