<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/jspf/dados-teste.jspf" %>
<%-- Mesmo formulario para cadastrar (sem livro) e editar (com livro) --%>
<c:set var="tituloPagina" value="${empty livro ? 'Novo livro' : 'Editar livro'}" scope="request"/>
<%@ include file="/WEB-INF/jspf/cabecalho.jspf" %>

<a class="link-voltar" href="listar.jsp">← Voltar para a lista</a>
<h1><c:out value="${tituloPagina}"/></h1>

<%-- O backend vai preencher ${erro} quando a validacao do servidor falhar --%>
<c:if test="${not empty erro}">
    <div class="aviso aviso-erro"><c:out value="${erro}"/></div>
</c:if>

<%-- TODO (backend): a action vai apontar para o Servlet de salvar --%>
<form class="formulario vidro" method="post" action="listar.jsp" style="margin-top:18px">
    <c:if test="${not empty livro}">
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
                   value="<c:out value='${livro.ano}'/>">
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
        <a class="botao botao-sec" href="listar.jsp">Cancelar</a>
    </div>
</form>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
