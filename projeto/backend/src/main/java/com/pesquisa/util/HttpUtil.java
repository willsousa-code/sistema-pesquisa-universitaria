package com.pesquisa.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpUtil {
    private static ObjectMapper mapper;

    public static void setMapper(ObjectMapper m) { mapper = m; }

    public static String lerBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Map<String, Object> lerJson(HttpExchange ex) throws IOException {
        String body = lerBody(ex);
        if (body == null || body.isBlank()) return new HashMap<>();
        return mapper.readValue(body, mapper.getTypeFactory()
            .constructMapType(HashMap.class, String.class, Object.class));
    }

    public static void responder(HttpExchange ex, int status, Object obj) throws IOException {
        String json = mapper.writeValueAsString(obj);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void responderErro(HttpExchange ex, int status, String mensagem) throws IOException {
        Map<String, String> erro = new LinkedHashMap<>();
        erro.put("erro", mensagem);
        responder(ex, status, erro);
    }

    public static void responderOptions(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.sendResponseHeaders(204, -1);
    }

    public static Map<String, String> queryParams(HttpExchange ex) {
        Map<String, String> params = new HashMap<>();
        URI uri = ex.getRequestURI();
        String query = uri.getQuery();
        if (query != null) {
            for (String par : query.split("&")) {
                String[] kv = par.split("=", 2);
                if (kv.length == 2) params.put(kv[0], java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                else params.put(kv[0], "");
            }
        }
        return params;
    }

    public static String[] pathParts(HttpExchange ex) {
        String path = ex.getRequestURI().getPath();
        return Arrays.stream(path.split("/"))
            .filter(s -> !s.isBlank())
            .toArray(String[]::new);
    }
}
