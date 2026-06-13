package com.pesquisa.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Projeto {
    private String id;
    private String titulo;
    private String descricao;
    private String areaEstudo;
    private String orientadorId;
    private String orientadorNome;
    private String dataInicio;
    private String prazo;
    private int vagas;
    private StatusProjeto status;
    private List<String> participantes;
    private List<Relatorio> relatorios;
    private List<String> solicitacoesPendentes;

    // Constante estática
    public static final int MAX_VAGAS = 20;

    public Projeto() {
        this.participantes = new ArrayList<>();
        this.relatorios = new ArrayList<>();
        this.solicitacoesPendentes = new ArrayList<>();
        this.status = StatusProjeto.ABERTO;
    }

    public Projeto(String id, String titulo, String descricao, String areaEstudo,
                   String orientadorId, String orientadorNome, String dataInicio,
                   String prazo, int vagas) {
        this();
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.areaEstudo = areaEstudo;
        this.orientadorId = orientadorId;
        this.orientadorNome = orientadorNome;
        this.dataInicio = dataInicio;
        this.prazo = prazo;
        this.vagas = Math.min(vagas, MAX_VAGAS);
    }

    public int getVagasDisponiveis() {
        return vagas - participantes.size();
    }

    public boolean temVaga() {
        return getVagasDisponiveis() > 0;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getAreaEstudo() { return areaEstudo; }
    public void setAreaEstudo(String areaEstudo) { this.areaEstudo = areaEstudo; }

    public String getOrientadorId() { return orientadorId; }
    public void setOrientadorId(String orientadorId) { this.orientadorId = orientadorId; }

    public String getOrientadorNome() { return orientadorNome; }
    public void setOrientadorNome(String orientadorNome) { this.orientadorNome = orientadorNome; }

    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }

    public String getPrazo() { return prazo; }
    public void setPrazo(String prazo) { this.prazo = prazo; }

    public int getVagas() { return vagas; }
    public void setVagas(int vagas) { this.vagas = vagas; }

    public StatusProjeto getStatus() { return status; }
    public void setStatus(StatusProjeto status) { this.status = status; }

    public List<String> getParticipantes() { return participantes; }
    public void setParticipantes(List<String> participantes) { this.participantes = participantes; }

    public List<Relatorio> getRelatorios() { return relatorios; }
    public void setRelatorios(List<Relatorio> relatorios) { this.relatorios = relatorios; }

    public List<String> getSolicitacoesPendentes() { return solicitacoesPendentes; }
    public void setSolicitacoesPendentes(List<String> solicitacoesPendentes) { this.solicitacoesPendentes = solicitacoesPendentes; }
}
