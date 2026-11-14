/**
 * Camada de <b>acesso a dados</b> (padrão DAO - Data Access Object).
 *
 * <ul>
 *   <li>{@link br.edu.catalogo.dao.ConexaoBD}: abre conexões com o MySQL a partir do
 *       arquivo {@code db.properties}.</li>
 *   <li>{@link br.edu.catalogo.dao.LivroDAO}: único lugar do sistema que escreve SQL.
 *       Usa sempre {@code PreparedStatement}, o que impede SQL Injection.</li>
 * </ul>
 */
package br.edu.catalogo.dao;
