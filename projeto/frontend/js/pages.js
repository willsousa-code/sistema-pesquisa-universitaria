/* ========================
   AUTH
======================== */
function authTab(tab, el) {
    document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('ativo'));
    if (el) el.classList.add('ativo');
    document.getElementById('form-login').style.display = tab === 'login' ? 'block' : 'none';
    document.getElementById('form-cadastro').style.display = tab === 'cadastro' ? 'block' : 'none';
}

function atualizarCamposCadastro() {
    const tipo = document.getElementById('cad-tipo').value;
    let html = '';
    if (tipo === 'ALUNO') {
        html = `
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Matrícula</label>
                    <input id="cad-extra1" type="text" class="form-control" placeholder="2024001">
                </div>
                <div class="form-group">
                    <label class="form-label">Curso</label>
                    <input id="cad-extra2" type="text" class="form-control" placeholder="Ciência da Computação">
                </div>
            </div>`;
    } else if (tipo === 'PROFESSOR') {
        html = `
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Departamento</label>
                    <input id="cad-extra1" type="text" class="form-control" placeholder="Computação">
                </div>
                <div class="form-group">
                    <label class="form-label">Titulação</label>
                    <input id="cad-extra2" type="text" class="form-control" placeholder="Doutor">
                </div>
            </div>`;
    } else {
        html = `
            <div class="form-group">
                <label class="form-label">SIAPE</label>
                <input id="cad-extra1" type="text" class="form-control" placeholder="1234567">
            </div>`;
    }
    document.getElementById('campos-extras').innerHTML = html;
}
atualizarCamposCadastro();

async function fazerLogin() {
    const email = document.getElementById('login-email').value.trim();
    const senha = document.getElementById('login-senha').value;
    if (!email || !senha) { ui.toast('Preencha todos os campos.', 'error'); return; }
    ui.loading(true);
    try {
        const usuario = await api.login(email, senha);
        session.salvar(usuario);
        mostrarApp();
        renderizarNavbar();
        carregarPaginaInicial();
        ui.toast(`Bem-vindo, ${usuario.nome}!`, 'success');
    } catch(e) {
        ui.toast(e.message, 'error');
    } finally {
        ui.loading(false);
    }
}

async function fazerCadastro() {
    const tipo = document.getElementById('cad-tipo').value;
    const nome = document.getElementById('cad-nome').value.trim();
    const email = document.getElementById('cad-email').value.trim();
    const senha = document.getElementById('cad-senha').value;
    const extra1 = document.getElementById('cad-extra1')?.value.trim() || '';
    const extra2 = document.getElementById('cad-extra2')?.value.trim() || '';
    if (!nome || !email || !senha) { ui.toast('Preencha todos os campos obrigatórios.', 'error'); return; }
    ui.loading(true);
    try {
        await api.cadastrar({ tipo, nome, email, senha, extra1, extra2 });
        ui.toast('Conta criada! Faça login.', 'success');
        const tabs = document.querySelectorAll('.auth-tab');
        authTab('login', tabs[0]);
    } catch(e) {
        ui.toast(e.message, 'error');
    } finally {
        ui.loading(false);
    }
}

function mostrarApp() {
    document.getElementById('page-auth').style.display = 'none';
    document.getElementById('page-main').style.display = 'flex';
    document.getElementById('nav-user-nome').textContent = session.nome;
    atualizarNotifBadge();
}

function logout() {
    session.limpar();
    document.getElementById('page-auth').style.display = 'flex';
    document.getElementById('page-main').style.display = 'none';
    document.getElementById('page-auth').style.removeProperty('display');
    location.reload();
}

/* ========================
   NAVBAR
======================== */
function renderizarNavbar() {
    const links = document.getElementById('nav-links');
    const tipo = session.tipo;
    let html = '';
    if (tipo === 'ALUNO') {
        html = `
            <a class="nav-link" onclick="paginaProjetos()">📋 Projetos</a>
            <a class="nav-link" onclick="paginaMeusProjetos()">📁 Meus projetos</a>
            <a class="nav-link" onclick="paginaPerfil()">👤 Perfil</a>`;
    } else if (tipo === 'PROFESSOR') {
        html = `
            <a class="nav-link" onclick="paginaProjetos()">📋 Projetos</a>
            <a class="nav-link" onclick="paginaMeusProjetosProf()">📁 Meus projetos</a>
            <a class="nav-link" onclick="paginaPerfil()">👤 Perfil</a>`;
    } else if (tipo === 'COORDENADOR') {
        html = `
            <a class="nav-link" onclick="paginaProjetos()">📋 Projetos</a>
            <a class="nav-link" onclick="paginaGerenciarUsuarios()">👥 Usuários</a>
            <a class="nav-link" onclick="paginaEstatisticas()">📊 Estatísticas</a>
            <a class="nav-link" onclick="paginaPerfil()">👤 Perfil</a>`;
    }
    links.innerHTML = html;
}

