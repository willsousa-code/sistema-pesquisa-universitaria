package com.pesquisa.model;

import java.util.List;

public interface Notificavel {
    void receberNotificacao(Notificacao notificacao);
    List<Notificacao> getNotificacoes();
}
