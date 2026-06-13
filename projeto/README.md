# 🎓 Sistema de Gerenciamento de Projetos de Pesquisa Universitários

> Trabalho Final — CRT0007 Programação Orientada a Objetos | UFC Campus Crateús

## Arquitetura

```
projeto/
├── backend/               # API REST em Java
│   ├── src/main/java/com/pesquisa/
│   │   ├── model/         # Entidades do domínio (POO)
│   │   ├── repository/    # Persistência em JSON
│   │   ├── service/       # Regras de negócio
│   │   ├── controller/    # Handlers HTTP
│   │   ├── exception/     # Exceções customizadas
│   │   └── util/          # Utilitários HTTP
│   ├── data/              # Arquivos JSON (gerados ao rodar)
│   └── pom.xml
├── frontend/              # SPA em HTML + CSS + JS puro
│   ├── index.html
│   ├── css/style.css
│   └── js/
│       ├── api.js         # Cliente da API REST
│       ├── app.js         # Estado, utilitários, roteamento
│       └── pages.js       # Lógica de todas as páginas
├── run.bat                # Executar no Windows
└── run.sh                 # Executar no Linux/Mac
```

## Requisitos

- Java 17+
- Maven 3.6+
- Navegador moderno (Chrome, Firefox, Edge)

## Como executar

### Windows
```
run.bat
```

### Linux / Mac
```bash
chmod +x run.sh
./run.sh
```

Após o servidor subir, abra `frontend/index.html` no navegador.

---

## Conceitos de POO aplicados

| Conceito | Onde está aplicado |
|---|---|
| **Classes e Objetos** | `Usuario`, `Aluno`, `Professor`, `Coordenador`, `Projeto`, `Relatorio`, `Notificacao` |
| **Encapsulamento** | Atributos privados com getters/setters em todas as classes de modelo |
| **Herança** | `Aluno`, `Professor`, `Coordenador` herdam de `Usuario` |
| **Polimorfismo** | Método `getPerfil()` abstrato implementado diferentemente por cada tipo |
| **Classes Abstratas** | `Usuario` é abstrata — não pode ser instanciada diretamente |
| **Interfaces** | `Notificavel` e `Relatoravel` |
| **Constantes e Estáticos** | `Projeto.MAX_VAGAS`, `Main.PORTA` |
| **Tratamento de Exceções** | `SistemaException` e subclasses: `ProjetoEncerradoException`, `SemVagasException`, `JaInscritoException`, `AcessoNegadoException`, `UsuarioNaoEncontradoException` |
| **Pacotes** | Separados por responsabilidade: `model`, `service`, `repository`, `controller`, `exception`, `util` |
| **Padrão Singleton** | `NotificacaoService.getInstancia()` — gerencia todas as notificações |

---

## Funcionalidades implementadas

### Aluno
- [x] Cadastro e login
- [x] Visualizar projetos disponíveis com busca e filtros
- [x] Solicitar participação em projeto
- [x] Cancelar participação
- [x] Enviar relatórios parciais
- [x] Ver histórico de projetos
- [x] Receber notificações

### Professor
- [x] Cadastro e login
- [x] Criar, editar e remover projetos
- [x] Aprovar/rejeitar solicitações de participação
- [x] Avaliar relatórios (aprovar/reprovar com feedback)
- [x] Ver participantes do projeto
- [x] Receber notificações

### Coordenador
- [x] Gerenciar todos os projetos (criar, editar, remover)
- [x] Gerenciar usuários (ativar, desativar, remover)
- [x] Acesso a estatísticas e relatórios gerais
- [x] Aprovar/rejeitar participações em qualquer projeto
- [x] Receber notificações

### Sistema
- [x] Notificações automáticas (novo projeto, aprovação, relatório avaliado, etc.)
- [x] Persistência em JSON (dados não se perdem ao reiniciar)
- [x] Filtros por área, status, orientador
- [x] Estatísticas: projetos por área, por status, mais populares
- [x] CORS configurado para frontend local