function carregarPaginaInicial() {
    const tipo = session.tipo;
    if (tipo === 'ALUNO') paginaProjetos();
    else if (tipo === 'PROFESSOR') paginaMeusProjetosProf();
    else if (tipo === 'COORDENADOR') paginaEstatisticas();
}

/* ========================
   NOTIFICAÇÕES
======================== */
let notifAberto = false;
function toggleNotificacoes() {
    notifAberto = !notifAberto;
    const panel = document.getElementById('notif-panel');
    panel.style.display = notifAberto ? 'flex' : 'none';
    if (notifAberto) carregarNotificacoes();
}

async function carregarNotificacoes() {
    try {
        const lista = await api.notificacoes(session.id);
        const el = document.getElementById('notif-list');
        if (!lista.length) {
            el.innerHTML = '<div class="notif-empty">Sem notificações</div>';
            return;
        }
        el.innerHTML = lista.map(n => `
            <div class="notif-item ${n.lida ? 'lida' : 'nao-lida'}">
                <div class="notif-msg">${n.mensagem}</div>
                <div class="notif-data">${ui.formatarData(n.data)}</div>
            </div>`).join('');
    } catch(e) { console.error(e); }
}

async function atualizarNotifBadge() {
    if (!session.logado) return;
    try {
        const lista = await api.notificacoes(session.id);
        const naoLidas = lista.filter(n => !n.lida).length;
        const badge = document.getElementById('notif-badge');
        if (naoLidas > 0) {
            badge.style.display = 'flex';
            badge.textContent = naoLidas > 9 ? '9+' : naoLidas;
        } else {
            badge.style.display = 'none';
        }
    } catch(e) {}
}

async function marcarTodasLidas() {
    await api.marcarLidas(session.id);
    document.getElementById('notif-badge').style.display = 'none';
    carregarNotificacoes();
}

/* ========================
   MODAL
======================== */
function abrirModal(titulo, bodyHtml, footerHtml = '') {
    document.getElementById('modal-title').textContent = titulo;
    document.getElementById('modal-body').innerHTML = bodyHtml;
    document.getElementById('modal-footer').innerHTML = footerHtml;
    document.getElementById('modal-overlay').style.display = 'flex';
}

function fecharModal(event) {
    if (event && event.target !== document.getElementById('modal-overlay')) return;
    document.getElementById('modal-overlay').style.display = 'none';
}
document.addEventListener('keydown', e => { if (e.key === 'Escape') fecharModal(); });

/* ========================
   CONTEÚDO MAIN
======================== */
function setContent(html) {
    document.getElementById('main-content').innerHTML = html;
}

/* ========================
   PÁGINA: PROJETOS
======================== */
async function paginaProjetos(filtros = {}) {
    ui.loading(true);
    try {
        const projetos = await api.listarProjetos(filtros);
        let inscrito = [];
        let pendentes = [];
        if (session.tipo === 'ALUNO') {
            const aluno = await api.buscarUsuario(session.id);
            inscrito = aluno.projetosInscritos || [];
        }
        setContent(`
            <div class="section-title">📋 Projetos disponíveis <span>${projetos.length} encontrados</span></div>
            <div class="filters">
                <input id="f-termo" class="form-control" placeholder="🔍 Buscar por título..." value="${filtros.termo||''}">
                <input id="f-area" class="form-control" placeholder="Área de estudo" value="${filtros.area||''}">
                <select id="f-status" class="form-control">
                    <option value="">Todos os status</option>
                    <option value="ABERTO" ${filtros.status==='ABERTO'?'selected':''}>Aberto</option>
                    <option value="EM_ANDAMENTO" ${filtros.status==='EM_ANDAMENTO'?'selected':''}>Em andamento</option>
                    <option value="ENCERRADO" ${filtros.status==='ENCERRADO'?'selected':''}>Encerrado</option>
                    <option value="CANCELADO" ${filtros.status==='CANCELADO'?'selected':''}>Cancelado</option>
                </select>
                <button class="btn btn-primary" onclick="aplicarFiltros()">Filtrar</button>
                <button class="btn btn-secondary" onclick="paginaProjetos()">Limpar</button>
            </div>
            <div class="grid grid-2">
                ${projetos.length === 0 ? `
                    <div class="empty" style="grid-column:1/-1">
                        <div class="empty-icon">🔍</div>
                        <div class="empty-title">Nenhum projeto encontrado</div>
                        <p>Tente outros filtros</p>
                    </div>` :
                projetos.map(p => projetoCardHtml(p, inscrito)).join('')}
            </div>
        `);
    } catch(e) {
        ui.toast(e.message, 'error');
    } finally {
        ui.loading(false);
    }
}

