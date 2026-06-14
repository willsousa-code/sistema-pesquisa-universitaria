package com.pesquisa.controller;

import com.pesquisa.exception.SistemaException;
import com.pesquisa.model.Projeto;
import com.pesquisa.model.Relatorio;
import com.pesquisa.service.ProjetoService;
import com.pesquisa.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProjetoController implements HttpHandler {
    private final ProjetoService service;

    public ProjetoController(ProjetoService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if ("OPTIONS".equals(ex.getRequestMethod())) { HttpUtil.responderOptions(ex); return; }
        try {
            String method = ex.getRequestMethod();
            String[] parts = HttpUtil.pathParts(ex);
            Map<String, String> qp = HttpUtil.queryParams(ex);

            if ("GET".equals(method) && parts.length == 2) {
                List<Projeto> lista = service.listar(qp.get("termo"), qp.get("area"), qp.get("status"));
                HttpUtil.responder(ex, 200, lista);
            }
            else if ("GET".equals(method) && parts.length == 3 && "estatisticas".equals(parts[2])) {
                HttpUtil.responder(ex, 200, service.gerarEstatisticas());
            }
    
            else if ("GET".equals(method) && parts.length == 3) {
                HttpUtil.responder(ex, 200, service.buscarPorId(parts[2]));
            }
        
            else if ("GET".equals(method) && parts.length == 4 && "orientador".equals(parts[2])) {
                HttpUtil.responder(ex, 200, service.listarPorOrientador(parts[3]));
            }
            else if ("POST".equals(method) && parts.length == 2) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                Projeto p = service.criar(
                    (String) body.get("professorId"),
                    (String) body.get("titulo"),
                    (String) body.get("descricao"),
                    (String) body.get("area"),
                    (String) body.get("dataInicio"),
                    (String) body.get("prazo"),
                    ((Number) body.getOrDefault("vagas", 5)).intValue()
                );
                HttpUtil.responder(ex, 201, p);
            }
        
            else if ("PUT".equals(method) && parts.length == 3) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                String usuarioId = (String) body.remove("usuarioId");
                HttpUtil.responder(ex, 200, service.editar(usuarioId, parts[2], body));
            }

            else if ("DELETE".equals(method) && parts.length == 3) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                service.remover((String) body.get("usuarioId"), parts[2]);
                HttpUtil.responder(ex, 200, Map.of("msg", "Projeto removido."));
            }
            
            else if ("POST".equals(method) && parts.length == 4 && "solicitar".equals(parts[3])) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                service.solicitarParticipacao((String) body.get("alunoId"), parts[2]);
                HttpUtil.responder(ex, 200, Map.of("msg", "Solicitação enviada."));
            }
            
            else if ("POST".equals(method) && parts.length == 4 && "aprovar".equals(parts[3])) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                service.aprovarParticipacao((String) body.get("professorId"), parts[2], (String) body.get("alunoId"));
                HttpUtil.responder(ex, 200, Map.of("msg", "Participação aprovada."));
            }
            
            else if ("POST".equals(method) && parts.length == 4 && "rejeitar".equals(parts[3])) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                service.rejeitarParticipacao((String) body.get("professorId"), parts[2], (String) body.get("alunoId"));
                HttpUtil.responder(ex, 200, Map.of("msg", "Solicitação rejeitada."));
            }
            
            else if ("POST".equals(method) && parts.length == 4 && "cancelar".equals(parts[3])) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                service.cancelarParticipacao((String) body.get("alunoId"), parts[2]);
                HttpUtil.responder(ex, 200, Map.of("msg", "Participação cancelada."));
            }
            
            else if ("POST".equals(method) && parts.length == 4 && "relatorio".equals(parts[3])) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                Relatorio rel = service.enviarRelatorio(
                    (String) body.get("alunoId"), parts[2],
                    (String) body.get("titulo"), (String) body.get("conteudo")
                );
                HttpUtil.responder(ex, 201, rel);
            }
           
            else if ("POST".equals(method) && parts.length == 6 && "avaliar".equals(parts[5])) {
                Map<String, Object> body = HttpUtil.lerJson(ex);
                Relatorio rel = service.avaliarRelatorio(
                    (String) body.get("professorId"), parts[2], parts[4],
                    (String) body.get("status"), (String) body.get("feedback")
                );
                HttpUtil.responder(ex, 200, rel);
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
