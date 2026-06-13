package com.pesquisa.model;

import java.util.ArrayList;
import java.util.List;

public class Professor extends Usuario {
    private String departamento;
    private String titulacao;
    private List<String> projetosCriados;

    public Professor() {
        super();
        this.projetosCriados = new ArrayList<>();
    }

    public Professor(String id, String nome, String email, String senha, String departamento, String titulacao) {
        super(id, nome, email, senha, "PROFESSOR");
        this.departamento = departamento;
        this.titulacao = titulacao;
        this.projetosCriados = new ArrayList<>();
    }

    @Override
    public String getPerfil() {
        return "Professor | Depto: " + departamento + " | " + titulacao;
    }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getTitulacao() { return titulacao; }
    public void setTitulacao(String titulacao) { this.titulacao = titulacao; }

    public List<String> getProjetosCriados() { return projetosCriados; }
    public void setProjetosCriados(List<String> projetosCriados) { this.projetosCriados = projetosCriados; }
}
