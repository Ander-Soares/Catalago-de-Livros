<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Atributos vindos do LivroServlet: livro, generos e (se a validação falhou) erros.
     Mesmo formulário para cadastrar (livro.id = 0) e editar (livro.id > 0). --%>
<c:set var="editando" value="${livro.id > 0}"/>
<c:set var="tituloPagina" value="${editando ? 'Editar livro' : 'Novo livro'}" scope="request"/>
<%@ include file="/WEB-INF/jspf/cabecalho.jspf" %>

<a class="link-voltar" href="${pageContext.request.contextPath}/livros">← Voltar para a lista</a>
<h1><c:out value="${tituloPagina}"/></h1>

<%-- Erros de validação do servidor (o formulário volta preenchido com o que a pessoa digitou) --%>
<c:if test="${not empty erros}">
    <div class="aviso aviso-erro" role="alert">
        <strong>Corrija os campos abaixo:</strong>
        <ul>
            <c:forEach var="e" items="${erros}">
                <li><c:out value="${e}"/></li>
            </c:forEach>
        </ul>
    </div>
</c:if>

<form class="formulario vidro" method="post" action="${pageContext.request.contextPath}/livros/salvar" style="margin-top:18px">
    <c:if test="${editando}">
        <input type="hidden" name="id" value="${livro.id}">
    </c:if>

    <label for="titulo">Título *</label>
    <input type="text" id="titulo" name="titulo" required maxlength="150"
           value="<c:out value='${livro.titulo}'/>">

    <label for="autor">Autor *</label>
    <input type="text" id="autor" name="autor" required maxlength="100"
           value="<c:out value='${livro.autor}'/>">

    <div class="linha">
        <div>
            <label for="ano">Ano de publicação *</label>
            <input type="number" id="ano" name="ano" required min="1" max="2100"
                   value="${livro.ano > 0 ? livro.ano : ''}">
        </div>
        <div>
            <label for="genero">Gênero *</label>
            <select id="genero" name="genero" required>
                <option value="">Selecione...</option>
                <c:forEach var="g" items="${generos}">
                    <option value="${g}" ${livro.genero == g ? 'selected' : ''}><c:out value="${g}"/></option>
                </c:forEach>
            </select>
        </div>
    </div>

    <label for="capaUrl">Link da capa (opcional)</label>
    <input type="url" id="capaUrl" name="capaUrl" maxlength="500" placeholder="https://..."
           value="<c:out value='${livro.capaUrl}'/>">

    <label for="detalhes">Sinopse / detalhes</label>
    <textarea id="detalhes" name="detalhes" rows="5" maxlength="2000"><c:out value="${livro.detalhes}"/></textarea>

    <div class="acoes">
        <button class="botao" type="submit">Salvar</button>
        <a class="botao botao-sec" href="${pageContext.request.contextPath}/livros">Cancelar</a>
    </div>
</form>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
