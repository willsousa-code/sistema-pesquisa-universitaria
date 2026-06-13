package com.pesquisa.exception;

public class AcessoNegadoException extends SistemaException {
    public AcessoNegadoException() {
        super("Acesso negado. Você não tem permissão para esta ação.", 403);
    }
}
