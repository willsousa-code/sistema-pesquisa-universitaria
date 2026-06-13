@echo off
SET WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
SET WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

IF NOT EXIST %WRAPPER_JAR% (
    echo Baixando Maven Wrapper via curl...
    curl -L -o %WRAPPER_JAR% %WRAPPER_URL%
)

IF NOT EXIST %WRAPPER_JAR% (
    echo ERRO: Nao foi possivel baixar o Maven Wrapper.
    echo Instale o Maven manualmente: https://maven.apache.org/download.cgi
    exit /B 1
)

IF "%JAVA_HOME%"=="" (
    java -classpath %WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %*
) ELSE (
    "%JAVA_HOME%\bin\java.exe" -classpath %WRAPPER_JAR% org.apache.maven.wrapper.MavenWrapperMain %*
)
