package com.pesquisa.service;

import com.pesquisa.exception.*;
import com.pesquisa.model.*;
import com.pesquisa.repository.ProjetoRepository;
import com.pesquisa.repository.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ProjetoService {
    private final ProjetoRepository projetoRepo;
    private final UsuarioRepository usuarioRepo;
    private final NotificacaoService notificacaoService;

    public ProjetoService(ProjetoRepository projetoRepo, UsuarioRepository usuarioRepo,
                          NotificacaoService notificacaoService) {
        this.projetoRepo = projetoRepo;
        this.usuarioRepo = usuarioRepo;
        this.notificacaoService = notificacaoService;
    }

    public Projeto criar(String professorId, String titulo, String descricao, String area,
                          String dataInicio, String prazo, int vagas) {
        Usuario prof = usuarioRepo.buscarPorId(professorId)
            .orElseThrow(UsuarioNaoEncontradoException::new);
        if (!"PROFESSOR".equals(prof.getTipo()) && !"COORDENADOR".equals(prof.getTipo())) {
            throw new AcessoNegadoException();
        }
        String id = UUID.randomUUID().toString();
        Projeto p = new Projeto(id, titulo, descricao, area, professorId, prof.getNome(), dataInicio, prazo, vagas);
        projetoRepo.salvar(p);

        if ("PROFESSOR".equals(prof.getTipo())) {
            Professor professor = (Professor) prof;
            professor.getProjetosCriados().add(id);
            usuarioRepo.salvar(professor);
        }

        notificacaoService.notificarNovoProjeto(p);
        return p;
    }

    public Projeto editar(String usuarioId, String projetoId, Map<String, Object> dados) {
        Projeto p = buscarPorId(projetoId);
        Usuario u = usuarioRepo.buscarPorId(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);

        boolean isOrientador = p.getOrientadorId().equals(usuarioId);
        boolean isCoordenador = "COORDENADOR".equals(u.getTipo());
        if (!isOrientador && !isCoordenador) throw new AcessoNegadoException();

        if (dados.containsKey("titulo")) p.setTitulo((String) dados.get("titulo"));
        if (dados.containsKey("descricao")) p.setDescricao((String) dados.get("descricao"));
        if (dados.containsKey("area")) p.setAreaEstudo((String) dados.get("area"));
        if (dados.containsKey("dataInicio")) p.setDataInicio((String) dados.get("dataInicio"));
        if (dados.containsKey("prazo")) p.setPrazo((String) dados.get("prazo"));
        if (dados.containsKey("vagas")) p.setVagas(((Number) dados.get("vagas")).intValue());
        if (dados.containsKey("status")) p.setStatus(StatusProjeto.valueOf((String) dados.get("status")));

        projetoRepo.salvar(p);
        notificacaoService.notificarAlteracaoProjeto(p);
        return p;
    }

    public void remover(String usuarioId, String projetoId) {
        Projeto p = buscarPorId(projetoId);
        Usuario u = usuarioRepo.buscarPorId(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        if (!p.getOrientadorId().equals(usuarioId) && !"COORDENADOR".equals(u.getTipo())) {
            throw new AcessoNegadoException();
        }
        projetoRepo.remover(projetoId);
    }

    public void solicitarParticipacao(String alunoId, String projetoId) {
        Projeto p = buscarPorId(projetoId);
        if (p.getStatus() == StatusProjeto.ENCERRADO || p.getStatus() == StatusProjeto.CANCELADO) {
            throw new ProjetoEncerradoException();
        }
        if (!p.temVaga()) throw new SemVagasException();
        if (p.getParticipantes().contains(alunoId)) throw new JaInscritoException();
        if (p.getSolicitacoesPendentes().contains(alunoId)) {
            throw new SistemaException("Solicitação já enviada. Aguarde aprovação.", 400);
        }
        p.getSolicitacoesPendentes().add(alunoId);
        projetoRepo.salvar(p);

        notificacaoService.notificarUsuario(p.getOrientadorId(),
            "Nova solicitação de participação no projeto \"" + p.getTitulo() + "\".", "SOLICITACAO");
    }

    public void aprovarParticipacao(String professorId, String projetoId, String alunoId) {
        Projeto p = buscarPorId(projetoId);
        Usuario prof = usuarioRepo.buscarPorId(professorId).orElseThrow(UsuarioNaoEncontradoException::new);
        if (!p.getOrientadorId().equals(professorId) && !"COORDENADOR".equals(prof.getTipo())) {
            throw new AcessoNegadoException();
        }
        if (!p.getSolicitacoesPendentes().contains(alunoId)) {
            throw new SistemaException("Solicitação não encontrada.", 404);
        }
        if (!p.temVaga()) throw new SemVagasException();

        p.getSolicitacoesPendentes().remove(alunoId);
        p.getParticipantes().add(alunoId);
        projetoRepo.salvar(p);

        usuarioRepo.buscarPorId(alunoId).ifPresent(u -> {
            if (u instanceof Aluno) {
                Aluno a = (Aluno) u;
                if (!a.getProjetosInscritos().contains(projetoId))
                    a.getProjetosInscritos().add(projetoId);
                usuarioRepo.salvar(a);
            }
        });

        notificacaoService.notificarInscricaoAprovada(alunoId, p.getTitulo());
    }

    public void rejeitarParticipacao(String professorId, String projetoId, String alunoId) {
        Projeto p = buscarPorId(projetoId);
        Usuario prof = usuarioRepo.buscarPorId(professorId).orElseThrow(UsuarioNaoEncontradoException::new);
        if (!p.getOrientadorId().equals(professorId) && !"COORDENADOR".equals(prof.getTipo())) {
            throw new AcessoNegadoException();
        }
        p.getSolicitacoesPendentes().remove(alunoId);
        projetoRepo.salvar(p);
        notificacaoService.notificarUsuario(alunoId,
            "Sua solicitação para o projeto \"" + p.getTitulo() + "\" não foi aprovada.", "SOLICITACAO_NEGADA");
    }

    public void cancelarParticipacao(String alunoId, String projetoId) {
        Projeto p = buscarPorId(projetoId);
        p.getParticipantes().remove(alunoId);
        p.getSolicitacoesPendentes().remove(alunoId);
        projetoRepo.salvar(p);

        usuarioRepo.buscarPorId(alunoId).ifPresent(u -> {
            if (u instanceof Aluno) {
                Aluno a = (Aluno) u;
                a.getProjetosInscritos().remove(projetoId);
                usuarioRepo.salvar(a);
            }
        });
    }

    public Relatorio enviarRelatorio(String alunoId, String projetoId, String titulo, String conteudo) {
        Projeto p = buscarPorId(projetoId);
        if (!p.getParticipantes().contains(alunoId)) {
            throw new SistemaException("Você não é participante deste projeto.", 403);
        }
        if (p.getStatus() == StatusProjeto.ENCERRADO || p.getStatus() == StatusProjeto.CANCELADO) {
            throw new SistemaException("Não é possível enviar relatório para projeto encerrado.", 400);
        }
        String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        Usuario aluno = usuarioRepo.buscarPorId(alunoId).orElseThrow(UsuarioNaoEncontradoException::new);
        Relatorio rel = new Relatorio(UUID.randomUUID().toString(), projetoId, alunoId, aluno.getNome(), titulo, conteudo, data);
        p.getRelatorios().add(rel);
        projetoRepo.salvar(p);
        notificacaoService.notificarUsuario(p.getOrientadorId(),
            "Novo relatório enviado no projeto \"" + p.getTitulo() + "\" por " + aluno.getNome(), "NOVO_RELATORIO");
        return rel;
    }

    public Relatorio avaliarRelatorio(String professorId, String projetoId, String relatorioId,
                                       String statusStr, String feedback) {
        Projeto p = buscarPorId(projetoId);
        Usuario prof = usuarioRepo.buscarPorId(professorId).orElseThrow(UsuarioNaoEncontradoException::new);
        if (!p.getOrientadorId().equals(professorId) && !"COORDENADOR".equals(prof.getTipo())) {
            throw new AcessoNegadoException();
        }
        Relatorio rel = p.getRelatorios().stream()
            .filter(r -> r.getId().equals(relatorioId))
            .findFirst()
            .orElseThrow(() -> new SistemaException("Relatório não encontrado.", 404));

        rel.setStatus(StatusRelatorio.valueOf(statusStr));
        rel.setFeedback(feedback);
        projetoRepo.salvar(p);
        notificacaoService.notificarRelatorioAvaliado(rel.getAlunoId(), p.getTitulo(),
            StatusRelatorio.APROVADO.name().equals(statusStr) ? "aprovado" : "reprovado");
        return rel;
    }

    public Projeto buscarPorId(String id) {
        return projetoRepo.buscarPorId(id)
            .orElseThrow(() -> new SistemaException("Projeto não encontrado.", 404));
    }

    public List<Projeto> listar(String termo, String area, String status) {
        return projetoRepo.buscar(termo, area, status);
    }

    public List<Projeto> listarPorOrientador(String orientadorId) {
        return projetoRepo.listarPorOrientador(orientadorId);
    }

    public Map<String, Object> gerarEstatisticas() {
        List<Projeto> todos = projetoRepo.listarTodos();
        Map<String, Long> porArea = todos.stream()
            .collect(Collectors.groupingBy(Projeto::getAreaEstudo, Collectors.counting()));
        Map<String, Long> porStatus = todos.stream()
            .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));

        List<Map<String, Object>> maisPart = todos.stream()
            .sorted(Comparator.comparingInt((Projeto p) -> p.getParticipantes().size()).reversed())
            .limit(5)
            .map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", p.getId());
                m.put("titulo", p.getTitulo());
                m.put("participantes", p.getParticipantes().size());
                return m;
            }).collect(Collectors.toList());

        List<Usuario> usuarios = usuarioRepo.listarTodos();
        long totalAlunos = usuarios.stream().filter(u -> "ALUNO".equals(u.getTipo())).count();
        long totalProf = usuarios.stream().filter(u -> "PROFESSOR".equals(u.getTipo())).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProjetos", todos.size());
        stats.put("porArea", porArea);
        stats.put("porStatus", porStatus);
        stats.put("maisPart", maisPart);
        stats.put("totalAlunos", totalAlunos);
        stats.put("totalProfessores", totalProf);
        stats.put("totalUsuarios", usuarios.size());
        return stats;
    }
}
