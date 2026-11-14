/**
 * Camada de <b>serviço</b>: regras de negócio e validações.
 *
 * <p>{@link br.edu.catalogo.service.LivroService} decide se os dados de um livro são
 * aceitáveis antes de chegarem ao banco e lança
 * {@link br.edu.catalogo.service.ValidacaoException} com todas as mensagens de erro.
 * Não depende de HTTP nem de Tomcat, por isso pode ser testada isoladamente.</p>
 */
package br.edu.catalogo.service;
