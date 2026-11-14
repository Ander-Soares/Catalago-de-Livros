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

    /** Registro (log) de mensagens técnicas, como erros de banco de dados. */
    private static final Logger LOG = Logger.getLogger(LivroServlet.class.getName());

    /** As JSPs ficam em WEB-INF: o navegador não consegue abri-las direto, só via Servlet. */
    private static final String VIEWS = "/WEB-INF/views/";

    /** Serviço com as regras de negócio; o Servlet nunca fala direto com o banco. */
    private final LivroService service = new LivroService(new LivroDAO());

    /** Construtor usado pelo Tomcat para criar o Servlet (uma única instância atende todas as requisições). */
    public LivroServlet() {
    }

    // ------------------------------------------------------------------
    // GET
    // ------------------------------------------------------------------

    /**
     * Trata as requisições GET, escolhendo a ação pela parte da URL depois de {@code /livros}.
     *
     * @param req  requisição recebida
     * @param resp resposta a enviar
     * @throws ServletException se o encaminhamento para a JSP falhar
     * @throws IOException      se houver erro ao escrever a resposta
     */
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

    /**
     * Mostra a lista de livros, aplicando a busca e o filtro de gênero recebidos na URL.
     *
     * @param req  requisição (parâmetros {@code busca} e {@code genero} opcionais)
     * @param resp resposta
     * @throws ServletException se o encaminhamento para a JSP falhar
     * @throws IOException      se houver erro ao escrever a resposta
     * @throws SQLException     se houver problema no banco de dados
     */
    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        String busca = req.getParameter("busca");
        String genero = req.getParameter("genero");

        req.setAttribute("livros", service.listar(busca, genero));
        req.setAttribute("generos", LivroService.GENEROS);
        moverMensagensDaSessao(req);
        encaminhar(req, resp, "lista.jsp");
    }

    /**
     * Mostra os detalhes de um livro. Se ele não existir, volta para a lista com um aviso.
     *
     * @param req  requisição (parâmetro {@code id})
     * @param resp resposta
     * @throws ServletException se o encaminhamento para a JSP falhar
     * @throws IOException      se houver erro ao escrever a resposta
     * @throws SQLException     se houver problema no banco de dados
     */
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

    /**
     * Mostra o formulário vazio para cadastrar um livro.
     *
     * @param req  requisição
     * @param resp resposta
     * @throws ServletException se o encaminhamento para a JSP falhar
     * @throws IOException      se houver erro ao escrever a resposta
     */
    private void novo(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("livro", new Livro());
        req.setAttribute("generos", LivroService.GENEROS);
        encaminhar(req, resp, "formulario.jsp");
    }

    /**
     * Mostra o formulário preenchido para editar um livro. Se ele não existir, volta para a lista.
     *
     * @param req  requisição (parâmetro {@code id})
     * @param resp resposta
     * @throws ServletException se o encaminhamento para a JSP falhar
     * @throws IOException      se houver erro ao escrever a resposta
     * @throws SQLException     se houver problema no banco de dados
     */
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

    /**
     * Trata as requisições POST ({@code /livros/salvar} e {@code /livros/excluir}).
     *
     * @param req  requisição recebida
     * @param resp resposta a enviar
     * @throws ServletException se o encaminhamento para a JSP falhar
     * @throws IOException      se houver erro ao escrever a resposta
     */
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

    /**
     * Cadastra ou atualiza um livro com os dados do formulário. Se a validação falhar,
     * volta ao formulário com as mensagens de erro e o que a pessoa já tinha digitado.
     *
     * @param req  requisição com os campos do formulário
     * @param resp resposta
     * @throws ServletException se o encaminhamento para a JSP falhar
     * @throws IOException      se houver erro ao escrever a resposta
     * @throws SQLException     se houver problema no banco de dados
     */
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

    /**
     * Exclui o livro indicado pelo campo {@code id} e volta para a lista com uma mensagem.
     *
     * @param req  requisição (campo {@code id})
     * @param resp resposta
     * @throws IOException  se houver erro ao redirecionar
     * @throws SQLException se houver problema no banco de dados
     */
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

    /**
     * Parte da URL depois de /livros ("" para a lista, "/novo", "/editar"...).
     *
     * @param req requisição recebida
     * @return a ação pedida, ou texto vazio para a lista
     */
    private String acao(HttpServletRequest req) {
        String info = req.getPathInfo();
        return (info == null || info.equals("/")) ? "" : info;
    }

    /**
     * Lê o parâmetro {@code id}.
     *
     * @param req requisição recebida
     * @return o id, ou 0 se estiver ausente ou não for número
     */
    private int lerId(HttpServletRequest req) {
        return lerInteiro(req.getParameter("id"));
    }

    /**
     * Converte um texto em número inteiro sem lançar exceção.
     *
     * @param texto texto a converter (pode ser {@code null})
     * @return o número, ou 0 se o texto for nulo ou não for um inteiro válido
     */
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
     *
     * @param req requisição com os campos do formulário
     * @return livro preenchido (ainda não validado)
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

    /**
     * Avisa que o livro não existe e volta para a lista.
     *
     * @param req  requisição
     * @param resp resposta
     * @throws IOException se houver erro ao redirecionar
     */
    private void livroNaoEncontrado(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        guardarMensagem(req, "mensagemErro", "Livro não encontrado.");
        resp.sendRedirect(req.getContextPath() + "/livros");
    }

    /**
     * Encaminha a requisição para uma JSP da pasta WEB-INF/views.
     *
     * @param req  requisição
     * @param resp resposta
     * @param jsp  nome do arquivo JSP (ex.: {@code "lista.jsp"})
     * @throws ServletException se a JSP falhar ao ser processada
     * @throws IOException      se houver erro ao escrever a resposta
     */
    private void encaminhar(HttpServletRequest req, HttpServletResponse resp, String jsp)
            throws ServletException, IOException {
        req.getRequestDispatcher(VIEWS + jsp).forward(req, resp);
    }

    /**
     * Mensagem "de uma vez só": guardada na sessão, mostrada na próxima tela e apagada.
     *
     * @param req   requisição (de onde vem a sessão)
     * @param chave nome da mensagem ({@code "mensagem"} para sucesso ou {@code "mensagemErro"})
     * @param texto texto a exibir
     */
    private void guardarMensagem(HttpServletRequest req, String chave, String texto) {
        req.getSession().setAttribute(chave, texto);
    }

    /**
     * Passa as mensagens guardadas na sessão para a requisição atual e as apaga da sessão.
     *
     * @param req requisição que receberá as mensagens
     */
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
     *
     * @param req  requisição
     * @param resp resposta (recebe o status 500)
     * @param e    erro de banco ocorrido
     * @throws ServletException se o encaminhamento para a página de erro falhar
     * @throws IOException      se houver erro ao escrever a resposta
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
