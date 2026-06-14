package com.pesquisa.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesquisa.model.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UsuarioRepository {
    private static final String ARQUIVO = "data/usuarios.json";
    private final ObjectMapper mapper;
    private Map<String, Usuario> usuarios;

    public UsuarioRepository(ObjectMapper mapper) {
        this.mapper = mapper;
        this.usuarios = new LinkedHashMap<>();
        carregarDados();
    }

    private Usuario fromNode(JsonNode node) {
        String tipo = node.has("tipo") && !node.get("tipo").isNull()
                ? node.get("tipo").asText()
                : "ALUNO";

        Usuario u;
        switch (tipo) {
            case "PROFESSOR":
                Professor prof = new Professor();
                prof.setDepartamento(textOf(node, "departamento"));
                prof.setTitulacao(textOf(node, "titulacao"));
                prof.setProjetosCriados(listOf(node, "projetosCriados"));
                u = prof;
                break;
            case "COORDENADOR":
                Coordenador coord = new Coordenador();
                coord.setSiape(textOf(node, "siape"));
                u = coord;
                break;
            default:
                Aluno aluno = new Aluno();
                aluno.setMatricula(textOf(node, "matricula"));
                aluno.setCurso(textOf(node, "curso"));
                aluno.setProjetosInscritos(listOf(node, "projetosInscritos"));
                aluno.setHistoricoProjetos(listOf(node, "historicoProjetos"));
                aluno.setAreasInteresse(listOf(node, "areasInteresse"));
                u = aluno;
                break;
        }

        u.setId(textOf(node, "id"));
        u.setNome(textOf(node, "nome"));
        u.setEmail(textOf(node, "email"));
        u.setSenha(textOf(node, "senha"));
        u.setAtivo(!node.has("ativo") || node.get("ativo").asBoolean(true));
        u.setTipo(tipo);
        return u;
    }

    private String textOf(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    private List<String> listOf(JsonNode node, String field) {
        List<String> list = new ArrayList<>();
        if (node.has(field) && node.get(field).isArray())
            node.get(field).forEach(n -> list.add(n.asText()));
        return list;
    }

    private void carregarDados() {
        File arquivo = new File(ARQUIVO);
        System.out.println("Arquivo existe: " + arquivo.exists());
        System.out.println("Caminho: " + arquivo.getAbsolutePath());
        if (!arquivo.exists())
            return;
        try {
            JsonNode root = mapper.readTree(arquivo);
            System.out.println("Total no JSON: " + root.size());
            root.fields().forEachRemaining(entry -> {
                Usuario u = fromNode(entry.getValue());
                System.out.println("Carregando: " + u.getEmail() + " | " + u.getTipo());
                if (u.getId() != null)
                    usuarios.put(u.getId(), u);
            });
            System.out.println("Total carregado: " + usuarios.size());
        } catch (IOException e) {
            System.err.println("Erro ao carregar usuários: " + e.getMessage());
        }
    }

    private void salvarDados() {
        try {
            new File("data").mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(ARQUIVO), usuarios);
        } catch (IOException e) {
            System.err.println("Erro ao salvar usuários: " + e.getMessage());
        }
    }

    public void salvar(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
        salvarDados();
    }

    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarios.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios.values());
    }

    public void remover(String id) {
        usuarios.remove(id);
        salvarDados();
    }

    public boolean existeEmail(String email) {
        return usuarios.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }
}