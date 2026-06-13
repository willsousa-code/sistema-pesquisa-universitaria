package com.pesquisa.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pesquisa.model.Usuario;

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

    private void carregarDados() {
        File arquivo = new File(ARQUIVO);
        if (arquivo.exists()) {
            try {
                Map<String, Usuario> dados = mapper.readValue(arquivo,
                    mapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Usuario.class));
                if (dados != null) this.usuarios = dados;
            } catch (IOException e) {
                System.err.println("Erro ao carregar usuários: " + e.getMessage());
            }
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
