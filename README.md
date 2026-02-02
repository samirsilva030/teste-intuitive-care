# Etapa 1

## Consolidação de Despesas – ANS

Projeto para processamento, normalização e consolidação de dados de despesas com
Eventos/Sinistros a partir dos arquivos públicos da ANS.

---

## Observação
Este README documenta as decisões técnicas referentes aos testes **1.1, 1.2 e 1.3**
do desafio proposto. As etapas seguintes serão documentadas conforme
implementadas.

---

## Funcionalidades Implementadas

- Download automático dos arquivos trimestrais da ANS
- Extração de arquivos zip
- Processamento apenas de despesas com Eventos ou Sinistros
- Identificação dinâmica da estrutura dos arquivos
- Consolidação dos dados por CNPJ, ano e trimestre
- Geração de CSV consolidado final
- arquivo zipado

---

## Trade-off Técnico e Análise de Inconsistências

### Trade-off Técnico — Estratégia de Processamento

Foi adotado o processamento incremental (linha a linha) dos arquivos, em vez de
carregar todos os dados em memória de uma única vez.

**Justificativa:**
- Os arquivos trimestrais da ANS possuem grande volume de dados
- O processamento incremental reduz demais o consumo de memória
- Para facilitar a escalabilidade do sistema

---

### Análise Crítica e Tratamento de Inconsistências

Durante a consolidação dos dados, foram identificadas as seguintes inconsistências:

#### CNPJs duplicados com razões sociais diferentes
- Tratamento: consolidação realizada com base no CNPJ
- Justificativa: o CNPJ é a chave única e confiável da operadora; a razão social
  pode variar por atualização cadastral ou divergência entre arquivos

#### Valores zerados ou negativos
- Tratamento: registros ignorados
- Justificativa: valores iguais ou menores que zero não representam despesas
  válidas e poderiam distorcer o resultado consolidado

#### Trimestres com formatos inconsistentes
- Tratamento: padronização do trimestre a partir do nome do arquivo zip
- Justificativa: o nome do arquivo é a fonte mais confiável e garante consistência
  entre diferentes layouts internos

