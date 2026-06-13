package com.pesquisa.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDeserializer extends StdDeserializer<Usuario> {

    public UsuarioDeserializer() {
        super(Usuario.class);
    }

    @Override
    public Usuario deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        String tipo = node.has("tipo") && !node.get("tipo").isNull()
            ? node.get("tipo").asText()
            : "";

        Usuario usuario;
        switch (tipo) {
            case "PROFESSOR":
                Professor prof = new Professor();
                prof.setDepartamento(getText(node, "departamento"));
                prof.setTitulacao(getText(node, "titulacao"));
                prof.setProjetosCriados(getStringList(node, "projetosCriados"));
                usuario = prof;
                break;
            case "COORDENADOR":
                Coordenador coord = new Coordenador();
                coord.setSiape(getText(node, "siape"));
                usuario = coord;
                break;
            case "ALUNO":
            default:
                Aluno aluno = new Aluno();
                aluno.setMatricula(getText(node, "matricula"));
                aluno.setCurso(getText(node, "curso"));
                aluno.setProjetosInscritos(getStringList(node, "projetosInscritos"));
                aluno.setHistoricoProjetos(getStringList(node, "historicoProjetos"));
                aluno.setAreasInteresse(getStringList(node, "areasInteresse"));
                usuario = aluno;
                break;
        }

        usuario.setId(getText(node, "id"));
        usuario.setNome(getText(node, "nome"));
        usuario.setEmail(getText(node, "email"));
        usuario.setSenha(getText(node, "senha"));
        usuario.setAtivo(node.has("ativo") && node.get("ativo").asBoolean(true));
        usuario.setTipo(tipo.isEmpty() ? "ALUNO" : tipo);

        return usuario;
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    private List<String> getStringList(JsonNode node, String field) {
        List<String> list = new ArrayList<>();
        if (node.has(field) && node.get(field).isArray()) {
            node.get(field).forEach(n -> list.add(n.asText()));
        }
        return list;
    }
}