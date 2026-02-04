# Etapa 1

## Consolidação de Despesas – ANS

Projeto para processamento, normalização e consolidação de dados de despesas com
Eventos/Sinistros a partir dos arquivos públicos da ANS.

---

## Observação
Este README documenta as decisões técnicas referentes aos testes **1.1 a 3.4**
do desafio proposto. As etapas seguintes serão documentadas conforme
implementadas.

---

## Funcionalidades Implementadas
1.1 a 1.3
- Download automático dos arquivos trimestrais da ANS
- Extração de arquivos zip
- Processamento apenas de despesas com Eventos ou Sinistros
- Identificação dinâmica da estrutura dos arquivos
- Consolidação dos dados por CNPJ, ano e trimestre
- Geração de CSV consolidado final
- arquivo zipado
- ----------------------------------------------------------------------
2.1 a 2.3
- Cruzamento de dados (Join) com o cadastro de operadoras ativas (CADOP)
- Enriquecimento de dados com colunas de Registro ANS, Modalidade e UF
- Agrupamento estatístico por Razão Social e UF
- Calculo de Média Trimestral e Desvio Padrão de despesas
- Ordenação decrescente por volume total de gastos
- Interface interativa via console para nomeação do pacote final (zip)
- ----------------------------------------------------------------------
3.1 a 3.4
- Estruturação de banco de dados relacional (PostgreSQL) para persistência de longo prazo
- Implementação de DDL com chaves primárias e índices para otimização de buscas
- Script de automação de carga de dados via comando COPY com suporte a múltiplos encodings
- Normalização de dados financeiros e cadastrais em tabelas distintas (Opção B)
- Desenvolvimento de query para cálculo de crescimento percentual entre o 1T e o 3T com tratamento de divisão por zero
- Análise de distribuição geográfica de despesas com cálculo de média por operadora em cada UF
- Query complexa com agregação e filtros para identificar operadoras acima da média geral em multiplos períodos
- Tratamento e sanitização de dados inconsistentes (NULLs e Strings) durante o pipeline de importação
---

## Trade-offs Técnicos

### Estratégia de Processamento (1.3)
Foi adotado o processamento incremental (linha a linha) dos arquivos, em vez de carregar todos os dados em memória.
- #### Justificativa:
- Redução drástica do consumo de RAM diante do grande volume de dados da ANS, facilitando a escalabilidade.

### Validação de Dados (2.1)
Adotei a estratégia de Filtragem Preemptiva .
- #### Justificativa:
- Registros inválidos (CNPJ incorreto ou valores negativos) são descartados na leitura feita em etapas anteriores. Isso evita o processamento de "dados sujos" no pipeline estatístico, garantindo a precisão do Desvio Padrão e da Média.

### Estratégia de Join (2.2)
Utilizei o método de para cruzar as despesas com o cadastro CADOP.
- #### Justificativa:
- Como o cadastro de operadoras é pequeno (~1.100 registros), mantê-lo em um `Map` permite buscas instantâneas ($O(1)$) enquanto processamos milhões de linhas de despesas, otimizando o tempo total de execução.

### Ordenação e Agregação (2.3)
Utilizei a estratégia via Java Streams para o relatório final.
- #### Justificativa:
- Após a agregação, o volume de dados é reduzido para cerca de 2.000 grupos. Ordenar esse volume em memória é extremamente eficiente e evita a complexidade de algoritmos de ordenação externa.

### Modelagem de Dados e Persistência (3.2)
Adotei a Opção B (Tabelas Normalizadas) para a estruturação do banco de dados.
#### Justificativa:
Separação clara entre dados cadastrais (fixos) e financeiros (sazonais). Isso reduz a redundância, economiza espaço em disco e permite atualizações no cadastro das operadoras sem a necessidade de replicar a alteração em milhões de registros de despesas.

### Escolha de Tipos de Dados (3.2)
Utilizei o tipo DECIMAL/NUMERIC para valores monetários e INTEGER para períodos.
#### Justificativa:
O uso de DECIMAL é indispensável para garantir a precisão de centavos em cálculos financeiros, evitando os erros de arredondamento inerentes ao tipo FLOAT. O tipo INTEGER para trimestres e anos otimiza a performance de indexação e busca.

### Resolução de Queries Analíticas (3.4)
Utilizei a estratégia de Common Table Expressions (CTEs) e Subqueries com Having.
#### Justificativa:
Essa abordagem aumenta a legibilidade e a manutenibilidade do código SQL. Para o cálculo de crescimento (Query 1), o uso de NULLIF foi estratégico para evitar erros de divisão por zero em operadoras que não possuíam dados no período inicial, garantindo a robustez do relatório.
 
---

## Análises Críticas 

### Integridade dos Dados 1.3

- CNPJs duplicados com nomes diferentes: A consolidação prioriza o CNPJ como chave única, garantindo que a soma financeira seja atribuída à entidade fiscal correta, independentemente de variações na string de Razão Social.
  
- Valores zerados ou negativos: Ignorados, pois não representam eventos reais de despesa e causariam distorção no cálculo da volatilidade (Desvio Padrão).
  
- Padronização de Trimestres: A identificação do período é feita via nome do arquivo zip, garantindo consistência mesmo que os metadados internos dos arquivos csv variem.

### Cruzamento de Dados (Join) 2.2

- Registros sem correspondência no CADOP: Despesas cujo CNPJ não consta no cadastro foram descartadas. Sem o match, não há informações de UF e Modalidade, inviabilizando o agrupamento geográfico exigido.
  
- CNPJs duplicados no cadastro: Para CNPJs com múltiplas entradas, adotou-se o First Match. Isso evita que o valor da despesa seja multiplicado indevidamente no join adicionando apenas o primeiro lido.


### Importação e Tratamento de Inconsistências 3.3
- Valores NULL em campos obrigatórios: Registros com valor_despesa ou registro_ans nulos foram rejeitados. A inclusão de dados incompletos comprometeria a integridade estatística da média geral e do desvio padrão exigidos no desafio.

- Strings em campos numéricos: Valores financeiros com pontuação brasileira (vírgula como decimal) passaram por sanitização via Regex. A conversão para o padrão ANSI foi necessária para garantir a precisão do tipo NUMERIC e evitar falhas de cálculo no PostgreSQL.

- Datas em formatos inconsistentes: Formatos de período (ex: 1T2025) foram decompostos em colunas INTEGER de ano e trimestre. Essa normalização elimina ambiguidades de string e permite o uso de índices numéricos, otimizando drasticamente a performance das queries analíticas.
  

