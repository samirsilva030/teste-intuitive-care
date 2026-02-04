package br.com.intuitivecare.ans.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serviço responsável por validar a integridade dos dados consolidados.
 * Ajustado para o layout enriquecido (8 colunas).
 */
public class DataValidationService {

    public static void validateConsolidatedFile(Path csvPath) throws IOException {
        System.out.println("\n--- INICIANDO VALIDAÇÃO DO ARQUIVO CONSOLIDADO ---");
        
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line = br.readLine(); 
            int totalLinhas = 0;
            int errosEncontrados = 0;

            while ((line = br.readLine()) != null) {
                totalLinhas++;
                String[] cols = line.split(";", -1);
                
                // Mapeamento atualizado conforme o novo csv (Requisito 2.2)
                // 0:CNPJ; 1:RazaoSocial; 2:RegistroANS; 3:Modalidade; 4:UF; 5:Trimestre; 6:Ano; 7:ValorDespesas
                if (cols.length < 8) {
                    System.err.println("ERRO: Linha com colunas insuficientes na linha: " + totalLinhas);
                    errosEncontrados++;
                    continue;
                }

                String cnpj = cols[0];
                String razaoSocial = cols[1];
                String valorStr = cols[7]; 

                // Validação de Razão Social
                if (razaoSocial == null || razaoSocial.trim().isEmpty()) {
                    System.err.println("ERRO: Razão Social vazia na linha: " + totalLinhas);
                    errosEncontrados++;
                }

                // Validação de Valor
                try {
                    double valor = Double.parseDouble(valorStr.replace("\"", ""));
                    if (valor <= 0) {
                        System.err.println("ERRO: Valor não positivo (" + valor + ") no CNPJ: " + cnpj);
                        errosEncontrados++;
                    }
                } catch (Exception e) {
                    System.err.println("ERRO: Formato de valor inválido (" + valorStr + ") no CNPJ: " + cnpj);
                    errosEncontrados++;
                }

                // Validação de CNPJ
                if (!isValidCNPJ(cnpj)) {
                    System.err.println("ERRO: CNPJ matematicamente inválido: " + cnpj);
                    errosEncontrados++;
                }
            }

            System.out.println("Validação concluída. Linhas verificadas: " + totalLinhas);
            System.out.println("Total de inconsistências encontradas: " + errosEncontrados);
            System.out.println("--------------------------------------------------\n");
        }
    }

    private static boolean isValidCNPJ(String cnpj) {
        cnpj = cnpj.replaceAll("\\D", ""); 
        if (cnpj.length() != 14) return false;
        if (cnpj.matches("(\\d)\\1{13}")) return false;
        return checkDigits(cnpj);
    }

    private static boolean checkDigits(String cnpj) {
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int d1 = calcularDigito(cnpj.substring(0, 12), pesos1);
        int d2 = calcularDigito(cnpj.substring(0, 12) + d1, pesos2);

        return cnpj.equals(cnpj.substring(0, 12) + d1 + d2);
    }

    private static int calcularDigito(String str, int[] peso) {
        int soma = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            soma += Integer.parseInt(str.substring(i, i + 1)) * peso[i];
        }
        soma = 11 - (soma % 11);
        return soma > 9 ? 0 : soma;
    }
}
