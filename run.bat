@echo off
setlocal enabledelayedexpansion

set "JDKBIN="
for /d %%D in (..\..\SQL\javaGUI\jdk\jdk-*) do (
  if exist "%%D\bin\java.exe" set "JDKBIN=%%~fD\bin"
)
if "%JDKBIN%"=="" (
  for /d %%D in (jdk\jdk-*) do (
    if exist "%%D\bin\java.exe" set "JDKBIN=%%~fD\bin"
  )
)
if "%JDKBIN%"=="" (
  if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JDKBIN=%JAVA_HOME%\bin"
)
if "%JDKBIN%"=="" (
  set "JDKBIN=java"
)

echo Dang khoi chay QuizApp...
"%JDKBIN%\java" -cp "bin;lib/*" Main
pause