function projetoCardHtml(p, inscrito = []) {
    const statusClass = { ABERTO:'aberto', EM_ANDAMENTO:'andamento', ENCERRADO:'encerrado', CANCELADO:'cancelado' }[p.status] || '';
    const estaInscrito = inscrito.includes(p.id);
    return `
        <div class="projeto-card" onclick="verProjeto('${p.id}')">
            <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:8px">
                <div class="projeto-titulo">${p.titulo}</div>
                <span class="badge badge-${statusClass}">${ui.statusLabel(p.status)}</span>
            </div>
            <p style="color:var(--text2);font-size:.85rem;margin:8px 0;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden">${p.descricao || ''}</p>
            <div class="projeto-meta">
                <span>👨‍🏫 ${p.orientadorNome}</span>
                <span>🔬 ${p.areaEstudo}</span>
                <span>👥 ${p.participantes?.length||0}/${p.vagas} vagas</span>
                <span>📅 Prazo: ${ui.formatarData(p.prazo)}</span>
            </div>
            <div class="projeto-footer">
                <span style="color:var(--text2);font-size:.8rem">${p.solicitacoesPendentes?.length||0} solicitações pendentes</span>
                ${estaInscrito ? '<span style="color:var(--success);font-size:.8rem">✅ Inscrito</span>' : ''}
            </div>
        </div>`;
}

function aplicarFiltros() {
    const filtros = {};
    const termo = document.getElementById('f-termo')?.value.trim();
    const area = document.getElementById('f-area')?.value.trim();
    const status = document.getElementById('f-status')?.value;
    if (termo) filtros.termo = termo;
    if (area) filtros.area = area;
    if (status) filtros.status = status;
    paginaProjetos(filtros);
}

