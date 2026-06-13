package com.pesquisa.exception;

public class ProjetoEncerradoException extends SistemaException {
    public ProjetoEncerradoException() {
        super("Não é possível se inscrever em um projeto encerrado ou cancelado.", 400);
    }
}
