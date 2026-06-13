package com.pesquisa.controller;

import com.pesquisa.exception.SistemaException;
import com.pesquisa.service.NotificacaoService;
import com.pesquisa.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class NotificacaoController implements HttpHandler {
    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if ("OPTIONS".equals(ex.getRequestMethod())) { HttpUtil.responderOptions(ex); return; }
        try {
            String method = ex.getRequestMethod();
            String[] parts = HttpUtil.pathParts(ex);

            // GET /api/notificacoes/{usuarioId}
            if ("GET".equals(method) && parts.length == 3) {
                HttpUtil.responder(ex, 200, service.listarNotificacoes(parts[2]));
            }
            // POST /api/notificacoes/{usuarioId}/lidas
            else if ("POST".equals(method) && parts.length == 4 && "lidas".equals(parts[3])) {
                service.marcarTodasComoLidas(parts[2]);
                HttpUtil.responder(ex, 200, Map.of("msg", "Notificações marcadas como lidas."));
            }
            else {
                HttpUtil.responderErro(ex, 404, "Rota não encontrada.");
            }
        } catch (SistemaException e) {
            HttpUtil.responderErro(ex, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            HttpUtil.responderErro(ex, 500, "Erro interno: " + e.getMessage());
        }
    }
}
