package br.com.intuitivecare.ans.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import br.com.intuitivecare.ans.model.Operadora;
import br.com.intuitivecare.ans.model.ConsolidatedRecord;
import br.com.intuitivecare.ans.service.ConsolidationService;

public class FileProcessor {

    // Carrega as operadoras a partir do csv Relatorio_cadop
    public static Map<String, Operadora> loadOperadoras(Path file) throws IOException {

        Map<String, Operadora> operadoras = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            // Ignora cabeçalho
            String header = br.readLine();
            if (header == null) return operadoras;

            String line;
            while ((line = br.readLine()) != null) {

                // csv separado por ;
                String[] cols = line.split(";", -1);
                if (cols.length < 3) continue;

                String registro = cols[0].trim();
                String cnpj = cols[1].trim();
                String razao = cols[2].trim();

                // Mapeia pelo registro ANS
                operadoras.put(registro, new Operadora(registro, cnpj, razao));
            }
        }

        System.out.println("Operadoras carregadas: " + operadoras.size());
        return operadoras;
    }

    // Processa um arquivo csv de um trimestre especifico
    public static void processTrimestre(Path file, int ano, int trimestre,
                                        Map<String, Operadora> operadoras,
                                        ConsolidationService service) throws IOException {

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            // Ler cabeçalho para identificar colunas
            String header = br.readLine();
            if (header == null) return;

            String[] headers = header.split(";", -1);

            int regAnsIdx = -1;
            int descricaoIdx = -1;
            int valorFinalIdx = -1;

            // Descobre os índices das colunas necessárias
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toLowerCase();
                if (h.contains("reg_ans")) regAnsIdx = i;
                else if (h.contains("descricao")) descricaoIdx = i;
                else if (h.contains("vl_saldo_final")) valorFinalIdx = i;
            }

            // Se faltar coluna obrigatoria, ignora o arquivo
            if (regAnsIdx == -1 || descricaoIdx == -1 || valorFinalIdx == -1) {
                System.out.println("Arquivo ignorado (colunas obrigatórias não encontradas): " + file.getFileName());
                return;
            }

            int linhasRelevantes = 0;
            String line;

            while ((line = br.readLine()) != null) {

                String[] cols = line.split(";", -1);
                if (cols.length <= valorFinalIdx) continue;

                // Filtra apenas despesas com eventos ou sinistros
                String descricao = cols[descricaoIdx];

                if (!isDespesaEventoOuSinistro(descricao)) continue;

                String regAns = cols[regAnsIdx].trim();

                Operadora op = operadoras.get(regAns);

                if (op == null) continue;

                BigDecimal valor;

                try {
                    valor = new BigDecimal(cols[valorFinalIdx].replace(",", "."));
                } catch (Exception e) {
                    continue;
                }

                // IGNORA valores zerados ou negativos (inconsistência)
                if (valor.compareTo(BigDecimal.ZERO) <= 0) continue;

                // envia o registro para consolidação
                service.addRecord(
                        new ConsolidatedRecord(
                                op.getCnpj(),
                                op.getRazaoSocial(),
                                trimestre,
                                ano,
                                valor
                        )
                );

                linhasRelevantes++;
            }

            if (linhasRelevantes > 0) {
                System.out.println("Arquivo processado: " + file.getFileName()
                        + " -> Linhas relevantes: " + linhasRelevantes);
            } else {
                System.out.println("Arquivo ignorado (nenhuma linha de despesa relevante): "
                        + file.getFileName());
            }
        }
    }

    // Define o que é considerado despesa com evento ou sinistro
    private static boolean isDespesaEventoOuSinistro(String descricao) {

        if (descricao == null || descricao.isEmpty()) return false;

        // Normalização para evitar problemas de escrita/acento
        descricao = descricao.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        boolean hasDespesa = descricao.contains("despesa");
        boolean hasEvento = descricao.contains("eventos");
        boolean hasSinistro = descricao.contains("sinistro");

        return hasDespesa && (hasEvento || hasSinistro);
    }

    // Baixa um arquivo (csv ou zip) a partir de uma URL
    public static Path downloadFile(String urlStr, Path dest) throws IOException {

        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Baixado: " + dest.getFileName());
        return dest;
    }
}


