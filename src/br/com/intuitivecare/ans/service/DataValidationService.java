package br.com.intuitivecare.ans.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


  //Serviço responsável por validar a integridade dos dados consolidados (Requisito 2.1).
public class DataValidationService {

    
     // Lê o arquivo CSV consolidado e aplica regras de validação em cada linha.
    public static void validateConsolidatedFile(Path csvPath) throws IOException {
        System.out.println("\n--- INICIANDO VALIDAÇÃO DO ARQUIVO CONSOLIDADO ---");
        
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line = br.readLine(); 
            int totalLinhas = 0;
            int errosEncontrados = 0;

            while ((line = br.readLine()) != null) {
                totalLinhas++;
                String[] cols = line.split(";", -1);
                
                // Mapeamento das colunas conforme o CSV gerado
                String cnpj = cols[0];
                String razaoSocial = cols[1];
                String valorStr = cols[4];

                // Validação de Razão Social: Não pode ser nula ou composta apenas de espaços
                if (razaoSocial == null || razaoSocial.trim().isEmpty()) {
                    System.err.println("ERRO: Razão Social vazia na linha: " + totalLinhas);
                    errosEncontrados++;
                }

                // Validação de Valor: Deve ser um número valido e maior que zero
                try {
                    // Remove aspas se o csv as contiver antes de converter
                    double valor = Double.parseDouble(valorStr.replace("\"", ""));
                    if (valor <= 0) {
                        System.err.println("ERRO: Valor não positivo (" + valor + ") no CNPJ: " + cnpj);
                        errosEncontrados++;
                    }
                } catch (Exception e) {
                    System.err.println("ERRO: Formato de valor inválido no CNPJ: " + cnpj);
                    errosEncontrados++;
                }

                // Validação de CNPJ: Checa formato (14 dígitos) e calculo dos digitos verificadores
                if (!isValidCNPJ(cnpj)) {
                    System.err.println("ERRO: CNPJ matematicamente inválido: " + cnpj);
                    errosEncontrados++;
                }
            }

            //final da validação para o log
            System.out.println("Validação concluída. Linhas verificadas: " + totalLinhas);
            System.out.println("Total de inconsistências encontradas: " + errosEncontrados);
            System.out.println("--------------------------------------------------\n");
        }
    }

    /**
     * Valida se o CNPJ é real usando a lógica de dígitos verificadores.
     */
    private static boolean isValidCNPJ(String cnpj) {
        // Remove qualquer caractere que não seja número (limpeza preventiva)
        cnpj = cnpj.replaceAll("\\D", ""); 

        // Um CNPJ deve ter exatamente 14 números
        if (cnpj.length() != 14) return false;

        // Elimina casos de CNPJs inválidos conhecidos (sequências repetidas)
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        return checkDigits(cnpj);
    }

    /**
     * Implementa o algoritmo de validação dos dois últimos dígitos do CNPJ.
     */
    private static boolean checkDigits(String cnpj) {
        // Pesos oficiais para o cálculo do primeiro e segundo dígito
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        // Calcula o primeiro dígito verificador (posição 13)
        int d1 = calcularDigito(cnpj.substring(0, 12), pesos1);
        // Calcula o segundo dígito verificador (posição 14)
        int d2 = calcularDigito(cnpj.substring(0, 12) + d1, pesos2);

        // O CNPJ é válido se os dígitos calculados forem iguais aos informados
        return cnpj.equals(cnpj.substring(0, 12) + d1 + d2);
    }

    /**
     * Realiza a soma ponderada e aplica o resto de divisão (Módulo 11) para achar o dígito.
     */
    private static int calcularDigito(String str, int[] peso) {
        int soma = 0;
        // Multiplica cada algarismo pelo seu peso respectivo
        for (int i = str.length() - 1; i >= 0; i--) {
            soma += Integer.parseInt(str.substring(i, i + 1)) * peso[i];
        }
        
        // Regra do Módulo 11 da Receita Federal
        soma = 11 - (soma % 11);
        
        // Se o resultado for 10 ou 11, o dígito é 0
        return soma > 9 ? 0 : soma;
    }
}
