@echo off
setlocal enabledelayedexpansion
if not exist bin mkdir bin

set "JDKBIN="
for /d %%D in (..\..\SQL\javaGUI\jdk\jdk-*) do (
  if exist "%%D\bin\javac.exe" set "JDKBIN=%%~fD\bin"
)
if "%JDKBIN%"=="" (
  for /d %%D in (jdk\jdk-*) do (
    if exist "%%D\bin\javac.exe" set "JDKBIN=%%~fD\bin"
  )
)
if "%JDKBIN%"=="" (
  if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JDKBIN=%JAVA_HOME%\bin"
)
if "%JDKBIN%"=="" (
  for /d %%D in ("C:\Program Files\Java\jdk-*") do (
    if exist "%%D\bin\javac.exe" set "JDKBIN=%%~fD\bin"
  )
)
if "%JDKBIN%"=="" (
  set "JDKBIN=javac"
)

echo Dang bien dich tat ca cac file Java trong src...
"%JDKBIN%\javac" -encoding UTF-8 -cp "lib/*" -d bin src/*.java
if %ERRORLEVEL% neq 0 (
  echo Bien dich that bai.
  pause
  exit /b 1
)
echo Bien dich thanh cong.
pause
