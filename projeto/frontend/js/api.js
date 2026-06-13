const API_BASE = 'http://localhost:8080/api';

const api = {
    async _req(method, path, body = null) {
        const opts = {
            method,
            headers: { 'Content-Type': 'application/json' },
        };
        if (body) opts.body = JSON.stringify(body);
        const res = await fetch(API_BASE + path, opts);
        const data = await res.json();
        if (!res.ok) throw new Error(data.erro || 'Erro desconhecido');
        return data;
    },

    // Auth
    login: (email, senha) => api._req('POST', '/usuarios/login', { email, senha }),
    cadastrar: (dados) => api._req('POST', '/usuarios', dados),

    // Usuários
    listarUsuarios: () => api._req('GET', '/usuarios'),
    buscarUsuario: (id) => api._req('GET', `/usuarios/${id}`),
    atualizarUsuario: (id, dados) => api._req('PUT', `/usuarios/${id}`, dados),
    ativarDesativar: (id, coordenadorId) => api._req('POST', `/usuarios/${id}/ativar`, { coordenadorId }),
    removerUsuario: (id, coordenadorId) => api._req('DELETE', `/usuarios/${id}`, { coordenadorId }),

    // Projetos
    listarProjetos: (params = {}) => {
        const q = new URLSearchParams(params).toString();
        return api._req('GET', `/projetos${q ? '?' + q : ''}`);
    },
    buscarProjeto: (id) => api._req('GET', `/projetos/${id}`),
    projetosPorOrientador: (id) => api._req('GET', `/projetos/orientador/${id}`),
    criarProjeto: (dados) => api._req('POST', '/projetos', dados),
    editarProjeto: (id, dados) => api._req('PUT', `/projetos/${id}`, dados),
    removerProjeto: (id, usuarioId) => api._req('DELETE', `/projetos/${id}`, { usuarioId }),
    estatisticas: () => api._req('GET', '/projetos/estatisticas'),

    // Participações
    solicitarParticipacao: (projetoId, alunoId) =>
        api._req('POST', `/projetos/${projetoId}/solicitar`, { alunoId }),
    aprovarParticipacao: (projetoId, professorId, alunoId) =>
        api._req('POST', `/projetos/${projetoId}/aprovar`, { professorId, alunoId }),
    rejeitarParticipacao: (projetoId, professorId, alunoId) =>
        api._req('POST', `/projetos/${projetoId}/rejeitar`, { professorId, alunoId }),
    cancelarParticipacao: (projetoId, alunoId) =>
        api._req('POST', `/projetos/${projetoId}/cancelar`, { alunoId }),

    // Relatórios
    enviarRelatorio: (projetoId, dados) =>
        api._req('POST', `/projetos/${projetoId}/relatorio`, dados),
    avaliarRelatorio: (projetoId, relId, dados) =>
        api._req('POST', `/projetos/${projetoId}/relatorio/${relId}/avaliar`, dados),

    // Notificações
    notificacoes: (usuarioId) => api._req('GET', `/notificacoes/${usuarioId}`),
    marcarLidas: (usuarioId) => api._req('POST', `/notificacoes/${usuarioId}/lidas`, {}),
};
