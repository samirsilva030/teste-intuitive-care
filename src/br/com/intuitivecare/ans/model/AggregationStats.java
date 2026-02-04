package br.com.intuitivecare.ans.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsavel por acumular os valores financeiros e calcular 
 * as métricas estatísticas (Total, Média e Desvio Padrão).
 */
public class AggregationStats {
	
    private BigDecimal total = BigDecimal.ZERO;
    private List<BigDecimal> valores = new ArrayList<>();
    
    //Adiciona um valor ao montante total e armazena na lista para calculos estatisticos.
    public void addValor(BigDecimal valor) {
        if (valor == null) return;
        total = total.add(valor);
        valores.add(valor);
    }

    public BigDecimal getTotal() { 
    	return total; 
    	}

    //Calcula a média aritmética simples baseada na quantidade de registros (trimestres).
    public double getMedia() {
        return valores.isEmpty() ? 0 : total.doubleValue() / valores.size();
    }

  
    //Calcula o Desvio Padrão para identificar variação entre trimestres.
    public double getDesvioPadrao() {
        if (valores.size() <= 1) return 0.0;
        double media = getMedia();
        double somaQuadrados = 0;
        for (BigDecimal v : valores) {
            somaQuadrados += Math.pow(v.doubleValue() - media, 2);
        }
        return Math.sqrt(somaQuadrados / valores.size());
    }
}