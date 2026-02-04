package br.com.intuitivecare.ans.service;

import br.com.intuitivecare.ans.model.AggregationStats;
import br.com.intuitivecare.ans.model.ConsolidatedRecord;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class AggregationService {

    
    //Agrupa dados e imprime o Top 10 operadoras por despesa.
    public Map<String, AggregationStats> processarAgregacao(Collection<ConsolidatedRecord> records) {
        Map<String, AggregationStats> agregados = new HashMap<>();

        // Agrupamento em memória
        for (ConsolidatedRecord r : records) {
            String chave = r.getRazaoSocial() + " | " + r.getUf();
            agregados.computeIfAbsent(chave, k -> new AggregationStats()).addValor(r.getValorDespesas());
        }

        System.out.println("\n" + "=".repeat(100));
        System.out.println("   RELATÓRIO ESTATÍSTICO DE DESPESAS (TOP 10 MAIORES GASTOS) - REQUISITO 2.3");
        System.out.println("=".repeat(100));
        
        // Estratégia de Ordenação: In-Memory Sort usando Stream API
        // Justificativa: Volume de dados tratável em RAM, priorizando performance de CPU.
        agregados.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().getTotal().compareTo(e1.getValue().getTotal()))
                .limit(10) //
                .forEach(entry -> {
                    AggregationStats stats = entry.getValue();
                    System.out.printf(Locale.US, "Operadora/UF: %-55s | Total: R$ %15.2f | Média: R$ %15.2f | Desvio: %10.2f%n",
                            entry.getKey(), 
                            stats.getTotal(), 
                            stats.getMedia(), 
                            stats.getDesvioPadrao());
                });
        
        System.out.println("=".repeat(100));
        System.out.println("Nota: Exibindo apenas o Top 10 para clareza do log. O arquivo CSV contém a base completa.");
        
        return agregados; // Retorno adicionado para permitir a gravação do csv
    }

    
    //Salva o resultado agregado em csv conforme requisito
    public void salvarCsvAgregado(Map<String, AggregationStats> agregados, Path targetPath) throws Exception {
        try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8)) {
            writer.write("RazaoSocial_UF;TotalDespesas;MediaTrimestral;DesvioPadrao\n");

            // Ordena todos os dados (maior para menor) para o arquivo final
            agregados.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().getTotal().compareTo(e1.getValue().getTotal()))
                    .forEach(entry -> {
                        try {
                            AggregationStats s = entry.getValue();
                            writer.write(String.format(Locale.US, "%s;%.2f;%.2f;%.2f\n",
                                    entry.getKey(), s.getTotal(), s.getMedia(), s.getDesvioPadrao()));
                        } catch (Exception e) { e.printStackTrace(); }
                    });
        }
    }
}