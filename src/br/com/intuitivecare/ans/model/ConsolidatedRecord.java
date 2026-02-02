package br.com.intuitivecare.ans.model;

import java.math.BigDecimal;

// Representa o resultado consolidado por operadora e periodo
public class ConsolidatedRecord {

    private String cnpj;
    private String razaoSocial;
    private int trimestre;
    private int ano;
    private BigDecimal valorDespesas;

    public ConsolidatedRecord(
            String cnpj,
            String razaoSocial,
            int trimestre,
            int ano,
            BigDecimal valorDespesas) {

        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.trimestre = trimestre;
        this.ano = ano;
        this.valorDespesas = valorDespesas;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public int getTrimestre() {
        return trimestre;
    }

    public int getAno() {
        return ano;
    }

    public BigDecimal getValorDespesas() {
        return valorDespesas;
    }
}

