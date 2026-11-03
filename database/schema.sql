-- =====================================================================
--  Catálogo de Livros — script de criação do banco de dados (MySQL 8+)
--  Execute no MySQL Workbench: File > Open SQL Script > raio (⚡) executar tudo.
--  Rode com um usuário administrador (ex.: root).
-- =====================================================================

SET NAMES utf8mb4;

-- 1) Banco de dados com UTF-8 completo (acentos e emojis)
CREATE DATABASE IF NOT EXISTS catalogo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE catalogo;

-- 2) Tabela principal.
--    Escolhas de modelagem (úteis para o relatório da Situação-Problema 1):
--      * id AUTO_INCREMENT como chave primária: identifica cada livro de forma única
--        e é o que as telas de detalhes/edição/exclusão usam.
--      * tipos e tamanhos adequados (VARCHAR com limite, ano como SMALLINT):
--        o próprio banco ajuda a barrar dados absurdos.
--      * NOT NULL nos campos obrigatórios + CHECK no ano: validação também no banco,
--        não só na tela.
--      * índices em titulo, autor e genero: aceleram buscas e filtros.
CREATE TABLE IF NOT EXISTS livros (
    id        INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    titulo    VARCHAR(150)  NOT NULL,
    autor     VARCHAR(100)  NOT NULL,
    ano       SMALLINT      NOT NULL,
    genero    VARCHAR(40)   NOT NULL,
    detalhes  TEXT          NULL,
    capa_url  VARCHAR(500)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_livros_ano CHECK (ano BETWEEN 1 AND 2100),
    INDEX idx_livros_titulo (titulo),
    INDEX idx_livros_autor  (autor),
    INDEX idx_livros_genero (genero)
) ENGINE = InnoDB;

-- 3) Dados de exemplo (só insere se a tabela estiver vazia)
INSERT INTO livros (titulo, autor, ano, genero, detalhes)
SELECT * FROM (
    SELECT 'Dom Casmurro' AS titulo, 'Machado de Assis' AS autor, 1899 AS ano, 'Romance' AS genero,
           'Bentinho narra a própria vida e tenta resolver a dúvida que o consome: Capitu o traiu ou não?' AS detalhes
    UNION ALL
    SELECT 'O Hobbit', 'J. R. R. Tolkien', 1937, 'Fantasia',
           'Bilbo Bolseiro, um hobbit pacato, é arrastado para uma viagem com treze anões para recuperar um tesouro guardado por um dragão.'
    UNION ALL
    SELECT '1984', 'George Orwell', 1949, 'Ficção',
           'Em um Estado totalitário que vigia tudo, Winston tenta guardar um pouco de liberdade e verdade.'
    UNION ALL
    SELECT 'Capitães da Areia', 'Jorge Amado', 1937, 'Drama',
           'Um grupo de meninos de rua vive de pequenos furtos pelas ruas de Salvador e cria seus próprios laços de lealdade.'
    UNION ALL
    SELECT 'Código Limpo', 'Robert C. Martin', 2008, 'Técnico',
           'Práticas para escrever código legível, simples e fácil de manter.'
) AS exemplo
WHERE NOT EXISTS (SELECT 1 FROM livros);

-- 4) Usuário exclusivo da aplicação, com o MÍNIMO de permissões (princípio do menor privilégio).
--    A aplicação nunca usa o root: se alguém explorar uma falha, não consegue apagar
--    tabelas nem mexer em outros bancos.
--    (Troque a senha abaixo se quiser; lembre de mudar também em db.properties.)
CREATE USER IF NOT EXISTS 'catalogo_app'@'localhost' IDENTIFIED BY 'catalogo123';
GRANT SELECT, INSERT, UPDATE, DELETE ON catalogo.* TO 'catalogo_app'@'localhost';
FLUSH PRIVILEGES;

-- 5) Conferência: deve listar 5 livros
SELECT id, titulo, autor, ano, genero FROM livros ORDER BY id;
