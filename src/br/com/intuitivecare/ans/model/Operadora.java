package br.com.intuitivecare.ans.model;

// Representa uma operadora de plano de saúde
public class Operadora {

    private String registro;
    private String cnpj;
    private String razaoSocial;
    private String modalidade;
    private String uf;

    public Operadora(String registro, String cnpj, String razaoSocial, String modalidade, String uf) {
        this.registro = registro;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.modalidade = modalidade;
        this.uf = uf;
    }

    public String getRegistro() {
        return registro;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getModalidade() {
        return modalidade;
    }

    public String getUf() {
        return uf;
    }
}
