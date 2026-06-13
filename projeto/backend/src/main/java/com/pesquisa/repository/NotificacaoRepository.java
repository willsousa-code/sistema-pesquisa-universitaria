package com.pesquisa.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesquisa.model.Notificacao;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NotificacaoRepository {
    private static final String ARQUIVO = "data/notificacoes.json";
    private final ObjectMapper mapper;
    private Map<String, Notificacao> notificacoes;

    public NotificacaoRepository(ObjectMapper mapper) {
        this.mapper = mapper;
        this.notificacoes = new LinkedHashMap<>();
        carregarDados();
    }

    private void carregarDados() {
        File arquivo = new File(ARQUIVO);
        if (arquivo.exists()) {
            try {
                Map<String, Notificacao> dados = mapper.readValue(arquivo,
                    mapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Notificacao.class));
                if (dados != null) this.notificacoes = dados;
            } catch (IOException e) {
                System.err.println("Erro ao carregar notificações: " + e.getMessage());
            }
        }
    }

    private void salvarDados() {
        try {
            new File("data").mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(ARQUIVO), notificacoes);
        } catch (IOException e) {
            System.err.println("Erro ao salvar notificações: " + e.getMessage());
        }
    }

    public void salvar(Notificacao n) {
        notificacoes.put(n.getId(), n);
        salvarDados();
    }

    public List<Notificacao> listarPorUsuario(String usuarioId) {
        return notificacoes.values().stream()
            .filter(n -> n.getUsuarioId().equals(usuarioId))
            .sorted(Comparator.comparing(Notificacao::getData).reversed())
            .collect(Collectors.toList());
    }

    public Optional<Notificacao> buscarPorId(String id) {
        return Optional.ofNullable(notificacoes.get(id));
    }

    public void marcarTodasComoLidas(String usuarioId) {
        notificacoes.values().stream()
            .filter(n -> n.getUsuarioId().equals(usuarioId))
            .forEach(n -> n.setLida(true));
        salvarDados();
    }
}
