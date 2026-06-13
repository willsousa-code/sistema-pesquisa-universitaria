package com.pesquisa.exception;

public class SistemaException extends RuntimeException {
    private final int statusCode;

    public SistemaException(String mensagem, int statusCode) {
        super(mensagem);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
