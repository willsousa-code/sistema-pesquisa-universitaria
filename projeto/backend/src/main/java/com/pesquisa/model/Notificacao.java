package com.pesquisa.model;

public class Notificacao {
    private String id;
    private String usuarioId;
    private String mensagem;
    private String tipo;
    private String data;
    private boolean lida;

    public Notificacao() {}

    public Notificacao(String id, String usuarioId, String mensagem, String tipo, String data) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.mensagem = mensagem;
        this.tipo = tipo;
        this.data = data;
        this.lida = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
}