/* ========================
   DETALHE DO PROJETO
======================== */
async function verProjeto(id) {
    ui.loading(true);
    try {
        const p = await api.buscarProjeto(id);
        let alunoInfo = null;
        let estaInscrito = false;
        let solicitacaoPendente = false;

        if (session.tipo === 'ALUNO') {
            alunoInfo = await api.buscarUsuario(session.id);
            estaInscrito = p.participantes?.includes(session.id);
            solicitacaoPendente = p.solicitacoesPendentes?.includes(session.id);
        }

        const isOrientador = p.orientadorId === session.id;
        const isCoordenador = session.tipo === 'COORDENADOR';
        const statusClass = { ABERTO:'aberto', EM_ANDAMENTO:'andamento', ENCERRADO:'encerrado', CANCELADO:'cancelado' }[p.status] || '';

        let acoesProfHtml = '';
        if (isOrientador || isCoordenador) {
            acoesProfHtml = `
                <div class="btn-group" style="margin-bottom:16px">
                    <button class="btn btn-secondary" onclick="modalEditarProjeto('${p.id}')">✏️ Editar</button>
                    <button class="btn btn-danger" onclick="removerProjeto('${p.id}')">🗑️ Remover</button>
                </div>`;
        }

        let acoesAlunoHtml = '';
        if (session.tipo === 'ALUNO') {
            if (estaInscrito) {
                acoesAlunoHtml = `
                    <button class="btn btn-danger" onclick="cancelarParticipacao('${p.id}')">❌ Cancelar participação</button>
                    <button class="btn btn-primary" onclick="modalEnviarRelatorio('${p.id}')">📄 Enviar relatório</button>`;
            } else if (solicitacaoPendente) {
                acoesAlunoHtml = `<button class="btn btn-secondary" disabled>⏳ Solicitação pendente</button>`;
            } else if (p.status === 'ABERTO' && p.participantes?.length < p.vagas) {
                acoesAlunoHtml = `<button class="btn btn-primary" onclick="solicitarParticipacao('${p.id}')">✋ Solicitar participação</button>`;
            }
        }

        // Participantes (para prof/coord)
        let participantesHtml = '';
        if ((isOrientador || isCoordenador) && p.participantes?.length > 0) {
            const participantesData = await Promise.all(
                p.participantes.map(uid => api.buscarUsuario(uid).catch(() => ({ nome: uid })))
            );
            participantesHtml = `
                <div class="card">
                    <div class="card-header"><div class="card-title">👥 Participantes (${p.participantes.length})</div></div>
                    <div class="table-wrap">
                        <table>
                            <thead><tr><th>Nome</th><th>Ações</th></tr></thead>
                            <tbody>
                                ${participantesData.map(u => `
                                    <tr>
                                        <td>${u.nome}</td>
                                        <td>
                                            <button class="btn btn-sm btn-danger" onclick="removerParticipante('${p.id}','${u.id}')">Remover</button>
                                        </td>
                                    </tr>`).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>`;
        }

        // Solicitações pendentes
        let solicitacoesHtml = '';
        if ((isOrientador || isCoordenador) && p.solicitacoesPendentes?.length > 0) {
            const solicitData = await Promise.all(
                p.solicitacoesPendentes.map(uid => api.buscarUsuario(uid).catch(() => ({ nome: uid, id: uid })))
            );
            solicitacoesHtml = `
                <div class="card">
                    <div class="card-header"><div class="card-title">📬 Solicitações pendentes (${p.solicitacoesPendentes.length})</div></div>
                    <div class="table-wrap">
                        <table>
                            <thead><tr><th>Aluno</th><th>Ações</th></tr></thead>
                            <tbody>
                                ${solicitData.map(u => `
                                    <tr>
                                        <td>${u.nome}</td>
                                        <td class="btn-group">
                                            <button class="btn btn-sm btn-success" onclick="aprovarParticipacao('${p.id}','${u.id}')">✅ Aprovar</button>
                                            <button class="btn btn-sm btn-danger" onclick="rejeitarParticipacao('${p.id}','${u.id}')">❌ Rejeitar</button>
                                        </td>
                                    </tr>`).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>`;
        }

        // Relatórios
        let relatoriosHtml = '';
        if (estaInscrito || isOrientador || isCoordenador) {
            const rels = p.relatorios || [];
            const relsMostrar = session.tipo === 'ALUNO'
                ? rels.filter(r => r.alunoId === session.id)
                : rels;
            if (relsMostrar.length > 0) {
                relatoriosHtml = `
                    <div class="card">
                        <div class="card-header"><div class="card-title">📄 Relatórios (${relsMostrar.length})</div></div>
                        <div class="table-wrap">
                            <table>
                                <thead><tr><th>Título</th><th>Aluno</th><th>Data</th><th>Status</th><th>Ações</th></tr></thead>
                                <tbody>
                                    ${relsMostrar.map(r => `
                                        <tr>
                                            <td>${r.titulo}</td>
                                            <td>${r.alunoNome}</td>
                                            <td>${ui.formatarData(r.dataEnvio)}</td>
                                            <td><span class="badge badge-${r.status?.toLowerCase()}">${ui.statusRelLabel(r.status)}</span></td>
                                            <td>
                                                <button class="btn btn-sm btn-secondary" onclick="verRelatorio('${p.id}','${r.id}')">Ver</button>
                                                ${(isOrientador || isCoordenador) && r.status === 'PENDENTE' ? `
                                                    <button class="btn btn-sm btn-success" onclick="avaliarRelatorio('${p.id}','${r.id}','APROVADO')">✅</button>
                                                    <button class="btn btn-sm btn-danger" onclick="modalAvaliarRelatorio('${p.id}','${r.id}')">❌</button>` : ''}
                                            </td>
                                        </tr>`).join('')}
                                </tbody>
                            </table>
                        </div>
                    </div>`;
            }
        }

        setContent(`
            <div style="margin-bottom:16px">
                <button class="btn btn-secondary btn-sm" onclick="paginaProjetos()">← Voltar</button>
            </div>
            <div class="card">
                <div class="card-header">
                    <div>
                        <div class="card-title" style="font-size:1.3rem">${p.titulo}</div>
                        <div style="color:var(--text2);font-size:.85rem;margin-top:4px">🔬 ${p.areaEstudo}</div>
                    </div>
                    <span class="badge badge-${statusClass}">${ui.statusLabel(p.status)}</span>
                </div>
                <p style="color:var(--text2);margin-bottom:20px">${p.descricao || ''}</p>
                <div class="detail-meta">
                    <div class="detail-item"><div class="detail-key">Orientador</div><div class="detail-val">${p.orientadorNome}</div></div>
                    <div class="detail-item"><div class="detail-key">Vagas</div><div class="detail-val">${p.participantes?.length||0} / ${p.vagas}</div></div>
                    <div class="detail-item"><div class="detail-key">Início</div><div class="detail-val">${ui.formatarData(p.dataInicio)}</div></div>
                    <div class="detail-item"><div class="detail-key">Prazo</div><div class="detail-val">${ui.formatarData(p.prazo)}</div></div>
                </div>
                ${acoesProfHtml}
                <div class="btn-group">${acoesAlunoHtml}</div>
            </div>
            ${solicitacoesHtml}
            ${participantesHtml}
            ${relatoriosHtml}
        `);
    } catch(e) {
        ui.toast(e.message, 'error');
    } finally {
        ui.loading(false);
    }
}

/* ========================
   CRIAR / EDITAR PROJETO
======================== */
function modalCriarProjeto() {
    abrirModal('Novo projeto de pesquisa', `
        <div class="form-group"><label class="form-label">Título</label>
            <input id="mp-titulo" class="form-control" placeholder="Título do projeto"></div>
        <div class="form-group"><label class="form-label">Descrição</label>
            <textarea id="mp-desc" class="form-control" placeholder="Descreva o projeto..."></textarea></div>
        <div class="form-row">
            <div class="form-group"><label class="form-label">Área de estudo</label>
                <input id="mp-area" class="form-control" placeholder="Ex: Inteligência Artificial"></div>
            <div class="form-group"><label class="form-label">Vagas</label>
                <input id="mp-vagas" type="number" class="form-control" value="5" min="1" max="20"></div>
        </div>
        <div class="form-row">
            <div class="form-group"><label class="form-label">Data de início</label>
                <input id="mp-inicio" type="date" class="form-control"></div>
            <div class="form-group"><label class="form-label">Prazo final</label>
                <input id="mp-prazo" type="date" class="form-control"></div>
        </div>`,
        `<button class="btn btn-secondary" onclick="fecharModal()">Cancelar</button>
         <button class="btn btn-primary" onclick="criarProjeto()">Criar projeto</button>`);
}

