-- =============================================================================
-- SCRIPTS DE EXECUÇÃO - POSTGRESQL
-- =============================================================================

-- 1. LIMPEZA E CRIAÇÃO DAS TABELAS (DDL)
DROP TABLE IF EXISTS despesas;
DROP TABLE IF EXISTS operadoras;

CREATE TABLE operadoras (
    registro_ans TEXT PRIMARY KEY,
    cnpj TEXT,
    razao_social TEXT,
    nome_fantasia TEXT,
    modalidade TEXT,
    logradouro TEXT,
    numero TEXT,
    complemento TEXT,
    bairro TEXT,
    cidade TEXT,
    uf TEXT,
    cep TEXT,
    ddd TEXT,
    telefone TEXT,
    fax TEXT,
    email TEXT,
    representante TEXT,
    cargo_representante TEXT,
    regiao_comercializacao TEXT,
    data_registro_ans TEXT
);

CREATE TABLE despesas (
    cnpj TEXT,
    razao_social TEXT,
    registro_ans INT,
    modalidade TEXT,
    uf CHAR(2),
    trimestre INT,
    ano INT,
    valor_despesa NUMERIC(18,2)
);

-- 2. COMANDOS DE IMPORTAÇÃO UTILIZADOS
-- COPY operadoras FROM 'C:/Users/Public/Relatorio_cadop.csv' WITH (FORMAT csv, DELIMITER ';', HEADER, ENCODING 'LATIN1', QUOTE '"');
-- COPY despesas FROM 'C:/Users/Public/consolidado_despesas.csv' WITH (FORMAT csv, DELIMITER ';', HEADER, ENCODING 'UTF8');


-- 3. QUERIES ANALÍTICAS (ITEM 3.4)

-- Query 1: Top 5 crescimento percentual (1T vs 3T)
WITH trimestres AS (
    SELECT 
        registro_ans,
        SUM(CASE WHEN trimestre = 1 THEN valor_despesa ELSE 0 END) as t1,
        SUM(CASE WHEN trimestre = 3 THEN valor_despesa ELSE 0 END) as t3
    FROM despesas
    GROUP BY registro_ans
)
SELECT 
    o.razao_social,
    t1 as valor_inicial,
    t3 as valor_final,
    ROUND(((t3 - t1) / NULLIF(t1, 0)) * 100, 2) as crescimento_percentual
FROM trimestres
JOIN operadoras o ON o.registro_ans::int = trimestres.registro_ans::int
WHERE t1 > 0 
ORDER BY crescimento_percentual DESC
LIMIT 5;


-- Query 2: Top 5 Estados (UF) por gasto total e média por operadora
SELECT 
    uf, 
    SUM(valor_despesa) as total_despesas,
    ROUND(AVG(valor_despesa), 2) as media_por_operadora
FROM despesas
GROUP BY uf
ORDER BY total_despesas DESC
LIMIT 5;


-- Query 3: Contagem de operadoras acima da média geral em pelo menos 2 trimestres
WITH media_geral AS (
    SELECT AVG(valor_despesa) as m_geral FROM despesas
)
SELECT COUNT(*) as total_vencedoras
FROM (
    SELECT registro_ans
    FROM despesas, media_geral
    WHERE valor_despesa > media_geral.m_geral
    GROUP BY registro_ans
    HAVING COUNT(DISTINCT trimestre) >= 2
) as subquery;