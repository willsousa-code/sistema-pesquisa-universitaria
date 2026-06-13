package com.pesquisa.exception;

public class JaInscritoException extends SistemaException {
    public JaInscritoException() {
        super("Aluno já está inscrito neste projeto.", 400);
    }
}
