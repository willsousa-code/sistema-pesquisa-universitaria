package com.pesquisa.service;

import com.pesquisa.exception.AcessoNegadoException;
import com.pesquisa.exception.SistemaException;
import com.pesquisa.exception.UsuarioNaoEncontradoException;
import com.pesquisa.model.*;
import com.pesquisa.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsuarioService {
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario cadastrar(String tipo, String nome, String email, String senha,
            String extra1, String extra2) {
        if (repository.existeEmail(email)) {
            throw new SistemaException("E-mail já cadastrado.", 400);
        }
        String id = UUID.randomUUID().toString();
        Usuario usuario;
        switch (tipo.toUpperCase()) {
            case "ALUNO":
                usuario = new Aluno(id, nome, email, senha, extra1, extra2);
                break;
            case "PROFESSOR":
                usuario = new Professor(id, nome, email, senha, extra1, extra2);
                break;
            case "COORDENADOR":
                usuario = new Coordenador(id, nome, email, senha, extra1);
                break;
            default:
                throw new SistemaException("Tipo de usuário inválido.", 400);
        }
        repository.salvar(usuario);
        return usuario;
    }

    public Usuario login(String email, String senha) {
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Email: " + email);
        System.out.println("Senha recebida: " + senha);

        Optional<Usuario> opt = repository.buscarPorEmail(email);
        System.out.println("Usuário encontrado: " + opt.isPresent());

        if (opt.isEmpty()) {
            throw new SistemaException("Credenciais inválidas.", 401);
        }
        Usuario u = opt.get();
        System.out.println("Senha no banco: " + u.getSenha());
        System.out.println("Tipo: " + u.getTipo());
        System.out.println("Ativo: " + u.isAtivo());

        if (u.getSenha() == null || !senha.equals(u.getSenha())) {
            throw new SistemaException("Credenciais inválidas.", 401);
        }
        if (u.getTipo() == null) {
            throw new SistemaException("Usuário com dados corrompidos. Recadastre-se.", 400);
        }
        if (!u.isAtivo()) {
            throw new SistemaException("Usuário inativo. Contate o coordenador.", 403);
        }
        return u;
    }

    public Usuario buscarPorId(String id) {
        return repository.buscarPorId(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);
    }

    public List<Usuario> listarTodos() {
        return repository.listarTodos();
    }

    public Usuario ativarDesativar(String coordenadorId, String usuarioId) {
        Usuario coord = buscarPorId(coordenadorId);
        if (!"COORDENADOR".equals(coord.getTipo()))
            throw new AcessoNegadoException();
        Usuario alvo = buscarPorId(usuarioId);
        alvo.setAtivo(!alvo.isAtivo());
        repository.salvar(alvo);
        return alvo;
    }

    public void remover(String coordenadorId, String usuarioId) {
        Usuario coord = buscarPorId(coordenadorId);
        if (!"COORDENADOR".equals(coord.getTipo()))
            throw new AcessoNegadoException();
        repository.remover(usuarioId);
    }

    public Usuario atualizar(String id, String nome, String email) {
        Usuario u = buscarPorId(id);
        if (nome != null)
            u.setNome(nome);
        if (email != null && !email.equals(u.getEmail())) {
            if (repository.existeEmail(email))
                throw new SistemaException("E-mail já em uso.", 400);
            u.setEmail(email);
        }
        repository.salvar(u);
        return u;
    }
}