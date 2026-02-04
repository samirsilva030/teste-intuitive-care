package br.com.intuitivecare.ans.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import br.com.intuitivecare.ans.model.Operadora;
import br.com.intuitivecare.ans.model.ConsolidatedRecord;
import br.com.intuitivecare.ans.service.ConsolidationService;

public class FileProcessor {

    /**
     * Carrega as operadoras mapeando as colunas dinamicamente pelos nomes.
     * Isso evita que campos como "Bairro" entrem no lugar da "UF".
     */
    public static Map<String, Operadora> loadOperadoras(Path file) throws IOException {
        Map<String, Operadora> operadoras = new HashMap<>();

        // ISO_8859_1 é essencial para os arquivos da ANS não virem com interrogações no lugar de acentos
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.ISO_8859_1)) {
            String headerLine = br.readLine();
            if (headerLine == null) return operadoras;

            // Remove o caractere invisível BOM que a ANS costuma colocar no inicio do arquivo
            headerLine = headerLine.replace("\uFEFF", "");
            String[] headers = headerLine.split(";", -1);

            // Indices que vamos descobrir
            int idxReg = -1, idxCnpj = -1, idxRazao = -1, idxMod = -1, idxUf = -1;

            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toUpperCase();
                
                // Verificação precisa para não confundir 'UF' com 'SUFIXO' ou 'LOGRADOURO'
                if (h.equals("REGISTRO_ANS")) idxReg = i;
                else if (h.equals("CNPJ")) idxCnpj = i;
                else if (h.equals("RAZAO_SOCIAL")) idxRazao = i;
                else if (h.equals("MODALIDADE")) idxMod = i;
                else if (h.equals("UF")) idxUf = i;
            }

            // Fallback caso o cabeçalho mude: se não achou pelo nome, usa as posições padrão da ANS
            if (idxReg == -1) idxReg = 0;
            if (idxCnpj == -1) idxCnpj = 1;
            if (idxRazao == -1) idxRazao = 2;
            if (idxMod == -1) idxMod = 5;
            if (idxUf == -1) idxUf = 12;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(";", -1);

                if (cols.length <= Math.max(idxReg, idxUf)) continue;

                // Limpa as aspas (") que a ANS coloca em volta dos nomes
                String registro = cols[idxReg].replace("\"", "").trim();
                String cnpj = cols[idxCnpj].replace("\"", "").trim();
                String razao = cols[idxRazao].replace("\"", "").trim();
                String modalidade = cols[idxMod].replace("\"", "").trim();
                String uf = cols[idxUf].replace("\"", "").trim();

                if (!registro.isEmpty()) {
                    operadoras.put(registro, new Operadora(registro, cnpj, razao, modalidade, uf));
                }
            }
        }
        System.out.println("Operadoras carregadas: " + operadoras.size());
        return operadoras;
    }
    
    /**
     * Processa os dados financeiros trimestrais e realiza o Enriquecimento (Join) 
     * com os dados cadastrais em memoria.
     */

    public static void processTrimestre(Path file, int ano, int trimestre,
                                        Map<String, Operadora> operadoras,
                                        ConsolidationService service) throws IOException {

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.ISO_8859_1)) {
            String headerLine = br.readLine();
            if (headerLine == null) return;

            headerLine = headerLine.replace("\uFEFF", "");
            String[] headers = headerLine.split(";", -1);

            int idxReg = -1, idxDesc = -1, idxValor = -1;

            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toUpperCase();
                if (h.contains("REG_ANS")) idxReg = i;
                else if (h.contains("DESCRICAO")) idxDesc = i;
                else if (h.contains("VL_SALDO_FINAL")) idxValor = i;
            }

            if (idxReg == -1 || idxDesc == -1 || idxValor == -1) return;

            int count = 0;
            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(";", -1);
                if (cols.length <= idxValor) continue;

                if (!isDespesaEventoOuSinistro(cols[idxDesc])) continue;

                String regAns = cols[idxReg].replace("\"", "").trim();
                Operadora op = operadoras.get(regAns);

                if (op != null) {
                    try {
                        String valStr = cols[idxValor].replace("\"", "").replace(",", ".");
                        BigDecimal valor = new BigDecimal(valStr);

                        if (valor.compareTo(BigDecimal.ZERO) > 0) {
                            service.addRecord(new ConsolidatedRecord(
                                op.getCnpj(), op.getRazaoSocial(), op.getRegistro(),
                                op.getModalidade(), op.getUf(), trimestre, ano, valor
                            ));
                            count++;
                        }
                    } catch (Exception e) {}
                }
            }
            if (count > 0) System.out.println("Processado " + file.getFileName() + ": " + count + " linhas.");
        }
    }
    
    // Validação baseada em palavras-chave para identificar linhas de despesa relevantes.
    private static boolean isDespesaEventoOuSinistro(String desc) {
        if (desc == null) return false;
        String d = desc.toLowerCase();
        return d.contains("despesa") && (d.contains("eventos") || d.contains("sinistro"));
    }
    
    // Realiza o download de arquivos via HTTP.
    public static void downloadFile(String urlStr, Path dest) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Download concluído: " + dest.getFileName());
    }
}