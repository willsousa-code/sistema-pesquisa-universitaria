package com.pesquisa.exception;

public class SemVagasException extends SistemaException {
    public SemVagasException() {
        super("Projeto sem vagas disponíveis.", 400);
    }
}
