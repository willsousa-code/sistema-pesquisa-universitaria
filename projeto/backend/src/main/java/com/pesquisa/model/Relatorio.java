package com.pesquisa.model;

public class Relatorio {
    private String id;
    private String projetoId;
    private String alunoId;
    private String alunoNome;
    private String titulo;
    private String conteudo;
    private String dataEnvio;
    private StatusRelatorio status;
    private String feedback;

    public Relatorio() {
        this.status = StatusRelatorio.PENDENTE;
    }

    public Relatorio(String id, String projetoId, String alunoId, String alunoNome,
                     String titulo, String conteudo, String dataEnvio) {
        this();
        this.id = id;
        this.projetoId = projetoId;
        this.alunoId = alunoId;
        this.alunoNome = alunoNome;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.dataEnvio = dataEnvio;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjetoId() { return projetoId; }
    public void setProjetoId(String projetoId) { this.projetoId = projetoId; }

    public String getAlunoId() { return alunoId; }
    public void setAlunoId(String alunoId) { this.alunoId = alunoId; }

    public String getAlunoNome() { return alunoNome; }
    public void setAlunoNome(String alunoNome) { this.alunoNome = alunoNome; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public String getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(String dataEnvio) { this.dataEnvio = dataEnvio; }

    public StatusRelatorio getStatus() { return status; }
    public void setStatus(StatusRelatorio status) { this.status = status; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
