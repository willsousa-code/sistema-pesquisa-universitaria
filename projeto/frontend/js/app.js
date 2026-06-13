// Estado global da sessão
const session = {
    usuario: null,

    salvar(usuario) {
        this.usuario = usuario;
        sessionStorage.setItem('usuario', JSON.stringify(usuario));
    },

    carregar() {
        const s = sessionStorage.getItem('usuario');
        if (s) this.usuario = JSON.parse(s);
        return this.usuario;
    },

    limpar() {
        this.usuario = null;
        sessionStorage.removeItem('usuario');
    },

    get logado() { return !!this.usuario; },
    get tipo() { return this.usuario?.tipo || null; },
    get id() { return this.usuario?.id || null; },
    get nome() { return this.usuario?.nome || null; },
};

// Utilitários de UI
const ui = {
    toast(msg, tipo = 'success') {
        const t = document.createElement('div');
        t.className = `toast toast-${tipo}`;
        t.textContent = msg;
        document.getElementById('toasts').appendChild(t);
        setTimeout(() => t.classList.add('show'), 10);
        setTimeout(() => { t.classList.remove('show'); setTimeout(() => t.remove(), 300); }, 3500);
    },

    loading(show) {
        document.getElementById('loading').style.display = show ? 'flex' : 'none';
    },

    confirmar(msg) {
        return confirm(msg);
    },

    formatarData(str) {
        if (!str) return '-';
        try { return new Date(str).toLocaleDateString('pt-BR'); }
        catch { return str; }
    },

    statusLabel(s) {
        const map = {
            ABERTO: '🟢 Aberto',
            EM_ANDAMENTO: '🔵 Em andamento',
            ENCERRADO: '🔴 Encerrado',
            CANCELADO: '⚫ Cancelado',
        };
        return map[s] || s;
    },

    statusRelLabel(s) {
        const map = { PENDENTE: '🕐 Pendente', APROVADO: '✅ Aprovado', REPROVADO: '❌ Reprovado' };
        return map[s] || s;
    },

    badgeTipo(t) {
        const map = { ALUNO: 'badge-aluno', PROFESSOR: 'badge-prof', COORDENADOR: 'badge-coord' };
        return `<span class="badge ${map[t] || ''}">${t}</span>`;
    },
};

// Roteador simples baseado em hash
const router = {
    rotas: {},

    registrar(hash, fn) { this.rotas[hash] = fn; },

    navegar(hash) { window.location.hash = hash; },

    init() {
        window.addEventListener('hashchange', () => this._resolver());
        this._resolver();
    },

    _resolver() {
        const hash = window.location.hash || '#/';
        const base = hash.split('?')[0];
        const fn = this.rotas[base];
        if (fn) fn();
        else if (this.rotas['#/404']) this.rotas['#/404']();
    },
};
