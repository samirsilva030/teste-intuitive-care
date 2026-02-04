package br.com.intuitivecare.ans;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import br.com.intuitivecare.ans.model.Operadora;
import br.com.intuitivecare.ans.model.AggregationStats; // Adicionado
import br.com.intuitivecare.ans.service.ConsolidationService;
import br.com.intuitivecare.ans.service.DataValidationService;
import br.com.intuitivecare.ans.service.AggregationService; // Adicionado para o Req 2.3
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

        // 1. Baixa e carrega as operadoras (Enriquecimento: traz Modalidade e UF)
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

            try {
                FileProcessor.downloadFile(zipUrl, zipPath);

                Path extractDir = downloadsDir.resolve(zipName.replace(".zip", ""));
                ZipExtractor.unzip(zipPath, extractDir);

                // Processa todos os csv extraídos
                Files.list(extractDir).filter(Files::isRegularFile).forEach(file -> {
                            try {
                                // Extrai o número do trimestre do nome do arquivo (ex: "1T2025" -> 1)
                                int trimestreNum = Integer.parseInt(zipName.substring(0, 1));
                                FileProcessor.processTrimestre(file, 2025, trimestreNum, operadoras, service);
                            } catch (Exception e) {
                                System.err.println("Erro ao processar arquivo: " + file.getFileName());
                                e.printStackTrace();
                            }
                        });
            } catch (Exception e) {
                System.err.println("Erro ao processar trimestre: " + zipName);
            }
        }

        // 3. Gera o csv consolidado final com Enriquecimento de Dados (Requisito 2.2)
        Path consolidatedPath = downloadsDir.resolve("consolidado_despesas.csv");

        try (var writer = Files.newBufferedWriter(consolidatedPath, StandardCharsets.UTF_8)) {

            // Cabeçalho atualizado com colunas extras
            writer.write("CNPJ;RazaoSocial;RegistroANS;Modalidade;UF;Trimestre;Ano;ValorDespesas\n");

            for (var record : service.getConsolidatedRecords()) {
                writer.write(String.join(";", 
                    record.getCnpj(), 
                    record.getRazaoSocial(), 
                    record.getRegistroAns(),
                    record.getModalidade(),
                    record.getUf(),
                    String.valueOf(record.getTrimestre()), 
                    String.valueOf(record.getAno()), 
                    record.getValorDespesas().toString()
                ));
                writer.write("\n");
            }
        }
        
        Scanner sc = new Scanner(System.in);

        System.out.println("Consolidação final gerada: " + consolidatedPath);
        System.out.println("Total de registros consolidados: " + service.getConsolidatedRecords().size());
        
        // Aqui validamos o CNPJ, valores e strings do arquivo que acabamos de criar
        DataValidationService.validateConsolidatedFile(consolidatedPath);
        
        // Agregação e Estatísticas
        System.out.println("\nIniciando Agregação de Dados...");
        AggregationService aggService = new AggregationService();
        
        // Processa agregação e captura o mapa para salvar em arquivo
        Map<String, AggregationStats> statsMap = aggService.processarAgregacao(new ArrayList<>(service.getConsolidatedRecords()));
        
        // Salva o resultado em um novo csv conforme o desafio
        Path aggregatedPath = downloadsDir.resolve("despesas_agregadas.csv");
        aggService.salvarCsvAgregado(statsMap, aggregatedPath);
        System.out.println("Arquivo de agregação gerado: " + aggregatedPath);
        
        // Compacta o arquivo final conforme regra de nomenclatura: Teste_{seu_nome}.zip
        System.out.println("Digite o Seu nome");
        String seuNome = sc.nextLine() ;
        Path zipFinal = downloadsDir.resolve("Teste_" + seuNome + ".zip");
        ZipCompressor.zip(aggregatedPath, zipFinal);

        System.out.println("Arquivo ZIP final gerado: " + zipFinal);
        
        sc.close();
    }
}