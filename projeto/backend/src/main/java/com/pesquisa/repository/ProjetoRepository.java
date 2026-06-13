package com.pesquisa.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesquisa.model.Projeto;
import com.pesquisa.model.StatusProjeto;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ProjetoRepository {
    private static final String ARQUIVO = "data/projetos.json";
    private final ObjectMapper mapper;
    private Map<String, Projeto> projetos;

    public ProjetoRepository(ObjectMapper mapper) {
        this.mapper = mapper;
        this.projetos = new LinkedHashMap<>();
        carregarDados();
    }

    private void carregarDados() {
        File arquivo = new File(ARQUIVO);
        if (arquivo.exists()) {
            try {
                Map<String, Projeto> dados = mapper.readValue(arquivo,
                    mapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Projeto.class));
                if (dados != null) this.projetos = dados;
            } catch (IOException e) {
                System.err.println("Erro ao carregar projetos: " + e.getMessage());
            }
        }
    }

    private void salvarDados() {
        try {
            new File("data").mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(ARQUIVO), projetos);
        } catch (IOException e) {
            System.err.println("Erro ao salvar projetos: " + e.getMessage());
        }
    }

    public void salvar(Projeto projeto) {
        projetos.put(projeto.getId(), projeto);
        salvarDados();
    }

    public Optional<Projeto> buscarPorId(String id) {
        return Optional.ofNullable(projetos.get(id));
    }

    public List<Projeto> listarTodos() {
        return new ArrayList<>(projetos.values());
    }

    public List<Projeto> listarPorStatus(StatusProjeto status) {
        return projetos.values().stream()
            .filter(p -> p.getStatus() == status)
            .collect(Collectors.toList());
    }

    public List<Projeto> listarPorOrientador(String orientadorId) {
        return projetos.values().stream()
            .filter(p -> p.getOrientadorId().equals(orientadorId))
            .collect(Collectors.toList());
    }

    public List<Projeto> buscar(String termo, String area, String status) {
        return projetos.values().stream()
            .filter(p -> {
                boolean ok = true;
                if (termo != null && !termo.isEmpty())
                    ok = p.getTitulo().toLowerCase().contains(termo.toLowerCase()) ||
                         p.getDescricao().toLowerCase().contains(termo.toLowerCase());
                if (area != null && !area.isEmpty())
                    ok = ok && p.getAreaEstudo().equalsIgnoreCase(area);
                if (status != null && !status.isEmpty())
                    ok = ok && p.getStatus().name().equalsIgnoreCase(status);
                return ok;
            })
            .collect(Collectors.toList());
    }

    public void remover(String id) {
        projetos.remove(id);
        salvarDados();
    }

    public List<Projeto> listarTodos(Map<String, ?> _unused) {
        return listarTodos();
    }
}
