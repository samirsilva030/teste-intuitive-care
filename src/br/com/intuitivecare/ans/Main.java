package br.com.intuitivecare.ans;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import br.com.intuitivecare.ans.model.Operadora;
import br.com.intuitivecare.ans.service.ConsolidationService;
import br.com.intuitivecare.ans.util.FileProcessor;
import br.com.intuitivecare.ans.util.ZipCompressor;
import br.com.intuitivecare.ans.util.ZipExtractor;

public class Main {

    // URLs base dos dados da ANS
    private static final String BASE_URL_DEMONSTRACOES = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/2025/";

    private static final String URL_OPERADORAS = "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv";

    public static void main(String[] args) throws Exception {

        // Diretorio local para download e processamento
        Path downloadsDir = Paths.get("downloads");
        Files.createDirectories(downloadsDir);

        // 1. Baixa e carrega as operadoras
        Path cadopPath = downloadsDir.resolve("Relatorio_cadop.csv");
        
        FileProcessor.downloadFile(URL_OPERADORAS, cadopPath);
        
        Map<String, Operadora> operadoras = FileProcessor.loadOperadoras(cadopPath);

        // 2. Define os trimestres a serem processados
        List<String> trimestres = List.of("1T2025.zip", "2T2025.zip", "3T2025.zip");

        ConsolidationService service = new ConsolidationService();

        // Processa cada trimestre
        for (String zipName : trimestres) {

            Path zipPath = downloadsDir.resolve(zipName);
            String zipUrl = BASE_URL_DEMONSTRACOES + zipName;

            FileProcessor.downloadFile(zipUrl, zipPath);

            Path extractDir = downloadsDir.resolve(zipName.replace(".zip", ""));
            ZipExtractor.unzip(zipPath, extractDir);

            // Processa todos os csv extraídos
            Files.list(extractDir).filter(Files::isRegularFile).forEach(file -> {
                        try {
                            int trimestreNum = Integer.parseInt(zipName.substring(0, 1));
                            FileProcessor.processTrimestre(file, 2025, trimestreNum, operadoras, service);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        // 3. Gera o csv consolidado final
        Path consolidatedPath = downloadsDir.resolve("consolidado_despesas.csv");

        try (var writer = Files.newBufferedWriter(consolidatedPath)) {

            writer.write("CNPJ;RazaoSocial;Trimestre;Ano;ValorDespesas\n");

            for (var record : service.getConsolidatedRecords()) {
                writer.write(String.join(";", record.getCnpj(), record.getRazaoSocial(), String.valueOf(record.getTrimestre()), String.valueOf(record.getAno()), record.getValorDespesas().toString()));
                writer.write("\n");
            }
        }

        System.out.println("Consolidação final gerada: " + consolidatedPath);
        System.out.println("Total de registros consolidados: " + service.getConsolidatedRecords().size());
        
     // 4. Compacta o CSV final
        Path zipFinal = downloadsDir.resolve("consolidado_despesas.zip");
        ZipCompressor.zip(consolidatedPath, zipFinal);

        System.out.println("Arquivo ZIP final gerado: " + zipFinal);
    }
}


