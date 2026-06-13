@echo off
echo ===============================================
echo   Sistema de Pesquisas Universitarias
echo ===============================================

cd backend

REM Verifica se Maven esta instalado
where mvn >nul 2>&1
IF %errorlevel% == 0 (
    echo [INFO] Maven encontrado.
    SET MVN_CMD=mvn
    GOTO compile
)

REM Tenta baixar wrapper via curl
echo [INFO] Maven nao encontrado. Tentando Maven Wrapper...
IF NOT EXIST ".mvn\wrapper\maven-wrapper.jar" (
    curl -s -L -o ".mvn\wrapper\maven-wrapper.jar" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
)

IF EXIST ".mvn\wrapper\maven-wrapper.jar" (
    SET MVN_CMD=mvnw.cmd
    GOTO compile
)

echo.
echo ===============================================
echo  Maven nao encontrado!
echo.
echo  Instale pelo link abaixo e reinicie o run.bat:
echo  https://maven.apache.org/download.cgi
echo.
echo  Passo a passo:
echo  1. Baixe o "Binary zip archive"
echo  2. Extraia em C:\maven
echo  3. Adicione C:\maven\bin no PATH do sistema
echo ===============================================
pause
exit /b 1

:compile
echo [1/2] Compilando projeto...
call %MVN_CMD% clean package -q

IF %errorlevel% neq 0 (
    echo ERRO: Falha na compilacao. Veja mensagens acima.
    pause
    exit /b 1
)

echo [2/2] Iniciando servidor...
echo.
echo  API:      http://localhost:8080
echo  Frontend: abra frontend\index.html no navegador
echo.
echo  Pressione Ctrl+C para encerrar.
echo ===============================================

java -jar target\sistema-pesquisa-1.0-jar-with-dependencies.jar
pause
