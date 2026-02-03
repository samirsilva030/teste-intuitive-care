package br.com.intuitivecare.ans.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import br.com.intuitivecare.ans.model.ConsolidatedRecord;

public class ConsolidationService {

    // Armazena os valores consolidados por CNPJ + Ano + Trimestre
    private final Map<String, ConsolidatedRecord> consolidated = new HashMap<>();

    // Soma valores quando já existe registro para a mesma chave
    public void addRecord(ConsolidatedRecord record) {

        String key = record.getCnpj() + "|" + record.getAno() + "|" + record.getTrimestre();

        if (consolidated.containsKey(key)) {

            ConsolidatedRecord existing = consolidated.get(key);
            
            BigDecimal novoValor = existing.getValorDespesas().add(record.getValorDespesas());
            
            // Aqui passamos todos os campos novos para o construtor atualizado
            consolidated.put(key, new ConsolidatedRecord(
                existing.getCnpj(), 
                existing.getRazaoSocial(), 
                existing.getRegistroAns(),
                existing.getModalidade(),
                existing.getUf(),
                existing.getTrimestre(), 
                existing.getAno(), 
                novoValor
            ));
            
        } else {
            
            consolidated.put(key, record);
        }
    }

    // Retorna apenas o resultado final da consolidação
    public Collection<ConsolidatedRecord> getConsolidatedRecords() {
        return consolidated.values();
    }
}


