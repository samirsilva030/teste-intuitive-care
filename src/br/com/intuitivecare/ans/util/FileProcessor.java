package br.com.intuitivecare.ans.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class FileProcessor {


    public static List<String[]> processFile(Path file) throws IOException {

        // Lista que armazenara apenas as linhas relevantes
        List<String[]> data = new ArrayList<>();

        String fileName = file.getFileName().toString().toLowerCase();

        // Segurança: processa apenas arquivos CSV ou TXT
        if (!fileName.endsWith(".csv") && !fileName.endsWith(".txt")) {
            return data;
        }

        // Leitura do arquivo usando UTF-8 (padrão dos arquivos da ANS)
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            // Leitura do cabeçalho
            String headerLine = br.readLine();
            if (headerLine == null) {
                // Arquivo vazio
                return data;
            }

            // Descobre dinamicamente a posição da coluna "DESCRICAO"
            int descricaoIdx = findDescricaoIndex(headerLine);

            // Se não existir coluna de descrição, o arquivo é ignorado
            if (descricaoIdx == -1) {
                System.out.println("Arquivo ignorado (sem coluna DESCRICAO): " + file.getFileName());
                return data;
            }

            String line;

            // Processamento incremental: ler uma linha por vez
            while ((line = br.readLine()) != null) {

                // Remove espaços extras
                line = line.trim();

                // Ignora linhas vazias
                if (line.isEmpty()) continue;

                // Quebra a linha em colunas
                String[] cols = parseLine(line);

                // Proteção contra linhas mal formatadas
                if (cols.length <= descricaoIdx) continue;

                // Normaliza o texto da coluna DESCRICAO
                String descricao = normalize(cols[descricaoIdx]);

                // Aplica a regra de negócio
                if (isDespesaEventoOuSinistro(descricao)) {
                    data.add(cols);
                }
            }
        }

        if (!data.isEmpty()) {
            System.out.println("Arquivo processado: " + file.getFileName() + " -> Linhas relevantes: " + data.size());
        } else {
            System.out.println("Arquivo ignorado (nenhuma linha de despesa relevante): " + file.getFileName());
        }
        
        return data;
    }

    //encontra a coluna descrição para filtrar as linhas
    private static int findDescricaoIndex(String headerLine) {

        String[] headers = headerLine.replace("\"", "").split(";", -1);

        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].trim().toLowerCase();

            if (h.contains("descricao")) {
                return i;
            }
        }
        return -1;
    }

    //Formatação das linhas
    private static String[] parseLine(String line) {

        String[] cols = line.split(";", -1);

        for (int i = 0; i < cols.length; i++) {
            cols[i] = cols[i].replaceAll("^\"|\"$", "").trim();
        }
        return cols;
    }
    
    //Padroniza para evitar erro na comparação
    private static String normalize(String text) {

        return text == null ? "" : text.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    //Metodo que filtra apenas os que tem despesa com evento OU sinistro
    private static boolean isDespesaEventoOuSinistro(String descricao) {

        boolean hasDespesa = descricao.contains("despesa");

        boolean hasEvento = descricao.contains("evento");

        boolean hasSinistro = descricao.contains("sinistro");

        return hasDespesa && (hasEvento || hasSinistro);
    }
}

