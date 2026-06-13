package com.pesquisa.exception;

public class UsuarioNaoEncontradoException extends SistemaException {
    public UsuarioNaoEncontradoException() {
        super("Usuário não encontrado.", 404);
    }
}
