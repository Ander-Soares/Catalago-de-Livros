package br.edu.catalogo.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Responsável por abrir conexões com o MySQL.
 *
 * <p>Os dados de acesso (url, usuário e senha) ficam no arquivo
 * {@code src/main/resources/db.properties}, e não escritos no código. Assim,
 * trocar a senha não exige mexer nem recompilar classes Java.</p>
 *
 * <p>Cada chamada a {@link #obterConexao()} devolve uma conexão nova, que quem
 * a recebeu deve fechar (de preferência com try-with-resources).</p>
 */
public final class ConexaoBD {

    private static final String ARQUIVO_CONFIG = "db.properties";
    private static final Properties CONFIG = carregarConfiguracao();

    /** Classe utilitária: não deve ser instanciada. */
    private ConexaoBD() {
    }

    private static Properties carregarConfiguracao() {
        Properties props = new Properties();
        try (InputStream entrada = ConexaoBD.class.getClassLoader().getResourceAsStream(ARQUIVO_CONFIG)) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "Arquivo " + ARQUIVO_CONFIG + " não encontrado em src/main/resources.");
            }
            props.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler o arquivo " + ARQUIVO_CONFIG, e);
        }

        try {
            // Garante que o driver do MySQL está carregado (importante dentro do Tomcat)
            Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Driver do MySQL não encontrado. Confira a dependência mysql-connector-j no pom.xml.", e);
        }
        return props;
    }

    /**
     * Abre uma nova conexão com o banco.
     *
     * @return conexão aberta (quem chamar precisa fechá-la)
     * @throws SQLException se o banco estiver fora do ar ou usuário/senha estiverem errados
     */
    public static Connection obterConexao() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.usuario"),
                CONFIG.getProperty("db.senha"));
    }
}
