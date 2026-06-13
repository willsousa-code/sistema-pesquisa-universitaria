#!/bin/bash
echo "==============================================="
echo "  Sistema de Pesquisas Universitárias - Build"
echo "==============================================="

cd backend

echo "[1/2] Compilando e empacotando..."
mvn clean package -q

if [ $? -ne 0 ]; then
    echo "ERRO: Falha na compilação."
    exit 1
fi

echo "[2/2] Iniciando servidor na porta 8080..."
echo ""
echo "Acesse o frontend em: frontend/index.html"
echo "API rodando em: http://localhost:8080"
echo ""
java -jar target/sistema-pesquisa-1.0-jar-with-dependencies.jar
