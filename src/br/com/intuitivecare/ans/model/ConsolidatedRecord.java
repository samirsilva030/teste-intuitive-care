package br.com.intuitivecare.ans.model;

import java.math.BigDecimal;

// Representa o resultado consolidado por operadora e periodo
public class ConsolidatedRecord {

    private String cnpj;
    private String razaoSocial;
    private String registroAns;
    private String modalidade;
    private String uf;
    private int trimestre;
    private int ano;
    private BigDecimal valorDespesas;
	
    public ConsolidatedRecord(String cnpj, String razaoSocial, String registroAns, String modalidade, String uf,
			int trimestre, int ano, BigDecimal valorDespesas) {
		this.cnpj = cnpj;
		this.razaoSocial = razaoSocial;
		this.registroAns = registroAns;
		this.modalidade = modalidade;
		this.uf = uf;
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

	public String getRegistroAns() {
		return registroAns;
	}

	public String getModalidade() {
		return modalidade;
	}

	public String getUf() {
		return uf;
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

    
