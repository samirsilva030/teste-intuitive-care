# Etapa 1

## Consolidação de Despesas – ANS

Projeto para processamento, normalização e consolidação de dados de despesas com
Eventos/Sinistros a partir dos arquivos públicos da ANS.

---

## Observação
Este README documenta as decisões técnicas referentes aos testes **1.1 a 2.3**
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

---

## Análises Críticas 

### Integridade dos Dados 1.3

- CNPJs duplicados com nomes diferentes: A consolidação prioriza o CNPJ como chave única, garantindo que a soma financeira seja atribuída à entidade fiscal correta, independentemente de variações na string de Razão Social.
  
- Valores zerados ou negativos: Ignorados, pois não representam eventos reais de despesa e causariam distorção no cálculo da volatilidade (Desvio Padrão).
  
- Padronização de Trimestres: A identificação do período é feita via nome do arquivo zip, garantindo consistência mesmo que os metadados internos dos arquivos csv variem.

### Cruzamento de Dados (Join) 2.2
- Registros sem correspondência no CADOP: Despesas cujo CNPJ não consta no cadastro foram descartadas. Sem o match, não há informações de UF e Modalidade, inviabilizando o agrupamento geográfico exigido.
  
- CNPJs duplicados no cadastro: Para CNPJs com múltiplas entradas, adotou-se o First Match. Isso evita que o valor da despesa seja multiplicado indevidamente no join adicionando apenas o primeiro lido.



  