async function criarProjeto() {
    const dados = {
        professorId: session.id,
        titulo: document.getElementById('mp-titulo').value.trim(),
        descricao: document.getElementById('mp-desc').value.trim(),
        area: document.getElementById('mp-area').value.trim(),
        vagas: parseInt(document.getElementById('mp-vagas').value),
        dataInicio: document.getElementById('mp-inicio').value,
        prazo: document.getElementById('mp-prazo').value,
    };
    if (!dados.titulo || !dados.area) { ui.toast('Preencha título e área.', 'error'); return; }
    ui.loading(true);
    try {
        await api.criarProjeto(dados);
        fecharModal();
        ui.toast('Projeto criado!', 'success');
        paginaMeusProjetosProf();
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function modalEditarProjeto(id) {
    const p = await api.buscarProjeto(id);
    abrirModal('Editar projeto', `
        <div class="form-group"><label class="form-label">Título</label>
            <input id="ep-titulo" class="form-control" value="${p.titulo}"></div>
        <div class="form-group"><label class="form-label">Descrição</label>
            <textarea id="ep-desc" class="form-control">${p.descricao||''}</textarea></div>
        <div class="form-row">
            <div class="form-group"><label class="form-label">Área</label>
                <input id="ep-area" class="form-control" value="${p.areaEstudo}"></div>
            <div class="form-group"><label class="form-label">Vagas</label>
                <input id="ep-vagas" type="number" class="form-control" value="${p.vagas}"></div>
        </div>
        <div class="form-group"><label class="form-label">Status</label>
            <select id="ep-status" class="form-control">
                <option value="ABERTO" ${p.status==='ABERTO'?'selected':''}>Aberto</option>
                <option value="EM_ANDAMENTO" ${p.status==='EM_ANDAMENTO'?'selected':''}>Em andamento</option>
                <option value="ENCERRADO" ${p.status==='ENCERRADO'?'selected':''}>Encerrado</option>
                <option value="CANCELADO" ${p.status==='CANCELADO'?'selected':''}>Cancelado</option>
            </select></div>
        <div class="form-row">
            <div class="form-group"><label class="form-label">Início</label>
                <input id="ep-inicio" type="date" class="form-control" value="${p.dataInicio||''}"></div>
            <div class="form-group"><label class="form-label">Prazo</label>
                <input id="ep-prazo" type="date" class="form-control" value="${p.prazo||''}"></div>
        </div>`,
        `<button class="btn btn-secondary" onclick="fecharModal()">Cancelar</button>
         <button class="btn btn-primary" onclick="salvarEdicaoProjeto('${id}')">Salvar</button>`);
}

async function salvarEdicaoProjeto(id) {
    const dados = {
        usuarioId: session.id,
        titulo: document.getElementById('ep-titulo').value.trim(),
        descricao: document.getElementById('ep-desc').value.trim(),
        area: document.getElementById('ep-area').value.trim(),
        vagas: parseInt(document.getElementById('ep-vagas').value),
        status: document.getElementById('ep-status').value,
        dataInicio: document.getElementById('ep-inicio').value,
        prazo: document.getElementById('ep-prazo').value,
    };
    ui.loading(true);
    try {
        await api.editarProjeto(id, dados);
        fecharModal();
        ui.toast('Projeto atualizado!', 'success');
        verProjeto(id);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function removerProjeto(id) {
    if (!ui.confirmar('Remover este projeto? Esta ação não pode ser desfeita.')) return;
    ui.loading(true);
    try {
        await api.removerProjeto(id, session.id);
        ui.toast('Projeto removido.', 'success');
        if (session.tipo === 'PROFESSOR') paginaMeusProjetosProf();
        else paginaProjetos();
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

/* ========================
   PARTICIPAÇÕES
======================== */
async function solicitarParticipacao(projetoId) {
    ui.loading(true);
    try {
        await api.solicitarParticipacao(projetoId, session.id);
        ui.toast('Solicitação enviada!', 'success');
        verProjeto(projetoId);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function cancelarParticipacao(projetoId) {
    if (!ui.confirmar('Cancelar sua participação neste projeto?')) return;
    ui.loading(true);
    try {
        await api.cancelarParticipacao(projetoId, session.id);
        ui.toast('Participação cancelada.', 'success');
        verProjeto(projetoId);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function aprovarParticipacao(projetoId, alunoId) {
    ui.loading(true);
    try {
        await api.aprovarParticipacao(projetoId, session.id, alunoId);
        ui.toast('Participação aprovada!', 'success');
        verProjeto(projetoId);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function rejeitarParticipacao(projetoId, alunoId) {
    ui.loading(true);
    try {
        await api.rejeitarParticipacao(projetoId, session.id, alunoId);
        ui.toast('Solicitação rejeitada.', 'success');
        verProjeto(projetoId);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function removerParticipante(projetoId, alunoId) {
    if (!ui.confirmar('Remover este participante do projeto?')) return;
    ui.loading(true);
    try {
        await api.cancelarParticipacao(projetoId, alunoId);
        ui.toast('Participante removido.', 'success');
        verProjeto(projetoId);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

/* ========================
   RELATÓRIOS
======================== */
function modalEnviarRelatorio(projetoId) {
    abrirModal('Enviar relatório parcial', `
        <div class="form-group"><label class="form-label">Título do relatório</label>
            <input id="rel-titulo" class="form-control" placeholder="Ex: Relatório 1 - Revisão bibliográfica"></div>
        <div class="form-group"><label class="form-label">Conteúdo</label>
            <textarea id="rel-conteudo" class="form-control" style="min-height:180px" placeholder="Descreva o progresso..."></textarea></div>`,
        `<button class="btn btn-secondary" onclick="fecharModal()">Cancelar</button>
         <button class="btn btn-primary" onclick="enviarRelatorio('${projetoId}')">Enviar</button>`);
}

async function enviarRelatorio(projetoId) {
    const titulo = document.getElementById('rel-titulo').value.trim();
    const conteudo = document.getElementById('rel-conteudo').value.trim();
    if (!titulo || !conteudo) { ui.toast('Preencha todos os campos.', 'error'); return; }
    ui.loading(true);
    try {
        await api.enviarRelatorio(projetoId, { alunoId: session.id, titulo, conteudo });
        fecharModal();
        ui.toast('Relatório enviado!', 'success');
        verProjeto(projetoId);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function avaliarRelatorio(projetoId, relId, status) {
    ui.loading(true);
    try {
        await api.avaliarRelatorio(projetoId, relId, { professorId: session.id, status, feedback: '' });
        ui.toast('Relatório avaliado!', 'success');
        verProjeto(projetoId);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

function modalAvaliarRelatorio(projetoId, relId) {
    abrirModal('Reprovar relatório', `
        <div class="form-group"><label class="form-label">Feedback ao aluno</label>
            <textarea id="fb-feedback" class="form-control" placeholder="Explique o motivo da reprovação..."></textarea></div>`,
        `<button class="btn btn-secondary" onclick="fecharModal()">Cancelar</button>
         <button class="btn btn-danger" onclick="enviarAvaliacao('${projetoId}','${relId}','REPROVADO')">Reprovar</button>`);
}

async function enviarAvaliacao(projetoId, relId, status) {
    const feedback = document.getElementById('fb-feedback')?.value.trim() || '';
    ui.loading(true);
    try {
        await api.avaliarRelatorio(projetoId, relId, { professorId: session.id, status, feedback });
        fecharModal();
        ui.toast('Relatório avaliado.', 'success');
        verProjeto(projetoId);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function verRelatorio(projetoId, relId) {
    const p = await api.buscarProjeto(projetoId);
    const rel = p.relatorios.find(r => r.id === relId);
    if (!rel) return;
    abrirModal(`📄 ${rel.titulo}`, `
        <div class="detail-meta" style="margin-bottom:16px">
            <div><div class="detail-key">Aluno</div><div class="detail-val">${rel.alunoNome}</div></div>
            <div><div class="detail-key">Data</div><div class="detail-val">${ui.formatarData(rel.dataEnvio)}</div></div>
            <div><div class="detail-key">Status</div><div class="detail-val"><span class="badge badge-${rel.status?.toLowerCase()}">${ui.statusRelLabel(rel.status)}</span></div></div>
        </div>
        <div style="background:var(--bg3);border-radius:8px;padding:16px;white-space:pre-wrap;font-size:.9rem">${rel.conteudo}</div>
        ${rel.feedback ? `<div style="margin-top:12px;padding:12px;background:var(--bg3);border-radius:8px;border-left:3px solid var(--warning)"><strong>Feedback:</strong> ${rel.feedback}</div>` : ''}`,
        `<button class="btn btn-secondary" onclick="fecharModal()">Fechar</button>`);
}

/* ========================
   PÁGINA: MEUS PROJETOS (ALUNO)
======================== */
async function paginaMeusProjetos() {
    ui.loading(true);
    try {
        const aluno = await api.buscarUsuario(session.id);
        const ids = aluno.projetosInscritos || [];
        const projetos = await Promise.all(ids.map(id => api.buscarProjeto(id).catch(() => null)));
        const validos = projetos.filter(Boolean);

        setContent(`
            <div class="section-title">📁 Meus projetos</div>
            ${validos.length === 0 ? `
                <div class="empty">
                    <div class="empty-icon">📋</div>
                    <div class="empty-title">Nenhum projeto ainda</div>
                    <p>Explore os projetos disponíveis e solicite participação</p>
                    <button class="btn btn-primary" style="margin-top:16px" onclick="paginaProjetos()">Ver projetos</button>
                </div>` :
            `<div class="grid grid-2">${validos.map(p => projetoCardHtml(p, ids)).join('')}</div>`}
        `);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

/* ========================
   PÁGINA: MEUS PROJETOS (PROFESSOR)
======================== */
async function paginaMeusProjetosProf() {
    ui.loading(true);
    try {
        const projetos = await api.projetosPorOrientador(session.id);
        setContent(`
            <div class="section-title">
                📁 Meus projetos <span>${projetos.length} projetos</span>
                <button class="btn btn-primary btn-sm" style="margin-left:auto" onclick="modalCriarProjeto()">➕ Novo projeto</button>
            </div>
            ${projetos.length === 0 ? `
                <div class="empty">
                    <div class="empty-icon">🔬</div>
                    <div class="empty-title">Nenhum projeto criado</div>
                    <button class="btn btn-primary" style="margin-top:16px" onclick="modalCriarProjeto()">Criar primeiro projeto</button>
                </div>` :
            `<div class="grid grid-2">${projetos.map(p => projetoCardHtml(p)).join('')}</div>`}
        `);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

/* ========================
   PÁGINA: GERENCIAR USUÁRIOS (COORDENADOR)
======================== */
async function paginaGerenciarUsuarios() {
    ui.loading(true);
    try {
        const usuarios = await api.listarUsuarios();
        setContent(`
            <div class="section-title">👥 Gerenciar usuários <span>${usuarios.length} cadastrados</span></div>
            <div class="card">
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Nome</th><th>E-mail</th><th>Tipo</th><th>Status</th><th>Ações</th></tr></thead>
                        <tbody>
                            ${usuarios.map(u => `
                                <tr>
                                    <td>${u.nome}</td>
                                    <td style="color:var(--text2)">${u.email}</td>
                                    <td>${ui.badgeTipo(u.tipo)}</td>
                                    <td><span class="badge ${u.ativo ? 'badge-aprovado' : 'badge-reprovado'}">${u.ativo ? 'Ativo' : 'Inativo'}</span></td>
                                    <td class="btn-group">
                                        <button class="btn btn-sm btn-warning" onclick="ativarDesativar('${u.id}')">
                                            ${u.ativo ? '🔒 Desativar' : '🔓 Ativar'}
                                        </button>
                                        ${u.id !== session.id ? `<button class="btn btn-sm btn-danger" onclick="removerUsuario('${u.id}')">🗑️</button>` : ''}
                                    </td>
                                </tr>`).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function ativarDesativar(id) {
    ui.loading(true);
    try {
        const u = await api.ativarDesativar(id, session.id);
        ui.toast(`Usuário ${u.ativo ? 'ativado' : 'desativado'}.`, 'success');
        paginaGerenciarUsuarios();
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

async function removerUsuario(id) {
    if (!ui.confirmar('Remover este usuário permanentemente?')) return;
    ui.loading(true);
    try {
        await api.removerUsuario(id, session.id);
        ui.toast('Usuário removido.', 'success');
        paginaGerenciarUsuarios();
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

/* ========================
   PÁGINA: ESTATÍSTICAS (COORDENADOR)
======================== */
async function paginaEstatisticas() {
    ui.loading(true);
    try {
        const stats = await api.estatisticas();
        const projetos = await api.listarProjetos();

        const areasHtml = Object.entries(stats.porArea || {}).map(([area, qtd]) => `
            <tr><td>${area}</td><td>${qtd}</td></tr>`).join('') || '<tr><td colspan="2" style="color:var(--text2)">Nenhum dado</td></tr>';

        const maisPart = (stats.maisPart || []).map(p => `
            <tr><td>${p.titulo}</td><td>${p.participantes}</td></tr>`).join('') || '';

        setContent(`
            <div class="section-title">📊 Estatísticas gerais</div>
            <div class="grid grid-4" style="margin-bottom:24px">
                <div class="stat-card"><div class="stat-num">${stats.totalProjetos}</div><div class="stat-label">Projetos total</div></div>
                <div class="stat-card"><div class="stat-num">${stats.totalAlunos}</div><div class="stat-label">Alunos</div></div>
                <div class="stat-card"><div class="stat-num">${stats.totalProfessores}</div><div class="stat-label">Professores</div></div>
                <div class="stat-card"><div class="stat-num">${stats.totalUsuarios}</div><div class="stat-label">Usuários total</div></div>
            </div>
            <div class="grid grid-2">
                <div class="card">
                    <div class="card-header"><div class="card-title">📈 Projetos por status</div></div>
                    <div class="table-wrap"><table>
                        <thead><tr><th>Status</th><th>Qtd</th></tr></thead>
                        <tbody>
                            ${Object.entries(stats.porStatus || {}).map(([s, q]) =>
                                `<tr><td>${ui.statusLabel(s)}</td><td>${q}</td></tr>`).join('')}
                        </tbody>
                    </table></div>
                </div>
                <div class="card">
                    <div class="card-header"><div class="card-title">🔬 Projetos por área</div></div>
                    <div class="table-wrap"><table>
                        <thead><tr><th>Área</th><th>Qtd</th></tr></thead>
                        <tbody>${areasHtml}</tbody>
                    </table></div>
                </div>
            </div>
            ${maisPart ? `
            <div class="card">
                <div class="card-header"><div class="card-title">🏆 Projetos com mais participantes</div></div>
                <div class="table-wrap"><table>
                    <thead><tr><th>Projeto</th><th>Participantes</th></tr></thead>
                    <tbody>${maisPart}</tbody>
                </table></div>
            </div>` : ''}
            <div class="card">
                <div class="card-header">
                    <div class="card-title">📋 Todos os projetos</div>
                    <button class="btn btn-primary btn-sm" onclick="modalCriarProjeto()">➕ Novo projeto</button>
                </div>
                <div class="grid grid-2">
                    ${projetos.map(p => projetoCardHtml(p)).join('')}
                </div>
            </div>
        `);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

/* ========================
   PÁGINA: PERFIL
======================== */
async function paginaPerfil() {
    ui.loading(true);
    try {
        const u = await api.buscarUsuario(session.id);
        let extraInfo = '';
        if (u.tipo === 'ALUNO') {
            extraInfo = `
                <div class="detail-item"><div class="detail-key">Matrícula</div><div class="detail-val">${u.matricula||'-'}</div></div>
                <div class="detail-item"><div class="detail-key">Curso</div><div class="detail-val">${u.curso||'-'}</div></div>
                <div class="detail-item"><div class="detail-key">Projetos inscritos</div><div class="detail-val">${(u.projetosInscritos||[]).length}</div></div>`;
        } else if (u.tipo === 'PROFESSOR') {
            extraInfo = `
                <div class="detail-item"><div class="detail-key">Departamento</div><div class="detail-val">${u.departamento||'-'}</div></div>
                <div class="detail-item"><div class="detail-key">Titulação</div><div class="detail-val">${u.titulacao||'-'}</div></div>
                <div class="detail-item"><div class="detail-key">Projetos criados</div><div class="detail-val">${(u.projetosCriados||[]).length}</div></div>`;
        } else {
            extraInfo = `<div class="detail-item"><div class="detail-key">SIAPE</div><div class="detail-val">${u.siape||'-'}</div></div>`;
        }

        setContent(`
            <div class="section-title">👤 Meu perfil</div>
            <div class="card" style="max-width:560px">
                <div class="card-header">
                    <div>
                        <div class="card-title">${u.nome}</div>
                        <div style="margin-top:4px">${ui.badgeTipo(u.tipo)}</div>
                    </div>
                    <button class="btn btn-secondary btn-sm" onclick="modalEditarPerfil()">✏️ Editar</button>
                </div>
                <div class="detail-meta">
                    <div class="detail-item"><div class="detail-key">E-mail</div><div class="detail-val">${u.email}</div></div>
                    <div class="detail-item"><div class="detail-key">Status</div><div class="detail-val">${u.ativo ? '✅ Ativo' : '❌ Inativo'}</div></div>
                    ${extraInfo}
                </div>
            </div>
        `);
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}

function modalEditarPerfil() {
    const u = session.usuario;
    abrirModal('Editar perfil', `
        <div class="form-group"><label class="form-label">Nome</label>
            <input id="ep-nome" class="form-control" value="${u.nome}"></div>
        <div class="form-group"><label class="form-label">E-mail</label>
            <input id="ep-email" type="email" class="form-control" value="${u.email}"></div>`,
        `<button class="btn btn-secondary" onclick="fecharModal()">Cancelar</button>
         <button class="btn btn-primary" onclick="salvarPerfil()">Salvar</button>`);
}

async function salvarPerfil() {
    const nome = document.getElementById('ep-nome').value.trim();
    const email = document.getElementById('ep-email').value.trim();
    ui.loading(true);
    try {
        const u = await api.atualizarUsuario(session.id, { nome, email });
        session.salvar({ ...session.usuario, nome: u.nome, email: u.email });
        document.getElementById('nav-user-nome').textContent = u.nome;
        fecharModal();
        ui.toast('Perfil atualizado!', 'success');
        paginaPerfil();
    } catch(e) { ui.toast(e.message, 'error'); }
    finally { ui.loading(false); }
}