package com.pesquisa;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pesquisa.controller.*;
import com.pesquisa.repository.*;
import com.pesquisa.service.*;
import com.pesquisa.util.HttpUtil;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static final int PORTA = 8080;

    public static void main(String[] args) throws Exception {
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        HttpUtil.setMapper(mapper);

        UsuarioRepository usuarioRepo = new UsuarioRepository(mapper);
        ProjetoRepository projetoRepo = new ProjetoRepository(mapper);
        NotificacaoRepository notificacaoRepo = new NotificacaoRepository(mapper);

        NotificacaoService notifService = NotificacaoService.getInstancia();
        notifService.inicializar(notificacaoRepo, usuarioRepo);

        UsuarioService usuarioService = new UsuarioService(usuarioRepo);
        ProjetoService projetoService = new ProjetoService(projetoRepo, usuarioRepo, notifService);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORTA), 0);
        server.createContext("/api/usuarios", new UsuarioController(usuarioService));
        server.createContext("/api/projetos", new ProjetoController(projetoService));
        server.createContext("/api/notificacoes", new NotificacaoController(notifService));
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Sistema de Pesquisas Universitárias    ║");
        System.out.println("║   API rodando em http://localhost:" + PORTA + "    ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("Pressione Ctrl+C para encerrar.");
    }
}
