<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Ops" scope="request"/>
<%@ include file="/WEB-INF/jspf/cabecalho.jspf" %>

<section class="hero vidro">
    <p class="sobretitulo">Algo não saiu como esperado</p>
    <h1>Ops, tivemos um problema</h1>
    <p class="autor" style="margin-top:14px">
        <c:choose>
            <c:when test="${not empty mensagemErro}"><c:out value="${mensagemErro}"/></c:when>
            <c:otherwise>A página não foi encontrada ou ocorreu um erro inesperado.</c:otherwise>
        </c:choose>
    </p>
    <p style="margin-top:22px">
        <a class="botao" href="${pageContext.request.contextPath}/livros">Voltar para a lista</a>
    </p>
</section>

<%@ include file="/WEB-INF/jspf/rodape.jspf" %>
