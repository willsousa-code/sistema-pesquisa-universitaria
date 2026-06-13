package com.pesquisa.model;

import java.util.ArrayList;
import java.util.List;

public class Aluno extends Usuario {
    private String matricula;
    private String curso;
    private List<String> projetosInscritos;
    private List<String> historicoProjetos;
    private List<String> areasInteresse;

    public Aluno() {
        super();
        this.projetosInscritos = new ArrayList<>();
        this.historicoProjetos = new ArrayList<>();
        this.areasInteresse = new ArrayList<>();
    }

    public Aluno(String id, String nome, String email, String senha, String matricula, String curso) {
        super(id, nome, email, senha, "ALUNO");
        this.matricula = matricula;
        this.curso = curso;
        this.projetosInscritos = new ArrayList<>();
        this.historicoProjetos = new ArrayList<>();
        this.areasInteresse = new ArrayList<>();
    }

    @Override
    public String getPerfil() {
        return "Aluno | Matrícula: " + matricula + " | Curso: " + curso;
    }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public List<String> getProjetosInscritos() { return projetosInscritos; }
    public void setProjetosInscritos(List<String> projetosInscritos) { this.projetosInscritos = projetosInscritos; }

    public List<String> getHistoricoProjetos() { return historicoProjetos; }
    public void setHistoricoProjetos(List<String> historicoProjetos) { this.historicoProjetos = historicoProjetos; }

    public List<String> getAreasInteresse() { return areasInteresse; }
    public void setAreasInteresse(List<String> areasInteresse) { this.areasInteresse = areasInteresse; }
}
