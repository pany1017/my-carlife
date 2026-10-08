@if "%DEBUG%" == "" @echo off
setlocal
set APP_HOME=%~dp0
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
set GRADLE_OPTS=-Dfile.encoding=UTF-8
java -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
