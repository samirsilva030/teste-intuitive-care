package br.com.intuitivecare.ans.model;

// Representa uma operadora de plano de saúde
public class Operadora {

    private String registro;
    private String cnpj;
    private String razaoSocial;

    public Operadora(String registro, String cnpj, String razaoSocial) {
        this.registro = registro;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
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
}

