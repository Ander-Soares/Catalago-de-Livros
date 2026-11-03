<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Detalhes do livro" scope="request"/>
<%-- Atributo vindo do LivroServlet: livro --%>
<%@ include file="/WEB-INF/jspf/cabecalho.jspf" %>

<a class="link-voltar" href="${pageContext.request.contextPath}/livros">← Voltar para a lista</a>

<article class="detalhes vidro">
    <c:choose>
        <c:when test="${not empty livro.capaUrl}">
            <img class="capa" src="<c:out value='${livro.capaUrl}'/>" alt="Capa de <c:out value='${livro.titulo}'/>">
        </c:when>
        <c:otherwise>
            <div class="capa capa-${livro.id % 6}">
                <span class="capa-genero"><c:out value="${livro.genero}"/></span>
                <span>
                    <span class="capa-titulo"><c:out value="${livro.titulo}"/></span><br>
                    <span class="capa-autor"><c:out value="${livro.autor}"/></span>
                </span>
            </div>
        </c:otherwise>
    </c:choose>

    <div class="detalhes-info">
        <h1><c:out value="${livro.titulo}"/></h1>
        <p class="autor"><c:out value="${livro.autor}"/></p>

        <dl class="ficha">
            <dt>Ano de publicação</dt>
            <dd><c:out value="${livro.ano}"/></dd>
            <dt>Gênero</dt>
            <dd><span class="etiqueta"><c:out value="${livro.genero}"/></span></dd>
        </dl>

        <h2>Sinopse</h2>
        <p class="sinopse">
            <c:choose>
                <c:when test="${not empty livro.detalhes}"><c:out value="${livro.detalhes}"/></c:when>
                <c:otherwise>Este livro ainda não tem sinopse.</c:otherwise>
            </c:choose>
        </p>

        <div class="acoes">
            <a class="botao" href="${pageContext.request.contextPath}/livros/editar?id=${livro.id}">Editar</a>
            <form method="post" action="${pageContext.request.contextPath}/livros/excluir"
                  onsubmit="return confirm('Tem certeza que deseja excluir este livro?');">
                <input type="hidden" name="id" value="${livro.id}">
                <button class="botao botao-perigo" type="submit">Excluir</button>
            </form>
        </div>
    </div>
</article>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
