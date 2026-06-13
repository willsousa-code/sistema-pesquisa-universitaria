package com.pesquisa.controller;

import com.pesquisa.exception.SistemaException;
import com.pesquisa.model.Usuario;
import com.pesquisa.service.UsuarioService;
import com.pesquisa.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class UsuarioController implements HttpHandler {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if ("OPTIONS".equals(ex.getRequestMethod())) { HttpUtil.responderOptions(ex); return; }
        try {
            String method = ex.getRequestMethod();
            String[] parts = HttpUtil.pathParts(ex);
            // /api/usuarios
            // /api/usuarios/login
            // /api/usuarios/{id}
            // /api/usuarios/{id}/ativar
            // /api/usuarios/{id}/remover (DELETE)

            if ("POST".equals(method) && parts.length == 2) {
                // Cadastro
                Map<String, Object> body = HttpUtil.lerJson(ex);
                Usuario u = service.cadastrar(
                    (String) body.get("tipo"),
                    (String) body.get("nome"),
                    (String) body.get("email"),
                    (String) body.get("senha"),
                    (String) body.getOrDefault("extra1", ""),
                    (String) body.getOrDefault("extra2", "")
                );
                // Não retorna a senha
                u.setSenha(null);
                HttpUtil.responder(ex, 201, u);
            } else if ("POST".equals(method) && parts.length == 3 && "login".equals(parts[2])) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                Usuario u = service.login((String) body.get("email"), (String) body.get("senha"));
                u.setSenha(null);
                HttpUtil.responder(ex, 200, u);
            } else if ("GET".equals(method) && parts.length == 2) {
                var lista = service.listarTodos();
                lista.forEach(u -> u.setSenha(null));
                HttpUtil.responder(ex, 200, lista);
            } else if ("GET".equals(method) && parts.length == 3) {
                Usuario u = service.buscarPorId(parts[2]);
                u.setSenha(null);
                HttpUtil.responder(ex, 200, u);
            } else if ("PUT".equals(method) && parts.length == 3) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                Usuario u = service.atualizar(parts[2],
                    (String) body.get("nome"), (String) body.get("email"));
                u.setSenha(null);
                HttpUtil.responder(ex, 200, u);
            } else if ("POST".equals(method) && parts.length == 4 && "ativar".equals(parts[3])) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                Usuario u = service.ativarDesativar((String) body.get("coordenadorId"), parts[2]);
                u.setSenha(null);
                HttpUtil.responder(ex, 200, u);
            } else if ("DELETE".equals(method) && parts.length == 3) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                service.remover((String) body.get("coordenadorId"), parts[2]);
                HttpUtil.responder(ex, 200, Map.of("msg", "Usuário removido."));
            } else {
                HttpUtil.responderErro(ex, 404, "Rota não encontrada.");
            }
        } catch (SistemaException e) {
            HttpUtil.responderErro(ex, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            HttpUtil.responderErro(ex, 500, "Erro interno: " + e.getMessage());
        }
    }
}
