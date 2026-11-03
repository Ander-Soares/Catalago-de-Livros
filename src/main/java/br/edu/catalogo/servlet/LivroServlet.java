package br.edu.catalogo.servlet;

import br.edu.catalogo.dao.LivroDAO;
import br.edu.catalogo.modelo.Livro;
import br.edu.catalogo.service.LivroService;
import br.edu.catalogo.service.ValidacaoException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador único do catálogo: recebe as requisições HTTP, chama o serviço e
 * escolhe qual JSP mostrar. Não tem SQL nem regra de validação, só "tráfego".
 *
 * <pre>
 *  GET  /livros                 lista (aceita ?busca=...&amp;genero=...)
 *  GET  /livros/detalhes?id=N   detalhes de um livro
 *  GET  /livros/novo            formulário vazio
 *  GET  /livros/editar?id=N     formulário preenchido
 *  POST /livros/salvar          cadastra (sem id) ou atualiza (com id)
 *  POST /livros/excluir         exclui (campo id)
 * </pre>
 *
 * <p>Padrão usado nos POSTs: <i>Post/Redirect/Get</i>. Depois de gravar, o Servlet
 * redireciona para a lista, assim o F5 não repete o cadastro.</p>
 */
@WebServlet(name = "LivroServlet", urlPatterns = {"/livros", "/livros/*"})
public class LivroServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LivroServlet.class.getName());

    /** As JSPs ficam em WEB-INF: o navegador não consegue abri-las direto, só via Servlet. */
    private static final String VIEWS = "/WEB-INF/views/";

    private final LivroService service = new LivroService(new LivroDAO());

    // ------------------------------------------------------------------
    // GET
    // ------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            switch (acao(req)) {
                case "" -> listar(req, resp);
                case "/detalhes" -> detalhes(req, resp);
                case "/novo" -> novo(req, resp);
                case "/editar" -> editar(req, resp);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            tratarErroBanco(req, resp, e);
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        String busca = req.getParameter("busca");
        String genero = req.getParameter("genero");

        req.setAttribute("livros", service.listar(busca, genero));
        req.setAttribute("generos", LivroService.GENEROS);
        moverMensagensDaSessao(req);
        encaminhar(req, resp, "lista.jsp");
    }

    private void detalhes(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        Livro livro = service.buscarPorId(lerId(req));
        if (livro == null) {
            livroNaoEncontrado(req, resp);
            return;
        }
        req.setAttribute("livro", livro);
        encaminhar(req, resp, "detalhes.jsp");
    }

    private void novo(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("livro", new Livro());
        req.setAttribute("generos", LivroService.GENEROS);
        encaminhar(req, resp, "formulario.jsp");
    }

    private void editar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        Livro livro = service.buscarPorId(lerId(req));
        if (livro == null) {
            livroNaoEncontrado(req, resp);
            return;
        }
        req.setAttribute("livro", livro);
        req.setAttribute("generos", LivroService.GENEROS);
        encaminhar(req, resp, "formulario.jsp");
    }

    // ------------------------------------------------------------------
    // POST
    // ------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Sem isto, acentos digitados no formulário chegam trocados (ex.: "Ã§")
        req.setCharacterEncoding("UTF-8");

        try {
            switch (acao(req)) {
                case "/salvar" -> salvar(req, resp);
                case "/excluir" -> excluir(req, resp);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            tratarErroBanco(req, resp, e);
        }
    }

    private void salvar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        Livro livro = lerFormulario(req);
        boolean cadastro = livro.getId() == 0;

        try {
            service.salvar(livro);
            guardarMensagem(req, "mensagem",
                    cadastro ? "Livro cadastrado com sucesso!" : "Livro atualizado com sucesso!");
            resp.sendRedirect(req.getContextPath() + "/livros");

        } catch (ValidacaoException e) {
            // Dados inválidos: volta para o formulário com os erros e o que a pessoa já digitou
            req.setAttribute("erros", e.getErros());
            req.setAttribute("livro", livro);
            req.setAttribute("generos", LivroService.GENEROS);
            encaminhar(req, resp, "formulario.jsp");
        }
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {
        boolean excluiu = service.excluir(lerId(req));

        if (excluiu) {
            guardarMensagem(req, "mensagem", "Livro excluído.");
        } else {
            guardarMensagem(req, "mensagemErro", "Livro não encontrado. Ele pode já ter sido excluído.");
        }
        resp.sendRedirect(req.getContextPath() + "/livros");
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** Parte da URL depois de /livros ("" para a lista, "/novo", "/editar"...). */
    private String acao(HttpServletRequest req) {
        String info = req.getPathInfo();
        return (info == null || info.equals("/")) ? "" : info;
    }

    /** Lê o parâmetro id; devolve 0 se estiver ausente ou não for número. */
    private int lerId(HttpServletRequest req) {
        return lerInteiro(req.getParameter("id"));
    }

    private int lerInteiro(String texto) {
        if (texto == null) {
            return 0;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Monta um Livro com o que veio do formulário. Ano ilegível vira 0, e o serviço
     * reclama com a mensagem "O ano deve ser um número inteiro entre 1 e 2100".
     */
    private Livro lerFormulario(HttpServletRequest req) {
        Livro livro = new Livro();
        livro.setId(lerId(req));
        livro.setTitulo(req.getParameter("titulo"));
        livro.setAutor(req.getParameter("autor"));
        livro.setAno(lerInteiro(req.getParameter("ano")));
        livro.setGenero(req.getParameter("genero"));
        livro.setDetalhes(req.getParameter("detalhes"));
        livro.setCapaUrl(req.getParameter("capaUrl"));
        return livro;
    }

    private void livroNaoEncontrado(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        guardarMensagem(req, "mensagemErro", "Livro não encontrado.");
        resp.sendRedirect(req.getContextPath() + "/livros");
    }

    private void encaminhar(HttpServletRequest req, HttpServletResponse resp, String jsp)
            throws ServletException, IOException {
        req.getRequestDispatcher(VIEWS + jsp).forward(req, resp);
    }

    /** Mensagem "de uma vez só": guardada na sessão, mostrada na próxima tela e apagada. */
    private void guardarMensagem(HttpServletRequest req, String chave, String texto) {
        req.getSession().setAttribute(chave, texto);
    }

    private void moverMensagensDaSessao(HttpServletRequest req) {
        HttpSession sessao = req.getSession(false);
        if (sessao == null) {
            return;
        }
        for (String chave : new String[]{"mensagem", "mensagemErro"}) {
            Object valor = sessao.getAttribute(chave);
            if (valor != null) {
                req.setAttribute(chave, valor);
                sessao.removeAttribute(chave);
            }
        }
    }

    /**
     * Erro de banco: o detalhe técnico vai só para o log do servidor.
     * O usuário vê uma mensagem genérica (mostrar a mensagem do SQL ajudaria um atacante).
     */
    private void tratarErroBanco(HttpServletRequest req, HttpServletResponse resp, SQLException e)
            throws ServletException, IOException {
        LOG.log(Level.SEVERE, "Erro ao acessar o banco de dados", e);
        req.setAttribute("mensagemErro",
                "Não foi possível acessar o banco de dados no momento. Tente novamente em instantes.");
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        encaminhar(req, resp, "erro.jsp");
    }
}
