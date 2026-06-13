package com.pesquisa.model;

public class Coordenador extends Usuario {
    private String siape;

    public Coordenador() {
        super();
    }

    public Coordenador(String id, String nome, String email, String senha, String siape) {
        super(id, nome, email, senha, "COORDENADOR");
        this.siape = siape;
    }

    @Override
    public String getPerfil() {
        return "Coordenador | SIAPE: " + siape;
    }

    public String getSiape() { return siape; }
    public void setSiape(String siape) { this.siape = siape; }
}
