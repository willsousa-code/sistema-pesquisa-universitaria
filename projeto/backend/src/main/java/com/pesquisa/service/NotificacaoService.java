package com.pesquisa.service;

import com.pesquisa.model.Notificacao;
import com.pesquisa.model.Projeto;
import com.pesquisa.model.Usuario;
import com.pesquisa.repository.NotificacaoRepository;
import com.pesquisa.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

// Padrão Singleton — gerencia todas as notificações do sistema
public class NotificacaoService {
    private static NotificacaoService instancia;
    private NotificacaoRepository notificacaoRepository;
    private UsuarioRepository usuarioRepository;

    private NotificacaoService() {}

    public static NotificacaoService getInstancia() {
        if (instancia == null) {
            instancia = new NotificacaoService();
        }
        return instancia;
    }

    public void inicializar(NotificacaoRepository notificacaoRepo, UsuarioRepository usuarioRepo) {
        this.notificacaoRepository = notificacaoRepo;
        this.usuarioRepository = usuarioRepo;
    }

    private String agora() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public void notificarUsuario(String usuarioId, String mensagem, String tipo) {
        Notificacao n = new Notificacao(UUID.randomUUID().toString(), usuarioId, mensagem, tipo, agora());
        notificacaoRepository.salvar(n);
    }

    public void notificarTodos(String mensagem, String tipo) {
        for (Usuario u : usuarioRepository.listarTodos()) {
            notificarUsuario(u.getId(), mensagem, tipo);
        }
    }

    public void notificarNovoProjeto(Projeto projeto) {
        String msg = "Novo projeto disponível: \"" + projeto.getTitulo() + "\" — Área: " + projeto.getAreaEstudo();
        for (Usuario u : usuarioRepository.listarTodos()) {
            if (u.getTipo().equals("ALUNO")) {
                notificarUsuario(u.getId(), msg, "NOVO_PROJETO");
            }
        }
    }

    public void notificarAlteracaoProjeto(Projeto projeto) {
        String msg = "O projeto \"" + projeto.getTitulo() + "\" foi atualizado.";
        for (String participanteId : projeto.getParticipantes()) {
            notificarUsuario(participanteId, msg, "ATUALIZACAO_PROJETO");
        }
    }

    public void notificarInscricaoAprovada(String alunoId, String projetoTitulo) {
        notificarUsuario(alunoId, "Sua participação no projeto \"" + projetoTitulo + "\" foi confirmada!", "INSCRICAO_APROVADA");
    }

    public void notificarRelatorioAvaliado(String alunoId, String projetoTitulo, String status) {
        notificarUsuario(alunoId, "Seu relatório do projeto \"" + projetoTitulo + "\" foi " + status + ".", "RELATORIO_AVALIADO");
    }

    public List<Notificacao> listarNotificacoes(String usuarioId) {
        return notificacaoRepository.listarPorUsuario(usuarioId);
    }

    public void marcarTodasComoLidas(String usuarioId) {
        notificacaoRepository.marcarTodasComoLidas(usuarioId);
    }
}
