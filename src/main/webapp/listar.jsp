<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Livros" scope="request"/>
<%@ include file="/WEB-INF/jspf/dados-teste.jspf" %>
<%@ include file="/WEB-INF/jspf/cabecalho.jspf" %>

<section class="hero vidro">
    <p class="sobretitulo">Catálogo pessoal</p>
    <h1>Descubra, organize e <em>guarde</em> seus livros</h1>

    <%-- Busca por título ou autor (GET: a busca aparece na URL) --%>
    <form class="busca" method="get" action="listar.jsp" role="search">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>
        <input type="search" name="busca" placeholder="Buscar por título ou autor"
               aria-label="Buscar por título ou autor" value="<c:out value='${param.busca}'/>">
        <c:if test="${not empty param.genero}">
            <input type="hidden" name="genero" value="<c:out value='${param.genero}'/>">
        </c:if>
        <button class="botao" type="submit">Buscar</button>
    </form>

    <%-- Filtro por gênero: cada chip é um link que mantém a busca atual --%>
    <nav class="chips" aria-label="Filtrar por gênero">
        <c:url var="urlTodos" value="listar.jsp">
            <c:param name="busca" value="${param.busca}"/>
        </c:url>
        <a class="chip ${empty param.genero ? 'ativo' : ''}" href="<c:out value='${urlTodos}'/>">Todos</a>
        <c:forEach var="g" items="${generos}">
            <c:url var="urlGenero" value="listar.jsp">
                <c:param name="busca" value="${param.busca}"/>
                <c:param name="genero" value="${g}"/>
            </c:url>
            <a class="chip ${param.genero == g ? 'ativo' : ''}" href="<c:out value='${urlGenero}'/>"><c:out value="${g}"/></a>
        </c:forEach>
    </nav>
</section>

<div class="resultado-topo">
    <p class="contagem">${livros.size()} livro(s) encontrado(s)</p>
    <c:if test="${not empty param.busca or not empty param.genero}">
        <a class="link-limpar" href="listar.jsp">Limpar filtros</a>
    </c:if>
</div>

<c:if test="${empty livros}">
    <div class="aviso">Nenhum livro encontrado. Tente outra busca ou cadastre um novo livro.</div>
</c:if>

<section class="grade">
    <c:forEach var="l" items="${livros}">
        <article class="livro">
            <c:choose>
                <c:when test="${not empty l.capaUrl}">
                    <a href="detalhes.jsp?id=${l.id}">
                        <img class="capa" src="<c:out value='${l.capaUrl}'/>" alt="Capa de <c:out value='${l.titulo}'/>">
                    </a>
                </c:when>
                <c:otherwise>
                    <%-- Sem imagem: capa gerada com o título (cor varia pelo id) --%>
                    <a class="capa capa-${l.id % 6}" href="detalhes.jsp?id=${l.id}" aria-label="Ver <c:out value='${l.titulo}'/>">
                        <span class="capa-genero"><c:out value="${l.genero}"/></span>
                        <span>
                            <span class="capa-titulo"><c:out value="${l.titulo}"/></span><br>
                            <span class="capa-autor"><c:out value="${l.autor}"/></span>
                        </span>
                    </a>
                </c:otherwise>
            </c:choose>

            <div class="livro-info">
                <h3><a href="detalhes.jsp?id=${l.id}"><c:out value="${l.titulo}"/></a></h3>
                <p class="autor"><c:out value="${l.autor}"/></p>
                <p class="meta">
                    <span class="etiqueta"><c:out value="${l.genero}"/></span>
                    <span><c:out value="${l.ano}"/></span>
                </p>
            </div>

            <div class="acoes">
                <a class="botao botao-sec" href="formulario.jsp?id=${l.id}">Editar</a>
                <%-- TODO (backend): a action vai apontar para o Servlet de exclusao --%>
                <form method="post" action="listar.jsp"
                      onsubmit="return confirm('Tem certeza que deseja excluir este livro?');">
                    <input type="hidden" name="id" value="${l.id}">
                    <button class="botao botao-perigo" type="submit">Excluir</button>
                </form>
            </div>
        </article>
    </c:forEach>
</section>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
